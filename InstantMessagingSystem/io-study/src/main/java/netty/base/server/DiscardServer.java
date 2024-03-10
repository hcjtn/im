package netty.base.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import netty.base.handler.DiscardServerHandler;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : netty.base.server
 * @ClassName : DiscardServer.java
 * @createTime : 2023/12/14 12:34
 * @Description : 服务端启动类
 * 模板代码
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }


    public void run() throws Exception {
        //NioEventLoopGroup本质 https://zhuanlan.zhihu.com/p/367218937
        //创建两个线程组 boosGroup、workerGroup
        /**
         * bossGroup 用于监听客户端连接，专门负责与客户端创建连接，并把连接注册到workerGroup的Selector中。
         * workerGroup 用于处理每一个连接发生的读写事件
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();//

        try {
            //创建服务端的启动对象，设置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 设置两个线程组boosGroup和workerGroup
            bootstrap.group(bossGroup, workerGroup)
                    // 设置服务端通道实现类型  这个方法用于设置通道类型，当建立连接后，会根据这个设置创建对应的Channel实例。
                    .channel(NioServerSocketChannel.class)
                    //使用匿名内部类的形式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
//                                    2048,0,
//                                    4,0,4
//                            ));
//                            ch.pipeline().addLast(new LengthFieldPrepender(2));
//                            ch.pipeline().addLast(new StringDecoder());
//                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    //设置线程队列得到连接个数  option()设置的是服务端用于接收进来的连接，也就是boosGroup线程。
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    //设置保持活动连接状态  childOption()是提供给父管道接收到的连接，也就是workerGroup线程。
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            System.out.println("java技术爱好者的服务端已经准备就绪...");
            //绑定端口号，启动服务端
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}
