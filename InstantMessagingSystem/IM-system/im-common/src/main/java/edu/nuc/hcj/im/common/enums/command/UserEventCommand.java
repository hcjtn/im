package edu.nuc.hcj.im.common.enums.command;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums.command
 * @ClassName : SystemCommand.java
 * @createTime : 2023/12/16 18:09
 * @Description :   私有协议中的command 获取用户的操作指令
 */
public enum UserEventCommand implements Command {
    //用户修改command 4000
    USER_MODIFY(0xFA0),

    //4001
    USER_ONLINE_STATUS_CHANGE(0xFA1),


    //4004 用户在线状态通知报文
    USER_ONLINE_STATUS_CHANGE_NOTIFY(0xFA4),

    //4005 用户在线状态通知同步报文
    USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC(0xFA5),


    ;

    private int command;

    UserEventCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
