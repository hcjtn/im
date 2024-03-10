package edu.nuc.hcj.im.common.model;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model
 * @ClassName : RequestBase.java
 * @createTime : 2023/12/6 18:43
 * @Description :
 */
@Data
public class RequestBase {

    private Integer appId;

    //作用是 谁再调用该接口
    private String operater;

    private Integer clientType;

    private String imei;
}
