package edu.nuc.hcj.im.service.friendship.model.callback;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.callback
 * @ClassName : DeleteFriendAfterCallBackDTO.java
 * @createTime : 2023/12/23 14:23
 * @Description :
 */
@Data
public class DeleteFriendAfterCallBackDTO {
    private String fromId;
    private String toId;
}
