package edu.nuc.hcj.im.codec.park.frienship;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 删除黑名单通知报文
 **/
@Data
public class DeleteBlackPack {

    private String fromId;

    private String toId;

    private Long sequence;
}
