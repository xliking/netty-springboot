package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.mapper.ChatFriendRequestMapper;
import xlike.top.nettydemo.pojo.domain.FriendRequest;
import xlike.top.nettydemo.service.FriendRequestService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

    private final ChatFriendRequestMapper friendRequestMapper;

    @Override
    public boolean sendRequest(Long fromUserId, Long toUserId, String message) {
        FriendRequest req = new FriendRequest();
        req.setFromUserId(fromUserId);
        req.setToUserId(toUserId);
        req.setMessage(message);
        req.setStatus(0);
        req.setCreateTime(LocalDateTime.now());
        return friendRequestMapper.insert(req) > 0;
    }

    @Override
    public List<FriendRequest> getRequests(Long userId) {
        return friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .orderByDesc(FriendRequest::getCreateTime));
    }

    @Override
    public boolean handleRequest(Long requestId, boolean agree) {
        FriendRequest req = friendRequestMapper.selectById(requestId);
        if (req == null || req.getStatus() != 0) return false;

        req.setStatus(agree ? 1 : 2);
        req.setHandleTime(LocalDateTime.now());
        return friendRequestMapper.updateById(req) > 0;
    }
}
