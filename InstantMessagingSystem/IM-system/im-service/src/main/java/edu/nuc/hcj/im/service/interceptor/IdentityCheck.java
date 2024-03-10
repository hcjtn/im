package edu.nuc.hcj.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import edu.nuc.hcj.im.common.BaseErrorCode;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.GateWayErrorCode;
import edu.nuc.hcj.im.common.exception.ApplicationExceptionEnum;
import edu.nuc.hcj.im.common.utils.SigAPI;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.interceptor
 * @ClassName : IdentifierCheck.java
 * @createTime : 2024/1/6 15:14
 * @Description : 操作人检查
 */
@Data
@Component
public class IdentityCheck {
    private static Logger logger = LoggerFactory.getLogger(IdentityCheck.class);

    @Autowired
    Appconfig appconfig; // 将appid和密钥联系起来 在生产环境中需要使用表格将两者的联系记录出来 这里使用配置文件的方式记录

    @Autowired //将联系起来的两者存储到redis中 将解密后的信息存储起来 无需每次进行解密
    StringRedisTemplate redisTemplate;

    @Autowired
    IMUserService imUserService;

    public ApplicationExceptionEnum checkUserSig(String identity, String appId, String userSig) {
        // 获取过期时间
        String cacheUserSig = redisTemplate.opsForValue()
                .get(appId + ":" + Constant.RedisConstant.userSign + ":"
                        + identity + userSig);
        if (!StringUtils.isBlank(cacheUserSig) && Long.valueOf(cacheUserSig) > System.currentTimeMillis() / 1000) {
//            this.setIsAdmin(identity, Integer.valueOf(appId));
            return BaseErrorCode.SUCCESS;
        }

        // 获取密钥
        String privateKey = appconfig.getPrivateKey();
        //根据appid和密钥创建sigApi
        SigAPI sigAPI = new SigAPI(Long.valueOf(appId), privateKey);
        // 调用sigApi对UserSig解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSig);

        // 取出解密后的appid和操作人和过期时间做匹配 不通过提示错误
        Long expireTime = 0L;
        // 过期秒数
        Long expireSec = 0L;
        Long time = 0L;
        String decoerAppId = "";
        String decoderidentifier = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderidentifier = jsonObject.getString("TLS.identifier");
            time = Long.valueOf(jsonObject.getString("TLS.expireTime"));
            expireSec = Long.valueOf(jsonObject.getString("TLS.expire"));
            expireTime = Long.valueOf(jsonObject.getString("TLS.expireTime")) + expireSec;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUserSig-error:{}", e.getMessage());
        }

        if (!identity.equals(decoderidentifier)) {
            return GateWayErrorCode.OPERATE_NOT_MATE;
        }
        if (!appId.equals(decoerAppId)) {
            return GateWayErrorCode.APPID_NOT_MATE;
        }
        // 已经过期
        if (expireSec == 0L) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }
        if (expireTime < System.currentTimeMillis() / 1000) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }


        // appid + "xxx" + userId + sign
        String genSig = sigAPI.genUserSig(identity, expireSec, time, null);
        if (genSig.toLowerCase().equals(userSig.toLowerCase())) {
            String key = appId + ":" + Constant.RedisConstant.userSign + ":"
                    + identity + userSig;

            Long etime = expireTime - System.currentTimeMillis() / 1000;
            redisTemplate.opsForValue().set(
                    key, expireTime.toString(), etime, TimeUnit.SECONDS);
//            this.setIsAdmin(identity, Integer.valueOf(appId));
            return BaseErrorCode.SUCCESS;
        }

        return GateWayErrorCode.USERSIGN_IS_ERROR;
    }

}
