package edu.nuc.hcj.im.service.user.model.req;

import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model.req
 * @ClassName : SubscribeUserOnlineStatusReq.java
 * @createTime : 2024/1/17 11:22
 * @Description :
 */
@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {

    private List<String> subUserId;

    private Long subTime;


}