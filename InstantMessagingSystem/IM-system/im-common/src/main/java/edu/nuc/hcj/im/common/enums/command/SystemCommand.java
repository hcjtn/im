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
public enum SystemCommand implements Command {
    //心跳 9999
    PING(0x270f),
    /**
     * 登录 9000
     */
    LOGIN(0x2328),

    //登录ack  9001
    LOGINACK(0x2329),

    //登出  9003
    LOGOUT(0x232b),

    //下线通知 用于多端互斥  9002
    MUTUALLOGIN(0x232a),

    ;

    private int command;

    SystemCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
