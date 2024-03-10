package edu.nuc.hcj.im.tcp.redis;

import edu.nuc.hcj.im.codec.config.BootStrapConfigration;
import edu.nuc.hcj.im.tcp.reciver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.redis
 * @ClassName : RedisManager.java
 * @createTime : 2023/12/17 14:19
 * @Description : redis管理类
 */
public class RedisManager {
    private static RedissonClient redissonClient;
    private static Integer loginModel;

    public static void init(BootStrapConfigration configration) {
        loginModel = configration.getIm().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(configration.getIm().getRedis());


        //用户登录消息侦听器
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
