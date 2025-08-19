package xlike.top.nettydemo.service.impl;

import org.springframework.stereotype.Service;
import xlike.top.nettydemo.mapper.ChatGroupMapper;
import xlike.top.nettydemo.service.ChatGroupService;

import java.util.List;

/**
 * 群组业务实现类
 * 封装群组相关操作（增删查）
 * @author Administrator
 */
@Service
public class ChatGroupServiceImpl implements ChatGroupService {

    private final ChatGroupMapper chatGroupMapper;

    public ChatGroupServiceImpl(ChatGroupMapper chatGroupMapper) {
        this.chatGroupMapper = chatGroupMapper;
    }

    /**
     * 判断用户是否在群组内
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return true 表示在群组内，false 表示不在
     */
    @Override
    public boolean isUserInGroup(Long groupId, Long userId) {
        int count = chatGroupMapper.checkUserInGroup(groupId, userId);
        return count > 0;
    }

    /**
     * 获取群组内所有成员ID
     *
     * @param groupId 群组ID
     * @return 成员ID列表
     */
    @Override
    public List<Long> getGroupMemberIds(Long groupId) {
        return chatGroupMapper.findUserIdsByGroupId(groupId);
    }

    /**
     * 获取群组成员数量
     *
     * @param groupId 群组ID
     * @return 成员数量
     */
    @Override
    public int getGroupMemberCount(Long groupId) {
        return chatGroupMapper.countUsersInGroup(groupId);
    }

    /**
     * 从群组中移除一个用户
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 是否成功
     */
    @Override
    public boolean removeUserFromGroup(Long groupId, Long userId) {
        int rows = chatGroupMapper.removeUserFromGroup(groupId, userId);
        return rows > 0;
    }

    /**
     * 往群组中添加新用户
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 是否成功
     */
    @Override
    public boolean addUserToGroup(Long groupId, Long userId) {
        int rows = chatGroupMapper.addUserToGroup(groupId, userId);
        return rows > 0;
    }
}
