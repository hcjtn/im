package edu.nuc.hcj.im.service.friendship.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipGroupEntity;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service.Impl
 * @ClassName : ImFriendShipGroupService.java
 * @createTime : 2023/12/12 12:39
 * @Description :  好友分组详细情况  创建分组 id 创建人id 创建时间 更新时间等等
 */
public interface ImFriendShipGroupService {

    // 添加分组
    public ResponseVO addGroup(AddFriendShipGroupReq req);

    // 删除分组
    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    // 获取分组
    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

    // 更新...
    public Long updateSeq(String fromId, String groupName, Integer appId);
}
