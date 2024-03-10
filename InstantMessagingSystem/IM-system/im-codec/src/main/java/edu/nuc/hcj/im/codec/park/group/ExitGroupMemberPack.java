package edu.nuc.hcj.im.codec.park.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 退出群通知报文
 **/
@Data
public class ExitGroupMemberPack {

    private String groupId;

    private String member;

}
