package edu.nuc.hcj.im.service.utils;

import edu.nuc.hcj.im.common.constant.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : WriteUserSeq.java
 * @createTime : 2024/1/15 17:19
 * @Description :
 */
@Component
public class WriteUserSeq {
    // redis
    @Autowired
    RedisTemplate redisTemplate;


    public void writeUserSeq(Integer appId,String userId,String type,Long seq){
        String key = appId + ":" + Constant.RedisConstant.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }
}
