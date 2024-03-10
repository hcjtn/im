package edu.nuc.hcj.im.service.group.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.service.group.dao.ImGroupEntity;
import edu.nuc.hcj.im.service.group.model.req.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.service
 * @ClassName : ImGroupService.java
 * @createTime : 2023/12/12 15:50
 * @Description :
 */

public interface ImGroupService {

    // 导入群
    public ResponseVO importGroup(ImportGroupReq req);

    // 创建群消息
    public ResponseVO createGroup(CreateGroupReq req);

    // 修改群组资料
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    // 获取用户加入的群聊列表业务
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    // 解散群组
    public ResponseVO destroyGroup(DestroyGroupReq req);

    // 转让群主
    public ResponseVO transferGroup(TransferGroupReq req);

    // 获取指定群消息
    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    // 获取指定群
    public ResponseVO getGroup(GetGroupReq req);

    // 禁言群
    public ResponseVO muteGroup(MuteGroupReq req);

//    ResponseVO syncJoinedGroupList(SyncReq req);

    Long getUserGroupMaxSeq(String userId, Integer appId);

    public ResponseVO syncJoinedGroupList(SyncReq req);
}
