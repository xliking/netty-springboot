package xlike.top.nettydemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xlike.top.nettydemo.pojo.domain.ChatGroup;

import java.util.List;

/**
 * @author Administrator
 */
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 根据用户ID查询其加入的所有群组
     */
    List<ChatGroup> findGroupsByUserId(Long userId);
}