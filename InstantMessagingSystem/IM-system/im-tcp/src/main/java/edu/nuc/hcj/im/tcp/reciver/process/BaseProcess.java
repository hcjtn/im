package edu.nuc.hcj.im.tcp.reciver.process;

import edu.nuc.hcj.im.codec.proto.MessagePack;
import edu.nuc.hcj.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.reciver.process
 * @ClassName : BaseProcess.java
 * @createTime : 2024/1/5 18:12
 * @Description :  process 处理
 */
public abstract class BaseProcess {
    public abstract void processBefore();

    public void process(MessagePack messagePack) {
        processBefore();
        NioSocketChannel nioSocketChannel = SessionSocketHolder.get(messagePack.getToId(), messagePack.getAppId(),
                messagePack.getImei(), messagePack.getClientType());
        if(nioSocketChannel != null){
            nioSocketChannel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();
}
