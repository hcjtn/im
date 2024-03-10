package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : CheckFriendShipTypeEnum.java
 * @createTime : 2023/12/7 16:23
 * @Description : 好友关系校验枚举类
 */
public enum CheckFriendShipTypeEnum {

    /**
     * 1 单方校验；2双方校验。
     */
    SINGLE(1),

    BOTH(2),
            ;

    private int type;

    CheckFriendShipTypeEnum(int type){
        this.type=type;
    }

    public int getType() {
        return type;
    }
}
