package edu.nuc.hcj.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import edu.nuc.hcj.im.codec.proto.Message;
import edu.nuc.hcj.im.codec.proto.MessageHeader;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.command.CommandType;
import edu.nuc.hcj.im.tcp.utils.MqFactory;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.publish
 * @ClassName : MqMessageProducer.java
 * @createTime : 2023/12/18 16:59
 * @Description : 封装 服务端与服务端之间投递消息的组件
 */
@Slf4j
public class MqMessageProducer {

    // 发送消息的方法
    public static void sendMessage(Message message,Integer command){
        Channel channel = null;
        String channelName = Constant.RabbitConstants.Im2MessageService;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        // 判断单聊环视群聊
        if(commandType == CommandType.MESSAGE){
            channelName = Constant.RabbitConstants.Im2MessageService;
        }else if(commandType == CommandType.GROUP){
            channelName = Constant.RabbitConstants.Im2GroupService;
        }else if(commandType == CommandType.FRIEND){
            channelName = Constant.RabbitConstants.Im2FriendshipService;
        }else if(commandType == CommandType.USER){
            channelName = Constant.RabbitConstants.Im2UserService;
        }
        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSONObject.toJSON(message.getMessagePack());
            o.put("command",command);
            o.put("clientType",message.getMessageHeader().getClientType());
            o.put("appId",message.getMessageHeader().getAppId());
            o.put("imei",message.getMessageHeader().getImei());


            /**
             * 基础的发布消息方法，它有四个参数
             * String exchange : 交换机名， 当不使用交换机时，传入“”空串。
             * String routingKey :（路由地址） 发布消息的队列， 无论channel绑定那个队列，最终发布消息的队列都有该字串指定
             * AMQP.BasicProperties props ：消息的配置属性，例如 MessageProperties.PERSISTENT_TEXT_PLAIN 表示消息持久化。
             * byte[] body ：消息数据本体， 必须是byte数组
             */
            channel.basicPublish
                    (channelName,"",null, o.toJSONString().getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送消息出现异常");
        }
    }
    // 发送消息的方法
    public static void sendMessage(Object message, MessageHeader header, Integer command){
        Channel channel = null;
        String channelName = Constant.RabbitConstants.Im2MessageService;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);
        // 判断单聊环视群聊
        if(commandType == CommandType.MESSAGE){
            channelName = Constant.RabbitConstants.Im2MessageService;
        }else if(commandType == CommandType.GROUP){
            channelName = Constant.RabbitConstants.Im2GroupService;
        }else if(commandType == CommandType.FRIEND){
            channelName = Constant.RabbitConstants.Im2FriendshipService;
        }else if(commandType == CommandType.USER){
            channelName = Constant.RabbitConstants.Im2UserService;
        }
        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSONObject.toJSON(message);
            o.put("command",command);
            o.put("clientType",header.getClientType());
            o.put("appId",header.getAppId());
            o.put("imei",header.getImei());


            /**
             * 基础的发布消息方法，它有四个参数
             * String exchange : 交换机名， 当不使用交换机时，传入“”空串。
             * String routingKey :（路由地址） 发布消息的队列， 无论channel绑定那个队列，最终发布消息的队列都有该字串指定
             * AMQP.BasicProperties props ：消息的配置属性，例如 MessageProperties.PERSISTENT_TEXT_PLAIN 表示消息持久化。
             * byte[] body ：消息数据本体， 必须是byte数组
             */
            channel.basicPublish
                    (channelName,"",null, o.toJSONString().getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送消息出现异常");
        }
    }



}
