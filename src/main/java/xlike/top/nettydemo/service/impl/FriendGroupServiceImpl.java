package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.mapper.FriendGroupMapper;
import xlike.top.nettydemo.pojo.domain.FriendGroup;
import xlike.top.nettydemo.service.FriendGroupService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendGroupServiceImpl implements FriendGroupService {

    private final FriendGroupMapper friendGroupMapper;

    @Override
    public boolean addGroup(Long userId, String name) {
        FriendGroup g = new FriendGroup();
        g.setUserId(userId);
        g.setGroupName(name);
        g.setCreateTime(LocalDateTime.now());
        return friendGroupMapper.insert(g) > 0;
    }

    @Override
    public boolean removeGroup(Long groupId) {
        return friendGroupMapper.deleteById(groupId) > 0;
    }

    @Override
    public List<FriendGroup> getGroups(Long userId) {
        return friendGroupMapper.selectList(new LambdaQueryWrapper<FriendGroup>()
                .eq(FriendGroup::getUserId, userId));
    }
}
