package edu.nuc.hcj.im.common.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums
 * @ClassName : ConversationTypeEnum.java
 * @createTime : 2024/1/15 11:35
 * @Description :
 */
public enum ConversationTypeEnum {

    /**
     * 0 单聊 1群聊 2机器人 3公众号
     */
    P2P(0),

    GROUP(1),

    ROBOT(2),
    ;

    private int code;

    ConversationTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
