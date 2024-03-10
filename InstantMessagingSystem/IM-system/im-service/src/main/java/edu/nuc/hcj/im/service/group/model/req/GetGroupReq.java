package edu.nuc.hcj.im.service.group.model.req;


import edu.nuc.hcj.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
