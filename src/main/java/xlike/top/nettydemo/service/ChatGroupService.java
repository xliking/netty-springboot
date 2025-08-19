package xlike.top.nettydemo.service;

import java.util.List;

/**
 * 群组业务接口
 * 提供群组成员管理相关功能
 *
 * @author
 */
public interface ChatGroupService {

    /**
     * 判断用户是否在群组内
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return true 表示在群组内，false 表示不在
     */
    boolean isUserInGroup(Long groupId, Long userId);

    /**
     * 获取群组内所有成员ID
     *
     * @param groupId 群组ID
     * @return 成员ID列表
     */
    List<Long> getGroupMemberIds(Long groupId);

    /**
     * 获取群组成员数量
     *
     * @param groupId 群组ID
     * @return 成员数量
     */
    int getGroupMemberCount(Long groupId);

    /**
     * 从群组中移除一个用户
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 是否移除成功
     */
    boolean removeUserFromGroup(Long groupId, Long userId);

    /**
     * 往群组中添加新用户
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 是否添加成功
     */
    boolean addUserToGroup(Long groupId, Long userId);
}
