package edu.nuc.hcj.im.service.friendship.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service
 * @ClassName : ImFriendShipGroupMemberService.java
 * @createTime : 2023/12/12 12:40
 * @Description :  好友分组  分组成员情况  有多少名成员
 */
public interface ImFriendShipGroupMemberService {

    // 添加好友分组成员
    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    // 删除好友成员
    public  ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    // 添加好友操作 被其他方法调用 addGroupMember等
    public int doAddGroupMember(Long groupId, String toId);

    // 清空好友分组成员
    public int clearGroupMember(Long groupId);
}
