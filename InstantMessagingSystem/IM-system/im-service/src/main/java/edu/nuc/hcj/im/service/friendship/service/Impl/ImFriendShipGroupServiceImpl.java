package edu.nuc.hcj.im.service.friendship.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.nuc.hcj.im.codec.park.frienship.AddFriendGroupPack;
import edu.nuc.hcj.im.codec.park.frienship.DeleteFriendGroupPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.DelFlagEnum;
import edu.nuc.hcj.im.common.enums.FriendShipErrorCode;
import edu.nuc.hcj.im.common.enums.command.FriendshipEventCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipGroupEntity;
import edu.nuc.hcj.im.service.friendship.dao.mapper.ImFriendShipGroupMapper;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipGroupMemberService;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipGroupService;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.WriteUserSeq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service.Impl
 * @ClassName : ImFriendShipGroupServiceImpl.java
 * @createTime : 2023/12/12 13:00
 * @Description :  好友分组模块
 */
@Service
public class ImFriendShipGroupServiceImpl implements ImFriendShipGroupService {
    @Autowired
    ImFriendShipGroupMapper imFriendShipGroupMapper;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    WriteUserSeq writeUserSeq;
    @Autowired
    MessageProducer messageProducer;

    // 创建分组
    @Override
    @Transactional
    public ResponseVO addGroup(AddFriendShipGroupReq req) {
        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", req.getGroupName());
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
//        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        // 进行查询 如果group已经创建 则直接返回 如果没有 则创建对应的group
        ImFriendShipGroupEntity imFriendShipGroupEntity = imFriendShipGroupMapper.selectOne(query);
        if (imFriendShipGroupEntity != null &&
                imFriendShipGroupEntity.getDelFlag() == DelFlagEnum.NORMAL.getCode()) {
            // 群组已经创建
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }
        // 群组已经创建 但 之前的操作已经对其删除
        if (imFriendShipGroupEntity != null &&
                imFriendShipGroupEntity.getDelFlag() == DelFlagEnum.DELETE.getCode()) {
            imFriendShipGroupEntity.setDelFlag(DelFlagEnum.NORMAL.getCode());
            imFriendShipGroupMapper.updateById(imFriendShipGroupEntity);
            return ResponseVO.successResponse();
        }

        // 创建对应的group
        ImFriendShipGroupEntity insertGroup = new ImFriendShipGroupEntity();
        insertGroup.setAppId(req.getAppId());
        insertGroup.setGroupName(req.getGroupName());
        insertGroup.setCreateTime(System.currentTimeMillis());
        insertGroup.setDelFlag(DelFlagEnum.NORMAL.getCode());
        insertGroup.setFromId(req.getFromId());
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.FriendshipGroup);
        insertGroup.setSequence(seq);
        try {
            int insert = imFriendShipGroupMapper.insert(insertGroup);
            // 好友分组创建失败
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
            }
            // 好友分组创建成功 直接将toId的信息添加到对应的db中
            if (insert == 1 && CollectionUtil.isNotEmpty(req.getToIds())) {
                AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
                addFriendShipGroupMemberReq.setFromId(req.getFromId());
                addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
                addFriendShipGroupMemberReq.setToIds(req.getToIds());
                addFriendShipGroupMemberReq.setAppId(req.getAppId());
                imFriendShipGroupMemberService.addGroupMember(addFriendShipGroupMemberReq);
                return ResponseVO.successResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 好友分组已存在
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }
        AddFriendGroupPack addFriendGropPack = new AddFriendGroupPack();
        addFriendGropPack.setFromId(req.getFromId());
        addFriendGropPack.setGroupName(req.getGroupName());
        addFriendGropPack.setSequence(seq);
        writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constant.SeqConstants.FriendshipGroup, seq);
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req) {
        for (String groupName : req.getGroupName()) {
            QueryWrapper<ImFriendShipGroupEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_name", groupName);
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.eq("from_id", req.getFromId());
            queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());
            // 查询当前的分组是否已经删除
            ImFriendShipGroupEntity deleteGroued = imFriendShipGroupMapper.selectOne(queryWrapper);

            // 未被删除
            if (deleteGroued != null) {
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.FriendshipGroup);
                // 将 del——flag的状态变为已经删除状态
                ImFriendShipGroupEntity deleteGroup = new ImFriendShipGroupEntity();
                deleteGroup.setSequence(seq);
                deleteGroup.setGroupId(deleteGroued.getGroupId());
                deleteGroup.setDelFlag(DelFlagEnum.DELETE.getCode());
                imFriendShipGroupMapper.updateById(deleteGroup);
                // 将组内的成员清空
                imFriendShipGroupMemberService.clearGroupMember(deleteGroup.getGroupId());

                DeleteFriendGroupPack deleteFriendGroupPack = new DeleteFriendGroupPack();
                deleteFriendGroupPack.setFromId(req.getFromId());
                deleteFriendGroupPack.setGroupName(groupName);
                deleteFriendGroupPack.setSequence(seq);
                //TCP通知
                messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_DELETE,
                        deleteFriendGroupPack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
                //写入seq
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constant.SeqConstants.FriendshipGroup, seq);

            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_DELETE);
            }
        }
        return ResponseVO.successResponse();
    }

    // 获取创建的好友群组分析
    @Override
    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId) {
        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", groupName);
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        // 获取组详细信息
        ImFriendShipGroupEntity getGroup = imFriendShipGroupMapper.selectOne(query);

        if (getGroup == null) {
            // 好友分组不存在
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(getGroup);
    }

    @Override
    public Long updateSeq(String fromId, String groupName, Integer appId) {
        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", groupName);
        query.eq("app_id", appId);
        query.eq("from_id", fromId);

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);

        long seq = redisSeq.doGetSeq(appId + ":" + Constant.SeqConstants.FriendshipGroup);

        ImFriendShipGroupEntity group = new ImFriendShipGroupEntity();
        group.setGroupId(entity.getGroupId());
        group.setSequence(seq);
        imFriendShipGroupMapper.updateById(group);
        return seq;
    }
}
