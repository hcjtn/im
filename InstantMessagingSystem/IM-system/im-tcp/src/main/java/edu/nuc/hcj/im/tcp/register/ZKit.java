package edu.nuc.hcj.im.tcp.register;

import edu.nuc.hcj.im.common.constant.Constant;
import org.I0Itec.zkclient.ZkClient;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.register
 * @ClassName : ZKit.java
 * @createTime : 2023/12/18 19:49
 * @Description : zookeeper服务配置中心
 */
public class ZKit {
    private ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    // 标识注册节点名称
    // im-coreRoot/tcp/ip:port  后续可能进行Dns管理
    public void createRootNode() {
        // 如果不能存在 就调用
        boolean exists = zkClient.exists(Constant.ImCoreZkRoot);
        if (!exists) {
            zkClient.createPersistent(Constant.ImCoreZkRoot);
        }
        boolean tcpExists = zkClient.exists(Constant.ImCoreZkRoot + Constant.ImCoreZkRootTcp);
        if (!tcpExists) {
            zkClient.createPersistent(Constant.ImCoreZkRoot + Constant.ImCoreZkRootTcp);
        }
        boolean webExists = zkClient.exists(Constant.ImCoreZkRoot+Constant.ImCoreZkRootWeb);
        if (!webExists) {
            zkClient.createPersistent(Constant.ImCoreZkRoot + Constant.ImCoreZkRootWeb);
        }
    }

    //ip+port
    public void createNode(String path){
        if(!zkClient.exists(path)){
            zkClient.createPersistent(path);
        }
    }
}
