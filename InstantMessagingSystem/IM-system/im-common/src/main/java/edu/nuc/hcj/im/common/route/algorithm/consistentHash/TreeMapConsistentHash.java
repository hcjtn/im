package edu.nuc.hcj.im.common.route.algorithm.consistentHash;

import edu.nuc.hcj.im.common.enums.UserErrorCode;
import edu.nuc.hcj.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.route.algorithm.consistentHash
 * @ClassName : TreeMapConsistentHash.java
 * @createTime : 2023/12/20 20:12
 * @Description :
 */
public class TreeMapConsistentHash extends AbstractConsistentHash{
    private TreeMap<Long,String> map = new TreeMap<>();
    // 需要创建的虚拟节点 防止netty服务器数量过少 导致节点分贝插件过大的情况
    private static final int NODE_SIZE = 2;
    @Override
    protected void add(Long key, String value) {
        // 创建了两个虚拟节点 和一个真实节点
        for (int i = 0; i < NODE_SIZE; i++) {
            map.put(super.hash("node" + key +i),value);
        }
        map.put(key,value);
    }

    // 使用一致性hash算法获取到距离最近的netty服务器  value 用户id
    @Override
    protected String getFirstNodeValue(String value) {
        Long hash = super.hash(value);
        SortedMap<Long, String> longStringSortedMap = map.tailMap(hash);
        if (!longStringSortedMap.isEmpty()) {
            return longStringSortedMap.get(longStringSortedMap.firstKey());
        }
        if (map.size() ==0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return map.firstEntry().getValue();
    }


    @Override
    protected void processBefore() {
        //清空节点 节点是动态的
        map.clear();

    }
}
