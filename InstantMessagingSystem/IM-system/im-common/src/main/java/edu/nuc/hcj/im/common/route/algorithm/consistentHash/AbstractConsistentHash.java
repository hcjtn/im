package edu.nuc.hcj.im.common.route.algorithm.consistentHash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route.algorithm.consistentHash
 * @ClassName : AbstractConsistentHash.java
 * @createTime : 2023/12/20 19:54
 * @Description : 一致哈希算法 抽象类
 */
public abstract class AbstractConsistentHash {
    // add  将用户id和 选择的netty服务器的ip和port存储进去
    protected abstract void add(Long key,String value);

    //sort
    protected void sort(){}


    //获取节点 get
    protected abstract String getFirstNodeValue(String value);

    /**
     * 处理之前事件
     */
    protected abstract void processBefore();

    /**
     * 传入节点列表以及客户端信息获取一个服务节点
     * @param values  对应的im(im-tcp)服务器节点
     * @param key  用户id
     * @return
     */
    public synchronized String process(List<String> values, String key){
        processBefore();
        for (String value : values) {
            add(hash(value), value);
        }
        sort();
        return getFirstNodeValue(key) ;
    }

    // 计算hash
    //hash
    /**
     * hash 运算
     * @param value
     * @return
     */
    public Long hash(String value){
        MessageDigest md5;
        try {
            //定义一个MessageDigest类型的变量md5，用于获取MD5实例
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        // 重置MD5实例，清除之前设置的值或状态。
        md5.reset();
        byte[] keyBytes = null;
        try {
            keyBytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + value, e);
        }
        // 使用字节数组更新MD5实例
        md5.update(keyBytes);
        // 获取MD5实例的摘要

        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        // 将摘要的四个字节转换为一个32位的哈希码：
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        //将32位的哈希码截断为32位。使用位运算符&将所有超过32位的位设置为0。
        long truncateHashCode = hashCode & 0xffffffffL;
        return truncateHashCode;
    }
}
