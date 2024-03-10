package edu.nuc.hcj.im.common.route.algorithm.consistentHash;

import edu.nuc.hcj.im.common.route.RouteHandle;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route.algorithm.consistentHash
 * @ClassName : ConsistentHashHandle.java
 * @createTime : 2023/12/20 19:52
 * @Description : 一致哈希算法实现 多服务器访问规则
 */
public class ConsistentHashHandle implements RouteHandle {
    private TreeMapConsistentHash hash;

    public void setHash(TreeMapConsistentHash hash){
        this.hash = hash;
    }



    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values,key);
    }
}
