package edu.nuc.hcj.im.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 0_fromId_toId  会话类型+ fromId+toId
    private String conversationId;

    //会话类型
    private Integer conversationType;

    private String fromId;

    private String toId;

    // 是否静音
    private int isMute;

    private int isTop;

    private Long sequence;

    // 记录已经读到那条 Sequence
    private Long readedSequence;

    private Integer appId;
}
