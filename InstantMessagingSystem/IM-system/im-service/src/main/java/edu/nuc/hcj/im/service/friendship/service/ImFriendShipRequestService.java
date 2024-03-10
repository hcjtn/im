package edu.nuc.hcj.im.service.friendship.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.friendship.model.req.ApproverFriendRequestReq;
import edu.nuc.hcj.im.service.friendship.model.req.FriendDto;
import edu.nuc.hcj.im.service.friendship.model.req.ReadFriendShipRequestReq;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service
 * @ClassName : ImFriendShipRequestService.java
 * @createTime : 2023/12/7 19:12
 * @Description :
 */
public interface ImFriendShipRequestService {

    // 添加好友申请
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId);

    //审批好友请求
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    // 已读好友申请
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    // 获取去好友申请列表
    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
