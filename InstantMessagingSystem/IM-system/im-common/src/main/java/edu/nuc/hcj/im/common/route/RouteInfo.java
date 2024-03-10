package edu.nuc.hcj.im.common.route;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route
 * @ClassName : RouteInfo.java
 * @createTime : 2023/12/20 19:24
 * @Description :Route实体类 用来将分开的port和ip封装起来
 */
@Data
public final class RouteInfo {

    private String ip;
    private Integer port;

    public RouteInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
