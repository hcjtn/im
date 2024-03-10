package edu.nuc.hcj.im.service.friendship.controller;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import edu.nuc.hcj.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipGroupMemberService;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipGroupService;
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
 * @Package : edu.nuc.hcj.im.service.friendship.controller
 * @ClassName : ImFriendShipGroupController.java
 * @createTime : 2023/12/12 13:39
 * @Description :
 */

@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {

    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;


    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipGroupService.addGroup(req);
    }

    @RequestMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipGroupService.deleteGroup(req);
    }

    @RequestMapping("/member/add")
    public ResponseVO memberAdd(@RequestBody @Validated AddFriendShipGroupMemberReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.addGroupMember(req);
    }

    @RequestMapping("/member/del")
    public ResponseVO memberdel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.delGroupMember(req);
    }

    @RequestMapping("/get")
    public ResponseVO memberdel(String fromId, String groupName, Integer appId) {

        return imFriendShipGroupService.getGroup(fromId, groupName, appId);
    }


}


