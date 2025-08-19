package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xlike.top.nettydemo.pojo.domain.FriendGroup;

/**
 * 好友分组表 Mapper
 * @author Administrator
 */
@Mapper
public interface FriendGroupMapper extends BaseMapper<FriendGroup> {
}
