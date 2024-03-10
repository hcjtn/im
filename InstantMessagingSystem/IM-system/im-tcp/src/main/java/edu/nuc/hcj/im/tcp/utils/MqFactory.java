package edu.nuc.hcj.im.tcp.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.nuc.hcj.im.codec.config.BootStrapConfigration;

import java.io.IOException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.utils
 * @ClassName : MqFactory.java
 * @createTime : 2023/12/18 16:37
 * @Description :
 *
 */

public class MqFactory {
    // 连接工厂
    public static ConnectionFactory connectionFactory = null;
    // 管道 连接mq的管道
    public static Channel defaultChannel;

    //接收 channel
    private static ConcurrentHashMap<String,Channel> channelMap = new ConcurrentHashMap<>();

    //获取连接的方法
    private static Connection getConnection() throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection();
        return connection;
    }

    // 获取channel的方法 rabbitMq的方法都是基于channel实现
    // 使用channelName去分辨command来源  用户的command使用用户的channel 群组的command使用群组的channel
    public static Channel getChannel(String channelName) throws IOException, TimeoutException {
        Channel channel = channelMap.get(channelName);
        if (channel == null) {
            channel  = getConnection().createChannel();
            //将channel装载到channelMap中去
            channelMap.put(channelName, channel);
        }
        return channel;
    }

    public static void init(BootStrapConfigration.Rabbitmq config){
        // 不存在时才创建
        if (connectionFactory == null) {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(config.getHost());
            connectionFactory.setPort(config.getPort());
            connectionFactory.setVirtualHost(config.getVirtualHost());
            connectionFactory.setUsername(config.getUserName());
            connectionFactory.setPassword(config.getPassword());

        }
    }

}
