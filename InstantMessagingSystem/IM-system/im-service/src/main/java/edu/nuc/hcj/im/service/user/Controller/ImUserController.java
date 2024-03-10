package edu.nuc.hcj.im.service.user.Controller;

import edu.nuc.hcj.im.common.ClientType;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.route.RouteHandle;
import edu.nuc.hcj.im.common.route.RouteInfo;
import edu.nuc.hcj.im.common.utils.RouteInfoParseUtil;
import edu.nuc.hcj.im.service.user.model.req.*;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import edu.nuc.hcj.im.service.user.service.ImUserStatusService;
import edu.nuc.hcj.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.Controller
 * @ClassName : ImUserController.java
 * @createTime : 2023/12/6 19:18
 * @Description : 处理用户接口
 */
@RestController
@RequestMapping("/v1/user")
public class ImUserController {

    @Autowired
    IMUserService imUserService;

    @Autowired
    RouteHandle routeHandle;
    @Autowired
    ZKit zKit;
    @Autowired
    ImUserStatusService imUserStatusService;

    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        // 将获取的appId添加到ImportUserReq中
        req.setAppId(appId);
        return imUserService.importUser(req);
    }

    //删除用户
    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }


    /**
     * im登录接口 返回im地址
     *
     * @return
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);
        if (imUserService.login(req).isOk()) {
            //TODO 去 zookeeper获取im的地址 返回给客户端
            List<String> allNode;
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {

                allNode = zKit.getAllTcpNode();
            }
            // 返回 对应要使用的netty服务器 ip:port
            String routeServer = routeHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(routeServer);
            return ResponseVO.successResponse(parse);
        }
        return ResponseVO.errorResponse();
    }

    /**
     * 用户登陆的时候 获取服务端的seq 如果相同 不进行增量拉取  不同 进行增量拉取
     *
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated
                                              GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequence(req);
    }

    // 订阅用户在线状态
    @RequestMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated
                                                        SubscribeUserOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    //用户自定义设置状态
    @RequestMapping("/setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated
                                                    SetUserCustomerStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        imUserStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }
}
