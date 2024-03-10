package edu.nuc.hcj.im.common.route.algorithm.random;

import edu.nuc.hcj.im.common.enums.UserErrorCode;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route.algorithm.random
 * @ClassName : RandomHandle.java
 * @createTime : 2023/12/20 17:21
 * @Description : 随机访问某一个netty服务器
 */
public class RandomHandle implements RouteHandle {
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        // 获取到一个小于 values.size() 的数字
        int i = ThreadLocalRandom.current().nextInt(size);

        return values.get(i);
    }
}
