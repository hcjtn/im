package edu.nuc.hcj.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.nuc.hcj.im.common.ResponseVO;
import edu.nuc.hcj.im.service.friendship.dao.ImFriendShipGroupMemberEntity;
import edu.nuc.hcj.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.friendship.dao.mapper
 * @ClassName : ImFriendShipGroupMemberMapper.java
 * @createTime : 2023/12/12 12:38
 * @Description :
 */
@Mapper
public interface ImFriendShipGroupMemberMapper extends BaseMapper<ImFriendShipGroupMemberEntity> {

}
