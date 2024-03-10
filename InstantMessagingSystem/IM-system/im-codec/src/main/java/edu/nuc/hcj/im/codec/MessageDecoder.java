package edu.nuc.hcj.im.codec;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.proto.Message;
import edu.nuc.hcj.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.config
 * @ClassName : MessageDecoder.java
 * @createTime : 2023/12/15 19:43
 * @Description : 私有协议消息解码类
 */
public class MessageDecoder extends ByteToMessageDecoder {
    public MessageDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext,
                          ByteBuf byteBuf, List<Object> list) throws Exception {
        // 传递的消息必须大于28
        if (byteBuf.readableBytes() < 28) {
            return;
        }

        // 获取command 指令
        int command = byteBuf.readInt();
        // 获取版本号
        int version = byteBuf.readInt();
        //获取clientType
        int clientType = byteBuf.readInt();  // 客户端类型
        //获取消息解析类型
        int messageType = byteBuf.readInt();
        // 确定 appID
        int appID = byteBuf.readInt();
        // 获取imei长度
        int imeiLen = byteBuf.readInt();
        // 确定 bodyLen
        int bodyLen = byteBuf.readInt();

        // 获取imei内容
        // 处理粘包拆包问题
        if (byteBuf.readableBytes() < bodyLen + imeiLen) {
            // 回溯读指针
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] imeiBody = new byte[imeiLen];
        byteBuf.readBytes(imeiBody);
        String Stirng_imeiBody = new String(imeiBody);

        // 获取请求体内容

        if (byteBuf.readableBytes() < bodyLen) {
            // 回溯读指针
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] bodyData = new byte[bodyLen];
        byteBuf.readBytes(bodyData);
        String Stirng_bodyData = new String(bodyData);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCommand(command);
        messageHeader.setVersion(version);
        messageHeader.setClientType(clientType);
        messageHeader.setMessageType(messageType);
        messageHeader.setImeiLength(imeiLen);
        messageHeader.setAppId(appID);
        messageHeader.setLength(bodyLen);


        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if (messageType == 0x0) {
            String body = new String(bodyData);
            System.out.println(body);
            JSONObject jsonObject = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(jsonObject);
        }

        // 标记读索引
        byteBuf.markReaderIndex();
        list.add(message);

//        channelHandlerContext.fireChannelRead(message);


    }
}
