package edu.nuc.hcj.im.service.user.service;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import edu.nuc.hcj.im.service.user.model.req.*;
import edu.nuc.hcj.im.service.user.model.resp.GetUserInfoResp;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.service
 * @ClassName : IMUserService.java
 * @createTime : 2023/12/6 18:35
 * @Description :
 */
public interface IMUserService {
    // 将User类导入进去
    public ResponseVO importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    // 用户登录
    public ResponseVO login(LoginReq req);


    ResponseVO getUserSequence(GetUserSequenceReq req);
}
