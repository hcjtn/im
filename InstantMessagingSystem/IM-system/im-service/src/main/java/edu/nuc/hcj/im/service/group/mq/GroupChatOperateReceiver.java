package edu.nuc.hcj.im.service.group.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.model.message.GroupMessageContent;
import edu.nuc.hcj.im.common.model.message.MessageReadedContent;
import edu.nuc.hcj.im.service.group.service.GroupMessageService;
import edu.nuc.hcj.im.service.message.service.MessageSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.mq
 * @ClassName : GroupChatOperateReceiver.java
 * @createTime : 2024/1/7 15:23
 * @Description :
 */
@Component
public class GroupChatOperateReceiver {
    private static Logger logger = LoggerFactory.getLogger(GroupChatOperateReceiver.class);

    @Autowired
    GroupMessageService groupMessageService;
    @Autowired
    MessageSyncService messageSyncService;

    /**
     * 使用 @Payload 和 @Headers 注解可以消息中的 body 与 headers 信息
     *
     * @param message
     * @param headers
     * @param channel
     * @RabbitListener 对rabbitmq队列进行监听
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    // value 绑定队列的名称    durable 是否持久化
                    value = @Queue(value = Constant.RabbitConstants.Im2GroupService, durable = "true"),
                    // 交换机名称
                    exchange = @Exchange(value = Constant.RabbitConstants.Im2GroupService, durable = "true")
                    // 并发数量，每次在队列中拉取消息的数量
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSONObject.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
                // 处理消息
                GroupMessageContent messageContent =
                        jsonObject.toJavaObject(GroupMessageContent.class);
                groupMessageService.process(messageContent);
            } else if (command.equals(GroupEventCommand.MSG_GROUP_READED.getCommand())) {
                MessageReadedContent messageReaded = JSON.parseObject(msg, new TypeReference<MessageReadedContent>() {
                }.getType());
                messageSyncService.groupReadMark(messageReaded);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }

    }
}
