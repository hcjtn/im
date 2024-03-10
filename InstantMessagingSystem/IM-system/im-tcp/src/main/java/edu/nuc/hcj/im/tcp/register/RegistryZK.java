package edu.nuc.hcj.im.tcp.register;

import edu.nuc.hcj.im.codec.config.BootStrapConfigration;
import edu.nuc.hcj.im.common.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.register
 * @ClassName : RegistryZK.java
 * @createTime : 2023/12/18 20:22
 * @Description : zookeeper注册中心
 */
public class RegistryZK implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(RegistryZK.class);

    private ZKit zKit;

    private String ip;

    private BootStrapConfigration.TcpConfig tcpConfig;

    public RegistryZK(ZKit zKit, String ip, BootStrapConfigration.TcpConfig tcpConfig) {
        this.zKit = zKit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {
        zKit.createRootNode();
        String tcpPath = Constant.ImCoreZkRoot + Constant.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath =
                Constant.ImCoreZkRoot + Constant.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebScoketPort();
        zKit.createNode(webPath);
        logger.info("Registry zookeeper webPath success, msg=[{}]", tcpPath);

    }
}
