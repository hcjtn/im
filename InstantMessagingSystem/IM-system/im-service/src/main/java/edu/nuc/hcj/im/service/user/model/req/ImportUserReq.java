package edu.nuc.hcj.im.service.user.model.req;

import edu.nuc.hcj.im.common.model.RequestBase;
import edu.nuc.hcj.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.user.model.req
 * @ClassName : ImportUserReq.java
 * @createTime : 2023/12/6 18:48
 * @Description : 导入用户请求封装类
 */
@Data
public class ImportUserReq extends RequestBase {
    private List<ImUserDataEntity> userData;
}
