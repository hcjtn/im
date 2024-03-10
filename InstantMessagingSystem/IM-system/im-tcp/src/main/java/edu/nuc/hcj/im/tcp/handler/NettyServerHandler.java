package edu.nuc.hcj.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import edu.nuc.hcj.im.codec.park.LoginPack;
import edu.nuc.hcj.im.codec.park.message.ChatMessageAck;
import edu.nuc.hcj.im.codec.park.user.LoginAckPack;
import edu.nuc.hcj.im.codec.park.user.UserStatusChangeNotifyPack;
import edu.nuc.hcj.im.codec.proto.Message;
import edu.nuc.hcj.im.codec.proto.MessagePack;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.common.constant.Constant;
import edu.nuc.hcj.im.common.enums.ImConnectStatusEnum;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.enums.command.MessageCommand;
import edu.nuc.hcj.im.common.enums.command.SystemCommand;
import edu.nuc.hcj.im.common.enums.command.UserEventCommand;
import edu.nuc.hcj.im.common.model.UserClientDto;
import edu.nuc.hcj.im.common.model.UserSession;
import edu.nuc.hcj.im.common.model.message.CheckSendMessageReq;
import edu.nuc.hcj.im.tcp.feign.FeignMessageService;
import edu.nuc.hcj.im.tcp.publish.MqMessageProducer;
import edu.nuc.hcj.im.tcp.redis.RedisManager;
import edu.nuc.hcj.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.handler
 * @ClassName : NettyServerHandler.java
 * @createTime : 2023/12/15 20:55
 * @Description :
 * <p>
 * <p>
 * channelHandlerContext.attr(AttributeKey.valueOf(Constant.UserId)).set(loginPack.getUserId());
 * 这行代码的主要作用是设置一个特定的属性值。
 * <p>
 * 具体来说，它使用了Netty框架的AttributeMap.AttributeKey类。AttributeKey是一个用于在Netty管道中存储和获取属性的键。
 * 在这个代码片段中，它使用了valueOf静态方法从字符串常量Constant.UserId创建一个新的AttributeKey实例。
 * <p>
 * 然后，使用这个AttributeKey实例，通过调用attr方法获取与该键关联的属性对象（如果该属性尚不存在，则创建一个新对象）。
 * 最后，调用属性对象的set方法，将loginPack.getUserId()返回的值设置为此属性。
 * <p>
 * 因此，这行代码的主要作用是在channelHandlerContext对象中设置一个名为Constant.UserId的属性，
 * 并将loginPack对象的getUserId()方法的返回值作为其值。
 * <p>
 * 在Netty中，这种机制通常用于存储和传递与网络通道相关的状态信息。例如，可以在一个处理器中设置属性，
 * 然后在后续的处理器中访问该属性。
 * <p>
 * <p>
 * <p>
 * AttributeMap在Netty中用于存储和获取属性。这些属性可以用于在管道中传递状态信息，例如用户ID、会话信息等。
 * <p>
 * 当你在Netty的管道中处理数据时，你可能需要在不同的处理器之间传递上下文信息。AttributeMap提供了一个方便的方式来存储
 * 和获取这些上下文信息。
 * <p>
 * 例如，在一个请求处理管道中，你可能会在某个处理器中设置一个属性（如用户ID），然后在后续的处理器中获取这个属性以进行特定
 * 的处理。
 * <p>
 * 总的来说，AttributeMap在Netty中提供了一个灵活的方式来管理管道中的状态信息，使得处理器之间的协作更加方便和高效。
 * <p>
 * <p>
 * channelHandlerContext.channel()是Netty网络框架中的一个重要方法，用于获取与当前ChannelHandler关联的Channel对象。
 * 这个方法的生命周期通常与ChannelHandler的执行生命周期相关。
 * <p>
 * 在Netty中，当一个网络事件（例如连接建立、数据接收等）发生时，事件会沿着管道（pipeline）传递。管道实际上是一个处理链，
 * 其中包含多个ChannelHandler实例。当事件到达管道时，Netty会按照管道的顺序调用每个ChannelHandler的channelRead()方法。
 * <p>
 * 在channelRead()方法内部，可以通过channelHandlerContext.channel()方法获取当前处理的Channel对象。
 * 这个对象在整个channelRead()方法的执行期间都是有效的，直到事件被完全处理并传递到下一个ChannelHandler。
 * <p>
 * 因此，channelHandlerContext.channel()的生命周期通常与单个网络事件的处理过程相对应。
 * 当事件被处理完毕并传递到下一个ChannelHandler后，channelHandlerContext.channel()方法返回的Channel对象就不再有效。
 * <p>
 * 需要注意的是，在Netty中，Channel对象和ChannelHandlerContext对象都是线程安全的，可以在多个线程之间共享和传递。
 * 但是，具体的生命周期和线程安全行为可能会受到具体使用场景和配置的影响。因此，在使用这些对象时，建议仔细阅读Netty的文档和
 * 相关资料，以确保正确理解和使用它们。
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;

    private String logicUrl;

    private FeignMessageService feignMessageService;

//    private FeignMessageService feignMessageService;

    public NettyServerHandler() {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        设置离线
        SessionSocketHolder.offlineUserSzession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);

    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        channelRead0(ctx, (Message) msg);
    }

    /**
     * 我们的im支持多平台登录
     *
     * @param channelHandlerContext
     * @param message
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        // 获取用户的操作指令
        // 我们的im支持多平台登录
        Integer command = message.getMessageHeader().getCommand();
        //用户登陆操作
        if (command == SystemCommand.LOGIN.getCommand()) {
            // 获取用户id
            /**
             *  在使用fastJson时,对于泛型的反序列化很多场景下都会使用到TypeReference
             *  使用TypeReference可以明确的指定反序列化的类型，具体实现逻辑参考TypeReference的构造函数
             */

            // 获取userid
            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType());

            String userId = loginPack.getUserId();
            /** 为channel设置用户id **/
            channelHandlerContext.channel().
                    attr(AttributeKey.valueOf(Constant.UserId)).set(userId);
            channelHandlerContext.channel().
                    attr(AttributeKey.valueOf(Constant.AppId)).set(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().
                    attr(AttributeKey.valueOf(Constant.ClientType)).set(message.getMessageHeader().getClientType());
            // 分辨是否为同一设备
            channelHandlerContext.channel().
                    attr(AttributeKey.valueOf(Constant.Imei)).set(message.getMessageHeader().getImei());

            //存储用户Session
            UserSession userSession = new UserSession();
            userSession.setUserId(loginPack.getUserId());
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setVersion(message.getMessageHeader().getVersion());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(message.getMessageHeader().getImei());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // TODO 存到redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            // 填写redis key
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId()
                    + Constant.RedisConstant.UserSessionConstants
                    + loginPack.getUserId());
            map.put(message.getMessageHeader().getClientType().toString() + ":" + message.getMessageHeader().getImei()
                    , JSONObject.toJSONString(userSession));


            // 将登录操作存储起来
            SessionSocketHolder.put(loginPack.getUserId(),
                    message.getMessageHeader().getAppId(),
                    message.getMessageHeader().getClientType(),
                    message.getMessageHeader().getImei(),
                    (NioSocketChannel) channelHandlerContext.channel());

            //相同端的数据踢下线  使用redis的发布订阅
            UserClientDto userClientDto = new UserClientDto();
            userClientDto.setImei(message.getMessageHeader().getImei());
            userClientDto.setUserId(userId);
            userClientDto.setClientType(message.getMessageHeader().getClientType());
            userClientDto.setAppId(message.getMessageHeader().getAppId());
            RTopic topic = redissonClient.getTopic(Constant.RedisConstant.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(userClientDto));


            //TODO 将用户的在线状态发送到mq
            UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
            userStatusChangeNotifyPack.setAppId(message.getMessageHeader().getAppId());
            userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userStatusChangeNotifyPack.setUserId(loginPack.getUserId());
            // TODO 发送给mq
            MqMessageProducer.sendMessage(userStatusChangeNotifyPack, message.getMessageHeader(),
                    UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());


            //TODO 通知客户端登录成功  补充登录ack
            MessagePack<LoginAckPack> loginSuccessPack = new MessagePack<LoginAckPack>();
            LoginAckPack loginAckPack = new LoginAckPack();
            loginAckPack.setUserId(loginPack.getUserId());
            loginSuccessPack.setCommand(SystemCommand.LOGINACK.getCommand());
            loginSuccessPack.setData(loginAckPack);
            loginSuccessPack.setImei(message.getMessageHeader().getImei());
            loginSuccessPack.setAppId(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().writeAndFlush(loginSuccessPack);


        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            // 用户退出登录
            System.out.println(channelHandlerContext.channel().
                    attr(AttributeKey.valueOf(Constant.UserId)).get());
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (message.getMessageHeader().getCommand() == SystemCommand.PING.getCommand()) {
            // 心跳包检测
            channelHandlerContext.channel().
                    attr(AttributeKey.valueOf(Constant.ReadTime)).set(System.currentTimeMillis());

            // 进行前端的校验
        } else if (command == MessageCommand.MSG_P2P.getCommand()
                || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            String toId = "";
            CheckSendMessageReq req = new CheckSendMessageReq();
            req.setAppId(message.getMessageHeader().getAppId());
            req.setCommand(message.getMessageHeader().getCommand());
            JSONObject jsonObject =
                    JSONObject.parseObject(JSONObject.toJSONString(message.getMessagePack()));
            String fromId = jsonObject.getString("fromId");
            if (command == MessageCommand.MSG_P2P.getCommand()) {

                toId = jsonObject.getString("toId");
            } else if (command == GroupEventCommand.MSG_GROUP.getCommand()) {
                toId = jsonObject.getString("groupId");
            }
            req.setFromId(fromId);
            req.setToId(toId);
            // 调用校验消息发送方的接口  成功直接转到mq中 如果失败 直接返回ack包 告知用户请求失败
            ResponseVO responseVO = feignMessageService.checkSeandMessage(req);
            if (responseVO.isOk()) {
                // 验证成功 发送消息
                MqMessageProducer.sendMessage(message, command);
            } else {
                Integer ackCommand = 0;
                if (command == MessageCommand.MSG_P2P.getCommand()) {

                    ackCommand = MessageCommand.MSG_ACK.getCommand();
                } else if (command == GroupEventCommand.MSG_GROUP.getCommand()) {
                    ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                }

                // TODO ack
                ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                responseVO.setData(chatMessageAck);
                MessagePack<ResponseVO> ack = new MessagePack<>();
                ack.setData(responseVO);
                ack.setCommand(ackCommand);
                // 传递ack
                channelHandlerContext.channel().writeAndFlush(ack);

            }

        } else {
            MqMessageProducer.sendMessage(message, command);
        }
    }


    // 心跳检测实现逻辑
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
