package edu.nuc.hcj.im.service.group.service;

import edu.nuc.hcj.im.codec.park.message.ChatMessageAck;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.enums.command.MessageCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.message.GroupChatMessageContent;
import edu.nuc.hcj.im.common.model.message.GroupMessageContent;
import edu.nuc.hcj.im.common.model.message.MessageContent;
import edu.nuc.hcj.im.common.model.message.OfflineMessageContent;
import edu.nuc.hcj.im.service.group.model.req.SendGroupMessageReq;
import edu.nuc.hcj.im.service.message.service.CheckSendMessageService;
import edu.nuc.hcj.im.service.message.service.MessageStoreService;
import edu.nuc.hcj.im.service.message.service.P2PMessageService;
import edu.nuc.hcj.im.service.seq.RedisSeq;
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
 * @Package : edu.nuc.hcj.im.service.group.service
 * @ClassName : GroupMessageService.java
 * @createTime : 2023/12/12 15:52
 * @Description :
 */
@Service
public class GroupMessageService {
    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);
    @Autowired
    CheckSendMessageService checkSendMessageService;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    ImGroupMemberService imGroupMemberService;
    @Autowired
    MessageStoreService messageStoreService;
    @Autowired
    RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger atomicLong = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                // 指定为后台线程 或者说是守护线程
                thread.setDaemon(true);
                thread.setName("message-group-thread" + atomicLong.getAndIncrement());
                return thread;
            }
        });
    }

    //
    public void process(GroupMessageContent messageContent) {
        //前置校验
        //这个用户是否被禁言，是否被禁用
        //发送方和接收方是否为好友
        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
//        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId, appId);
//        if (responseVO.isOk()){

        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(messageContent.getAppId(),
                messageContent.getMessageId(), GroupChatMessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {
                //1.回ack成功给自己
                ack(messageContent, ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageContent, messageContent);
                //3.发消息给对方在线端
                dispatchMessage(messageContent);
            });
            long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constant.SeqConstants.GroupMessage
                    + messageContent.getGroupId());
            messageContent.setMessageSequence(seq);
            threadPoolExecutor.execute(() -> {

                // 将群聊信息初始化到db中去
                messageStoreService.storeGroupMessage(messageContent);

                // TODO 插入离线消息
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                List<String> groupMemberId =
                        imGroupMemberService.getGroupMemberId(messageContent.getGroupId(), messageContent.getAppId());
                messageContent.setMemberId(groupMemberId);
                BeanUtils.copyProperties(messageContent, offlineMessageContent);
                offlineMessageContent.setToId(messageContent.getGroupId());

                messageStoreService.storeGroupOfflineMessage(offlineMessageContent,groupMemberId);

                // 1.回ack给自己 成功
                ack(messageContent, ResponseVO.successResponse());
                // 2.发消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3.发消息给对方在线端
                dispatchMessage(messageContent);

                // 将
                messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(),
                        messageContent.getMessageId(), messageContent);
            });
//        }else {
//            //ack 给自己告诉自己失败了
//            ack(messageContent,responseVO);
//        }
        }


    }

    private ResponseVO imServerPermissionCheck(String fromId, String toId,
                                               Integer appId) {
        // 好友
        ResponseVO responseVO = checkSendMessageService.checkGroupMessage(fromId, toId, appId);
//        if(!responseVO.isOk()){
//            return responseVO;
//        }
//        // 禁用/禁言
//        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return responseVO;
    }

    // ack方法 内部调用
    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        logger.info("msg ack,msgId={},checkResut{}", messageContent.getMessageId(), responseVO.getCode());
        // 定义ack里面的消息需要填写的内容
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //發消息
        messageProducer.sendToUser(messageContent.getFromId(), GroupEventCommand.MSG_GROUP,
                responseVO, messageContent
        );// 发送给某一端
    }

    // 同步给发送端
    private void syncToSender(GroupMessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P, messageContent, messageContent);// 发送给除自己的所有端
    }

    //  同步发送给接收端
    private void dispatchMessage(GroupMessageContent messageContent) {
        // 获取到所有的群成员


        for (String memberId : messageContent.getMemberId()) {
            if (!memberId.equals(messageContent.getFromId())) {
                messageProducer.sendToUser(memberId,
                        GroupEventCommand.MSG_GROUP,
                        messageContent, messageContent.getAppId());
            }
        }

//        return clientInfos; // //发送给所有端的方法
    }

    public Object send(SendGroupMessageReq req) {
        return null;
    }
}
