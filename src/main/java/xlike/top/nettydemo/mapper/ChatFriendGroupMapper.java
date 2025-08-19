package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xlike.top.nettydemo.pojo.domain.FriendGroup;

/**
 * 好友分组 Mapper
 * 对应表：chat_friend_group
 * @author Administrator
 */
@Mapper
public interface ChatFriendGroupMapper extends BaseMapper<FriendGroup> {
}
