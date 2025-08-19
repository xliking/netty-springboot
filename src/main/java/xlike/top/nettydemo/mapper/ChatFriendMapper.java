package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xlike.top.nettydemo.pojo.domain.Friend;

/**
 * @author Administrator
 */
@Mapper
public interface ChatFriendMapper extends BaseMapper<Friend> {
}
