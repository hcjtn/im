package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : GroupTypeEnum.java
 * @createTime : 2023/12/12 15:40
 * @Description :
 */
public enum GroupTypeEnum {

    /**
     * 群类型 1私有群（类似微信） 2公开群(类似qq）
     */
    PRIVATE(1),

    PUBLIC(2),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static GroupTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupTypeEnum.values().length; i++) {
            if (GroupTypeEnum.values()[i].getCode() == ordinal) {
                return GroupTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}

