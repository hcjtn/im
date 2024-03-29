package edu.nuc.hcj.im.service.user.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import edu.nuc.hcj.im.codec.park.user.UserStatusChangeNotifyPack;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.command.UserEventCommand;
import edu.nuc.hcj.im.service.message.mq.ChatOperateReceiver;
import edu.nuc.hcj.im.service.user.model.UserStatusChangeNotifyContent;
import edu.nuc.hcj.im.service.user.service.ImUserStatusService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.mq
 * @ClassName : UserOnlineStatusReceiver.java
 * @createTime : 2024/1/17 10:23
 * @Description : 逻辑层处理用户上线下线
 */
@Component
public class UserOnlineStatusReceiver {
    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);
    @Autowired
    ImUserStatusService imUserStatusService;

    /**
     * 订阅MQ单聊消息队列--处理
     *
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constant.RabbitConstants.Im2UserService, durable = "true"),
            exchange = @Exchange(value = Constant.RabbitConstants.Im2UserService, durable = "true")
    ), concurrency = "1")
    @RabbitHandler
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        long start = System.currentTimeMillis();
        Thread t = Thread.currentThread();
        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FROM QUEUE :::::" + msg);
        //deliveryTag 用于回传 rabbitmq 确认该消息处理成功
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand())) {
                UserStatusChangeNotifyContent content = JSON.parseObject(msg, new TypeReference<UserStatusChangeNotifyContent>() {
                }.getType());
                //TODO  处理用户的在线状态
                imUserStatusService.processUserOnlineStatusNotify(content);
            }


            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        } finally {
            long end = System.currentTimeMillis();
            logger.debug("channel {} basic-Ack ,it costs {} ms,threadName = {},threadId={}", channel, end - start, t.getName(), t.getId());
        }
    }

}
