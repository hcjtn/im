package edu.nuc.hcj.im.service.utils;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : ConversationIdGenerate.java
 * @createTime : 2024/1/13 11:48
 * @Description : 对话 ID 生成  生成 生成 P2Pid
 */
public class ConversationIdGenerate {
    //A|B
    //B A
    public static String generateP2PId(String fromId,String toId){
        int i = fromId.compareTo(toId);
        if(i < 0){
            return toId+"|"+fromId;
        }else if(i > 0){
            return fromId+"|"+toId;
        }

        throw new RuntimeException("");
    }
}
