package edu.nuc.hcj.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import edu.nuc.hcj.im.codec.park.conversation.DeleteConversationPack;
import edu.nuc.hcj.im.codec.park.conversation.UpdateConversationPack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ConversationErrorCode;
import edu.nuc.hcj.im.common.enums.ConversationTypeEnum;
import edu.nuc.hcj.im.common.enums.command.ConversationEventCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.common.model.SyncResp;
import edu.nuc.hcj.im.common.model.message.MessageReadedContent;
import edu.nuc.hcj.im.service.conversation.dao.ImConversationSetEntity;
import edu.nuc.hcj.im.service.conversation.dao.mapper.ImConversationSetMapper;
import edu.nuc.hcj.im.service.conversation.model.DeleteConversationReq;
import edu.nuc.hcj.im.service.conversation.model.UpdateConversationReq;
import edu.nuc.hcj.im.service.seq.RedisSeq;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import edu.nuc.hcj.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.conversation.service
 * @ClassName : ConversationService.java
 * @createTime : 2024/1/15 10:55
 * @Description :
 */
@Service
public class ConversationService {
    @Autowired
    ImConversationSetMapper imConversationSetMapper;
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    Appconfig appConfig;
    @Autowired
    WriteUserSeq writeUserSeq;

//    @Autowired
//    WriteUserSeq writeUserSeq;

    // 生成对应的 ConversationId
    public String convertConversationId(Integer type, String fromId, String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    /**
     * 把已读的sequence标记到对应的会话当中去
     * 通用 私聊 群聊 通用
     */
    public void messageMarkRead(MessageReadedContent messageReadedContent) {

        String toId = messageReadedContent.getToId();
        // 群聊使用groupId
        if (messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadedContent.getGroupId();
        }

        String conversationId = convertConversationId(messageReadedContent.getConversationType(),
                messageReadedContent.getFromId(),
                toId);
        /**
         * 根据该Id 创建会话
         */
        // 查询该会话 是否存在
        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        // 会话不存在
        if (imConversationSetEntity == null) {
            // 创建会话
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constant.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(), Constant.SeqConstants.Conversation,seq);
        } else {
            // 更新会话
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constant.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
        }
    }


    // 删除会话
    public ResponseVO deleteConversation(DeleteConversationReq req) {
        //置顶 有免打扰  变成默认设置
//        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("conversation_id",req.getConversationId());
//        queryWrapper.eq("app_id",req.getAppId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
//        if(imConversationSetEntity != null){
//            imConversationSetEntity.setIsMute(0);
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
//        }
        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    // 更新会话 置顶or免打扰
    public ResponseVO updateConversation(UpdateConversationReq req) {
        // 两个不能全为空
        if (req.getIsTop() == null && req.getIsMute() == null) {
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        // 查询对应的会话是否存在
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);

        // 如果存在
        if (imConversationSetEntity != null) {
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constant.SeqConstants.Conversation);

            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            // 跟新操作
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(),
                    Constant.SeqConstants.Conversation, seq);
        // 跟新给同步端
            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(),
                            req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    /**
     * 会话的增量接口
     * @param req
     * @return
     */
    public ResponseVO syncConversation(SyncReq req) {
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        // seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getOperater());
        queryWrapper.gt("sequence", req.getLastSequence());
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper
                .selectList(queryWrapper);

        if (!CollectionUtils.isEmpty(list)) {
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            // 设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            // 设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }
}
