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
 * @Package : edu.nuc.hcj.im.service.user.service
 * @ClassName : IMUserService.java
 * @createTime : 2023/12/6 18:35
 * @Description : 获取用户信息的请求封装类
 */
@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;


}
