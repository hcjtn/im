package edu.nuc.hcj.im.codec.proto;

import lombok.Data;
import lombok.ToString;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.proto
 * @ClassName : Message.java
 * @createTime : 2023/12/15 20:03
 * @Description :
 */
@Data
@ToString
public class Message {
    private MessageHeader messageHeader;
    private Object messagePack;

}
