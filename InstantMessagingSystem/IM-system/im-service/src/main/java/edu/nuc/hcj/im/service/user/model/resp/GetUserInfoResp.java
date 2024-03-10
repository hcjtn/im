package edu.nuc.hcj.im.service.user.model.resp;


import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model.resp
 * @ClassName : ImportUserResp.java
 * @createTime : 2023/12/6 19:05
 * @Description :  获取用户信息的响应封装类
 */

@Data
public class GetUserInfoResp {

    private Map<String, ImUserDataEntity> userDataItem;

    private List<String> failUser;


}
