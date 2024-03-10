package edu.nuc.hcj.im.service.user.mq;

import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.mq
 * @ClassName : PullUserOnlineStatusReq.java
 * @createTime : 2024/1/17 12:01
 * @Description :
 */
@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
