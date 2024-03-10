package edu.nuc.hcj.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import edu.nuc.hcj.im.codec.proto.Message;
import edu.nuc.hcj.im.codec.proto.MessagePack;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.tcp.reciver.process.BaseProcess;
import edu.nuc.hcj.im.tcp.reciver.process.ProcessFactory;
import edu.nuc.hcj.im.tcp.utils.MqFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.reciver
 * @ClassName : MessageReciver.java
 * @createTime : 2023/12/18 17:15
 * @Description :服务端与服务端之间监听消息的组件
 */
@Slf4j
public class  MessageReciver {

    private static String brokerId;

    //监听消息的方法
    private static void startReciverMessage() {
        try {

            Channel channel = MqFactory.getChannel(Constant.RabbitConstants.MessageService2Im + brokerId);

            /**
             * 方法参数详细说明如下：
             *
             * queue：队列的名称
             *
             * durable：设置是否持久化。为true则设置队列为持久化。持久化的队列会存盘，在服务器重启的时候可以保证不丢失相关信息
             *
             * exclusive：设置是否排他。为true则设置对列为排他的。如果一个队列被声明为排他队列，该队列仅对首次声明它的连接可见，
             * 并在连接断开时自动删除。这里需要注意的三点：排他队列是基于连接可见，同一个连接的不同信道是可以同时
             * 访问同一个连接创建的排他队列；”首次“是指如果一个连接已经声明了一个排他队列，其他连接是不允许建立同名的排他队列的，
             * 这个与普通队列不同；即使该队列是持久华东，一旦连接关闭或者客户端退出，该排他队列都会自动被删除，这种队列适
             * 用于一个客户端同事发送和读取消息的应用场景
             *
             * autoDelete：设置是否自动删除。为true则设置队列为自动删除。自动删除的前提是：至少有一个消费者连接到这个队列，
             * 之后所有与这个队列连接的消费者都断开时，才会自动删除。不能把这个参数错误地理解为：”当连接到此队列的所有客户端断开时
             * ，这个队列自动删除“，因为生产者客户端创建这个队列，或者没有消费者客户端与这个队列连接时，都不会自动删除这个队列
             *
             * arguments：设置队列的其他一些参数，如x-message-ttl等
             */
            // 队列声明
            channel.queueDeclare(Constant.RabbitConstants.MessageService2Im + brokerId,
                    true, false, false, null);


            /**
             * queue：队列的名字。
             * exchange：交换器的名字。
             * routingKey：用于绑定的路由键。
             * arguments：用于绑定的参数
             */
            // 将一个队列绑定到一个交换器。
            channel.queueBind(Constant.RabbitConstants.MessageService2Im + brokerId,
                    Constant.RabbitConstants.MessageService2Im, brokerId);


            /**
             * 启动一个消费者，并返回服务端生成的消费者标识
             * queue:队列名
             * autoAck：true 接收到传递过来的消息后acknowledged（应答服务器），false 接收到消息后不应答服务器
             * deliverCallback： 当一个消息发送过来后的回调接口
             * cancelCallback：当一个消费者取消订阅时的回调接口;取消消费者订阅队列时除了使用{@link Channel#basicCancel}之外的所有方式都会调用该回调方法
             * @return 服务端生成的消费者标识
             */

            // 启动一个消费者，并返回服务端生成的消费者标识
            channel.basicConsume(Constant.RabbitConstants
                            .MessageService2Im + brokerId, false,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            try {
                                String msgStr = new String(body);
//                                log.info(msgStr);
//                                System.out.println(msgStr);
                                MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                                BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                                messageProcess.process(messagePack);
                                // envelope.getDeliveryTag() 可以认为这条消息的标记符   false 是否为批量提交
                                channel.basicAck(envelope.getDeliveryTag(),false);

                            }catch (Exception e){
                                e.printStackTrace();
                                // 第三个参数为是否重回队列
                                channel.basicNack(envelope.getDeliveryTag(),false,false);
                            }
                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        startReciverMessage();
    }
    public static void init(String brokerId) {

        if (StringUtils.isBlank(MessageReciver.brokerId)){
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }

}
