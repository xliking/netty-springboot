package xlike.top.nettydemo.service;

import xlike.top.nettydemo.pojo.domain.FriendRequest;

import java.util.List;

public interface FriendRequestService {

    boolean sendRequest(Long fromUserId, Long toUserId, String message);

    List<FriendRequest> getRequests(Long userId);

    boolean handleRequest(Long requestId, boolean agree);
}
