package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : GroupMuteTypeEnum.java
 * @createTime : 2023/12/12 15:39
 * @Description :
 */
public enum GroupMuteTypeEnum {

    /**
     * 是否全员禁言，0 不禁言；1 全员禁言。
     */
    NOT_MUTE(0),


    MUTE(1),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static GroupMuteTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupMuteTypeEnum.values().length; i++) {
            if (GroupMuteTypeEnum.values()[i].getCode() == ordinal) {
                return GroupMuteTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupMuteTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
