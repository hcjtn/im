package edu.nuc.hcj.im.common.model;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model
 * @ClassName : UserSession.java
 * @createTime : 2023/12/13 15:43
 * @Description : 用户类型封装类
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识 是 pc端 还是 安卓 ios 等等
     */
    private Integer clientType;

    //sdk 版本号
    private Integer version;

    // 枚举类标识
    //连接状态 1=在线 2=离线
    private Integer connectState;
    //
    private Integer brokerId;

    private String brokerHost;

    private String imei;

}
