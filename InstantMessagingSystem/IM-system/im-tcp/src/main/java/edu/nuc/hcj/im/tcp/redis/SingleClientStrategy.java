package edu.nuc.hcj.im.tcp.redis;



import edu.nuc.hcj.im.codec.config.BootStrapConfigration;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.redis
 * @ClassName : SingleClientStrategy.java
 * @createTime : 2023/12/17 14:21
 * @Description : 管理单机模式下的redis客户端
 */
public class SingleClientStrategy {
    // 获取redis客户端
    public RedissonClient getRedissonClient(BootStrapConfigration.RedisConfig redisConfig) {
        Config config = new Config();
        // 获取reids地址
        String node = redisConfig.getSingle().getAddress();
        // 添加 redis://
        node = node.startsWith("redis://") ? node : "redis://" + node;
        // 添加redis配置
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(node)
                .setDatabase(redisConfig.getDatabase())
                .setTimeout(redisConfig.getTimeout())
                .setConnectionMinimumIdleSize(redisConfig.getPoolMinIdle())
                .setConnectTimeout(redisConfig.getPoolConnTimeout())
                .setConnectionPoolSize(redisConfig.getPoolSize());
        // 设置单机redis连接密码
        if (StringUtils.isNotBlank(redisConfig.getPassword())) {
            serverConfig.setPassword(redisConfig.getPassword());
        }
        StringCodec stringCodec = new StringCodec();
        //使用stringCodec编解码器
        config.setCodec(stringCodec);
        //开启redis客户端
        return Redisson.create(config);
    }

}
