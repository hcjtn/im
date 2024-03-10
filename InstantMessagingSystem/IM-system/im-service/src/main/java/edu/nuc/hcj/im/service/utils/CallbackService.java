package edu.nuc.hcj.im.service.utils;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.config.Appconfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import edu.nuc.hcj.im.common.utils.HttpRequestUtils;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : CallbackService.java
 * @createTime : 2023/12/22 13:24
 * @Description :
 */
@Component
@Slf4j
public class CallbackService {

    private Logger logger = LoggerFactory.getLogger(CallbackService.class);

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    Appconfig appConfig;



    @Autowired
    ShareThreadPool shareThreadPool;


    public void callback(Integer appId,String callbackCommand,String jsonBody){
        shareThreadPool.submit(() -> {
            try {
                httpRequestUtils.doPost(appConfig.getCallbackUrl(),Object.class,builderUrlParams(appId,callbackCommand),
                        jsonBody,null);
            }catch (Exception e){
                logger.error("callback 回调{} : {}出现异常 ： {} ",callbackCommand , appId, e.getMessage());
            }
        });
    }

    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody){
        try {
            ResponseVO responseVO = httpRequestUtils.doPost(appConfig.getCallbackUrl(), ResponseVO.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
            return responseVO;
        }catch (Exception e){
            logger.error("callback 之前 回调{} : {}出现异常 ： {} ",callbackCommand , appId, e.getMessage());
            return ResponseVO.successResponse();
        }
    }

    public Map builderUrlParams(Integer appId, String command) {
        Map map = new HashMap();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }


}
