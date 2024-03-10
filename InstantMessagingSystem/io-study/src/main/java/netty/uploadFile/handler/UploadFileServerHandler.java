package netty.uploadFile.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netty.uploadFile.FileDto;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

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
public class UploadFileServerHandler extends ChannelInboundHandlerAdapter {

    static Set<Channel> channelList = new HashSet<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FileDto){
            FileDto dto = (FileDto) msg;
            if(dto.getCommand() == 1){
                //创建文件
                File file = new File("C://"+dto.getFileName());
                if(!file.exists()){
                    file.createNewFile();
                }
            }else if(dto.getCommand() == 2){
                //写入文件
                save2File("C://"+dto.getFileName(),dto.getBytes());
            }
        }

    }

    public static boolean save2File(String fname, byte[] msg){
        OutputStream fos = null;
        try{
            File file = new File(fname);
            File parent = file.getParentFile();
            boolean bool;
            if ((!parent.exists()) &
                    (!parent.mkdirs())) {
                return false;
            }
            fos = new FileOutputStream(file,true);
            fos.write(msg);
            fos.flush();
            return true;
        }catch (FileNotFoundException e){
            return false;
        }catch (IOException e){
            File parent;
            return false;
        }
        finally{
            if (fos != null) {
                try{
                    fos.close();
                }catch (IOException e) {}
            }
        }
    }


}
