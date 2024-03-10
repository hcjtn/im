package edu.nuc.hcj.im.common.utils;

import edu.nuc.hcj.im.common.BaseErrorCode;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.common.route.RouteInfo;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.utils
 * @ClassName : RouteInfoParseUtil.java
 * @createTime : 2023/12/20 19:23
 * @Description : 路由信息解析实用程序 用来将 ip 和 port 分开
 */
public class RouteInfoParseUtil {

    //将 ip和port 分开来
    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
