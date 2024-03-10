package edu.nuc.hcj.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import edu.nuc.hcj.im.codec.park.message.MessageReadedPack;
import edu.nuc.hcj.im.codec.park.message.RecallMessageNotifyPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ConversationTypeEnum;
import edu.nuc.hcj.im.common.enums.DelFlagEnum;
import edu.nuc.hcj.im.common.enums.MessageErrorCode;
import edu.nuc.hcj.im.common.enums.command.Command;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.enums.command.MessageCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.common.model.SyncResp;
import edu.nuc.hcj.im.common.model.message.*;
import edu.nuc.hcj.im.service.conversation.service.ConversationService;
import edu.nuc.hcj.im.service.group.service.ImGroupMemberService;
import edu.nuc.hcj.im.service.message.dao.ImMessageBodyEntity;
import edu.nuc.hcj.im.service.message.dao.mapper.ImMessageBodyMapper;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.utils.ConversationIdGenerate;
import edu.nuc.hcj.im.service.utils.GroupMessageProducer;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.message.service
 * @ClassName : MessageSyncService.java
 * @createTime : 2024/1/13 10:39
 * @Description : 用于处理消息接收确认   即处理接收返回的ack包
 */
@Service
public class MessageSyncService {
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    ConversationService conversationService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    ImGroupMemberService imGroupMemberService;
    @Autowired
    GroupMessageProducer groupMessageProducer;

    public void receiveMark(MessageReciveAckContent message){
        messageProducer.sendToUser(message.getToId(),
                MessageCommand.MSG_RECIVE_ACK,message, message.getAppId());

    }

    /**
     * 消息已读  更新会话的seq 通知在线的同步端发送指定command、通知对方（消息发起方）已经读取
     * @param messageContent
     */
    public void readMark(MessageReadedContent messageContent) {
        // 跟新会话seq
        conversationService.messageMarkRead(messageContent);

        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent,messageReadedPack);
        // 发送自己的同步端
        syncToSender(messageReadedPack,messageContent, MessageCommand.MSG_READED_NOTIFY);
        //发送给对方
        messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_READED_RECEIPT,messageReadedPack,messageContent.getAppId());
    }

    /**
     * 发送给自己的同步端
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command){
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(),
                command,pack,
                content);
    }

    /**
     * 群聊
     * 消息已读  更新会话的seq 通知在线的同步端发送指定command、通知对方（消息发起方）已经读取
     * @param messageReaded
     */
    public void groupReadMark(MessageReadedContent messageReaded) {
        // 跟新会话的seq
        conversationService.messageMarkRead(messageReaded);

        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded,messageReadedPack);
        // 通知在线的同步端发送指定的command
        syncToSender(messageReadedPack,messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY
        );
        // fromId和toId相同 就不发送已读
        if(!messageReaded.getFromId().equals(messageReaded.getToId())){
            //通知对方已经读取
            messageProducer.sendToUser(messageReadedPack.getToId(),GroupEventCommand.MSG_GROUP_READED_RECEIPT
                    ,messageReaded,messageReaded.getAppId());
        }
    }

    public ResponseVO syncOfflineMessage(SyncReq req) {
        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constant.RedisConstant.OfflineMessage + ":" + req.getOperater();
        //获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        // 获取最大的值
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if(!CollectionUtils.isEmpty(set)){
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList();
        resp.setMaxSequence(maxSeq);
        // 获取离线数据
        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);
        if(!CollectionUtils.isEmpty(respList)){
            // 获取最大的seq
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }

    // 修改历史消息的状态
    // 修改离线消息的状态
    // ack 给发送方
    // 发送给同步端
    // 分发给消息的接收方
    public void recallMessage(RecallMessageContent messageContent) {
        // 获取到消息的发起时间
        Long messageTime = messageContent.getMessageTime();
        // 获取到现在的时间
        Long nowTime = System.currentTimeMillis();
        RecallMessageNotifyPack pack = new RecallMessageNotifyPack();
        BeanUtils.copyProperties(messageContent,pack);
        // 判断两时间之差
        // 2 * 60 * 1000  两分钟的 时间戳大小
        if(120000L > nowTime - messageTime){
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALL_TIME_OUT),messageContent);
            return;
        }

        QueryWrapper<ImMessageBodyEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("app_id",messageContent.getAppId());
        queryWrapper.eq("message_key",messageContent.getMessageKey());
        // 查询 当前消息是否存在
        ImMessageBodyEntity body = imMessageBodyMapper.selectOne(queryWrapper);
        if(body == null){
            //TODO ack失败 不存在的消息不能撤回
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGEBODY_IS_NOT_EXIST),messageContent);
            return;
        }
        // 修改消息状态
        body.setDelFlag(DelFlagEnum.DELETE.getCode());
        imMessageBodyMapper.update(body,queryWrapper);

        // 判断消息类型是否是 p2p 状态  // 修改离线消息的状态
        if(messageContent.getConversationType() == ConversationTypeEnum.P2P.getCode()){
            // 找到fromId的队列
            String fromKey = messageContent.getAppId() + ":" +
                    Constant.RedisConstant.OfflineMessage + ":" + messageContent.getFromId();
            // 找到toId的队列
            String toKey = messageContent.getAppId() + ":" +
                    Constant.RedisConstant.OfflineMessage + ":" + messageContent.getToId();

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setMessageKey(messageContent.getMessageKey());
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                    ,messageContent.getFromId(),messageContent.getToId()));
            offlineMessageContent.setMessageBody(body.getMessageBody());

            long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" +
                    Constant.SeqConstants.Message + ":" +
                    ConversationIdGenerate.generateP2PId(messageContent.getFromId(),messageContent.getToId()));
            offlineMessageContent.setMessageSequence(seq);
            long messageKey = snowflakeIdWorker.nextId();

            redisTemplate.opsForZSet().add(fromKey,JSONObject.toJSONString(offlineMessageContent),messageKey);
            redisTemplate.opsForZSet().add(toKey,JSONObject.toJSONString(offlineMessageContent),messageKey);

            //ack
            recallAck(pack,ResponseVO.successResponse(),messageContent);
            //分发给同步端
            messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                    MessageCommand.MSG_RECALL_NOTIFY,pack,messageContent);
            //分发给对方
            messageProducer.sendToUser(messageContent.getToId(),MessageCommand.MSG_RECALL_NOTIFY,
                    pack,messageContent.getAppId());
        } else {
            // 群聊
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getToId(), messageContent.getAppId());
            long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constant.SeqConstants.Message + ":" +
                    ConversationIdGenerate.generateP2PId(messageContent.getFromId(),messageContent.getToId()));
            //ack
            recallAck(pack,ResponseVO.successResponse(),messageContent);
            //发送给同步端
            messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack
                    , messageContent);
            for (String memberId : groupMemberId) {
                String toKey = messageContent.getAppId() + ":" + Constant.SeqConstants.Message + ":" + memberId;
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
                BeanUtils.copyProperties(messageContent,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
                offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                        ,messageContent.getFromId(),messageContent.getToId()));
                offlineMessageContent.setMessageBody(body.getMessageBody());
                offlineMessageContent.setMessageSequence(seq);
                redisTemplate.opsForZSet().add(toKey,JSONObject.toJSONString(offlineMessageContent),seq);

                groupMessageProducer.producer(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack,messageContent);
            }
        }

    }

    private void recallAck(RecallMessageNotifyPack recallPack, ResponseVO<Object> success, ClientInfo clientInfo) {
        ResponseVO<Object> wrappedResp = success;
        messageProducer.sendToUser(recallPack.getFromId(),
                MessageCommand.MSG_RECALL_ACK, wrappedResp, clientInfo);
    }
}

