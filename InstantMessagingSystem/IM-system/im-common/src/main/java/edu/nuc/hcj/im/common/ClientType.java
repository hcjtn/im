package edu.nuc.hcj.im.common;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common
 * @ClassName : ClientType.java
 * @createTime : 2023/12/19 13:33
 * @Description :  客户端类型
 */
public enum ClientType {

    WEBAPI(0,"webApi"),
    WEB(1,"web"),
    IOS(2,"ios"),
    ANDROID(3,"android"),
    WINDOWS(4,"windows"),
    MAC(5,"mac"),
    ;

    private int code;
    private String error;

    ClientType(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }



}
