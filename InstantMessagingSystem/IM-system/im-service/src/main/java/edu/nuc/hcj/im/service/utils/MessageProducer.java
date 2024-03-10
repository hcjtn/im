package edu.nuc.hcj.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.command.Command;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.common.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import edu.nuc.hcj.im.codec.proto.MessagePack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : MessageProducer.java
 * @createTime : 2023/12/23 18:52
 * @Description : 给用户发送消息的封装类
 */
@Component
@Slf4j
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constant.RabbitConstants.MessageService2Im;

    public boolean sendMessage(UserSession session, Object msg) {
        try {
            logger.info("send message == " + msg);
            rabbitTemplate.convertAndSend(queueName, session.getBrokerId() + "", msg);
            logger.info("send success == " + msg);
            return true;
        } catch (Exception e) {
            logger.info("send error == " + e.getMessage());
            return false;
        }
    }

    //包装数据，调用sendMessage
    public boolean sendPack(String toId, Command command, Object msg, UserSession session) {
        MessagePack messagePack = new MessagePack();
        // 封装路由属性
        messagePack.setAppId(session.getAppId());
        messagePack.setClientType(session.getClientType());
        messagePack.setToId(toId);
        messagePack.setCommand(command.getCommand());
        messagePack.setImei(session.getImei());
        // 下面是真正的传递的数据
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(session));
        messagePack.setData(jsonObject);

        // 将整个messagePack在转换
        String data = JSONObject.toJSONString(jsonObject);
        boolean b = sendMessage(session, data);
        return b;
    }

    //发送给所有端的方法
    public List<ClientInfo> sendToUser(String toId,Command command,Object data,Integer appId){
        List<UserSession> userSession
                = userSessionUtils.getUserSessions(appId, toId);
        List<ClientInfo> list = new ArrayList<>();
        for (UserSession session : userSession) {
            boolean b = sendPack(toId, command, data, session);
            if(b){
                list.add(new ClientInfo(session.getAppId(),session.getClientType(),session.getImei()));
            }
        }
        return list;
    }

    public void sendToUser(String toId, Integer clientType,String imei, Command command,
                           Object data, Integer appId){
        //
        if(clientType != null && StringUtils.isNotBlank(imei)){
            // 为true 说明是发起者的app调用 需要发送给除这个端的所有端
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId,command,data,clientInfo);
        }else{
            sendToUser(toId,command,data,appId);
        }
    }

    //发送给某个用户的指定客户端
    public void sendToUser(String toId, Command command
            , Object data, ClientInfo clientInfo){
        UserSession userSession = userSessionUtils.getLocationUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(),
                clientInfo.getImei());
        sendPack(toId,command,data,userSession);
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

    //发送给除了某一端的其他端
    public void sendToUserExceptClient(String toId, Command command
            , Object data, ClientInfo clientInfo){
        List<UserSession> userSession = userSessionUtils
                .getUserSessions(clientInfo.getAppId(),
                        toId);
        for (UserSession session : userSession) {
            if(!isMatch(session,clientInfo)){
                sendPack(toId,command,data,session);
            }
        }
    }
}