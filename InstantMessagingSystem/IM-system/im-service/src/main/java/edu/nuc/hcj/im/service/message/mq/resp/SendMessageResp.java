package edu.nuc.hcj.im.service.message.mq.resp;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class SendMessageResp {

    private Long messageKey;

    private Long messageTime;

}
