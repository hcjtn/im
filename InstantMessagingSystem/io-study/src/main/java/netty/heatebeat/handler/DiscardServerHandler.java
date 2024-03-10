package netty.heatebeat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

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
    int readTimeOut = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        System.out.println("触发了：" + event.state() + "事件");

        if (event.state() == IdleState.READER_IDLE) {
            readTimeOut++;
        }
        if (readTimeOut>=5){
            System.out.println("超时超过5次，断开连接");
            ctx.close();
        }

    }
}
