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
 * @ClassName : UserId.java
 * @createTime : 2023/12/7 12:07
 * @Description :  将查单个用户信息的请求参数UserId封装起来 目的是为了能够拥有父类的appId
 */
@Data
public class UserId extends RequestBase {
    private String UserId;
}
