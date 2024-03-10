package edu.nuc.hcj.im.common.model.message;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model.message
 * @ClassName : DoStoreP2PMessageDto.java
 * @createTime : 2024/1/12 19:41
 * @Description : p2p 消息持久化封装类
 *
 * 需要对 edu.nuc.hcj.im.service.message.service.MessageStoreService.storeP2PMessage进行持久化的两个进行封装成为一个类
 */
@Data
public class DoStoreP2PMessageDto {
    private MessageContent messageContent;

    // 最好不要直接使用带有数据库注解的javaBean实体类 所以创一个新的和原类的成员变量相同的类
    private ImMessageBody messageBody;

}
