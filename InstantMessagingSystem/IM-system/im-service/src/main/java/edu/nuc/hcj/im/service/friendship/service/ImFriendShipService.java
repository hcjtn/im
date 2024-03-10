package edu.nuc.hcj.im.service.friendship.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.model.RequestBase;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.service.friendship.model.req.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service
 * @ClassName : ImFriendShipService.java
 * @createTime : 2023/12/7 13:27
 * @Description : 实现有关好友的业务
 */
public interface ImFriendShipService {

    public ResponseVO importFriendShip(ImporFriendShipReq req);

    // 添加好友
    public ResponseVO addFriend(AddFriendReq req);

    // 修改好友接口
    public ResponseVO updateFriend(UpdateFriendReq req);
    // 删除好友
    public ResponseVO DeleteFriend(DeleteFriendReq req);
    // 删除所有好友
    public ResponseVO DeleteAllFriend(DeleteFriendReq req);

    // 获取所有好友信息
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    // 获取指定好友信息
    public ResponseVO getRelation(GetRelationReq req);

    //验证好友关系
    public ResponseVO CheckFriendShipRequest(CheckFriendShipReq req);

    // 添加黑名单
    public ResponseVO addBlack(AddFriendShipBlackReq req);

    // 删除黑名单
    public ResponseVO deleteBlack(DeleteBlackReq req);

    // 校验黑名单
    public ResponseVO checkBlack(CheckFriendShipReq req);

    public ResponseVO doAddFriend (RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    public List<String> getAllFriendId(String userId, Integer appId);

    ResponseVO syncFindshipList(SyncReq req);
}
