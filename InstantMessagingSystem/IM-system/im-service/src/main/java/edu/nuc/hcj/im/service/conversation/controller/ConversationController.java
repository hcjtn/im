package edu.nuc.hcj.im.service.conversation.controller;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.service.conversation.model.DeleteConversationReq;
import edu.nuc.hcj.im.service.conversation.model.UpdateConversationReq;
import edu.nuc.hcj.im.service.conversation.service.ConversationService;
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
 * @Package : edu.nuc.hcj.im.service.conversation.controller
 * @ClassName : ConversationController.java
 * @createTime : 2024/1/15 11:48
 * @Description :
 */
@RestController
@RequestMapping("v1/conversation")
public class ConversationController {
    @Autowired
    ConversationService conversationService;
    @RequestMapping("/deleteConversation")
    public ResponseVO deleteConversation(@RequestBody @Validated DeleteConversationReq
                                                 req, Integer appId, String identifier)  {
        req.setAppId(appId);
//        req.setOperater(identifier);
        return conversationService.deleteConversation(req);
    }

    @RequestMapping("/updateConversation")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq
                                                 req, Integer appId, String identifier)  {
        req.setAppId(appId);
//        req.setOperater(identifier);
        return conversationService.updateConversation(req);
    }

    @RequestMapping("/syncConversation")
    public ResponseVO syncConversation(@RequestBody @Validated SyncReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
//        req.setOperater(identifier);
        return conversationService.syncConversation(req);
    }




}
