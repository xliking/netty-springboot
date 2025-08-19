package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xlike.top.nettydemo.pojo.domain.ChatGroup;

import java.util.List;

/**
 * @author Administrator
 */
@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 根据用户ID获取群组列表
     */
    List<ChatGroup> findGroupsByUserId(@Param("userId") Long userId);


}
