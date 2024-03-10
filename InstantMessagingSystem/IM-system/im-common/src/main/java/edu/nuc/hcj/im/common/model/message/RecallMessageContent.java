package edu.nuc.hcj.im.common.model.message;

import edu.nuc.hcj.im.common.model.ClientInfo;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : RecallMessageContent.java
 * @createTime : 2024/1/17 16:08
 * @Description :
 */
@Data
public class RecallMessageContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageTime;

    private Long messageSequence;

    private Integer conversationType;


//    {
//        "messageKey":419455774914383872,
//            "fromId":"lld",
//            "toId":"lld4",
//            "messageTime":"1665026849851",
//            "messageSequence":2,
//            "appId": 10000,
//            "clientType": 1,
//            "imei": "web",
//    "conversationType":0
//    }
}
