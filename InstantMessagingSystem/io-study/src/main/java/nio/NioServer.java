package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : nio
 * @ClassName : NioServer.java
 * @createTime : 2023/12/13 17:06
 * @Description :  nio 1.0
 * 同步非阻塞
 *
 * 缺点 ：
 * 大量的无用请求，服务器无法对其过滤 需要对所有的请求进行循环，才能将所有的有用请求进行操作  请求结束后还是会一直执行，没有退出机制
 */
public class NioServer {
    // 保存客户端连接
    static List<SocketChannel> channelList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // socket 检索与此通道关联的服务器套接字。
        // bind ServerSocket类绑定到指定的地址
        serverSocketChannel.socket().bind(new InetSocketAddress(9001));
        //设置ServerSocketChannel为非阻塞
        serverSocketChannel.configureBlocking(false);
        System.out.println("服务启动成功");
        while (true) {
            // 非阻塞模式accept方法不会阻塞，否则会阻塞
            // NIO的非阻塞是由操作系统内部实现的，底层调用了linux内核的accept函数
            SocketChannel accept = serverSocketChannel.accept();
            if (accept != null) {
                System.out.println("连接成功！");
                // 提前引用会导致 空指针异常
                accept.configureBlocking(false);
                // 保存客户端连接在List中
                channelList.add(accept);
            }
            // 遍历连接进行数据读取 10w - 1000 读写事件   将某个客户端的数据数据存储起来
            Iterator<SocketChannel> iterator = channelList.iterator();
            while (iterator.hasNext()) {
                SocketChannel next = iterator.next();
                ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                // 非阻塞模式read方法不会阻塞，否则会阻塞
                int len = next.read(byteBuffer);
                if (len > 0) {
                    System.out.println(Thread.currentThread().getName() + " 接收到消息：" + new String(byteBuffer.array()));
                } else if (len == -1) { // 如果客户端断开，把socket从集合中去掉
                    iterator.remove();
                    System.out.println("客户端断开连接");
                }
            }
        }
    }
}
