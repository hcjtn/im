package edu.nuc.hcj.im.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.nuc.hcj.im.service.conversation.dao.ImConversationSetEntity;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.conversation.dao.mapper
 * @ClassName : ImConversationSetMapper.java
 * @createTime : 2024/1/15 10:54
 * @Description :
 */
@Repository
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {

    @Update(" update im_conversation_set set readed_sequence = #{readedSequence},sequence = #{sequence} " +
            " where conversation_id = #{conversationId} and app_id = #{appId} AND readed_sequence < #{readedSequence}")
    public void readMark(ImConversationSetEntity imConversationSetEntity);

    @Select(" select max(sequence) from im_conversation_set where app_id = #{appId} AND from_id = #{userId} ")
    Long geConversationSetMaxSeq(Integer appId, String userId);
}
