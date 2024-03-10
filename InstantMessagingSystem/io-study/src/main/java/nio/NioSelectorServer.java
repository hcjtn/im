package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : nio
 * @ClassName : NioSelectorServer.java
 * @createTime : 2023/12/13 20:00
 * @Description :  nio 2.0    少去了大量的无用操作  实现了什么操作，调用什么代码
 * 一个多路复用器Selector可以同时轮询多个Channel，由于JDK使用了epoll代替传统的select实现，
 * 所以它并没有最大连接句柄1024/2048的限制。这也就意味着只需要一个线程负责Selector的轮询，就可以介入成千上万的客户端。
 *
 * 为什么使用Selector?
 * 仅用单个线程来处理多个Channels的好处是，只需要更少的线程来处理通道。
 * 事实上，可以只用一个线程处理所有的通道。对于操作系统来说，线程之间上下文切换的开销很大，
 * 而且每个线程都要占用系统的一些资源（如内存）。因此，使用的线程越少越好。
 *
 *
 *
 */
public class NioSelectorServer {
    public static void main(String[] args) throws IOException {
        int OP_ACCEPT = 1 << 4;
//        System.out.println(OP_ACCEPT); 16
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.socket().bind(new InetSocketAddress(9001));
        // 非阻塞
        socketChannel.configureBlocking(false);
        // 打开Selector处理Channel，即创建epoll
        Selector selector = Selector.open();  // 启动了多路复用器
        // 把ServerSocketChannel注册到selector上，并且selector对客户端accept连接操作感兴趣
        /**
         * register()方法的第二个参数。这是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣。
         * 可以监听四种不同类型的事件：
         * Connect连接
         * Accept接受
         * Read读
         * Write写
         * 对某一事件进行监听
         */
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动成功");

        while (true) {
            // 阻塞等待需要处理的事件发生 已注册事件发生后，会执行后面逻辑
            selector.select();
            //获取selector中注册的全部事件的SelectionKey实例
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            // 遍历SelectionKey对事件进行处理
            while(iterator.hasNext()){
                SelectionKey next = iterator.next();
                // 如果是OP_ACCEPT事件，则进行连接获取和事件注册
                if (next.isAcceptable()){
                    ServerSocketChannel channel = (ServerSocketChannel) next.channel();
                    SocketChannel accept = channel.accept();
                    socketChannel.configureBlocking(false);
                    // 这里只注册了读事件，如果需要给客户端发送数据可以注册写事件
                    SelectionKey selKey = socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接成功");
                }else if(next.isReadable()){ // 如果是OP_READ事件，则进行读取和打印
                    SocketChannel socketChannel1 = (SocketChannel) next.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    int len = socketChannel1.read(byteBuffer);
                    // 如果有数据，把数据打印出来
                    if (len > 0) {
                        System.out.println(Thread.currentThread().getName() +  "接收到消息：" + new String(byteBuffer.array()));
                    } else if (len == -1) { // 如果客户端断开连接，关闭Socket
                        System.out.println("客户端断开连接");
                        socketChannel.close();
                    }
                }
                //从事件集合里删除本次处理的key，防止下次select重复处理
                iterator.remove();
            }
        }
    }
}
