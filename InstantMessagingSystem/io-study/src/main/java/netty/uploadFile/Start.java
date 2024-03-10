package netty.uploadFile;

import netty.uploadFile.server.UploadFileServer;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : netty
 * @ClassName : Start.java
 * @createTime : 2023/12/14 12:33
 * @Description :
 * 上传文件
 *
 * 1.请求上传
 * 2.创建文件
 * 3.将客户端写入本地磁盘
 *  command 4  filename 4 所以 可读产犊必须大于8才能往下执行 4+4=8
 *  command 文件上传指令类型 1 创建文件 2 写入文件
 *
 */
public class Start {
    public static void main(String[] args) throws Exception {
        new UploadFileServer(9001).run();
    }
}
