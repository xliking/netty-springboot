package xlike.top.nettydemo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.mapper.ChatGroupMapper;
import xlike.top.nettydemo.pojo.domain.ChatGroup;
import xlike.top.nettydemo.service.ChatGroupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGroupServiceImpl implements ChatGroupService {

    private final ChatGroupMapper chatGroupMapper;

    @Override
    public List<ChatGroup> getGroupsForUser(Long userId) {
        return chatGroupMapper.findGroupsByUserId(userId);
    }

    @Override
    public boolean createGroup(ChatGroup group) {
        group.setCreateTime(LocalDateTime.now());
        return chatGroupMapper.insert(group) > 0;
    }

    @Override
    public boolean deleteGroup(Long groupId) {
        return chatGroupMapper.deleteById(groupId) > 0;
    }
}
