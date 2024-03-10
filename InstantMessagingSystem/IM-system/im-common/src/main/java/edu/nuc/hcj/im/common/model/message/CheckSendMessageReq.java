package edu.nuc.hcj.im.common.model.message;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : CheckSendMessageReq.java
 * @createTime : 2024/1/12 18:06
 * @Description :
 */
@Data
public class CheckSendMessageReq {
    private String fromId;
    private String toId;
    private Integer appId;
    private Integer command;
}
