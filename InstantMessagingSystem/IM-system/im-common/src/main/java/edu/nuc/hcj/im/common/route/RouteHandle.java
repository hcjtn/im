package edu.nuc.hcj.im.common.route;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route
 * @ClassName : RouteHandle.java
 * @createTime : 2023/12/20 17:18
 * @Description : 路由控制
 */
public interface RouteHandle {
    // 根据该key值在这一堆的values 选出一个服务器地址
    public String routeServer(List<String>values,String key);
}
