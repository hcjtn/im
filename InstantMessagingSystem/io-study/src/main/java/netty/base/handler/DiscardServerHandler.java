package netty.base.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : io-study
 * @Package : netty.base.handler
 * @ClassName : DiscardServerHandler.java
 * @createTime : 2023/12/14 13:51
 * @Description :
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有客户端连接了");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("qwer");
        ByteBuf out = (ByteBuf) msg;
        String data = out.toString(CharsetUtil.UTF_8);
        System.out.println(data);
//        short length = (short) data
//                .getBytes().length;
//        ByteBuf byteBuf = Unpooled.directBuffer(8);
//        byteBuf.writeShort(length);
//        byteBuf.writeBytes(data.getBytes());
//        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }

}
