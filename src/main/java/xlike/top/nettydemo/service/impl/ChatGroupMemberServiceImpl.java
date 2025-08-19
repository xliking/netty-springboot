package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.mapper.ChatGroupMemberMapper;
import xlike.top.nettydemo.pojo.domain.ChatGroupMember;
import xlike.top.nettydemo.service.ChatGroupMemberService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群组成员业务实现类
 * 使用 MyBatis-Plus 代替 XML 配置
 *
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
public class ChatGroupMemberServiceImpl implements ChatGroupMemberService {

    private final ChatGroupMemberMapper chatGroupMemberMapper;

    /**
     * 判断用户是否在群组内
     */
    @Override
    public boolean isUserInGroup(Long groupId, Long userId) {
        Long count = chatGroupMemberMapper.selectCount(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId)
        );
        return count > 0;
    }

    /**
     * 获取群组内所有成员ID
     */
    @Override
    public List<Long> getGroupMemberIds(Long groupId) {
        return chatGroupMemberMapper.selectList(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
        ).stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
    }

    /**
     * 获取群组成员数量
     */
    @Override
    public int getGroupMemberCount(Long groupId) {
        return Math.toIntExact(chatGroupMemberMapper.selectCount(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
        ));
    }

    /**
     * 从群组中移除一个用户
     */
    @Override
    public boolean removeUserFromGroup(Long groupId, Long userId) {
        int rows = chatGroupMemberMapper.delete(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId)
        );
        return rows > 0;
    }

    /**
     * 往群组中添加新用户
     */
    @Override
    public boolean addUserToGroup(Long groupId, Long userId) {
        ChatGroupMember member = new ChatGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setJoinTime(LocalDateTime.now());
        return chatGroupMemberMapper.insert(member) > 0;
    }
}
