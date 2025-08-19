package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xlike.top.nettydemo.pojo.domain.FriendRequest;

/**
 * 好友申请表 Mapper
 * @author Administrator
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
}
