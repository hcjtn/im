package edu.nuc.hcj.im.service.group.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import edu.nuc.hcj.im.codec.park.group.CreateGroupPack;
import edu.nuc.hcj.im.codec.park.group.DestroyGroupPack;
import edu.nuc.hcj.im.codec.park.group.TransferGroupPack;
import edu.nuc.hcj.im.codec.park.group.UpdateGroupInfoPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.GroupErrorCode;
import edu.nuc.hcj.im.common.enums.GroupMemberRoleEnum;
import edu.nuc.hcj.im.common.enums.GroupStatusEnum;
import edu.nuc.hcj.im.common.enums.GroupTypeEnum;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.common.model.SyncResp;
import edu.nuc.hcj.im.service.group.dao.ImGroupEntity;
import edu.nuc.hcj.im.service.group.dao.mapper.ImGroupMapper;
import edu.nuc.hcj.im.service.group.model.callBack.DestroyGroupCallbackDto;
import edu.nuc.hcj.im.service.group.model.req.*;
import edu.nuc.hcj.im.service.group.model.resp.GetGroupResp;
import edu.nuc.hcj.im.service.group.model.resp.GetJoinedGroupResp;
import edu.nuc.hcj.im.service.group.model.resp.GetRoleInGroupResp;
import edu.nuc.hcj.im.service.group.service.ImGroupMemberService;
import edu.nuc.hcj.im.service.group.service.ImGroupService;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.utils.CallbackService;
import edu.nuc.hcj.im.service.utils.GroupMessageProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.service.Impl
 * @ClassName : ImGroupServiceImpl.java
 * @createTime : 2023/12/12 15:57
 * @Description :
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {
    @Autowired
    ImGroupMapper imGroupMapper;
    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    Appconfig appconfig;
    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    ImGroupMemberService groupMemberService;

    //将所有的群组盗取进去
    @Override
    public ResponseVO importGroup(ImportGroupReq req) {
        // 判断群是否存在组ID 则直接创建一个 groupID
        QueryWrapper group = new QueryWrapper();
        if (StringUtils.isEmpty(req.getGroupId())) {
            // 如果传递的参数Body中不存在
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            group.eq("group_id", req.getGroupId());
            group.eq("app_id", req.getAppId());
            ImGroupEntity imGroupEntity = imGroupMapper.selectOne(group);
            // group 已经存在
            if (imGroupEntity != null) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }
        ImGroupEntity imGroupEntity = new ImGroupEntity();

        // 公开群必须指定群主
        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        BeanUtils.copyProperties(req, imGroupEntity);
        // 创建时间
        if (req.getCreateTime() == null) {
            imGroupEntity.setCreateTime(System.currentTimeMillis());
        }
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        // 创建群组
        int insert = imGroupMapper.insert(imGroupEntity);

        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse();
    }

    // 创建群  接口
    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false;
        // 将当权操作者设置为群主
        if (!isAdmin) {
            req.setOwnerId(req.getOperater());
        }

        //  1.判断传递的参数中 群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        // 不存在直接用UUID创建新的 组ID
        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            // 存在 组ID
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupMapper.selectCount(query);
            // 判断以后的群聊当中是否有id相同的情况
            if (integer > 0) {
                // 群聊已经存在
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }
        // 判断群聊是否为公共群  当前操作用户的id是否为空
        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            // 公共群聊必须有群主
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }


        // 创建新群聊
        ImGroupEntity imGroupEntity = new ImGroupEntity();
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        imGroupEntity.setCreateTime(System.currentTimeMillis());
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        int insert = imGroupMapper.insert(imGroupEntity);
        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.CREATE_GROUP_ERROR);
        }
        // 将群成员插入进新创建的群聊当中
        for (GroupMemberDto dto : req.getMember()) {
            imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
        }

        // 创建群组成功之后进行回调
        if (appconfig.isCreateGroupAfterCallback()) {
            callbackService.callback(req.getAppId(),
                    Constant.CallbackCommand.CreateGroupAfter,
                    JSONObject.toJSONString(imGroupEntity));
        }
        // 创建群成功之后进行 数据通知
        CreateGroupPack createGroupPack = new CreateGroupPack();
        BeanUtils.copyProperties(imGroupEntity, createGroupPack);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, createGroupPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));


        return ResponseVO.successResponse();
    }

    /**
     * 修改群基础信息，如果是后台管理员调用，则不检查权限，如果不是则检查权限，如果是私有群（微信群）任何人都可以修改资料，公开群只有管理员可以修改
     * 如果是群主或者管理员可以修改其他信息。
     *
     * @param req
     * @return
     */
    @Override
    @Transactional
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req) {
        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("group_id", req.getGroupId());
        query.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(query);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
        }
        // 判断 群组是否已经解散
        if (imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }
        // 校验权限  只能群主或者管理员才能修改群资料
        boolean isAdmin = false;
        if (!isAdmin) {
            // 权限判断
            //用户在某个群的身份(权限)
            ResponseVO<GetRoleInGroupResp> role = imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            // 获取到里面的权限值   roleInfo
            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            // 判断当前是否为管理员或者为群主
            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode()
                    || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            // 公开群只能群主或者管理员 修改群资料
            if (!isManager && GroupTypeEnum.PUBLIC.getCode() == imGroupEntity.getGroupType()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

        }


        // 填写群资料
        ImGroupEntity update = new ImGroupEntity();
        BeanUtils.copyProperties(req, update);
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        update.setUpdateTime(System.currentTimeMillis());
        // 修改群资料
        int row = imGroupMapper.update(update, query);
        // 修改失败
        if (row != 1) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        if (appconfig.isModifyGroupAfterCallback()) {
            callbackService.callback(req.getAppId(),
                    Constant.CallbackCommand.UpdateGroupAfter,
                    JSONObject.toJSONString(update));
        }

        // 更新群成功之后进行 数据通知
        UpdateGroupInfoPack updateGroupInfoPack = new UpdateGroupInfoPack();
        BeanUtils.copyProperties(imGroupEntity, updateGroupInfoPack);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, updateGroupInfoPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }

    /**
     * 获取用户加入的群组
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req) {
        // 获取到加入的所有的群聊
        ResponseVO<Collection<String>> memberJoinedGroup = imGroupMemberService.getMemberJoinedGroup(req);
        if (memberJoinedGroup.isOk()) {
            GetJoinedGroupResp resp = new GetJoinedGroupResp();
            // 该用户没有加入任何群聊
            if (CollectionUtils.isEmpty(memberJoinedGroup.getData())) {
                resp.setTotalCount(0);
                resp.setGroupList(new ArrayList<>());
                return ResponseVO.successResponse(resp);
            }
            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.in("group_id", memberJoinedGroup.getData());

            if (CollectionUtils.isNotEmpty(req.getGroupType())) {
                query.in("group_type", req.getGroupType());
            }
            // 根据群id获取群信息
            List<ImGroupEntity> groupList = imGroupMapper.selectList(query);

            resp.setGroupList(groupList);
            if (req.getLimit() == null) {
                resp.setTotalCount(groupList.size());
            } else {
                resp.setTotalCount(imGroupMapper.selectCount(query));
            }
            return ResponseVO.successResponse(resp);
        } else {
            // 返回
            return memberJoinedGroup;
        }
    }

    /**
     * 转让群主
     *
     * @param req
     * @return
     */
    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(req.getGroupId(),
                req.getOperater(), req.getAppId());
        // 判断当前操作的人是否还在群内
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }
        //判断当前操作的人是否是群主
        if (roleInGroupOne.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        // 判断被操作人是否在群内
        ResponseVO<GetRoleInGroupResp> newOwnerRole = imGroupMemberService.getRoleInGroupOne(req.getGroupId(),
                req.getOwnerId(), req.getAppId());
        if (!newOwnerRole.isOk()) {
            return newOwnerRole;
        }

        // 判断组是否存在
        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(objectQueryWrapper);

        if (imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        // 跟新操作
        ImGroupEntity updateGroup = new ImGroupEntity();
        updateGroup.setOwnerId(req.getOwnerId());
        UpdateWrapper<ImGroupEntity> updateGroupWrapper = new UpdateWrapper<>();
        updateGroupWrapper.eq("app_id", req.getAppId());
        updateGroupWrapper.eq("group_id", req.getGroupId());
        imGroupMapper.update(updateGroup, updateGroupWrapper);
        imGroupMemberService.transferGroupMember(req.getOwnerId(), req.getGroupId(), req.getAppId());


        // 更新群成功之后进行 数据通知
        TransferGroupPack transferGroupPack = new TransferGroupPack();
        BeanUtils.copyProperties(imGroupEntity, transferGroupPack);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, transferGroupPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();


    }

    /**
     * 解散群组，只支持后台管理员和群主解散，私有群只支待app管理员解散
     *
     * @param req
     * @return
     */
    @Override
    @Transactional
    public ResponseVO destroyGroup(DestroyGroupReq req) {
        // 先判断需要解散的群组是否已经解散 存在
        QueryWrapper<ImGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", req.getGroupId());
        wrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(wrapper);
        if (imGroupEntity == null) {
            // 群不存在
            throw new ApplicationException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        // 群组已经解散
        if (imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }
        boolean isAdmin = false;
        // 判断是否为后台管理员 如果是 直接解散群聊
        if (!isAdmin) {
            // 判断是否为公开群聊
            if (imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
            }

            // 判断是否为 群主或者管理员
            if (imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                    !imGroupEntity.getOwnerId().equals(req.getOperater())) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        // 执行删除群聊操作
        ImGroupEntity update = new ImGroupEntity();
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        update.setStatus(GroupStatusEnum.DESTROY.getCode());
        int update1 = imGroupMapper.update(update, wrapper);
        // 执行解散群聊失败
        if (update1 != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        if (appconfig.isDeleteGroupMemberAfterCallback()) {
            DestroyGroupCallbackDto destroyGroupCallbackDto = new DestroyGroupCallbackDto();
            destroyGroupCallbackDto.setGroupId(req.getGroupId());
            callbackService.callback(req.getAppId(),
                    Constant.CallbackCommand.DestoryGroupAfter,
                    JSONObject.toJSONString(destroyGroupCallbackDto));
        }

        // 解散群成功之后进行 数据通知
        DestroyGroupPack destroyGroupPack = new DestroyGroupPack();
        BeanUtils.copyProperties(imGroupEntity, destroyGroupPack);
//        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, destroyGroupPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }

    // 获取指定群消息
    @Override
    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(query);
        if (imGroupEntity == null) {
            // 群不存在
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }


        return ResponseVO.successResponse(imGroupEntity);
    }

    // 获取指定群
    @Override
    public ResponseVO getGroup(GetGroupReq req) {
        ResponseVO<ImGroupEntity> group = this.getGroup(req.getGroupId(), req.getAppId());
        if (group.isOk()) {
            return group;
        }
        // 将查询到的信息 封装起来
        GetGroupResp getGroupResp = new GetGroupResp();
        BeanUtils.copyProperties(group.getData(), getGroupResp);

        try {
            // 查询组内的成员情况
            ResponseVO<List<GroupMemberDto>> groupMember =
                    imGroupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
            if (!groupMember.isOk()) {
                getGroupResp.setMemberList(groupMember.getData());
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return ResponseVO.successResponse(getGroupResp);
    }


    //群禁言
    @Override
    public ResponseVO muteGroup(MuteGroupReq req) {
        // 判断群是否存在
        ResponseVO<ImGroupEntity> groupResp = getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }
        // 判断 群 是否已经解散
        if (groupResp.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }
        boolean isadmin = false;

        if (!isadmin) {
            // 获取当前用户的身份 权限
            ResponseVO<GetRoleInGroupResp> roleInGroupOne =
                    imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
            // 该成员不存在 或者 已经离开
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }


            Integer role = roleInGroupOne.getData().getRole();// 获取该用户的权限

            // 判断当前用户是群主或者管理员
            boolean isManager = role == GroupMemberRoleEnum.MAMAGER.getCode()
                    || role == GroupMemberRoleEnum.OWNER.getCode();

            // 只有管理员或者群主才能进行群禁言
            if (!isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        // 执行群禁言
        ImGroupEntity update = new ImGroupEntity();
        update.setMute(req.getMute());

        UpdateWrapper<ImGroupEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id", req.getGroupId());
        wrapper.eq("app_id", req.getAppId());
        imGroupMapper.update(update, wrapper);

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO syncJoinedGroupList(SyncReq req) {
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);

        }

        SyncResp<ImGroupEntity> resp = new SyncResp<>();
        // 同步加入的群组  获取加入的群组
        ResponseVO<Collection<String> > memberJoinedGroup = groupMemberService.syncMemberJoinedGroup(req.getOperater(),
                req.getAppId());
        if (memberJoinedGroup.isOk()) {

            Collection<String> data = memberJoinedGroup.getData();
            QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.in("group_id", data);
            queryWrapper.gt("sequence",req.getLastSequence());
            queryWrapper.orderByAsc("sequence");
            queryWrapper.last(" limit " + req.getMaxLimit());


            List<ImGroupEntity> list = imGroupMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(list)) {
                ImGroupEntity maxSeqEntity = list.get(list.size() - 1);
                resp.setDataList(list);
                // 设置最大seq
                Long maxSeq = imGroupMapper.getGroupMaxSeq(data, req.getAppId());
                resp.setMaxSequence(maxSeq);
                // 设置是否拉取完毕
                resp.setCompleted(maxSeqEntity.getSequence() >= maxSeq);
                return ResponseVO.successResponse(resp);
            }

        }resp.setCompleted(true);return ResponseVO.successResponse(resp);

    }

    @Override
    public Long getUserGroupMaxSeq(String userId, Integer appId) {

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.syncMemberJoinedGroup(userId, appId);
        if(!memberJoinedGroup.isOk()){
            throw new ApplicationException(500,"");
        }
        Long maxSeq =
                imGroupMapper.getGroupMaxSeq(memberJoinedGroup.getData(),
                        appId);
        return maxSeq;
    }
}
