package edu.nuc.hcj.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.resp
 * @ClassName : ImportFriendShipResp.java
 * @createTime : 2023/12/7 14:07
 * @Description :
 */
@Data
public class ImportFriendShipResp {
    private List<String> successIds;
    private List<String> errorIds;
}
