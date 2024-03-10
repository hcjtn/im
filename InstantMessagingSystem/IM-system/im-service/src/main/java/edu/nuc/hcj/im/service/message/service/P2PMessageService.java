package edu.nuc.hcj.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.park.message.ChatMessageAck;
import edu.nuc.hcj.im.codec.park.message.MessageReciveServerAckPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ConversationTypeEnum;
import edu.nuc.hcj.im.common.enums.command.MessageCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.message.MessageContent;
import edu.nuc.hcj.im.common.model.message.OfflineMessageContent;
import edu.nuc.hcj.im.service.message.mq.req.SendMessageReq;
import edu.nuc.hcj.im.service.message.mq.resp.SendMessageResp;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.utils.CallbackService;
import edu.nuc.hcj.im.service.utils.ConversationIdGenerate;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.message.service
 * @ClassName : P2PMessageService.java
 * @createTime : 2024/1/6 17:50
 * @Description :
 */
@Service
public class P2PMessageService {
    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);
    @Autowired
    CheckSendMessageService checkSendMessageService;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    MessageStoreService messageStoreService;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    Appconfig appConfig;
    @Autowired
    CallbackService callbackService;
    // 创建一个私有的线程池
    private final ThreadPoolExecutor threadPoolExecutor;

    // 代码块中进行初始化
    {
        AtomicInteger atomicLong = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 90,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                // 指定为后台线程 或者说是守护线程
                thread.setDaemon(true);
                thread.setName("message-proccess-thread" + atomicLong.getAndIncrement());
                return thread;
            }
        });
    }

    /**
     * 在多线程的情况下很容易形成消息的乱序
     * 需要一个标杆 来确定消息的发送时间，确保消息不会乱序
     * 三种方法
     * 1.记录客户端发送的时间  客户端的时间可以随意的修改
     * 2.messageKey雪花算法 是趋势递增 不是绝对递增 不能保证消息之间的绝对有序
     * 3.使用redis的sequence
     *
     * @param messageContent
     */
    public void process(MessageContent messageContent) {
        //前置校验
        //这个用户是否被禁言，是否被禁用
        //发送方和接收方是否为好友
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();

        // Todo 用messageId
        MessageContent messageFromMessageIdCache =
                messageStoreService.getMessageFromMessageIdCache(appId, messageContent.getMessageId(),
                        MessageContent.class);
        if (messageFromMessageIdCache != null) {
            // 无需持久化直接进行消息分发
            threadPoolExecutor.execute(() -> {
                // 1.回ack给自己 成功
                ack(messageContent, ResponseVO.successResponse());
                // 2.发消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3.发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageContent);
                if (clientInfos.isEmpty()) {
                    // 接收方没有一端在线直接有服务器发送返回 接受方方的ack包  要带上是服务端发送的标识
                    receiveMessageAck(messageContent);
                }
            });
        }

        // 回调
        ResponseVO responseVO = ResponseVO.successResponse();
        if(appConfig.isSendMessageAfterCallback()){
            responseVO = callbackService.beforeCallback(messageContent.getAppId(),
                    Constant.CallbackCommand.SendMessageBefore
                    , JSONObject.toJSONString(messageContent));
        }
        // 进行判断  回调失败
        if(!responseVO.isOk()){
            ack(messageContent,responseVO);
            return;
        }
        // 生成 fromId+ toId 的String (id)
        String key = ConversationIdGenerate.generateP2PId(fromId, toId);
        // 在插入数据之前进行分配序列号
        // key:  appId + Seq + (from + to)/groupId
        long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" +
                Constant.SeqConstants.Message + ":" + key);
        messageContent.setMessageSequence(seq);


        // 校验转到了tcp层的服务器 减少service服务器的依赖
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if (responseVO.isOk()){
        threadPoolExecutor.execute(() -> {

            //插入数据 最占用服务器时间 将该操作拆分出去 使用mq实现异步的消息持久化
            messageStoreService.storeP2PMessage(messageContent);
            // TODO 插入离线消息
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            messageStoreService.storeOfflineMessage(offlineMessageContent);



            // 1.回ack给自己 成功
            ack(messageContent, ResponseVO.successResponse());
            // 2.发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 3.发消息给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            if (clientInfos.isEmpty()) {
                // 接收方没有一端在线直接有服务器发送返回 接受方方的ack包  要带上是服务端发送的标识
                receiveMessageAck(messageContent);
            }

            // Todo 将messageId 存到缓存中
            messageStoreService.setMessageFromMessageIdCache(appId, messageContent.getMessageId(),
                    messageContent);
        });

//        }else {
//            //ack 给自己告诉自己失败了
//            ack(messageContent,responseVO);
//        }


    }

    // 服务器检查权限
    public ResponseVO imServerPermissionCheck(String fromId, String toId,
                                              Integer appId) {
        // 好友
        ResponseVO responseVO = checkSendMessageService.checkSenderForbiddenAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        // 禁用/禁言
        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return responseVO;
    }

    // ack方法 内部调用
    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        logger.info("msg ack,msgId={},checkResut{}", messageContent.getMessageId(), responseVO.getCode());
        // 定义ack里面的消息需要填写的内容
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(),
                messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);
        //發消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                responseVO, messageContent
        );// 发送给某一端
    }

    // 同步给发送端
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P, messageContent, messageContent);// 发送给除自己的所有端
    }

    //  同步发送给接收端
    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_P2P,
                messageContent, messageContent.getAppId());
        return clientInfos; // //发送给所有端的方法
    }


    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req, message);
        //插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步在线端
        syncToSender(message, message);
        //3.发消息给对方在线端
        dispatchMessage(message);
        return sendMessageResp;
    }

    public void receiveMessageAck(MessageContent messageContent) {
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECIVE_ACK,
                pack, new ClientInfo(messageContent.getAppId(), messageContent.getClientType()
                        , messageContent.getImei()));


    }
}
