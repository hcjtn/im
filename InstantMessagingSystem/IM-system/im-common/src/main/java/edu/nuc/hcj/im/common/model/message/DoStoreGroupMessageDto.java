package edu.nuc.hcj.im.common.model.message;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : DoStoreGroupMessageDto.java
 * @createTime : 2024/1/13 18:00
 * @Description : Group消息持久化 封装类
 */
@Data
public class DoStoreGroupMessageDto {
    private GroupMessageContent messageContent;

    // 最好不要直接使用带有数据库注解的javaBean实体类 所以创一个新的和原类的成员变量相同的类
    private ImMessageBody messageBody;
}
