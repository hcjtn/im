package edu.nuc.hcj.im.service.friendship.model.req;

import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.req
 * @ClassName : DeleteBlackReq.java
 * @createTime : 2023/12/7 17:18
 * @Description :
 */
@Data
public class DeleteBlackReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    @NotBlank(message = "好友id不能为空")
    private String toId;

}