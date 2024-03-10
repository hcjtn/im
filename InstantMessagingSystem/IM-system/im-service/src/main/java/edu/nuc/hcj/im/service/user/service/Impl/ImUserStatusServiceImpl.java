package edu.nuc.hcj.im.service.user.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.park.user.UserCustomStatusChangeNotifyPack;
import edu.nuc.hcj.im.codec.park.user.UserStatusChangeNotifyPack;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.command.UserEventCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.UserSession;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipService;
import edu.nuc.hcj.im.service.user.model.UserStatusChangeNotifyContent;
import edu.nuc.hcj.im.service.user.model.req.PullFriendOnlineStatusReq;
import edu.nuc.hcj.im.service.user.model.req.SetUserCustomerStatusReq;
import edu.nuc.hcj.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import edu.nuc.hcj.im.service.user.model.resp.UserOnlineStatusResp;
import edu.nuc.hcj.im.service.user.mq.PullUserOnlineStatusReq;
import edu.nuc.hcj.im.service.user.service.ImUserStatusService;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.service.Impl
 * @ClassName : ImUserStatusServiceImpl.java
 * @createTime : 2024/1/17 10:45
 * @Description :
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {
    @Autowired
    UserSessionUtils userSessionUtils;
    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendShipService imFriendService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ImUserStatusService userStatusService;

    //    处理用户在线状态通知
    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {
        // 获取用户所有的userSession
        List<UserSession> userSessions =
                userSessionUtils.getUserSessions(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSessions);

        //TODO 发送给自己的同步端
        syncSender(userStatusChangeNotifyPack, content.getUserId(), content);
        // TODO 同步给好友和订阅了自己的人
        dispatcher(userStatusChangeNotifyPack, content.getUserId(),
                content.getAppId());
    }

    //TODO 发送给自己的同步端
    private void syncSender(Object pack, String userId, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                pack, clientInfo);
    }

    // TODO 同步给好友和订阅了自己的人
    private void dispatcher(Object pack, String userId, Integer appId) {
        // 获取所有的好友Id
        List<String> allFriendId = imFriendService.getAllFriendId(userId, appId);
        // 遍历好友id
        for (String fid : allFriendId) {
            messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack, appId);
        }

        // TODO 发送给临时订阅的人
        String userKey = appId + ":" + Constant.RedisConstant.subscribe + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            // 谁订阅了我    req.getOperater()
            String filed = (String) key;
            // 获取过期时间
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            // 过期时间大于零 过期时间大于当前时间
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack, appId);
            } else {
                // 过期删掉
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

    /**
     * 订阅用户在线状态
     *
     * @param req
     */
    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        // A
        // Z
        // A - B C D
        // C：A Z F
        //hash
        // B - [A:xxxx,C:xxxx]
        // C - []
        // D - []
        Long subExpireTime = 0L;
        if (req != null && req.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constant.RedisConstant.subscribe + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey, req.getOperater(), subExpireTime.toString());
        }
    }

    //用户自定义设置状态
    @Override
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {
        // 封装 用户自定义状态更改通知包
        UserCustomStatusChangeNotifyPack userCustomStatusChangeNotifyPack =
                new UserCustomStatusChangeNotifyPack();
        userCustomStatusChangeNotifyPack.setCustomStatus(req.getCustomStatus());
        userCustomStatusChangeNotifyPack.setCustomText(req.getCustomText());
        userCustomStatusChangeNotifyPack.setUserId(req.getUserId());
        stringRedisTemplate.opsForValue().set(req.getAppId()
                        + ":" + Constant.RedisConstant.userCustomerStatus + ":" + req.getUserId()
                , JSONObject.toJSONString(userCustomStatusChangeNotifyPack));
        // 将封装好的对象发送给自己的同步端
        syncSender(userCustomStatusChangeNotifyPack,
                req.getUserId(), new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        // 将封装好的对象发送给临时订阅的用户
        dispatcher(userCustomStatusChangeNotifyPack, req.getUserId(), req.getAppId());
    }


    @Override
    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req) {

        List<String> allFriendId = imFriendService.getAllFriendId(req.getOperater(), req.getAppId());
        return getUserOnlineStatus(allFriendId, req.getAppId());
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        return getUserOnlineStatus(req.getUserList(), req.getAppId());
    }

    private Map<String, UserOnlineStatusResp> getUserOnlineStatus(List<String> userId, Integer appId) {

        Map<String, UserOnlineStatusResp> result = new HashMap<>(userId.size());
        for (String uid : userId) {

            UserOnlineStatusResp resp = new UserOnlineStatusResp();
            List<UserSession> userSession = userSessionUtils.getUserSessions(appId, uid);
            resp.setSession(userSession);
            String userKey = appId + ":" + Constant.RedisConstant.userCustomerStatus + ":" + uid;
            String s = stringRedisTemplate.opsForValue().get(userKey);
            if (StringUtils.isNotBlank(s)) {
                JSONObject parse = (JSONObject) JSON.parse(s);
                resp.setCustomText(parse.getString("customText"));
                resp.setCustomStatus(parse.getInteger("customStatus"));
            }
            result.put(uid, resp);
        }
        return result;
    }
}
