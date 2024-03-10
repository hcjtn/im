package edu.nuc.hcj.im.service.group.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.nuc.hcj.im.codec.park.group.AddGroupMemberPack;
import edu.nuc.hcj.im.codec.park.group.ExitGroupMemberPack;
import edu.nuc.hcj.im.codec.park.group.RemoveGroupMemberPack;
import edu.nuc.hcj.im.codec.park.group.TransferGroupPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.GroupErrorCode;
import edu.nuc.hcj.im.common.enums.GroupMemberRoleEnum;
import edu.nuc.hcj.im.common.enums.GroupTypeEnum;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.service.group.dao.ImGroupEntity;
import edu.nuc.hcj.im.service.group.dao.ImGroupMemberEntity;
import edu.nuc.hcj.im.service.group.dao.mapper.ImGroupMemberMapper;
import edu.nuc.hcj.im.service.group.model.callBack.AddMemberAfterCallback;
import edu.nuc.hcj.im.service.group.model.req.*;
import edu.nuc.hcj.im.service.group.model.resp.AddMemberResp;
import edu.nuc.hcj.im.service.group.model.resp.GetRoleInGroupResp;
import edu.nuc.hcj.im.service.group.service.ImGroupMemberService;
import edu.nuc.hcj.im.service.group.service.ImGroupService;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import edu.nuc.hcj.im.service.utils.CallbackService;
import edu.nuc.hcj.im.service.utils.GroupMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.service.Impl
 * @ClassName : ImGroupMemberService.java
 * @createTime : 2023/1m/12 15:57
 * @Description :
 */
@Service
@Slf4j
public class ImGroupMemberServiceImpl implements ImGroupMemberService {
    @Autowired
    ImGroupService imGroupService;
    @Autowired
    IMUserService imUserService;
    @Autowired
    ImGroupMemberMapper imGroupManagerMapper;
    @Autowired
    ImGroupMemberService imGroupMemberService;
    @Autowired
    Appconfig appconfig;
    @Autowired
    CallbackService callbackService;
    @Autowired
    GroupMessageProducer groupMessageProducer;


    @Override
    public ResponseVO importGroupMember(ImportGroupMemberReq req) {
        List<AddMemberResp> resp = new ArrayList<>();

        // 先判断该群聊是否存在
        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }
        for (GroupMemberDto memberId :
                req.getMembers()) {
            ResponseVO responseVO = null;
            try {
                // 添加群成员
                responseVO = imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberId);
            } catch (Exception e) {
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
            }

            // 将添加状态封装起来
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            if (responseVO.isOk()) {
                //成功
                addMemberResp.setResult(0);
            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                // 已经是群成员
                addMemberResp.setResult(2);
            } else {
                // 失败
                addMemberResp.setResult(1);
            }
            resp.add(addMemberResp);
        }

        return ResponseVO.successResponse(resp);
    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 添加群成员，拉人入群的逻辑，直接进入群聊。如果是后台管理员，则直接拉入群，
     * 否则只有私有群可以调用本接口，并且群成员也可以拉人入群.只有私有群可以调用本接口
     * @author hcj
     */
    @Override
    public ResponseVO addMember(AddGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();
        // 默认超级管理员是 false
        boolean isAdmin = false;
        // 现判断添加的群是否存在
        ResponseVO<ImGroupEntity> group =
                imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }


        List<GroupMemberDto> memberDtos = req.getMembers();
        if (appconfig.isAddGroupMemberBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(),
                    Constant.CallbackCommand.GroupMemberAddBefore,
                    JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }
            try {
                memberDtos = JSONArray.parseArray(JSONObject.toJSONString(responseVO.getData()), GroupMemberDto.class);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("GroupMemberAddBefore 回调失败：{}", req.getAppId());
            }
        }

        /**
         * 私有群（private） 类似普通微信群，创建后仅支持已在群内的好友邀请加群，且无需被邀请方同意或群主审批
         * 公开群（Public） 类似 QQ 群，创建后群主可以指定群管理员，需要群主或管理员审批通过才能入群
         * 群类型 1私有群（类似微信） 2公开群(类似qq）
         *
         */
        // 获取群聊数据
        ImGroupEntity groupData = group.getData();
        // 群聊不是公共群聊
        if (!isAdmin && groupData.getGroupType() == GroupTypeEnum.PUBLIC.getCode()) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
        }
        //将添加成功的成员 ID 记录出来
        List<String> successId = new ArrayList<>();
        for (GroupMemberDto memberId : memberDtos) {
            ResponseVO responseVO = null;
            try {
                imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberId);
            } catch (Exception e) {
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse();
            }
            // 将添加成功 用户Id 数据封装起来
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            if (responseVO.isOk()) {
                successId.add(memberId.getMemberId());
                // 添加成功
                addMemberResp.setResult(0);
            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                // 该用户已经进入该群
                addMemberResp.setResult(2);
                addMemberResp.setResultMessage(responseVO.getMsg());
            } else {
                // 添加失败
                addMemberResp.setResult(1);
                addMemberResp.setResultMessage(responseVO.getMsg());
            }
            resp.add(addMemberResp);
        }

        if (appconfig.isAddGroupMemberAfterCallback()) {
            AddMemberAfterCallback dto = new AddMemberAfterCallback();
            dto.setGroupId(req.getGroupId());
            dto.setGroupType(group.getData().getGroupType());
            dto.setMemberId(resp);
            dto.setOperater(req.getOperater());
            callbackService.callback(req.getAppId(), Constant.CallbackCommand.GroupMemberAddAfter,
                    JSONObject.toJSONString(dto));
        }

        // 群成员进入群之后 进行 数据通知
        AddGroupMemberPack addGroupMemberPack = new AddGroupMemberPack();
        addGroupMemberPack.setGroupId(req.getGroupId());
//        addGroupMemberPack.setMembers(req.getMembers());
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, addGroupMemberPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        return ResponseVO.successResponse(resp);
    }

    /**
     * 踢人出群
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO removeMember(RemoveGroupMemberReq req) {
        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (group.isOk()) {
            return group;
        }

        if (!isAdmin) {
            // 获取操作人的权限 是管理员or群主or群成员
            ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperater(),
                    req.getAppId());
            if (!role.isOk()) {
                return role;
            }
            // 获取群成员成分

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode();

            // 踢人必须是群主或者管理者
            if (!isOwner && !isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            //私有群必须是群主才能踢人
            if (GroupTypeEnum.PRIVATE.getCode() == group.getCode() && !isOwner) {
                throw new ApplicationException(GroupErrorCode.PRIVATE_GROUP_REMOVE_BY_OWNER);
            }

            // 公开群管理员和群主可踢人，但管理员只能踢普通群成员
            if (GroupTypeEnum.PUBLIC.getCode() == group.getCode()) {
                // 获取 被踢人 的权限  获取群成员在群中的身份
                ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(),
                        req.getMemberId(), req.getAppId());
                if (!roleInGroupOne.isOk()) {
                    return roleInGroupOne;
                }
                GetRoleInGroupResp memberRole = roleInGroupOne.getData();
                if (memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                    throw new ApplicationException(GroupErrorCode.GROUP_OWNER_IS_NOT_REMOVE);
                }
                // 是管理员并且被踢人不是群成员，无法操作
                if (isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }
            }
        }
        // 执行移除 踢人 条件
        ResponseVO responseVO = imGroupMemberService.removeGroupMember(req.getGroupId(), req.getAppId(),
                req.getMemberId());

        if (responseVO.isOk()) {
            if (appconfig.isDeleteGroupMemberAfterCallback()){
                callbackService.callback(req.getAppId(),
                        Constant.CallbackCommand.GroupMemberDeleteAfter,
                        JSONObject.toJSONString(req));
            }
        }
        // 群成员移除群之后 进行 数据通知
        RemoveGroupMemberPack removeGroupMemberPack = new RemoveGroupMemberPack();
        removeGroupMemberPack.setGroupId(req.getGroupId());
        removeGroupMemberPack.setMember(req.getMemberId());
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, removeGroupMemberPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        return responseVO;
    }

    /**
     * 添加群成员，内部调用
     *
     * @param groupId
     * @param appId
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto) {
        //  判断当前用户否存在
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(dto.getMemberId(), appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        if (dto.getRole() != null && GroupMemberRoleEnum.OWNER.getCode() == dto.getRole()) {
            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id", groupId);
            queryOwner.eq("app_id", appId);
            queryOwner.eq("role", GroupMemberRoleEnum.OWNER.getCode());
            Integer ownerNum = imGroupManagerMapper.selectCount(queryOwner);
            if (ownerNum > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        query.eq("app_id", appId);
        query.eq("member_id", dto.getMemberId());
        ImGroupMemberEntity memberDto = imGroupManagerMapper.selectOne(query);

        long now = System.currentTimeMillis();
        if (memberDto == null) {
            //初次加群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setGroupId(groupId);
            memberDto.setAppId(appId);
            memberDto.setJoinTime(now);
            int insert = imGroupManagerMapper.insert(memberDto);
            if (insert == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        } else if (GroupMemberRoleEnum.LEAVE.getCode() == memberDto.getRole()) {
            //重新进群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setJoinTime(now);
            int update = imGroupManagerMapper.update(memberDto, query);
            if (update == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);
    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 删除群成员，内部调用
     * @author lld
     */
    @Override
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId) {
        //  获取单个用户详情
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(memberId, appId);
        // 用户不存在
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }
        // 防止多线程的情况 进行二次判断
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = getRoleInGroupOne(groupId, memberId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        // 执行踢人操作
        GetRoleInGroupResp data = roleInGroupOne.getData();
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.LEAVE.getCode());
        imGroupMemberEntity.setLeaveTime(System.currentTimeMillis());
        imGroupMemberEntity.setGroupMemberId(data.getGroupMemberId());
        imGroupManagerMapper.updateById(imGroupMemberEntity);


        return ResponseVO.successResponse();
    }

    /**
     * 查询用户在群内的身份
     *
     * @param groupId
     * @param memberId
     * @param appId
     * @return
     */
    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId) {
        GetRoleInGroupResp resp = new GetRoleInGroupResp();
        // 建立查询条件 进行查询
        QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
        queryOwner.eq("group_id", groupId);
        queryOwner.eq("app_id", appId);
        queryOwner.eq("member_id", memberId);

        ImGroupMemberEntity imGroupMemberEntity = imGroupManagerMapper.selectOne(queryOwner);
        // 该成员不存在 或者 已经离开
        if (imGroupMemberEntity == null || imGroupMemberEntity.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            // 成员不在群聊中
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }
        // 将拆寻到的信息封装起来
        resp.setSpeakDate(imGroupMemberEntity.getSpeakDate());
        resp.setGroupMemberId(imGroupMemberEntity.getGroupMemberId());
        resp.setMemberId(imGroupMemberEntity.getMemberId());
        resp.setRole(imGroupMemberEntity.getRole());
        return ResponseVO.successResponse(resp);

    }

    /**
     * 获取群成员信息
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req) {
        if (req.getLimit() != null) {
            Page<ImGroupMemberEntity> objectPage = new Page<>(req.getOffset(), req.getLimit());
            QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("member_id", req.getMemberId());
            IPage<ImGroupMemberEntity> imGroupMemberEntityPage = imGroupManagerMapper.selectPage(objectPage, query);

            Set<String> groupId = new HashSet<>();
            List<ImGroupMemberEntity> records = imGroupMemberEntityPage.getRecords();
            records.forEach(e -> {
                groupId.add(e.getGroupId());
            });
            return ResponseVO.successResponse(groupId);
        } else {
            return ResponseVO.successResponse(imGroupManagerMapper.getJoinedGroupId(req.getAppId(), req.getMemberId()));
        }
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId) {
        List<GroupMemberDto> groupMember = imGroupManagerMapper.getGroupMember(appId, groupId);
        return ResponseVO.successResponse(groupMember);
    }

    @Override
    public List<String> getGroupMemberId(String groupId, Integer appId) {
        return imGroupManagerMapper.getGroupMemberId(appId, groupId);
    }


    /**
     * 退出群聊
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO exitGroup(ExitGroupReq req) {
        // 先判断该群聊是否存在
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }
        // 判断该用户是否在群内
        ResponseVO<GetRoleInGroupResp> inGroupOne =
                imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
        if (!group.isOk()) {
            return inGroupOne;
        }

        //执行退出群聊操作
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.LEAVE.getCode());
        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", req.getAppId());
        updateWrapper.eq("group_id", req.getGroupId());
        updateWrapper.eq("member_id", req.getOperater());
        imGroupManagerMapper.update(imGroupMemberEntity, updateWrapper);

        // 群成员退出群之后 进行 数据通知
        ExitGroupMemberPack exitGroupMemberPack = new ExitGroupMemberPack();
        exitGroupMemberPack.setGroupId(req.getGroupId());
        exitGroupMemberPack.setMember(req.getOperater());
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, exitGroupMemberPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        return ResponseVO.successResponse();
    }

    // 获取群聊 内的成员
    @Override
    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId) {
        List<GroupMemberDto> groupManager = imGroupManagerMapper.getGroupManager(groupId, appId);
        return groupManager;
    }

    // 修改群成员信息
    @Override
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req) {
        boolean isadmin = false;
        // 判断群聊是否存在
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }
        // 判断群状态 创建 或者已经解散
        ImGroupEntity groupData = group.getData();
        if (groupData.getStatus() == GroupErrorCode.GROUP_IS_DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        //是否是自己修改自己的资料
        boolean isMeOperater = req.getOperater().equals(req.getMemberId());
        if (!isadmin) {
            // 群昵称只能自己修改 群主或者管理员没有权限管理
            if (StringUtils.isBlank(req.getAlias()) && !isMeOperater) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_ONESELF);
            }
            //私有群不能设置管理员  并且只能有一位群主
            if (group.getData().getGroupType() == GroupTypeEnum.PRIVATE.getCode() &&
                    req.getRole() != null && (req.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() ||
                    req.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
                throw new ApplicationException(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_MANAGER);
            }

            // 如果要修改权限相关的则走下面的逻辑
            if (req.getRole() != null) {
                // 获取被操作人的是否在群内
                ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(),
                        req.getMemberId(), req.getAppId());
                if (!roleInGroupOne.isOk()) {
                    return roleInGroupOne;
                }

                // 获取操作人权限
                ResponseVO<GetRoleInGroupResp> operateRoleInGroupOne = this.getRoleInGroupOne(req.getGroupId(),
                        req.getOperater(), req.getAppId());
                if (!operateRoleInGroupOne.isOk()) {
                    return operateRoleInGroupOne;
                }

                GetRoleInGroupResp data = operateRoleInGroupOne.getData();
                Integer roleInfo = data.getRole();
                boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode();

                // 不是管理员不能修改权限
                if (req.getRole() != null && !isOwner && !isManager) {
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                }

                // 管理员只有群主能够设置
                if (req.getRole() != null && req.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() && !isOwner) {
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }

            }

        }

        ImGroupMemberEntity update = new ImGroupMemberEntity();

        if (StringUtils.isNotBlank(req.getAlias())) {
            update.setAlias(req.getAlias());
        }

        // 不能直接修改为群主
        if (req.getRole() != null && req.getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            update.setRole(req.getRole());
        }

        // 执行更新操作
        UpdateWrapper<ImGroupMemberEntity> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("app_id", req.getAppId());
        objectUpdateWrapper.eq("member_id", req.getMemberId());
        objectUpdateWrapper.eq("group_id", req.getGroupId());
        imGroupManagerMapper.update(update, objectUpdateWrapper);

        return ResponseVO.successResponse();
    }


    @Override
    @Transactional
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId) {
        // 更新旧群主
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        // 将旧群主更新为普通成员
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", appId);
        updateWrapper.eq("group_id", groupId);
        updateWrapper.eq("role", GroupMemberRoleEnum.OWNER.getCode());
        // 执行操作
        imGroupManagerMapper.update(imGroupMemberEntity, updateWrapper);

        // 更新新群主
        ImGroupMemberEntity newOwner = new ImGroupMemberEntity();
        // 设置为群主
        newOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        UpdateWrapper<ImGroupMemberEntity> ownerWrapper = new UpdateWrapper<>();
        ownerWrapper.eq("app_id", appId);
        ownerWrapper.eq("group_id", groupId);
        ownerWrapper.eq("member_id", owner);
        // 执行操作
        imGroupManagerMapper.update(newOwner, ownerWrapper);

        return ResponseVO.successResponse();
    }

    // 禁言群成员
    @Override
    @Transactional
    public ResponseVO speak(SpeaMemberReq req) {
        // 判断群是否存在
        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        //
        boolean isadmin = false;
        boolean isManager = false;
        boolean isManagerORisOwner = false;
        GetRoleInGroupResp memberRole = null;

        if (!isadmin) {
            // 判断操作人权限 是管理员or群主or群成员
            ResponseVO<GetRoleInGroupResp> roleInGroupOne =
                    getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }
            Integer role = roleInGroupOne.getData().getRole();// 获取操作人的权限
            isManagerORisOwner = role == GroupMemberRoleEnum.MAMAGER.getCode()
                    || role == GroupMemberRoleEnum.OWNER.getCode();

            isManager = role == GroupMemberRoleEnum.MAMAGER.getCode();
            if (!isManagerORisOwner) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            // 获取被操作人员的权限
            ResponseVO<GetRoleInGroupResp> roleIn =
                    imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
            if (!roleIn.isOk()) {
                return roleIn;
            }
            memberRole = roleInGroupOne.getData();
            // 被操作人是群主只能app管理员操作
            if (memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
            }
            // 是管理员并且被操作人不是群成员，无法操作
            if (isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                throw new ApplicationException(GroupErrorCode.CAN_NOT_BANED);
            }
        }
        // 在获取一遍被操作人员的权限 确保做高并发的情况下 数据的准确性
        if (memberRole == null) {
            // 获取被操作的权限
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(),
                    req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }
            memberRole = roleInGroupOne.getData();
        }
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setGroupMemberId(memberRole.getGroupMemberId());
        // 禁言时间不能为空  如果禁言时间大于零 则在当前时间的基础上添加 禁言时间
        if (req.getSpeakDate() > 0) {
            imGroupMemberEntity.setSpeakDate(System.currentTimeMillis() + req.getSpeakDate());
        } else {
            // 如果禁言时间不大于零 则表示解除禁言
            imGroupMemberEntity.setSpeakDate(req.getSpeakDate());
        }
        // 更新操作
        int i = imGroupManagerMapper.updateById(imGroupMemberEntity);

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<Collection<String>> syncMemberJoinedGroup(String operater, Integer appId) {
        return ResponseVO.successResponse(
                imGroupManagerMapper.syncJoinedGroupId(appId,operater,GroupMemberRoleEnum.LEAVE.getCode())
        );
    }
}
