package edu.nuc.hcj.im.common.enums;

import edu.nuc.hcj.im.common.exception.ApplicationExceptionEnum;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : GroupErrorCode.java
 * @createTime : 2023/12/12 15:38
 * @Description :  群错误码
 */
public enum GroupErrorCode implements ApplicationExceptionEnum {

    GROUP_IS_NOT_EXIST(40000,"群不存在"),

    GROUP_IS_EXIST(40001,"群已存在"),

    GROUP_IS_HAVE_OWNER(40002,"群已存在群主"),

    USER_IS_JOINED_GROUP(40003,"该用户已经进入该群"),

    USER_JOIN_GROUP_ERROR(40004,"群成员添加失败"),

    GROUP_MEMBER_IS_BEYOND(40005,"群成员已达到上限"),

    MEMBER_IS_NOT_JOINED_GROUP(40006,"该用户不在群内"),

    THIS_OPERATE_NEED_MANAGER_ROLE(40007,"该操作只允许群主/管理员操作"),

    THIS_OPERATE_NEED_APPMANAGER_ROLE(40008,"该操作只允许APP管理员操作"),

    THIS_OPERATE_NEED_OWNER_ROLE(40009,"该操作只允许群主操作"),

    GROUP_OWNER_IS_NOT_REMOVE(40010,"群主无法移除"),

    UPDATE_GROUP_BASE_INFO_ERROR(40011,"更新群信息失败"),

    THIS_GROUP_IS_MUTE(40012,"该群禁止发言"),

    IMPORT_GROUP_ERROR(40013,"导入群组失败"),

    THIS_OPERATE_NEED_ONESELF(40014,"该操作只允许自己操作"),

    PRIVATE_GROUP_CAN_NOT_DESTORY(40015,"私有群不允许解散"),

    PUBLIC_GROUP_MUST_HAVE_OWNER(40016,"公开群必须指定群主"),

    GROUP_MEMBER_IS_SPEAK(40017,"群成员被禁言"),

    GROUP_IS_DESTROY(40018,"群组已解散"),

    CREATE_GROUP_ERROR(40018,"创建群聊失败"),

    PRIVATE_GROUP_REMOVE_BY_OWNER(40019,"私有群只能群主才能踢人"),

    PRIVATE_GROUP_CAN_NOT_MANAGER(40019,"私有群只能有群主"),

    CAN_NOT_BANED(40019,"该成员无法禁言"),



    ;

    private int code;
    private String error;

    GroupErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    @Override
    public int getCode() {
        return this.code;
    }
    @Override
    public String getError() {
        return this.error;
    }

}
