package edu.nuc.hcj.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.dao
 * @ClassName : ImFriendShipGroupMemberEntity.java
 * @createTime : 2023/12/12 12:35
 * @Description :
 */

@Data
@TableName("im_friendship_group_member")
public class ImFriendShipGroupMemberEntity {
    @TableId(value = "group_id")
    private Long groupId;

    private String toId;
}
