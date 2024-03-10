package edu.nuc.hcj.im.tcp.handler;

import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.handler
 * @ClassName : HeartBeatHandler.java
 * @createTime : 2023/12/17 16:42
 * @Description :
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    // 规定的超时时间
    private Long heartBeatTime;

    public HeartBeatHandler(Long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;		// 强制类型转换
            if (event.state() == IdleState.READER_IDLE) {
                log.info("读空闲");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("进入写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {
                // 全空闲时间 在NettyServerHandler.channelRead0()中获取到的上一次进行心跳检测的时间
                Long lastReadTime = (Long) ctx.channel()
                        .attr(AttributeKey.valueOf(Constant.ReadTime)).get();
                long now = System.currentTimeMillis();

                if(lastReadTime != null && now - lastReadTime > heartBeatTime){
                    //TODO 退后台逻辑  离线逻辑
                    SessionSocketHolder.offlineUserSzession((NioSocketChannel) ctx.channel());
                }

            }
        }
    }
}
