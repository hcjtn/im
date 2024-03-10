package edu.nuc.hcj.im.codec.park.conversation;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.park.conversation
 * @ClassName : UpdateConversationPack.java
 * @createTime : 2024/1/15 12:00
 * @Description :
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
