package edu.nuc.hcj.im.service.interceptor;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import edu.nuc.hcj.im.common.BaseErrorCode;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.enums.GateWayErrorCode;
import edu.nuc.hcj.im.common.exception.ApplicationExceptionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.interceptor
 * @ClassName : GateWayInterceptor.java
 * @createTime : 2024/1/6 14:43
 * @Description :  用于判断 关键参数是否存在以及用户的签名是否正确（验证用户请求是否正确）
 */
@Component
public class GateWayInterceptor implements HandlerInterceptor {
    @Autowired
    IdentityCheck identityCheck;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取appid 操作人 userSign(用户签名)
        String appId = request.getParameter("appId");
        if (StringUtils.isBlank(appId)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST),response);
            return false;
        }
        String identifier = request.getParameter("identifier");
        if (StringUtils.isBlank(identifier)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST),response);
            return false;
        }
        String userSign = request.getParameter("userSign");
        if (StringUtils.isBlank(userSign)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_IS_ERROR),response);
            return false;
        }
        //签名和操作人和appid是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSig(identifier, appId, userSign);
        if(applicationExceptionEnum != BaseErrorCode.SUCCESS){
            resp(ResponseVO.errorResponse(applicationExceptionEnum),response);
            return false;
        }


        return true;
    }

    private void resp(ResponseVO respVo ,HttpServletResponse response) {
        PrintWriter printWriter = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            String resp = JSONObject.toJSONString(respVo);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Allow-Credentials","true");
            response.setHeader("Access-Control-Allow-Methods","*");
            response.setHeader("Access-Control-Allow-Headers","*");
            response.setHeader("Access-Control-Max-Age","3600");
            printWriter = response.getWriter();
            printWriter.write(resp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.checkError();
            }
        }
    }
}
