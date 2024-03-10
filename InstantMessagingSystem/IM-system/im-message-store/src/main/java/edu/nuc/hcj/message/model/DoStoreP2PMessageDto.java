package edu.nuc.hcj.message.model;


import edu.nuc.hcj.im.common.model.message.MessageContent;
import edu.nuc.hcj.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
