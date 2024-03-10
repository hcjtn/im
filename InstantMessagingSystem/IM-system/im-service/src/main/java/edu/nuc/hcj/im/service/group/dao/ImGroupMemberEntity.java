package edu.nuc.hcj.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.group.dao
 * @ClassName : ImGroupMemberEntity.java
 * @createTime : 2023/12/12 15:44
 * @Description :
 */
@Data
@TableName("im_group_member")
public class ImGroupMemberEntity {

    @TableId(type = IdType.AUTO)
    private Long groupMemberId;

    private Integer appId;

    private String groupId;

    //成员id
    private String memberId;

    //群成员类型，0 普通成员, 1 管理员, 2 群主， 3 禁言，4 已经移除的成员
    private Integer role;

    private Long speakDate;

    //群昵称
    private String alias;

    //加入时间
    private Long joinTime;

    //离开时间
    private Long leaveTime;

    private String joinType;

    private String extra;
}
