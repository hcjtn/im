package edu.nuc.hcj.im.codec;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.proto.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec
 * @ClassName : MessageEncode.java
 * @createTime : 2023/12/16 17:32
 * @Description : 私有协议解码
 */
public class MessageEncode extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (o instanceof MessagePack){
            MessagePack messagePack = (MessagePack) o;
            String jsonString = JSONObject.toJSONString(messagePack.getData());
            byte[] bytes = jsonString.getBytes();
            byteBuf.writeInt(messagePack.getCommand());
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
