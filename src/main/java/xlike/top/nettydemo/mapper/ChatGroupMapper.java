package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xlike.top.nettydemo.entity.ChatGroup;

import java.util.List;

/**
 * @author Administrator
 */
//@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 根据用户ID查询其加入的所有群组
     */
    List<ChatGroup> findGroupsByUserId(Long userId);
}