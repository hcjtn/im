package edu.nuc.hcj.im.service.friendship.controller;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.model.SyncReq;
import edu.nuc.hcj.im.service.friendship.model.req.*;
import edu.nuc.hcj.im.service.friendship.service.ImFriendShipService;
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
 * @ClassName : ImFriendShipController.java
 * @createTime : 2023/12/7 13:27
 * @Description :
 */
@RestController
@RequestMapping("/v1/friendship")
public class ImFriendShipController {
    @Autowired
    ImFriendShipService imFriendShipService;

    // 将好友批量导入数据库
    @RequestMapping("/importFriendShip")
    public ResponseVO importFriendShip(@RequestBody @Validated ImporFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.importFriendShip(req);
    }

    // 添加好友
    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody @Validated AddFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.addFriend(req);
    }

    // 修改好友
    @RequestMapping("/updateFriend")
    public ResponseVO updateFriend(@RequestBody @Validated UpdateFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.updateFriend(req);
    }

    // 删除好友
    @RequestMapping("/DeleteFriend")
    public ResponseVO DeleteFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.DeleteFriend(req);
    }

    // 删除所有好友
    @RequestMapping("/DeleteAllFriend")
    public ResponseVO DeleteAllFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.DeleteAllFriend(req);
    }

    // 获取所有好友信息
    @RequestMapping("/getAllFriendShip")
    public ResponseVO getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.getAllFriendShip(req);
    }

    // 获取指定好友信息
    @RequestMapping("/getRelation")
    public ResponseVO getRelation(@RequestBody @Validated GetRelationReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.getRelation(req);
    }
    // 数据同步
    @RequestMapping("/syncFindshipList")
    public ResponseVO syncFindshipList(@RequestBody @Validated SyncReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.syncFindshipList(req);
    }
}
