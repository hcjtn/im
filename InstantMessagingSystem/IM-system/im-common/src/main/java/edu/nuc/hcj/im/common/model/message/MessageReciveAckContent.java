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
 * @ClassName : MessageReciveAckContent.java
 * @createTime : 2024/1/13 10:33
 * @Description :
 */
@Data
public class MessageReciveAckContent extends ClientInfo {
    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

}
