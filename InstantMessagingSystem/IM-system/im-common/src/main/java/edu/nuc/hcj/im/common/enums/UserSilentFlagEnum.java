package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : UserSilentFlagEnum.java
 * @createTime : 2024/1/6 18:11
 * @Description : 用户禁言标志枚举
 */
public enum UserSilentFlagEnum {

    /**
     * 0 正常；1 禁言。
     */
    NORMAL(0),

    MUTE(1),
    ;

    private int code;

    UserSilentFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
