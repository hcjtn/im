package edu.nuc.hcj.im.service.friendship.model.req;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.model.req
 * @ClassName : FriendDto.java
 * @createTime : 2023/12/7 14:38
 * @Description :好友的基本信息
 */
@Data
public class FriendDto {

    private String toId;

    private String remark;

    private String addSource;

    private String extra;

    private String addWording;

}
