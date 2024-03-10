package edu.nuc.hcj.im.service.friendship.model.callback;

import edu.nuc.hcj.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.callback
 * @ClassName : AddfriendAfterCallBackDTO.java
 * @createTime : 2023/12/23 14:00
 * @Description :
 */
@Data
public class AddfriendAfterCallBackDTO {
    private String fromId;
    private FriendDto toItem;
}
