package edu.nuc.hcj.im.service.user.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.nuc.hcj.im.codec.park.user.*;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.DelFlagEnum;
import edu.nuc.hcj.im.common.enums.UserErrorCode;
import edu.nuc.hcj.im.common.enums.command.UserEventCommand;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.service.group.service.ImGroupService;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import edu.nuc.hcj.im.service.user.dao.mapper.ImUserDataMapper;
import edu.nuc.hcj.im.service.user.model.req.*;
import edu.nuc.hcj.im.service.user.model.resp.GetUserInfoResp;
import edu.nuc.hcj.im.service.user.model.resp.ImportUserResp;
import edu.nuc.hcj.im.service.user.service.IMUserService;
import edu.nuc.hcj.im.service.utils.CallbackService;
import edu.nuc.hcj.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.service.Impl
 * @ClassName : IMUserServiceImpl.java
 * @createTime : 2023/12/6 18:36
 * @Description :
 */
@Service
public class IMUserServiceImpl implements IMUserService {

    @Autowired
    ImUserDataMapper imUserDataMapper;

    @Autowired
    Appconfig appconfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ImGroupService imGroupService;


    //将用户数据添加到数据库中
    @Override
    public ResponseVO importUser(ImportUserReq req) {

        if (req.getUserData().size() > 100) {
            //返回数量过多
            return ResponseVO.errorResponse();
        }

        // 将正确 和 失败的信息分别存储起来
        List<String> successID = new ArrayList<>();
        List<String> errorID = new ArrayList<>();

        req.getUserData().forEach(userdata -> {
            try {
                //eg：防止用户导入的时候输入别人的appid，比如 用户本身的appId是10000的AppId 但导入的别人的数据中是10001的AppId
                // 所以在这里直接获取用户本身的appid
                userdata.setAppId(req.getAppId());
                int insert = imUserDataMapper.insert(userdata);
                // 用户导入成功
                if (insert == 1) {
                    successID.add(userdata.getUserId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorID.add(userdata.getUserId());
            }
        });
        ImportUserResp userResp = new ImportUserResp();
        userResp.setSuccessID(successID);
        userResp.setErrorID(errorID);

        return ResponseVO.successResponse(userResp);
    }


    // 获取多个用户详情
    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        //QueryWrapper 用法 https://blog.csdn.net/bird_tp/article/details/105587582
        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.in("user_id", req.getUserIds());
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        //根据提供的user_id获取对应的 user数据 将获取到的user数据封装到List中
        List<ImUserDataEntity> imUserDataEntities = imUserDataMapper.selectList(queryWrapper);
        Map<String, ImUserDataEntity> map = new HashMap<>();
        // 将所有的imUserDataEntity与对应的UserID联系起来封装到 map中
        for (ImUserDataEntity imUserDataEntity : imUserDataEntities) {
            map.put(imUserDataEntity.getUserId(), imUserDataEntity);
        }

        // 将没有查询到的uid收集起来
        List<String> failUserId = new ArrayList<>();
        for (String uid : req.getUserIds()) {
            if (!map.containsKey(uid)) {
                failUserId.add(uid);
            }
        }

        GetUserInfoResp getUserInfo = new GetUserInfoResp();
        getUserInfo.setUserDataItem(map);
        getUserInfo.setFailUser(failUserId);


        return ResponseVO.successResponse(getUserInfo);
    }

    // 获取单个用户详情
    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId) {
        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        ImUserDataEntity imUserDataEntity = imUserDataMapper.selectOne(queryWrapper);
        if (imUserDataEntity == null) {
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(imUserDataEntity);
    }

    //删除用户
    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {
        ImUserDataEntity entity = new ImUserDataEntity();
        entity.setDelFlag(DelFlagEnum.DELETE.getCode());

        List<String> errorId = new ArrayList<>();
        List<String> successId = new ArrayList<>();
        for (String userId : req.getUserIds()) {
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("app_id", req.getAppId());
            wrapper.eq("user_id", userId);
            wrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());
            int update = 0;

            try {
                update = imUserDataMapper.update(entity, wrapper);
                if (update > 0) {
                    successId.add(userId);
                } else {
                    errorId.add(userId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(userId);
            }
        }

        ImportUserResp resp = new ImportUserResp();
        resp.setSuccessID(successId);
        resp.setErrorID(errorId);
        return ResponseVO.successResponse(resp);
    }

    // 修改用户信息
    @Override
    @Transactional
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {
        QueryWrapper query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("user_id", req.getUserId());
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        ImUserDataEntity user = imUserDataMapper.selectOne(query);
        if (user == null) {
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        ImUserDataEntity update = new ImUserDataEntity();
        BeanUtils.copyProperties(user,update);

        update.setAppId(null);
        update.setUserId(null);
        int update1 = imUserDataMapper.update(update, query);
        if (update1 == 1) {
            // TODO 通知
            UserModifyPack pack = new UserModifyPack();
            BeanUtils.copyProperties(req, pack);
            messageProducer.sendToUser(req.getUserId(), req.getClientType(),req.getImei(),
                    UserEventCommand.USER_MODIFY,pack, req.getAppId());


            // TODO 执行回调
            //是否开启修改用户之后回调
            if(appconfig.isModifyUserAfterCallback()){
                callbackService.callback(req.getAppId(),
                        Constant.CallbackCommand.ModifyUserAfter,
                        JSONObject.toJSONString(req));
            }
            return ResponseVO.successResponse();
        }
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    /**
     * 用户登录
     *
     * @param req
     * @return
     */
    @Override
    public ResponseVO login(LoginReq req) {
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getUserSequence(GetUserSequenceReq req) {
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(req.getAppId() + ":" + Constant.RedisConstant.SeqPrefix + ":" + req.getUserId());
        Long groupSeq = imGroupService.getUserGroupMaxSeq(req.getUserId(),req.getAppId());
        map.put(Constant.SeqConstants.Group,groupSeq);
        return ResponseVO.successResponse(map);
    }
}

