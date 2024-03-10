package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : GroupMemberRoleEnum.java
 * @createTime : 2023/12/12 15:39
 * @Description :   群成员 身份 群主 管理员 用户
 */
public enum GroupMemberRoleEnum {

    /**
     * 普通成员
     */
    ORDINARY(0),

    /**
     * 管理员
     */
    MAMAGER(1),

    /**
     * 群主
     */
    OWNER(2),

    /**
     * 离开
     */
    LEAVE(3);
    ;


    private int code;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static GroupMemberRoleEnum getItem(int ordinal) {
        for (int i = 0; i < GroupMemberRoleEnum.values().length; i++) {
            if (GroupMemberRoleEnum.values()[i].getCode() == ordinal) {
                return GroupMemberRoleEnum.values()[i];
            }
        }
        return null;
    }

    GroupMemberRoleEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
