package edu.nuc.hcj.im.codec.park.user;

import edu.nuc.hcj.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.park.user
 * @ClassName : UserStatusChangeNotifyPack.java
 * @createTime : 2024/1/17 9:16
 * @Description :
 */
@Data
public class UserStatusChangeNotifyPack {
    private Integer appId;
    private String userId;
    private Integer status;

    private List<UserSession> client;


}
