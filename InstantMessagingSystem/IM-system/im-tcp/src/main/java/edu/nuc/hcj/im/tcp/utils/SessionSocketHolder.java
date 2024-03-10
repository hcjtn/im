package edu.nuc.hcj.im.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.park.user.UserStatusChangeNotifyPack;
import edu.nuc.hcj.im.codec.proto.MessageHeader;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ImConnectStatusEnum;
import edu.nuc.hcj.im.common.enums.command.UserEventCommand;
import edu.nuc.hcj.im.common.model.UserClientDto;
import edu.nuc.hcj.im.common.model.UserSession;
import edu.nuc.hcj.im.tcp.publish.MqMessageProducer;
import edu.nuc.hcj.im.tcp.redis.RedisManager;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.utils
 * @ClassName : SessionSocketHolder.java
 * @createTime : 2023/12/16 18:17
 * @Description : 用来存储 连接Channel
 */
public class SessionSocketHolder {
    // ConcurrentHashMap 实现Map下的线程安全
    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    // 存储
    public static void put(String userId, Integer appId, Integer clientType, String imei, NioSocketChannel channel) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        CHANNELS.put(userClientDto, channel);
    }

    // 取出
    public static NioSocketChannel get(String userId, Integer appId, String imei, Integer clientType) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        return CHANNELS.get(userClientDto);

    }

    // 获取改用户在所有端下的channe连接
    public static List<NioSocketChannel> get(Integer appId, String userId) {
        Set<UserClientDto> userClientDtos = CHANNELS.keySet();
        ArrayList<NioSocketChannel> nioSocketChannels = new ArrayList<>();
        userClientDtos.forEach(channel->{
            if (channel.getAppId().equals(appId) && channel.getUserId().equals(userId)){
                nioSocketChannels.add(CHANNELS.get(channel));
            }
        });
        return nioSocketChannels;
    }

    // 删除
    public static NioSocketChannel remove(String userId, Integer appId, Integer clientType, String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setUserId(userId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        return CHANNELS.remove(userClientDto);

    }

    //
    public static void remove(NioSocketChannel channel) {
        CHANNELS.entrySet().stream().filter(entity -> entity.getValue() == channel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    // 用户退出
    public static void removeUserSession(NioSocketChannel nioSocketChannel) {

//        System.out.println(nioSocketChannel.attr(AttributeKey.valueOf(Constant.UserId)));
//        System.out.println(nioSocketChannel.
//                attr(AttributeKey.valueOf(Constant.UserId)).get());
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constant.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constant.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constant.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constant.Imei)).get();

        //删除SessionSocketHolder
        SessionSocketHolder.remove(userId, appId, clientType, imei);
        // redis 删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId +
                Constant.RedisConstant.UserSessionConstants + userId);
        map.remove(clientType + ":" + imei);


        // 封装 messageHeader
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);
        //TODO 通知客户端用户下线
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(appId);
        userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
        userStatusChangeNotifyPack.setUserId(userId);
        MqMessageProducer.sendMessage(userStatusChangeNotifyPack,messageHeader,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());
        nioSocketChannel.close();
    }

    // 离线用户 Szession  删除内存里面的   不能删除redis里面的map 要修改器状态
    public static void offlineUserSzession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constant.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constant.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constant.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constant.Imei)).get();
        //删除SessionSocketHolder
        SessionSocketHolder.remove(userId, appId, clientType, imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId +
                Constant.RedisConstant.UserSessionConstants + userId);
        // 获取redis中对应的内容
        String sessionStr = map.get(clientType.toString());

        if (!StringUtils.isNotBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            // 修改状态为离线状态
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString()+":" + imei, JSONObject.toJSONString(userSession));

        }

        // 封装 messageHeader
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);
        //TODO 通知客户端用户下线
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(appId);
        userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
        userStatusChangeNotifyPack.setUserId(userId);
        MqMessageProducer.sendMessage(userStatusChangeNotifyPack,messageHeader,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());
        nioSocketChannel.close();

        nioSocketChannel.close();
    }
}
