package edu.nuc.hcj.im.service.friendship.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.nuc.hcj.im.codec.park.frienship.AddFriendGroupMemberPack;
import edu.nuc.hcj.im.codec.park.frienship.DeleteFriendGroupMemberPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.enums.command.FriendshipEventCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipGroupEntity;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipGroupMemberEntity;
import edu.nuc.hcj.im.service.friendship.dao.mapper.ImFriendShipGroupMemberMapper;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.resp.ImportFriendShipResp;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipGroupMemberService;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipGroupService;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.WriteUserSeq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service.Impl
 * @ClassName : ImFriendShipGroupMemberServiceImpl.java
 * @createTime : 2023/12/12 12:56
 * @Description :  好友分组模块
 */
@Service
public class ImFriendShipGroupMemberServiceImpl implements ImFriendShipGroupMemberService {

    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;
    @Autowired
    ImFriendShipGroupMemberMapper imFriendShipGroupMemberMapper;
    @Autowired
    IMUserService imUserService;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    WriteUserSeq writeUserSeq;


    @Override
    @Transactional
    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req) {
        // 先判断 分组 是 否存在
        ResponseVO<ImFriendShipGroupEntity> group =
                imFriendShipGroupService.getGroup(req.getFromId(), req.getGroupName(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }
        // 将所有的ToiD添加到db中 并记录成功添加的id
        List<String> sucess = new ArrayList<>();
        List<String> error = new ArrayList<>();
        for (String toId : req.getToIds()) {
            int i = this.doAddGroupMember(group.getData().getGroupId(), toId);
            if (i == 1) {
                sucess.add(toId);
            } else {
                error.add(toId);
            }
        }

        // 用于将成功ID和失败ID封装起来
        ImportFriendShipResp importFriendShipResp = new ImportFriendShipResp();
        importFriendShipResp.setSuccessIds(sucess);
        importFriendShipResp.setErrorIds(error);
        Long seq = imFriendShipGroupService.updateSeq(req.getFromId(), req.getGroupName(), req.getAppId());
        AddFriendGroupMemberPack pack = new AddFriendGroupMemberPack();
        pack.setFromId(req.getFromId());
        pack.setGroupName(req.getGroupName());
        pack.setToIds(sucess);
        pack.setSequence(seq);
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_ADD,
                pack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));


        return ResponseVO.successResponse(importFriendShipResp);
    }

    // 删除群成员
    @Override
    @Transactional
    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req) {
// 先判断 分组 是 否存在
        ResponseVO<ImFriendShipGroupEntity> group =
                imFriendShipGroupService.getGroup(req.getFromId(), req.getGroupName(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }
        List<String> successId = new ArrayList<>();
        for (String toId : req.getToIds()) {
            // 判断要删除的用户是否存在
            ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if (singleUserInfo.isOk()) {
                int i = deleteGroupMember(group.getData().getGroupId(), toId);
                if (i == 1) {
                    successId.add(toId);
                }
            }
        }
        Long seq = imFriendShipGroupService.updateSeq(req.getFromId(), req.getGroupName(), req.getAppId());
        DeleteFriendGroupMemberPack pack = new DeleteFriendGroupMemberPack();
        pack.setFromId(req.getFromId());
        pack.setGroupName(req.getGroupName());
        pack.setToIds(successId);
        pack.setSequence(seq);
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_DELETE,
                pack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));

        return ResponseVO.successResponse(successId);
    }


    // 添加分组成员 方便其他方法调用
    @Override
    public int doAddGroupMember(Long groupId, String toId) {
        ImFriendShipGroupMemberEntity imFriendShipGroupMemberEntity = new ImFriendShipGroupMemberEntity();
        imFriendShipGroupMemberEntity.setGroupId(groupId);
        imFriendShipGroupMemberEntity.setToId(toId);
        try {
            int insert = imFriendShipGroupMemberMapper.insert(imFriendShipGroupMemberEntity);
            return insert;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteGroupMember(Long groupId, String toId) {
        QueryWrapper<ImFriendShipGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("to_id", toId);

        try {
            int delete = imFriendShipGroupMemberMapper.delete(queryWrapper);
//            int insert = imFriendShipGroupMemberMapper.insert(imFriendShipGroupMemberEntity);
            return delete;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int clearGroupMember(Long groupId) {
        QueryWrapper<ImFriendShipGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id",groupId);
        int delete = imFriendShipGroupMemberMapper.delete(query);
        return delete;
    }
}
