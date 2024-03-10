package edu.nuc.hcj.im.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.seq
 * @ClassName : RedisSeq.java
 * @createTime : 2024/1/13 11:37
 * @Description : 借用redis 的 increment 操作原子性 实现序列号
 */
@Component
public class RedisSeq {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // 获取序列号
    public long doGetSeq(String key){

        return stringRedisTemplate.opsForValue().increment(key);
    }

}
