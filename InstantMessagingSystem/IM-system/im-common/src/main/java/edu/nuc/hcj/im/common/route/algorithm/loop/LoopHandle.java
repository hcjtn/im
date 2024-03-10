package edu.nuc.hcj.im.common.route.algorithm.loop;

import edu.nuc.hcj.im.common.enums.UserErrorCode;
import edu.nuc.hcj.im.common.exception.ApplicationException;
import edu.nuc.hcj.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route.algorithm.loop
 * @ClassName : LoopHandle.java
 * @createTime : 2023/12/20 19:36
 * @Description : 访问多服务器之间的 轮询规则  hash取余算法
 */
public class LoopHandle implements RouteHandle {

    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int l = (int)index.incrementAndGet() % size;
        if (l < 0) {
            l = 0;
        }

        return values.get(l) ;
    }
}
