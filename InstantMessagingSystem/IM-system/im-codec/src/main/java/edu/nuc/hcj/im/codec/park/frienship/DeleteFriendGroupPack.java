package edu.nuc.hcj.im.codec.park.frienship;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 删除好友分组通知报文
 **/
@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
