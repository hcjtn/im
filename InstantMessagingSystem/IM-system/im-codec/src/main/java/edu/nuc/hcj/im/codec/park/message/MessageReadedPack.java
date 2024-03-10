package edu.nuc.hcj.im.codec.park.message;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.park.message
 * @ClassName : MessageReadedPack.java
 * @createTime : 2024/1/15 10:26
 * @Description :
 */
@Data
public class MessageReadedPack {
    private long messageSequence;

    private String fromId;

    private String toId;

    private String groupId;

    private String conversationType;
}

