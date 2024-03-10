package edu.nuc.hcj.im.common.model.message;

import edu.nuc.hcj.im.common.model.ClientInfo;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : MessageReadedContent.java
 * @createTime : 2024/1/15 10:15
 * @Description :
 */
@Data
public class MessageReadedContent extends ClientInfo {
    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;
}
