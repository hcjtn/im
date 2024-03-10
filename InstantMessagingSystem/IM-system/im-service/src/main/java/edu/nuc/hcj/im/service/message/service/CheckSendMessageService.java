package edu.nuc.hcj.im.service.message.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.enums.*;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipEntity;
import edu.nuc.hcj.im.service.friendship.model.req.GetRelationReq;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipService;
import edu.nuc.hcj.im.service.group.dao.ImGroupEntity;
import edu.nuc.hcj.im.service.group.model.resp.GetRoleInGroupResp;
import edu.nuc.hcj.im.service.group.service.ImGroupMemberService;
import edu.nuc.hcj.im.service.group.service.ImGroupService;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.message.service
 * @ClassName : CheckSendMessageService.java
 * @createTime : 2024/1/6 17:56
 * @Description :
 */
@Service
public class CheckSendMessageService {
    @Autowired
    IMUserService imUserService;
    @Autowired
    ImFriendShipService imFriendShipService;
    @Autowired
    Appconfig appconfig;
    @Autowired
    ImGroupMemberService imGroupMemberService;
    @Autowired
    ImGroupService imGroupService;


    // 检查发件人禁用和禁言
    public ResponseVO checkSenderForbiddenAndMute(String fromId, Integer appId) {
        // 判断单个用户是否存在
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ImUserDataEntity data = singleUserInfo.getData();
        // 禁言标识
        Integer forbiddenFlag = data.getForbiddenFlag();
        // 禁用标识
        if (forbiddenFlag == UserForbiddenFlagEnum.FORBIBBEN.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
            // 判断禁言标识
        } else if (forbiddenFlag == UserSilentFlagEnum.MUTE.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }
        return ResponseVO.successResponse();
    }

    //  检查好友关系
    public ResponseVO checkFriendShip(String fromId, String toId, Integer appId) {
        if (appconfig.isSendMessageCheckFriend()) {
            // 判断好友关系的 data 是否存在
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendShipService.getRelation(fromReq);
            if (!fromRelation.isOk()) {
                return ResponseVO.errorResponse();
            }
            GetRelationReq toReq = new GetRelationReq();
            fromReq.setFromId(toId);
            fromReq.setToId(fromId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendShipService.getRelation(toReq);
            if (!toRelation.isOk()) {
                return ResponseVO.errorResponse();
            }
            // 数据存在 判断是否为好友
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()
                    != fromRelation.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()
                    != toRelation.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
            }
            if (appconfig.isSendMessageCheckBlack()) {
                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != fromRelation.getData().getStatus()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }
                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != toRelation.getData().getStatus()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }

        }
        return ResponseVO.successResponse();
    }


    public ResponseVO checkGroupMessage(String fromId, String groupId, Integer appId) {
        // 检查当前用户是否被禁言 / 被禁用
        ResponseVO responseVO = checkSenderForbiddenAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        // 对群逻辑进行判断
        // 判断群是否存在
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(groupId, appId);
        if (!group.isOk()) {
            return group;
        }
        // 判断发送消息的群成员 是否在群内
        ResponseVO<GetRoleInGroupResp> roleInGroupOne =
                imGroupMemberService.getRoleInGroupOne(groupId, fromId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }
        // 判断群是否被禁言
        ImGroupEntity groupData = group.getData();
        // 获取群的消息消息 用于贩毒案该发消息成员是否为群主或者管理员
        GetRoleInGroupResp data = roleInGroupOne.getData();
        if (groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode()
                || data.getRole() == GroupMemberRoleEnum.MAMAGER.getCode()
                || data.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }
        // 判断当前用户是否已经 结束禁言
        if(data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()){
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }


        return ResponseVO.successResponse();

    }
}
