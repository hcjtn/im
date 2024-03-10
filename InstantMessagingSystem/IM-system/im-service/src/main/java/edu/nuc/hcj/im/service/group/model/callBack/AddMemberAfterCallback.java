package edu.nuc.hcj.im.service.group.model.callBack;

import edu.nuc.hcj.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.model.callBack
 * @ClassName : AddMemberAfterCallback.java
 * @createTime : 2023/12/23 15:46
 * @Description :
 */
@Data
public class AddMemberAfterCallback {
    private String groupId;
    private Integer groupType;
    private String operater;
    private List<AddMemberResp> memberId;
}
