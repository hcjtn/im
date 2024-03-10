package edu.nuc.hcj.im.common.enums.command;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.enums.command
 * @ClassName : CommandType.java
 * @createTime : 2024/1/7 15:51
 * @Description :
 */
public enum CommandType {

    USER("4"),

    FRIEND("3"),

    GROUP("2"),

    MESSAGE("1"),

    ;

    private String commandType;

    public String getCommandType() {
        return commandType;
    }

    CommandType(String commandType) {
        this.commandType = commandType;
    }

    public static CommandType getCommandType(String ordinal) {
        for (int i = 0; i < CommandType.values().length; i++) {
            if (CommandType.values()[i].getCommandType().equals(ordinal)) {
                return CommandType.values()[i];
            }
        }
        return null;
    }

}
