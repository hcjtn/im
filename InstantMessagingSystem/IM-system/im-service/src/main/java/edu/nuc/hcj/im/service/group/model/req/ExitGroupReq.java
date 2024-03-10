package edu.nuc.hcj.im.service.group.model.req;

import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.model.req
 * @ClassName : ExitGroupReq.java
 * @createTime : 2023/12/13 13:18
 * @Description :
 */
@Data
public class ExitGroupReq  extends RequestBase {
    private String groupId;
}
