package edu.nuc.hcj.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;

    /** messageBodyId  消息主键 */
    private Long messageKey;

    /** messageBody  消息实体*/
    private String messageBody;
// 对messagebody进行加密
    private String securityKey;
//客户端发送消息的时间
    private Long messageTime;
//服务端插入记录的时间
    private Long createTime;
//拓展字段
    private String extra;
//删除标识
    private Integer delFlag;



}
