package edu.nuc.hcj.im.service.message.controller;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.common.model.message.CheckSendMessageReq;
import edu.nuc.hcj.im.service.message.mq.req.SendMessageReq;
import edu.nuc.hcj.im.service.message.service.MessageSyncService;
import edu.nuc.hcj.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.message.controller
 * @ClassName : MessageController.java
 * @createTime : 2024/1/7 19:22
 * @Description :
 * 手把手带你编写发送单聊  amp群聊消息的接口
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {
    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody
                                         @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }


    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        ResponseVO responseVO = p2PMessageService.imServerPermissionCheck(req.getFromId(), req.getToId(), req.getAppId());
        return responseVO;
    }

}