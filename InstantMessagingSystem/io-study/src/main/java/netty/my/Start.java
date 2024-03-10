package netty.my;

import netty.my.server.DiscardServer;

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
 */
public class Start {
    public static void main(String[] args) throws Exception {
        new DiscardServer(9001).run();
    }
}
