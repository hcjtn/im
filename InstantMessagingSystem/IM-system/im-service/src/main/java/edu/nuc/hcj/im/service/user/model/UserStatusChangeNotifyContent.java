package edu.nuc.hcj.im.service.user.model;

import edu.nuc.hcj.im.common.model.ClientInfo;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model
 * @ClassName : UserStatusChangeNotifyContent.java
 * @createTime : 2024/1/17 10:33
 * @Description : 用户状态变更 用户状态更改通知内容
 */
@Data
public class UserStatusChangeNotifyContent extends ClientInfo {


    private String userId;

    //服务端状态 1上线 2离线
    private Integer status;



}
