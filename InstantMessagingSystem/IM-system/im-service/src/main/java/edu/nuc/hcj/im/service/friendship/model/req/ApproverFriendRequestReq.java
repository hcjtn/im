package edu.nuc.hcj.im.service.friendship.model.req;

import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.req
 * @ClassName : ApproverFriendRequestReq.java
 * @createTime : 2023/12/7 20:06
 * @Description :
 */
@Data
public class ApproverFriendRequestReq extends RequestBase {

    private Long id;

    //1同意 2拒绝
    private Integer status;
}

