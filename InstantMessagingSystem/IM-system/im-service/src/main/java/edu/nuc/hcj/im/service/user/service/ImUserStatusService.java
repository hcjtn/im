package edu.nuc.hcj.im.service.user.service;

import edu.nuc.hcj.im.service.user.model.UserStatusChangeNotifyContent;
import edu.nuc.hcj.im.service.user.model.req.PullFriendOnlineStatusReq;
import edu.nuc.hcj.im.service.user.model.req.SetUserCustomerStatusReq;
import edu.nuc.hcj.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import edu.nuc.hcj.im.service.user.model.resp.UserOnlineStatusResp;
import edu.nuc.hcj.im.service.user.mq.PullUserOnlineStatusReq;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.service
 * @ClassName : ImUserStatusService.java
 * @createTime : 2024/1/17 10:42
 * @Description :  用于处理用户的显现状态服务类
 */
public interface ImUserStatusService {
    // 处理用户在线状态通知
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);

}
