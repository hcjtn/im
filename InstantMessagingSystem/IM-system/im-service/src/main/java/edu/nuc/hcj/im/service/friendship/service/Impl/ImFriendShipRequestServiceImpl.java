package edu.nuc.hcj.im.service.friendship.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.nuc.hcj.im.codec.park.frienship.ApproverFriendRequestPack;
import edu.nuc.hcj.im.codec.park.frienship.ReadAllFriendRequestPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ApproverFriendRequestStatusEnum;
import edu.nuc.hcj.im.common.enums.FriendShipErrorCode;
import edu.nuc.hcj.im.common.enums.command.FriendshipEventCommand;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipRequestEntity;
import edu.nuc.hcj.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import edu.nuc.hcj.im.service.friendship.model.req.ApproverFriendRequestReq;
import edu.nuc.hcj.im.service.friendship.model.req.FriendDto;
import edu.nuc.hcj.im.service.friendship.model.req.ReadFriendShipRequestReq;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipRequestService;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipService;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.service.Impl
 * @ClassName : ImFriendShipRequestServiceImpl.java
 * @createTime : 2023/12/7 19:12
 * @Description : 好友申请服务
 */
@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {

    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;
    @Autowired
    ImFriendShipService imFriendShipService;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    WriteUserSeq writeUserSeq;
    @Autowired
    Appconfig appConfig;
    @Autowired
    RedisSeq redisSeq;


    // 添加好用请求 a向b添加好友申请 是将申请队列发送给 b的所有端
    @Override
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("to_id", dto.getToId());
        queryWrapper.eq("from_id", fromId);
        // 现进行查询 可能已经有一条好友申请 如果有 ，就不再发送新的好友申请 只是修改记录内容和更新时间  如果没有 发送好友申请
        ImFriendShipRequestEntity imFriendShipRequestEntity =
                imFriendShipRequestMapper.selectOne(queryWrapper);

        long seq = redisSeq.doGetSeq(appId+":"+
                Constant.SeqConstants.FriendshipRequest);

        if (imFriendShipRequestEntity == null) {
            imFriendShipRequestEntity = new ImFriendShipRequestEntity();
            imFriendShipRequestEntity.setFromId(fromId);
            imFriendShipRequestEntity.setAddSource(dto.getAddSource());
            imFriendShipRequestEntity.setAddWording(dto.getAddWording());
            imFriendShipRequestEntity.setAppId(appId);
            imFriendShipRequestEntity.setRemark(dto.getRemark());
            imFriendShipRequestEntity.setToId(dto.getToId());
            imFriendShipRequestEntity.setReadStatus(0);
            imFriendShipRequestEntity.setApproveStatus(0);
            imFriendShipRequestEntity.setCreateTime(System.currentTimeMillis());
            imFriendShipRequestEntity.setSequence(seq);
            // 将好友申请添加进去
            imFriendShipRequestMapper.insert(imFriendShipRequestEntity);
        } else {
            //修改记录内容 和更新时间
            if (StringUtils.isNotBlank(dto.getAddSource())) {
                imFriendShipRequestEntity.setAddWording(dto.getAddWording());
            }
            if (StringUtils.isNotBlank(dto.getRemark())) {
                imFriendShipRequestEntity.setRemark(dto.getRemark());
            }
            if (StringUtils.isNotBlank(dto.getAddWording())) {
                imFriendShipRequestEntity.setAddWording(dto.getAddWording());
            }
            imFriendShipRequestEntity.setApproveStatus(0);
            imFriendShipRequestEntity.setReadStatus(0);
            imFriendShipRequestEntity.setSequence(seq);
            imFriendShipRequestMapper.updateById(imFriendShipRequestEntity);

        }
        writeUserSeq.writeUserSeq(appId,dto.getToId(),
                Constant.SeqConstants.FriendshipRequest,seq);
        //发送好友申请的tcp给接收方
        messageProducer.sendToUser(dto.getToId(),
                null, "", FriendshipEventCommand.FRIEND_REQUEST,
                imFriendShipRequestEntity, appId);
        return ResponseVO.successResponse();
    }

    // 审批好友申请
    @Override
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req) {
        // 获取好友申请的数据
        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        // 如果返回值为空就提示该好友申请不存在
        if (imFriendShipRequestEntity == null) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if (req.getOperater().equals(imFriendShipRequestEntity.getToId())) {
            //只能申请自己的好友申请列表  无法审批其他人的好友请求
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }
        long seq = redisSeq.doGetSeq(req.getAppId()+":"+
                Constant.SeqConstants.FriendshipRequest);
        // 将用户对好友申请的操作更新在数据库中
        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setSequence(seq);
        update.setId(req.getId());
        imFriendShipRequestMapper.updateById(update);

        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperater(),
                Constant.SeqConstants.FriendshipRequest,seq);
        if (req.getStatus() == ApproverFriendRequestStatusEnum.AGREE.getCode()) {
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO =
                    imFriendShipService.doAddFriend(req, imFriendShipRequestEntity.getFromId(), dto, req.getAppId());

            if (responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()) {
                return responseVO;
            }
        }


        // tcp通知
        ApproverFriendRequestPack approverFriendRequestPack = new ApproverFriendRequestPack();
        approverFriendRequestPack.setId(req.getId());
        approverFriendRequestPack.setSequence(seq);
        approverFriendRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(),req.getClientType(),req.getImei(), FriendshipEventCommand
                .FRIEND_REQUEST_APPROVER,approverFriendRequestPack,req.getAppId());
        return ResponseVO.successResponse();
    }

    //读取好友申请列表 一次性读取所有内容
    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());


        long seq = redisSeq.doGetSeq(req.getAppId()+":"+
                Constant.SeqConstants.FriendshipRequest);
        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        update.setSequence(seq);
        imFriendShipRequestMapper.update(update, query);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getOperater(),
                Constant.SeqConstants.FriendshipRequest,seq);



        //TCP通知
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
//        readAllFriendRequestPack.setSequence(seq);
        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImei(),FriendshipEventCommand
                .FRIEND_REQUEST_READ,readAllFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }

    // 获取好友申请
    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper();
        query.eq("app_id", appId);
        query.eq("to_id", fromId);

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);

        return ResponseVO.successResponse(requestList);
    }
}
