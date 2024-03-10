package edu.nuc.hcj.im.tcp.reciver.process;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.tcp.reciver
 * @ClassName : ProcessFactory.java
 * @createTime : 2024/1/5 18:09
 * @Description : 工厂类 处理消息通知
 *
 */
public class ProcessFactory {
    private static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess(Integer command) {
        return defaultProcess;
    }
}
