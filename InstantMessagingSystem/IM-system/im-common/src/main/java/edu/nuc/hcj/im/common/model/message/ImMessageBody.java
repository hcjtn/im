package edu.nuc.hcj.im.common.model.message;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : ImMessageBody.java
 * @createTime : 2024/1/12 19:43
 * @Description :
 *
 * 和ImMessageBodyEntity中的成员变量一样
 * 最好不要使用是带有数据库标识的实体类
 *
 *
 */
@Data
public class ImMessageBody {
    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;
}