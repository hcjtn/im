package edu.nuc.hcj.im.service.group.model.resp;


import edu.nuc.hcj.im.service.group.dao.ImGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetJoinedGroupResp {

    private Integer totalCount;

    private List<ImGroupEntity> groupList;

}
