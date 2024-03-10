package edu.nuc.hcj.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.proto.MessagePack;
import edu.nuc.hcj.im.common.ClientType;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.DeviceMultiLoginEnum;
import edu.nuc.hcj.im.common.enums.command.SystemCommand;
import edu.nuc.hcj.im.common.model.UserClientDto;
import edu.nuc.hcj.im.tcp.redis.RedisManager;
import edu.nuc.hcj.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.reciver
 * @ClassName : UserLoginMessageListener.java
 * @createTime : 2023/12/19 14:11
 * @Description :  用户登录消息侦听器
 * <p>
 * 多端同步： 1单端登录：一端在线：踢掉除了本clinetType + imel 的设备
 * *        2双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
 * *        3 三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
 * *        4 不做任何处理
 */
@Slf4j
public class UserLoginMessageListener {
    private final static Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    // 登录模型 1 / 2 / 3 / 4
    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }


    //
    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constant.RedisConstant.UserLoginChannel);
        // 监听消息
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String mssage) {
                logger.info("收到用户上线通知：" + mssage);
                // 获取到发送的主题的消息
                UserClientDto userClientDto = JSONObject.parseObject(mssage, UserClientDto.class);
                // 获取到该用户的所有端登陆的channel  n台netty服务器
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(userClientDto.getAppId(), userClientDto.getUserId());
                for (NioSocketChannel channel : nioSocketChannels) {
                    // 单端登录
                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constant.ClientType)).get();
                        String imei = (String) channel.attr(AttributeKey.valueOf(Constant.Imei)).get();
                        // 如果 端类型和imei号不相同使用 单端登录 直接退出
                        if (!(clientType + ":" + imei).equals(userClientDto.getClientType()+":"+userClientDto.getImei())) {
                            // TODO 踢掉客户端
                            // 心跳超时判断 超时 踢出下线
                            /**
                             * 服务端任何时候都不能主动和客户端断开连接 除非客户端主动断开连接
                             */
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constant.UserId)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constant.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }


                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        // 如果是web端 不退出
                        if (userClientDto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }
                        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constant.ClientType)).get();
                        String imei = (String) channel.attr(AttributeKey.valueOf(Constant.Imei)).get();
                        // 已经登录的是web端口
                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }
                        // 如果 端类型和imei号不相同使用 单端登录 直接退出
                        if (!(clientType + ":" + imei).equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                            // TODO 踢掉客户端
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constant.UserId)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constant.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }


                    } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constant.ClientType)).get();
                        String imei = (String) channel.attr(AttributeKey.valueOf(Constant.Imei)).get();
                        // web端不做任何处理

                        // 判断是否为同一种类型客户端
                        Boolean isSameClient = false;
                        // 已经登录的设备是ios或者安卓 并且新登录的设备是ios或者安卓 则是同一种类型客户端
                        if ((clientType == ClientType.IOS.getCode() ||
                                clientType == ClientType.ANDROID.getCode()) &&
                                (userClientDto.getClientType() == ClientType.IOS.getCode() ||
                                        userClientDto.getClientType() == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }
                        //已经登录的设备是mac或者windows 并且新登录的设备是mac或者windows 则是同一种类型客户端
                        if ((clientType == ClientType.MAC.getCode() ||
                                clientType == ClientType.WINDOWS.getCode()) &&
                                (userClientDto.getClientType() == ClientType.MAC.getCode() ||
                                        userClientDto.getClientType() == ClientType.WINDOWS.getCode())) {
                            isSameClient = true;
                        }

                        if (isSameClient && !(clientType + ":" + imei).equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                            // TODO 踢掉客户端
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constant.UserId)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constant.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            channel.writeAndFlush(pack);
                            channel.writeAndFlush(pack);
                        }


                    }
                }


            }
        });
    }

}
