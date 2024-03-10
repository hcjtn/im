package edu.nuc.hcj.im.service.friendship.dao;

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
 * @Package : edu.nuc.hcj.im.service.friendship.dao
 * @ClassName : ImFriendShipGroupEntity.java
 * @createTime : 2023/12/12 12:36
 * @Description :
 */
@Data
@TableName("im_friendship_group")
public class ImFriendShipGroupEntity {
    @TableId(value = "group_id",type = IdType.AUTO)
    private Long groupId;

    private String fromId;

    private Integer appId;

    private String groupName;
    /** 备注*/
    private Long createTime;

    /** 备注*/
    private Long updateTime;

    /** 序列号*/
    private Long sequence;

    private int delFlag;


}