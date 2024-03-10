package netty.my.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : netty.my.codec
 * @ClassName : MyDecodecer.java
 * @createTime : 2023/12/14 17:31
 * @Description :
 */
public class MyDecodecer extends ByteToMessageDecoder {

    //数据长度 + 数据
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 读取数据长度  数据中第一位是数据长度 int长度大于4
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        //数据长度
        int i = byteBuf.readInt();

        // 剩余可读是否小于数据长度
        if (byteBuf.readableBytes()<i){
            // 重置读索引  读取数据失败(即传递数据丢失)时，已经读取了第一位int数据（用来表示数据长度），需要回退
            byteBuf.resetReaderIndex();
            return;
        }
        // 读取数据
        byte[] data = new byte[i];//10000
        byteBuf.readBytes(data);
        System.out.println(new String(data));

        // 标记读索引，下一次读取数据的起点
        byteBuf.markReaderIndex();//10004

    }
}
