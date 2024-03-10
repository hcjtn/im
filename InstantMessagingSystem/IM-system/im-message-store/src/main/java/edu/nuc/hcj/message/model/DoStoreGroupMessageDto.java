package edu.nuc.hcj.message.model;

import edu.nuc.hcj.im.common.model.message.GroupChatMessageContent;
import edu.nuc.hcj.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.message.model
 * @ClassName : DoStoreGroupMessageDto.java
 * @createTime : 2024/1/13 18:22
 * @Description :
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}