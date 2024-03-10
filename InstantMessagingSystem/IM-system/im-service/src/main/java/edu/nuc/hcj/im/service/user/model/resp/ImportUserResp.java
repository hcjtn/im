package edu.nuc.hcj.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model.resp
 * @ClassName : ImportUserResp.java
 * @createTime : 2023/12/6 19:05
 * @Description :  导入用户响应封装类
 */
@Data
public class ImportUserResp {
    private List<String> successID;
    private List<String> errorID;
}
