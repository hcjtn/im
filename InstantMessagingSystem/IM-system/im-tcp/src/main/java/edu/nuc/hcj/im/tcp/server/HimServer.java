package edu.nuc.hcj.im.tcp.server;


import edu.nuc.hcj.im.codec.MessageDecoder;
import edu.nuc.hcj.im.codec.MessageEncode;
import edu.nuc.hcj.im.codec.config.BootStrapConfigration;
import edu.nuc.hcj.im.tcp.handler.HeartBeatHandler;
import edu.nuc.hcj.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.server
 * @ClassName : HimServer.java
 * @createTime : 2023/12/15 11:58
 * @Description :
 */
public class HimServer {
    // 日志类
    private final static Logger logger = LoggerFactory.getLogger(HimServer.class);
    BootStrapConfigration.TcpConfig config;
    NioEventLoopGroup bossGroup;
    NioEventLoopGroup workGroup;
    ServerBootstrap bootstarp;


    public HimServer(BootStrapConfigration.TcpConfig config) {
        this.config = config;
        bossGroup = new NioEventLoopGroup(config.getBossThreadSize());
        workGroup = new NioEventLoopGroup(config.getWorkThreadSize());
        bootstarp = new ServerBootstrap();
        bootstarp.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR, true) // 参数表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY, true) // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保活开关2h没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new MessageEncode());
//                        //配置心跳检测
//                        ch.pipeline().addLast(new IdleStateHandler(
//                                0, 0,
//                               10));
                        ch.pipeline().addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        ch.pipeline().addLast(new NettyServerHandler(config.getBrokerId(),config.getLogicUrl()));
                    }
                });


    }

    public void start() {
        this.bootstarp.bind(this.config.getTcpPort());
    }

}
