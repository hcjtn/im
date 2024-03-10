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
 * @ClassName : SetUserCustomerStatusReq.java
 * @createTime : 2024/1/17 11:23
 * @Description :
 */
@Data
public class SetUserCustomerStatusReq extends RequestBase {

    private String userId;

    private String customText;

    private Integer customStatus;

}
