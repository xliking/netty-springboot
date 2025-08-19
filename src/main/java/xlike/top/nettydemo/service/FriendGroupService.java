package xlike.top.nettydemo.service;

import xlike.top.nettydemo.pojo.domain.FriendGroup;
import java.util.List;

public interface FriendGroupService {

    boolean addGroup(Long userId, String name);

    boolean removeGroup(Long groupId);

    List<FriendGroup> getGroups(Long userId);
}
