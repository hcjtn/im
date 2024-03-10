package edu.nuc.hcj.im.codec.park.user;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.park.user
 * @ClassName : UserCustomStatusChangeNotifyPack.java
 * @createTime : 2024/1/17 11:47
 * @Description :
 */
@Data
public class UserCustomStatusChangeNotifyPack {

    private String customText;

    private Integer customStatus;

    private String userId;

}
