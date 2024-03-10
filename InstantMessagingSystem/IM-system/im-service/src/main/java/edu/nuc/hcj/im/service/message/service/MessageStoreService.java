package edu.nuc.hcj.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ConversationTypeEnum;
import edu.nuc.hcj.im.common.enums.DelFlagEnum;
import edu.nuc.hcj.im.common.model.message.*;
import edu.nuc.hcj.im.service.conversation.service.ConversationService;
import edu.nuc.hcj.im.service.group.dao.ImGroupMessageHistoryEntity;
import edu.nuc.hcj.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import edu.nuc.hcj.im.service.message.dao.ImMessageBodyEntity;
import edu.nuc.hcj.im.service.message.dao.ImMessageHistoryEntity;
import edu.nuc.hcj.im.service.message.dao.mapper.ImMessageBodyMapper;
import edu.nuc.hcj.im.service.message.dao.mapper.ImMessageHistoryMapper;
import edu.nuc.hcj.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.message.service
 * @ClassName : MessageStoreService.java
 * @createTime : 2024/1/7 17:11
 * @Description :  持久化聊天记录
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;
    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;
    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ConversationService conversationService;
    @Autowired
    Appconfig appConfig;

    /**
     * 对该方法进行优化
     * 数据的持久化 是非常占用服务器时间的
     * 在这块儿可以考虑 使用mq进行异步的持久化消息
     * 不需要真正的持久化消息 只需要分配ID即可
     * <p>
     * 只需要分配一个Id给我们的实体类， 再将实体类转换成json对象给我们的mq mq异步的去把这条消息插入到我们数据库里面去
     * 主线程直接使用id去做我们消息的分发
     *
     * @param messageContent
     */
    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
        //1.messageContent 转化为 messageBody
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);

//        // 2.插入messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        //3. 转化成为messageHistory
//        List<ImMessageHistoryEntity> imMessageHistoryEntities
//                = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
//        //4. 批量插入
//        Integer integer = imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

        ImMessageBody imMessageBodyEntity = extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBodyEntity);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        // TODO 使用 mq实现消息持久化的异步操作  该方法目前只是往mq队列里面仍数据
        rabbitTemplate.convertAndSend(Constant.RabbitConstants.StoreP2PMessage, "",
                JSONObject.toJSONString(dto));


    }


    @Transactional
    public void storeGroupMessage(GroupMessageContent messageContent) {
        //1.messageContent 转化为 messageBody
        ImMessageBody imMessageBodyEntity = extractMessageBody(messageContent);
//        // 2.插入messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        //3. 转化成为messageHistory  采用读扩散 无需List
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
//        //4. 批量插入
//        Integer integer = imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

        DoStoreGroupMessageDto groupMessageDto = new DoStoreGroupMessageDto();
        groupMessageDto.setMessageContent(messageContent);
        groupMessageDto.setMessageBody(imMessageBodyEntity);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        rabbitTemplate.convertAndSend(Constant.RabbitConstants.StoreGroupMessage, "",
                JSONObject.toJSONString(groupMessageDto));


    }


    // 提取 ImMessageBodyEntity
    public ImMessageBody extractMessageBody(MessageContent messageContent) {
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    // 提取 ImMessageBodyEntity
    public ImMessageBodyEntity extractMessageBody2(MessageContent messageContent) {
        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setMessageTime(System.currentTimeMillis());

        list.add(toHistory);
        list.add(fromHistory);
        return list;
    }

    public ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupMessageContent messageContent,
                                                                    ImMessageBodyEntity imMessageBodyEntity) {

        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(imMessageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());

        return result;
    }

    /**
     * 将messageID保存到缓存中(redis中)
     *
     * @param appId
     * @param messageId
     * @param messageContent
     */
    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent) {
        //appid : cache : messageId
        String key = appId + ":" + Constant.RedisConstant.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 300, TimeUnit.SECONDS);
    }

    /**
     * 从缓存中(redis中) 获取messageId
     *
     * @param appId
     * @param messageId
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz) {
        //appid : cache : messageId
        String key = appId + ":" + Constant.RedisConstant.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }


    /**
     * @param
     * @return void
     * @description: 存储单人离线消息
     * @author lld
     * <p>
     * 采用数量存储策略 只能存储一定数量的离线消息
     * 采用时间存储策略 比如说 存储7天内的聊天信息
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {
        // 找到fromId队列
        String fromKey = offlineMessage.getAppId() + ":" + Constant.RedisConstant.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到toId队列
        String toKey = offlineMessage.getAppId() + ":" + Constant.RedisConstant.OfflineMessage + ":" + offlineMessage.getToId();
        //对redis做数据操作
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        // 判断 队列中的数据是否超过设定值
        // 返回的是成员数量
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            // 删除第一个元素
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getFromId(), offlineMessage.getToId()
        ));

        // 插入数据，根据messageKey作为峰值
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());

        // 判断 队列中的数据是否超过设定值
        // 返回的是成员数量
        if (operations.zCard(toKey) > 1000) {
            // 删除第一个元素
            operations.removeRange(toKey, 0, 0);
        }

        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getToId(), offlineMessage.getFromId()
        ));
        // 插入数据，根据messageKey作为峰值
        operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());


    }

    /**
     * @param
     * @return void
     * @description: 存储群聊离线消息
     * @author lld
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage
            , List<String> memberIds) {

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constant.RedisConstant.OfflineMessage + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(), memberId, offlineMessage.getToId()
            ));
            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey, JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }


    }

}
