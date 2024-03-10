package edu.nuc.hcj.im.service.utils;

import edu.nuc.hcj.im.common.constant.Constant;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : ZKit.java
 * @createTime : 2023/12/20 17:52
 * @Description : zk 客户端
 */
@Component
public class ZKit {

    private static Logger logger = LoggerFactory.getLogger(ZKit.class);

    @Autowired
    private ZkClient zkClient;

    /**
     * get all TCP server node from zookeeper
     *
     * @return
     */
    // 获取到所有的tcp节点
    public List<String> getAllTcpNode() {
        List<String> children = zkClient.getChildren(Constant.ImCoreZkRoot + Constant.ImCoreZkRootTcp);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }

    /**
     * get all WEB server node from zookeeper
     *
     * @return
     */
    // 获取到所有的web节点
    public List<String> getAllWebNode() {
        List<String> children = zkClient.getChildren(Constant.ImCoreZkRoot + Constant.ImCoreZkRootWeb);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }
}
