package edu.nuc.hcj.im.tcp;

import edu.nuc.hcj.im.codec.config.BootStrapConfigration;
import edu.nuc.hcj.im.tcp.reciver.MessageReciver;
import edu.nuc.hcj.im.tcp.redis.RedisManager;
import edu.nuc.hcj.im.tcp.register.RegistryZK;
import edu.nuc.hcj.im.tcp.register.ZKit;
import edu.nuc.hcj.im.tcp.server.HimServer;
import edu.nuc.hcj.im.tcp.server.HimWebSocketServer;
import edu.nuc.hcj.im.tcp.utils.MqFactory;
import io.netty.bootstrap.BootstrapConfig;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp
 * @ClassName : Starter.java
 * @createTime : 2023/12/15 11:57
 * @Description :
 */

/**
 *使用私有协议时需要：
 *                 确定指令 了解到本次发起通知需要做那些事情 （类似 http请求时确定 是 post、get、delete或者put）
 *                 确定当前协议的版本号
 *                 确定客户端类型( IOS 安卓 pc(windows mac) web )
 *                 确定消息解析类型 是支持 json、multimedia还是什么类型
 *                 确定imei长度
 *                 确定 appId  系统支持多个服务去接入 每个服务用appid去隔离
 *                 确定bodylen
 * 请求头中每个 会占用4个字节   4*7=28个字节
 *
 *  + imei号
 *
 *  + 请求体
 *
 */
public class Starter {
    public static void main(String[] args) {
        // 配置文件地址写在了 运行/调试配置 当中
        if (args.length >=1) {
            try {
                start(args[0]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param path    存放的是 yaml的地址路径
     * @throws UnknownHostException
     */
    private static void start(String path) throws UnknownHostException {
        Yaml yaml = new Yaml();
        try {
            FileInputStream fileInputStream =
                    new FileInputStream(path);
            BootStrapConfigration bootStrapConfigration = yaml.loadAs(fileInputStream, BootStrapConfigration.class);
            // 启动redis单例服务器  与redis进行连接
            RedisManager.init(bootStrapConfigration);
            // 启动mq连接
            // 启动MqFactory接收操作  init进行的是配置操作
            MqFactory.init(bootStrapConfigration.getIm().getRabbitmq());

            new HimServer(bootStrapConfigration.getIm()).start();
            new HimWebSocketServer(bootStrapConfigration.getIm()).start();
            // 消息接收
            MessageReciver.init(bootStrapConfigration.getIm().getBrokerId().toString());
            registerZK(bootStrapConfigration);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //
    public static void registerZK(BootStrapConfigration config) throws UnknownHostException {
        //获取 本机的ip地址
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        //读取yml ， 将yml配置文件中的数据封装到zkClient
        ZkClient zkClient = new ZkClient(config.getIm().getZkConfig().getZkAddr(),
                config.getIm().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegistryZK registryZK = new RegistryZK(zKit, hostAddress, config.getIm());
        Thread thread = new Thread(registryZK);
        thread.start();
    }
}
