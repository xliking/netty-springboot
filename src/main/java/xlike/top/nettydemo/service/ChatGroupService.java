package xlike.top.nettydemo.service;

import xlike.top.nettydemo.pojo.domain.ChatGroup;

import java.util.List;

/**
 * 群组相关业务接口
 */
public interface ChatGroupService {

    /**
     * 获取某个用户加入的所有群组
     */
    List<ChatGroup> getGroupsForUser(Long userId);

    /**
     * 创建群组
     */
    boolean createGroup(ChatGroup group);

    /**
     * 删除群组
     */
    boolean deleteGroup(Long groupId);
}
