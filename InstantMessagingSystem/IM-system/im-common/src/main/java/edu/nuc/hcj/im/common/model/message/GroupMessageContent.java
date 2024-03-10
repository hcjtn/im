package edu.nuc.hcj.im.common.model.message;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : GroupMessageContent.java
 * @createTime : 2024/1/7 15:28
 * @Description :
 */
@Data
public class GroupMessageContent extends MessageContent{
    private String groupId;
    private List<String> memberId;

}
