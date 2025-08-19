package xlike.top.nettydemo.service;

import xlike.top.nettydemo.pojo.domain.Friend;
import xlike.top.nettydemo.pojo.domain.FriendGroup;
import xlike.top.nettydemo.pojo.domain.FriendRequest;

import java.util.List;

public interface FriendService {

    boolean sendFriendRequest(Long fromUserId, Long toUserId, String message);

    boolean handleFriendRequest(Long requestId, boolean accept);

    List<Friend> getFriendList(Long userId);

    boolean updateRemark(Long userId, Long friendId, String remark);

    boolean moveFriendToGroup(Long userId, Long friendId, Long groupId);

    List<FriendGroup> getFriendGroups(Long userId);

    boolean addFriendGroup(Long userId, String groupName);

    boolean removeFriendGroup(Long groupId);

    List<FriendRequest> getFriendRequests(Long userId);
}
