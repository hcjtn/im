package edu.nuc.hcj.im.codec.park.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.codec.park.message
 * @ClassName : RecallMessageNotifyPack.java
 * @createTime : 2024/1/17 16:52
 * @Description :
 */
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
