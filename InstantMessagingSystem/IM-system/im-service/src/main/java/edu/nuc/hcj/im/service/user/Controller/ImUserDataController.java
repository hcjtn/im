package edu.nuc.hcj.im.service.user.Controller;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.user.model.req.GetUserInfoReq;
import edu.nuc.hcj.im.service.user.model.req.ModifyUserInfoReq;
import edu.nuc.hcj.im.service.user.model.req.UserId;
import edu.nuc.hcj.im.service.user.service.IMUserService;
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
 * @Package : edu.nuc.hcj.im.service.user.Controller
 * @ClassName : ImUserDataController.java
 * @createTime : 2023/12/6 19:18
 * @Description :
 */
@RestController
@RequestMapping("v1/user/data")
public class ImUserDataController {

    @Autowired
    IMUserService imUserService;

    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@RequestBody GetUserInfoReq req, Integer appId){//@Validated
        req.setAppId(appId);
        return imUserService.getUserInfo(req);
    }

    //  @Validated 用于数据校验
    // @RequestBody 是直接接受传递过来的json字符串，所以在这里需要创建用于封装的类，自动将所需要的数据接受，否则只能接受json字符串
    @RequestMapping("/getSingleUserInfo")
    public ResponseVO getSingleUserInfo(@RequestBody  @Validated UserId userId, Integer appId) {//@Validated
        userId.setAppId(appId);
        return imUserService.getSingleUserInfo(userId.getUserId(), userId.getAppId());
    }

    //修改用户
    @RequestMapping("/modifyUser")
    public ResponseVO modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.modifyUserInfo(req);
    }




}
