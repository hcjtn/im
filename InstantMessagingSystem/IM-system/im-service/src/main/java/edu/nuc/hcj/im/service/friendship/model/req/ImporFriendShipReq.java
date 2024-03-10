package edu.nuc.hcj.im.service.friendship.model.req;

import edu.nuc.hcj.im.common.enums.FriendShipStatusEnum;
import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.req
 * @ClassName : ImporFriendShipReq.java
 * @createTime : 2023/12/7 13:35
 * @Description :
 */
@Data
public class ImporFriendShipReq  extends RequestBase {
    @NotBlank(message = "fromId不能为空")
    private String fromId;
    private List<ImportFriendDto> friendItem;
    @Data
    public static class ImportFriendDto{

        private String toId;

        private String remark;

        private String addSource;

        // 状态
        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();
        // 黑名单
        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }

}
