package edu.nuc.hcj.im.common.model.message;

import edu.nuc.hcj.im.common.model.ClientInfo;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.message.model
 * @ClassName : MessageContent.java
 * @createTime : 2024/1/6 17:41
 * @Description :
 */
@Data
public class MessageContent extends ClientInfo {
    private String messageId;
    private String toId;
    private String fromId;
    private String messageBody;

    private Long messageKey;
    private Long messageTime;
    private Long createTime;
    private String extra;
    private long messageSequence;



}
