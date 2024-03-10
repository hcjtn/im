package edu.nuc.hcj.im.common.enums;

import edu.nuc.hcj.im.common.exception.ApplicationExceptionEnum;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : MessageErrorCode.java
 * @createTime : 2024/1/6 18:14
 * @Description :
 */
public enum MessageErrorCode implements ApplicationExceptionEnum {


    FROMER_IS_MUTE(50002,"发送方被禁言"),

    FROMER_IS_FORBIBBEN(50003,"发送方被禁用"),


    MESSAGEBODY_IS_NOT_EXIST(50003,"消息体不存在"),

    MESSAGE_RECALL_TIME_OUT(50004,"消息已超过可撤回时间"),

    MESSAGE_IS_RECALLED(50005,"消息已被撤回"),

    ;

    private int code;
    private String error;

    MessageErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

}
