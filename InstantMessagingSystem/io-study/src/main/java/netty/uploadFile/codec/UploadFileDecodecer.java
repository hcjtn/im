package netty.uploadFile.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import netty.uploadFile.FileDto;

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
public class UploadFileDecodecer extends ByteToMessageDecoder {

    //数据长度 + 数据
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 读取数据长度  数据中第一位是数据长度 int长度大于4
        if (byteBuf.readableBytes() < 8) {
            return;
        }
        // 文件上传指令 长度为4
        int command = byteBuf.readInt();
        FileDto fileDto = new FileDto();


        int fileNameLen = byteBuf.readInt();
        if(byteBuf.readableBytes() < fileNameLen){
            byteBuf.resetReaderIndex();
            return;
        }
        // 读取操作指令已经文件名
        byte[] data = new byte[fileNameLen];
        byteBuf.readBytes(data);
        String fileName = new String(data);
        fileDto.setCommand(command);
        fileDto.setFileName(fileName);

        // 判断操作指令类型  如果时写入指令 读取文件内容
        if(command == 2){
            int dataLen = byteBuf.readInt();
            if(byteBuf.readableBytes() < dataLen){
                // 读取文件内容
                byteBuf.resetReaderIndex();
                return;
            }
            byte[] fileData = new byte[dataLen];
            byteBuf.readBytes(fileData);
            fileDto.setBytes(fileData);
        }
        byteBuf.markReaderIndex();//10004
        list.add(fileDto);

    }
}
