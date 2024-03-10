package edu.nuc.hcj.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.common.model
 * @ClassName : SyncResp.java
 * @createTime : 2024/1/16 9:27
 * @Description :
 */
@Data
public class SyncResp<T> {

    // 服务端最大的色情、
    private Long maxSequence;
    // 是否拉取结束
    private boolean isCompleted;
    // 数据集
    private List<T> dataList;

}