package edu.nuc.hcj.im.service.conversation.model;


import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;  // 是否静音

    private Integer isTop;

    private String fromId;


}
