package xlike.top.nettydemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xlike.top.nettydemo.pojo.domain.ChatGroup;

import java.util.List;

@Mapper
public interface ChatGroupMapper {

    /**
     * 根据用户ID获取群组列表
     */
    List<ChatGroup> findGroupsByUserId(@Param("userId") Long userId);

    /**
     * 判断用户是否在群组内
     */
    int checkUserInGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 获取群组内的所有成员ID
     */
    List<Long> findUserIdsByGroupId(@Param("groupId") Long groupId);

    /**
     * 统计群组成员数量
     */
    int countUsersInGroup(@Param("groupId") Long groupId);

    /**
     * 从群组中移除用户
     */
    int removeUserFromGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 往群组中添加用户
     */
    int addUserToGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
