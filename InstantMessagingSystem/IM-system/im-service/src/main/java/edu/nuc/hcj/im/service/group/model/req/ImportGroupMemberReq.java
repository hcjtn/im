package edu.nuc.hcj.im.service.group.model.req;


import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class ImportGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private List<GroupMemberDto> members;

}
