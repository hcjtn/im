package edu.nuc.hcj.im.codec.park.message;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.park.message
 * @ClassName : ChatMessageAck.java
 * @createTime : 2024/1/6 20:33
 * @Description :
 */
@Data
public class ChatMessageAck {

    private String messageId;
    private Long messageSequence;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }

    public ChatMessageAck(String messageId,Long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }

}
