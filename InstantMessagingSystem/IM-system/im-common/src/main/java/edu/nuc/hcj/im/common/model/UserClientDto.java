package edu.nuc.hcj.im.common.model;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model
 * @ClassName : UserClientDto.java
 * @createTime : 2023/12/17 15:06
 * @Description : 将私有协议的 前三项封装起来  userID  addId ClientType
 */
@Data
public class UserClientDto {

    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;

}