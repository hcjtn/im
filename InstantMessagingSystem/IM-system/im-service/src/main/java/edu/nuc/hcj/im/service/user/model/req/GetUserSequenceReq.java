package edu.nuc.hcj.im.service.user.model.req;

import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model.req
 * @ClassName : GetUserSequenceReq.java
 * @createTime : 2024/1/16 11:58
 * @Description :
 */
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
