package edu.nuc.hcj.im.service.user.model.req;


import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model.req
 * @ClassName : ImportUserReq.java
 * @createTime : 2023/12/6 18:48
 * @Description : 删除用户请求封装类
 */

@Data
public class DeleteUserReq extends RequestBase {

    @NotEmpty(message = "用户id不能为空")
    private List<String> userIds;
}
