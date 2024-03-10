package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : UserForbiddenFlagEnum.java
 * @createTime : 2024/1/6 18:10
 * @Description :
 */
public enum UserForbiddenFlagEnum {

    /**
     * 0 正常；1 禁用。
     */
    NORMAL(0),

    FORBIBBEN(1),
    ;

    private int code;

    UserForbiddenFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
