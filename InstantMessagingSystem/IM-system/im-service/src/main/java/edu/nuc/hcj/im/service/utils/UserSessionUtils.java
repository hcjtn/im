package edu.nuc.hcj.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ImConnectStatusEnum;
import edu.nuc.hcj.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : UserSessionUtils.java
 * @createTime : 2023/12/23 17:32
 * @Description :
 */
@Component
public class UserSessionUtils {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // 1.获取所有的用户session
    public List<UserSession> getUserSessions(Integer appId,String userId) {
        // 获取 key
        String userSessionKey = appId + Constant.RedisConstant.UserSessionConstants + userId;
        // 根据Usersessionkey获取存储在redis中的Usersession map
        Map<Object, Object> userSessionMap =
                stringRedisTemplate.opsForHash().entries(userSessionKey);
        List<UserSession> userSessionList = new ArrayList<>();
        //遍历usersession 获取在线的userSession
        Collection<Object> userSessions = userSessionMap.values();
        for (Object userSession : userSessions) {
            String userSession1 = (String) userSession;
            UserSession session = JSONObject.parseObject(userSession1, UserSession.class);
            // 用户当前状态在线
            if(session.getConnectState().equals(ImConnectStatusEnum.ONLINE_STATUS.getCode())){
                userSessionList.add(session);
            }
        }
        return userSessionList;
    }

    // 获取用户 本地端的session

    public UserSession getLocationUserSession(Integer appId,String userId,
                                              Integer clientType,String imei) {
        // 获取 key
        String userSessionKey = appId + Constant.RedisConstant.UserSessionConstants + userId;
        String hashkey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashkey);
        UserSession session = JSONObject.parseObject(o.toString(), UserSession.class);

        return session;
    }
}
