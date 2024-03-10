package edu.nuc.hcj.im.service.friendship.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import edu.nuc.hcj.im.codec.park.frienship.*;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.AllowFriendTypeEnum;
import edu.nuc.hcj.im.common.enums.CheckFriendShipTypeEnum;
import edu.nuc.hcj.im.common.enums.FriendShipErrorCode;
import edu.nuc.hcj.im.common.enums.FriendShipStatusEnum;
import edu.nuc.hcj.im.common.enums.command.FriendshipEventCommand;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.common.model.RequestBase;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.common.model.SyncResp;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipEntity;
import edu.nuc.hcj.im.service.friendship.dao.mapper.ImFriendShipMapper;
import edu.nuc.hcj.im.service.friendship.model.callback.AddFriendBlackAfterCallBackDTO;
import edu.nuc.hcj.im.service.friendship.model.callback.AddfriendAfterCallBackDTO;
import edu.nuc.hcj.im.service.friendship.model.callback.DeleteFriendAfterCallBackDTO;
import edu.nuc.hcj.im.service.friendship.model.req.*;
import edu.nuc.hcj.im.service.friendship.model.resp.CheckFriendShipResp;
import edu.nuc.hcj.im.service.friendship.model.resp.ImportFriendShipResp;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipRequestService;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipService;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import edu.nuc.hcj.im.service.utils.CallbackService;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service.Impl
 * @ClassName : ImFriendShipServiceImpl.java
 * @createTime : 2023/12/7 13:27
 * @Description :
 */
@Service
public class ImFriendShipServiceImpl implements ImFriendShipService {
    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    IMUserService imUserService;
    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;
    @Autowired
    CallbackService callbackService;
    @Autowired
    Appconfig appconfig;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    WriteUserSeq writeUserSeq;
    @Autowired
    RedisSeq redisSeq;

    @Override
    public ResponseVO importFriendShip(ImporFriendShipReq req) {
        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp importFriendShipResp = new ImportFriendShipResp();
        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        for (ImporFriendShipReq.ImportFriendDto friendDto : req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            //copyProperties 将成员变量相同的内容进行拷贝， 无需传统的方式对属性逐个赋值。
            BeanUtils.copyProperties(friendDto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            entity.setCreateTime(System.currentTimeMillis());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successIds.add(friendDto.getToId());
                } else {
                    errorIds.add(friendDto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorIds.add(friendDto.getToId());
            }
        }
        importFriendShipResp.setSuccessIds(successIds);
        importFriendShipResp.setErrorIds(errorIds);
        return ResponseVO.successResponse(importFriendShipResp);
    }

    /***
     * 存储好友关系的时候，采用a是b的好友 b是a的好友 则a和b才是好友
     * @param req
     * @return
     */
    @Override
    public ResponseVO addFriend(AddFriendReq req) {
        // 查询
        ResponseVO<ImUserDataEntity> fromInfo =
                imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (fromInfo.isOk()) {
            return fromInfo;
        }
        ResponseVO<ImUserDataEntity> toInfo =
                imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        //之前回调
        // 是否开启回调
        if (appconfig.isAddFriendBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(),
                    Constant.CallbackCommand.AddFriendBefore,
                    JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }
        }

        // 添加逻辑 添加好友方式  1无需验证  2需要验证  im_user_data
        if (toInfo.getData().getFriendAllowType() != null
                && toInfo.getData().getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {

            // 无需验证直接添加好友
            return this.doAddFriend(req, req.getFromId(), req.getToItem(), req.getAppId());

        } else {
            // 判断当前是否已经是好友
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("to_id", req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if (fromItem == null || fromItem.getStatus()
                    != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                // 需要验证添加好友  申请流程
                ResponseVO responseVO =
                        imFriendShipRequestService.addFienshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    return responseVO;
                }
            } else {
                // 已经是好友关系了
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }


        //之后回调
        // 是否开启回调
        if (appconfig.isAddFriendAfterCallback()) {
            AddfriendAfterCallBackDTO addfriendAfterCallBackDTO = new AddfriendAfterCallBackDTO();
            addfriendAfterCallBackDTO.setFromId(req.getFromId());
            addfriendAfterCallBackDTO.setToItem(req.getToItem());
            callbackService.callback(req.getAppId(),
                    Constant.CallbackCommand.AddFriendAfter,
                    JSONObject.toJSONString(addfriendAfterCallBackDTO));

        }
        return ResponseVO.successResponse();
    }

    // 修改好友接口
    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {
        // 校验这两个用户是否存在
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }
        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
        if (responseVO.isOk()) {
            // 发送用户跟新操作通知
            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setRemark(req.getToItem().getRemark());
            updateFriendPack.setToId(req.getToItem().getToId());
            // 将数据发送给客户端
            messageProducer.sendToUser(req.getFromId(),
                    req.getClientType(), req.getImei(), FriendshipEventCommand
                            .FRIEND_UPDATE, updateFriendPack, req.getAppId());


            //之后回调
            // 是否开启回调
            if (appconfig.isModifyFriendAfterCallback()) {
                AddfriendAfterCallBackDTO addfriendAfterCallBackDTO = new AddfriendAfterCallBackDTO();
                addfriendAfterCallBackDTO.setFromId(req.getFromId());
                addfriendAfterCallBackDTO.setToItem(req.getToItem());
                callbackService.callback(req.getAppId(),
                        Constant.CallbackCommand.UpdateFriendAfter,
                        JSONObject.toJSONString(addfriendAfterCallBackDTO));

            }
        }

        return responseVO;
    }

    @Override
    public ResponseVO DeleteFriend(DeleteFriendReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (fromItem.getStatus() != null && fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.Friendship);
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendShipMapper.update(update, query);
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constant.SeqConstants.Friendship, seq);
                DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
                deleteFriendPack.setFromId(req.getFromId());
                deleteFriendPack.setSequence(seq);
                deleteFriendPack.setToId(req.getToId());
                messageProducer.sendToUser(req.getFromId(),
                        req.getClientType(), req.getImei(),
                        FriendshipEventCommand.FRIEND_DELETE,
                        deleteFriendPack, req.getAppId());

                //之后回调
                if (appconfig.isAddFriendAfterCallback()) {
                    DeleteFriendAfterCallBackDTO callbackDto = new DeleteFriendAfterCallBackDTO();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());
                    callbackService.beforeCallback(req.getAppId(),
                            Constant.CallbackCommand.DeleteFriendAfter, JSONObject
                                    .toJSONString(callbackDto));
                }

            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }
        return ResponseVO.successResponse();
    }


    @Override
    public ResponseVO DeleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("status", FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
        //只要 status 状态是好友的全部删除
        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update, query);

        // 通知
        DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
//        deleteFriendPack.setSequence(seq);
        deleteFriendPack.setToId(req.getToId());
        messageProducer.sendToUser(req.getFromId(),
                req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_DELETE,
                deleteFriendPack, req.getAppId());


        // 回调


        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(queryWrapper));

    }


    // 获取好友关系
    @Override
    public ResponseVO getRelation(GetRelationReq req) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());
        queryWrapper.eq("to_id", req.getToId());
        ImFriendShipEntity imFriendShipEntity = imFriendShipMapper.selectOne(queryWrapper);
        if (imFriendShipEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(imFriendShipEntity);
    }

    // 验证好友关系
    @Override
    public ResponseVO CheckFriendShipRequest(CheckFriendShipReq req) {

        // 将to_id转化成为map存储
        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp = new ArrayList<>();
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            // 单项校验
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBoth(req);
        }

        // 将查询到的为好友的数据转化成为map存储
        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId
                        , CheckFriendShipResp::getStatus));
        // 循环判断，查看不是好友的UID
        for (String toId : result.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
            }
        }

        return ResponseVO.successResponse(resp);
    }

    // 添加黑名单
    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {
        ResponseVO<ImUserDataEntity> fromId = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromId.isOk()) {
            return fromId;
        }
        ResponseVO<ImUserDataEntity> toId = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!fromId.isOk()) {
            return toId;
        }
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        Long seq = 0L;
        if (fromItem == null) { // 好友列表没这号人 直接拉黑
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.FriendshipBlack);
            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setAppId(req.getAppId());
            fromItem.setBlackSequence(seq);
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constant.SeqConstants.Friendship, seq);
        } else {
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.FriendshipBlack);
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                update.setFriendSequence(seq);
                int result = imFriendShipMapper.update(update, query);
                if (result != 0) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constant.SeqConstants.Friendship, seq);
            }

        }

        // 通知用户添加黑名单
        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        addFriendBlackPack.setFromId(req.getFromId());
        addFriendBlackPack.setSequence(seq);
        addFriendBlackPack.setToId(req.getToId());
        //发送tcp通知
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());

        //之后回调
        // 是否开启回调
        if (appconfig.isAddFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallBackDTO addfriendAfterCallBackDTO =
                    new AddFriendBlackAfterCallBackDTO();
            addfriendAfterCallBackDTO.setFromId(req.getFromId());
            addfriendAfterCallBackDTO.setToId(req.getToId());
            callbackService.callback(req.getAppId(),
                    Constant.CallbackCommand.AddBlackAfter,
                    JSONObject.toJSONString(addfriendAfterCallBackDTO));

        }
        return ResponseVO.successResponse();
    }

    //将某一个人拉出黑名单
    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper queryWrap = new QueryWrapper();
        queryWrap.eq("from_id", req.getFromId());
        queryWrap.eq("to_id", req.getToId());
        queryWrap.eq("app_id", req.getAppId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryWrap);
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.Friendship);

        ImFriendShipEntity updateBack = new ImFriendShipEntity();
        updateBack.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        updateBack.setFriendSequence(seq);
        int update = imFriendShipMapper.update(updateBack, queryWrap);
        if (update == 1) {

            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constant.SeqConstants.Friendship, seq);
            DeleteBlackPack deleteFriendPack = new DeleteBlackPack();
            deleteFriendPack.setFromId(req.getFromId());
            deleteFriendPack.setSequence(seq);
            deleteFriendPack.setToId(req.getToId());
            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_BLACK_DELETE,
                    deleteFriendPack, req.getAppId());

            // 回调
            if (appconfig.isAddFriendShipBlackAfterCallback()) {
                AddFriendBlackAfterCallBackDTO addfriendAfterCallBackDTO =
                        new AddFriendBlackAfterCallBackDTO();
                addfriendAfterCallBackDTO.setFromId(req.getFromId());
                addfriendAfterCallBackDTO.setToId(req.getToId());
                callbackService.callback(req.getAppId(),
                        Constant.CallbackCommand.DeleteBlack,
                        JSONObject.toJSONString(addfriendAfterCallBackDTO));

            }
        }

        return ResponseVO.successResponse();
    }

    //校验黑名单   检查黑名单状态：是单方面拉黑 还是 双方面拉黑 还是都没有拉黑
    @Override
    public ResponseVO checkBlack(CheckFriendShipReq req) {
        Map<String, Integer> toIdMap = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result = new ArrayList<>();
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendShipResp::getToId,
                                CheckFriendShipResp::getStatus));
        for (String toId : toIdMap.keySet()) {
            // 将不是黑名单的添加进 返回值当中去
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(collect.get(toIdMap.get(toIdMap)));
                result.add(checkFriendShipResp);
            }
        }
        return ResponseVO.successResponse(result);
    }

    @Override
    public List<String>  getAllFriendId(String userId, Integer appId) {
        return imFriendShipMapper.getAllFriendId(userId, appId);
    }


    /**
     * 好友增量接口
     * @param req
     * @return
     */
    @Override
    public ResponseVO syncFindshipList(SyncReq req) {
        // 客户端最大拉取数量只能为100
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }
        SyncResp<ImFriendShipEntity> resp = new SyncResp<>();
        // 判断客户端最大的seq和服务端的seq的大小关系
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getOperater());
        queryWrapper.gt("friend_sequence", req.getLastSequence());
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.last("limit" + req.getMaxLimit());
        queryWrapper.orderByAsc("friend_sequence");
        // 获取相关的数据集合
        List<ImFriendShipEntity> list = imFriendShipMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(list)){
            // list里面最后一个元素 最大的元素
            ImFriendShipEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq =
                    imFriendShipMapper.getFriendShipMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getFriendSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }
        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }

    /**
     * 添加好友 具体实现方法  方便其他地方进行应用
     *
     * @param
     * @param fromId
     * @param dto
     * @param appId
     * @return
     */
    @Override
    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId) {

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态
        // from
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        long seq = 0L;
        // 判断是否已经添加
        if (fromItem == null) {
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            seq = redisSeq.doGetSeq(appId + ":" + Constant.SeqConstants.Friendship);
            fromItem.setAppId(appId);
            fromItem.setFriendSequence(seq);
            fromItem.setFromId(fromId);
//            entity.setToId(to);
            BeanUtils.copyProperties(dto, fromItem);
            // 将状态设置为添加状态
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            // 设置创建时间
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            //返回添加失败
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(appId, fromId, Constant.SeqConstants.Friendship, seq);
        } else {
            //如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                // 返回已添加
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                // 如果是已经删除状态
                ImFriendShipEntity update = new ImFriendShipEntity();

                if (StringUtils.isNotBlank(dto.getAddSource())) {
                    update.setAddSource(dto.getAddSource());
                }

                if (StringUtils.isNotBlank(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }

                if (StringUtils.isNotBlank(dto.getExtra())) {
                    update.setExtra(dto.getExtra());
                }
                seq = redisSeq.doGetSeq(appId + ":" + Constant.SeqConstants.Friendship);
                update.setFriendSequence(seq);
                // 将其设置为添加状态
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                // 执行添加操作
                int result = imFriendShipMapper.update(update, query);
                //返回添加失败
                if (result != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
                writeUserSeq.writeUserSeq(appId, fromId, Constant.SeqConstants.Friendship, seq);
            }

        }
        //  to


        // 双向添加
        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", dto.getToId());
        query.eq("to_id", fromId);
        // 判断是否有该记录 如果有改记录 还有三种情况 添加和未添加
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        // 没有添加
        if (toItem == null) {
            seq = redisSeq.doGetSeq(appId + ":" + Constant.SeqConstants.Friendship);
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            toItem.setToId(fromId);
            BeanUtils.copyProperties(dto, toItem);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            int insert = imFriendShipMapper.insert(toItem);
            writeUserSeq.writeUserSeq(appId, dto.getToId(), Constant.SeqConstants.Friendship, seq);
        } else {
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                    toItem.getStatus()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(update, toQuery);
                writeUserSeq.writeUserSeq(appId, dto.getToId(), Constant.SeqConstants.Friendship, seq);
            }
        }


        // TODO A B  通知 a b两个用户的所有端 进行更新好友列表
        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem, addFriendPack);
        addFriendPack.setSequence(seq);
        // 发送给from
        if (requestBase != null) {
            //表示发送的好友申请方面 只需要发送出自己之外的端
            messageProducer.sendToUser(fromId, requestBase.getClientType(),
                    requestBase.getImei(), FriendshipEventCommand.FRIEND_ADD, addFriendPack
                    , requestBase.getAppId());
        } else {

            messageProducer.sendToUser(fromId, FriendshipEventCommand.FRIEND_ADD,
                    addFriendPack, requestBase.getAppId());
        }
        // 发送给 to
        // 被加好友方 需要将message发送到所有已经登录的设备
        //表示发送的好友申请方面 只需要发送出自己之外的端
        AddFriendPack addFriendToPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addFriendPack);
        messageProducer.sendToUser(toItem.getFromId(),
                FriendshipEventCommand.FRIEND_ADD, addFriendToPack
                , requestBase.getAppId());


        return ResponseVO.successResponse();
    }


    @Transactional
    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId) {
        long seq = redisSeq.doGetSeq(appId + ":" + Constant.SeqConstants.Friendship);
        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendShipEntity::getExtra, dto.getExtra())
                .set(ImFriendShipEntity::getRemark, dto.getRemark())
                .set(ImFriendShipEntity::getFriendSequence, seq)
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getToId, dto.getToId())
                .eq(ImFriendShipEntity::getFromId, fromId);
        // 执行更新接口
        int update = imFriendShipMapper.update(null, updateWrapper);

        if (update == 1) {
//            //之后回调
//            // 是否开启回调
//            if (appconfig.isModifyFriendAfterCallback()) {
//                AddfriendAfterCallBackDTO addfriendAfterCallBackDTO = new AddfriendAfterCallBackDTO();
//                addfriendAfterCallBackDTO.setFromId(fromId);
//                addfriendAfterCallBackDTO.setToItem(dto);
//                callbackService.callback(appId,
//                        Constant.CallbackCommand.UpdateFriendAfter,
//                        JSONObject.toJSONString(addfriendAfterCallBackDTO));
//
//            }
            writeUserSeq.writeUserSeq(appId, fromId, Constant.SeqConstants.Friendship, seq);
            return ResponseVO.successResponse(update);
        }
        return ResponseVO.errorResponse();
    }


}
