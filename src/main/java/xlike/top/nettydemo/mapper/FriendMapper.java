package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xlike.top.nettydemo.pojo.domain.Friend;

/**
 * 好友表 Mapper
 * @author Administrator
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
}
