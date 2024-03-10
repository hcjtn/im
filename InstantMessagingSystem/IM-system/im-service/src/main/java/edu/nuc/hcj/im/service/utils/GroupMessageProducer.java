package edu.nuc.hcj.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import edu.nuc.hcj.im.codec.park.group.AddGroupMemberPack;
import edu.nuc.hcj.im.common.ClientType;
import edu.nuc.hcj.im.common.enums.command.Command;
import edu.nuc.hcj.im.common.enums.command.GroupEventCommand;
import edu.nuc.hcj.im.common.model.ClientInfo;
import edu.nuc.hcj.im.service.group.model.req.GroupMemberDto;
import edu.nuc.hcj.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.utils
 * @ClassName : GroupMessageProducer.java
 * @createTime : 2024/1/5 16:41
 * @Description :  群组的消息通知类
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;
    @Autowired
    ImGroupMemberService imGroupMemberService;


    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());

        // 如果是用户入群操作 只需要将消息通知给群主和管理员 和被加入人本身 就好
        // 踢人出群 将消息传递给被踢用户和群主管理员
        //  EXIT_GROUP(2004)
        if (command.equals(GroupEventCommand.ADDED_MEMBER)|| command.equals(GroupEventCommand.DELETED_MEMBER)
        || command.equals(GroupEventCommand.EXIT_GROUP)||command.equals(GroupEventCommand.UPDATED_MEMBER)
        ||command.equals(GroupEventCommand.SPEAK_GOUP_MEMBER)) {
            // 获取群内管理员和群主
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            // 获取被加入群的userID
            AddGroupMemberPack addGroupMemberPack = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();

            // 循环进行通知
            for (GroupMemberDto groupMemberDto : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && groupMemberDto.getMemberId().equals(userId)) {
                    // 发送给该用户的其他端
                    messageProducer.sendToUserExceptClient(groupMemberDto.getMemberId(), command, data, clientInfo);

                } else {
                    // 不是当前用户或者是web端
                    // 将消息传递到所有端口
                    messageProducer.sendToUser(groupMemberDto.getMemberId(), command, data, clientInfo);
                }
            }
            for (String member : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    // 发送给该用户的其他端
                    messageProducer.sendToUserExceptClient(member, command, data, clientInfo);
                } else {
                    // 不是当前用户或者是web端
                    // 将消息传递到所有端口
                    messageProducer.sendToUser(member, command, data, clientInfo);
                }
            }

        } else {
            // 将消息通知给所有的群成员
            for (String memberId : groupMemberId) {
                // 判断是否为 app发起请求  判断当前用户id是否为发起userid
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                    // 发送给该用户的其他端
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);

                } else {
                    // 不是当前用户或者是web端
                    // 将消息传递到所有端口
                    messageProducer.sendToUser(memberId, command, data, clientInfo);
                }
            }
        }
    }


}
