package bio;

import jdk.internal.org.objectweb.asm.Handle;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : PACKAGE_NAME
 * @ClassName : bio.java
 * @createTime : 2023/12/13 16:41
 * @Description :bio线程模型
 *
 * 同步阻塞I/O模式，数据的读取写入必须阻塞在一个线程内等待其完成
 *
 *
 *
 */
public class SocketServer {
    //1.0 服务端在处理完第一个客户端的所有事件之前，无法为其他客户端提供服务。
    //2.0 会产生大量空闲线程，浪费服务器资源。
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9001);
        while (true) {
            System.out.println("等待连接..");
            // 阻塞方法
            // 侦听要连接到此套接字并接受它。
            Socket accept = serverSocket.accept();
            System.out.println("有客户端连接了..");
//           new SocketServer().handle(accept);
        //采用多线程
            ThreadPoolExecutor threadPoolExecutor =
                    new ThreadPoolExecutor(5,10,3,
                            TimeUnit.MINUTES,new ArrayBlockingQueue<Runnable>(5));
            threadPoolExecutor.execute(()->{
                try {
                    new SocketServer().handle(accept);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            threadPoolExecutor.shutdown();
        }
        
    }
    
    public void handle(Socket accept) throws IOException {
        byte[] bytes = new byte[1024];
        System.out.println(Thread.currentThread().getName() +"准备read..");
        //接收客户端的数据，阻塞方法，没有数据可读时就阻塞
        int read = accept.getInputStream().read(bytes);
        System.out.println(Thread.currentThread().getName() +"read完毕。。");
        if (read != -1) {
            System.out.println(Thread.currentThread().getName() +  "接收到客户端的数据：" + new String(bytes, 0, read));
        }
    }
}
