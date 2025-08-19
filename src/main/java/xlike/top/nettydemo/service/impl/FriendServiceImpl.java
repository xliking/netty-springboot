package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xlike.top.nettydemo.mapper.ChatFriendMapper;
import xlike.top.nettydemo.mapper.ChatFriendRequestMapper;
import xlike.top.nettydemo.mapper.ChatFriendGroupMapper;
import xlike.top.nettydemo.pojo.domain.Friend;
import xlike.top.nettydemo.pojo.domain.FriendGroup;
import xlike.top.nettydemo.pojo.domain.FriendRequest;
import xlike.top.nettydemo.service.FriendService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友业务实现类
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final ChatFriendMapper friendMapper;
    private final ChatFriendGroupMapper friendGroupMapper;
    private final ChatFriendRequestMapper friendRequestMapper;

    /**
     * 发送好友申请
     */
    @Override
    public boolean sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        // 0: 待处理
        request.setStatus(0);
        request.setCreateTime(LocalDateTime.now());
        return friendRequestMapper.insert(request) > 0;
    }

    /**
     * 处理好友申请
     */
    @Override
    @Transactional
    public boolean handleFriendRequest(Long requestId, boolean accept) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || request.getStatus() != 0) {
            return false;
        }
        // 1: 已同意, 2: 已拒绝
        request.setStatus(accept ? 1 : 2);
        request.setHandleTime(LocalDateTime.now());
        friendRequestMapper.updateById(request);

        if (accept) {
            // 互相加好友
            Friend f1 = new Friend();
            f1.setUserId(request.getFromUserId());
            f1.setFriendId(request.getToUserId());
            f1.setCreateTime(LocalDateTime.now());
            friendMapper.insert(f1);

            Friend f2 = new Friend();
            f2.setUserId(request.getToUserId());
            f2.setFriendId(request.getFromUserId());
            f2.setCreateTime(LocalDateTime.now());
            friendMapper.insert(f2);
        }
        return true;
    }

    /**
     * 获取好友列表
     */
    @Override
    public List<Friend> getFriendList(Long userId) {
        return friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId));
    }

    /**
     * 修改好友备注
     */
    @Override
    public boolean updateRemark(Long userId, Long friendId, String remark) {
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId));
        if (friend == null) {
            return false;
        }
        friend.setRemark(remark);
        return friendMapper.updateById(friend) > 0;
    }

    /**
     * 移动好友到分组
     */
    @Override
    public boolean moveFriendToGroup(Long userId, Long friendId, Long groupId) {
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId));
        if (friend == null) {
            return false;
        }
        friend.setGroupId(groupId);
        return friendMapper.updateById(friend) > 0;
    }

    @Override
    public List<FriendGroup> getFriendGroups(Long userId) {
        return friendGroupMapper.selectList(new LambdaQueryWrapper<FriendGroup>()
                .eq(FriendGroup::getUserId, userId));
    }

    @Override
    public boolean addFriendGroup(Long userId, String groupName) {
        FriendGroup group = new FriendGroup();
        group.setUserId(userId);
        group.setGroupName(groupName);
        group.setCreateTime(LocalDateTime.now());
        return friendGroupMapper.insert(group) > 0;
    }

    @Override
    public boolean removeFriendGroup(Long groupId) {
        return friendGroupMapper.deleteById(groupId) > 0;
    }

    @Override
    public List<FriendRequest> getFriendRequests(Long userId) {
        return friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .orderByDesc(FriendRequest::getCreateTime));
    }

}
