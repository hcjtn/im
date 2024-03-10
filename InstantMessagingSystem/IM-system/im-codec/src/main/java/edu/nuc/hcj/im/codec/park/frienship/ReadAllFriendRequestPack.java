package edu.nuc.hcj.im.codec.park.frienship;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 已读好友申请通知报文
 **/
@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
