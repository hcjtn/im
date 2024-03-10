package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : ApproverFriendRequestStatusEnum.java
 * @createTime : 2023/12/7 20:26
 * @Description : 审批好友请求状态 枚举
 */

public enum ApproverFriendRequestStatusEnum {

    /**
     * 1 同意；2 拒绝。
     */
    AGREE(1),

    REJECT(2),
    ;

    private int code;

    ApproverFriendRequestStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}