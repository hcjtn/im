package edu.nuc.hcj.im.service.group.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.group.model.req.*;
import edu.nuc.hcj.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.service
 * @ClassName : ImGroupMemberService.java
 * @createTime : 2023/12/12 15:51
 * @Description :
 */
public interface ImGroupMemberService {

    // 导入群成员
    public ResponseVO importGroupMember(ImportGroupMemberReq req);

    public ResponseVO addMember(AddGroupMemberReq req);

    public ResponseVO removeMember(RemoveGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);

    // 用户在某个群的身份(权限)
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId);

    // 获取加入的所有群聊
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);

    public List<String> getGroupMemberId(String groupId, Integer appId);

    public ResponseVO exitGroup(ExitGroupReq req);

    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId);

    public ResponseVO updateGroupMember(UpdateGroupMemberReq req);

    // 修改群成员的身份、权限
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId);

    // 禁言群成员
    public ResponseVO speak(SpeaMemberReq req);

    ResponseVO<Collection<String>> syncMemberJoinedGroup(String operater, Integer appId);
}
