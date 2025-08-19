package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xlike.top.nettydemo.pojo.domain.FriendRequest;

/**
 * 好友请求 Mapper
 * 对应表：chat_friend_request
 * @author Administrator
 */
@Mapper
public interface ChatFriendRequestMapper extends BaseMapper<FriendRequest> {
}
