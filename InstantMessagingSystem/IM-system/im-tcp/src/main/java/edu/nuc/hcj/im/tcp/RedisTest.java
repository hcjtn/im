package edu.nuc.hcj.im.tcp;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp
 * @ClassName : RedisTest.java
 * @createTime : 2023/12/16 18:54
 * @Description : 192.168.70.138
 */
public class RedisTest {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.70.138:6378").setPassword("123456");
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        RedissonClient redissonClient = Redisson.create(config);

        // String
//        RBucket<Object> hcj = redissonClient.getBucket("hcj");
//        System.out.println(hcj.get());
//        hcj.set("tn");
//        System.out.println(hcj.get());

        //Map
//        RMap<Object, Object> client = redissonClient.getMap("imap");
//        System.out.println(client.get("hcj"));
//        client.put("hcj","tn");
//        System.out.println(client.get("hcj"));

        // redis 实现消息的发布订阅   redisson的发布订阅它会发送给所有监听这个topic的客户端，
        // getTopic("topic") 中的topic是订阅的主题 相当于appId 用于确定订阅的源头
        RTopic topic = redissonClient.getTopic("topic");
        topic.addListener(String.class, new MessageListener<String>() {
            // 获取消息
            @Override
            public void onMessage(CharSequence charSequence, String s) {
                System.err.println(" topic + 收到消息:" + s);
            }
        });
        RTopic topic2 = redissonClient.getTopic("topic");

        topic2.addListener(String.class, new MessageListener<String>() {
            // 获取消息
            @Override
            public void onMessage(CharSequence charSequence, String s) {
                System.err.println(" topic2 + 收到消息:" + s);
            }
        });
        // 发布消息 要在监听消息后进行发布
        topic.publish("hello hcj");

    }
}
