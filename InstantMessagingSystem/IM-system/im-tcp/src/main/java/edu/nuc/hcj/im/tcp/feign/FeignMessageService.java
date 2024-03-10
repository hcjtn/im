package edu.nuc.hcj.im.tcp.feign;

import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.feign
 * @ClassName : FeignMessageService.java
 * @createTime : 2024/1/12 17:57
 * @Description :
 */

public interface FeignMessageService {
    // 请求头配置
    @Headers({"Content-Type: application/json","Accept: application/json"})
    // 请求行配置
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSeandMessage(CheckSendMessageReq checkSendMessageReq);


}
