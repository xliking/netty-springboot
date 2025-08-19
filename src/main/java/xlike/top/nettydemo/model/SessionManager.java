package xlike.top.nettydemo.model;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 管理用户会话和Channel的单例类
 * @author Administrator
 */
public final class SessionManager {

    /**
     * 单例实例
     */
    private static final SessionManager INSTANCE = new SessionManager();

    /**
     * 用于在 Channel 上附加用户ID的AttributeKey
     */
    public static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    /**
     * 存储用户ID和对应Channel的映射 (在线用户)
     */
    private final Map<Long, Channel> userChannelMap = new ConcurrentHashMap<>();

    /**
     * 存储群组ID和对应ChannelGroup的映射
     */
    private final Map<Long, ChannelGroup> groupChannelGroupMap = new ConcurrentHashMap<>();

    private SessionManager() {}

    /**
     * 获取单例实例
     *
     * @return SessionManager实例
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * 用户登录成功后，绑定用户ID和Channel
     *
     * @param userId  用户ID
     * @param channel 用户的Channel
     */
    public void userLogin(Long userId, Channel channel) {
        channel.attr(USER_ID_KEY).set(userId);
        userChannelMap.put(userId, channel);
    }

    /**
     * 用户登出或连接断开时，解除绑定
     *
     * @param channel 用户的Channel
     */
    public void userLogout(Channel channel) {
        Long userId = channel.attr(USER_ID_KEY).get();
        if (userId != null) {
            userChannelMap.remove(userId);
        }
        // 还需要将该channel从所有群组中移除
        groupChannelGroupMap.values().forEach(group -> group.remove(channel));
    }

    /**
     * 将用户Channel加入到指定群组的ChannelGroup中
     *
     * @param groupId 群组ID
     * @param channel 用户Channel
     */
    public void joinGroup(Long groupId, Channel channel) {
        ChannelGroup channelGroup = groupChannelGroupMap.computeIfAbsent(groupId,
                key -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        channelGroup.add(channel);
    }

    /**
     * 根据用户ID获取其Channel
     *
     * @param userId 用户ID
     * @return Channel，如果用户不在线则返回null
     */
    public Channel getChannelByUserId(Long userId) {
        return userChannelMap.get(userId);
    }

    /**
     * 根据群组ID获取其ChannelGroup
     *
     * @param groupId 群组ID
     * @return ChannelGroup，如果群组不存在或无在线成员则返回null
     */
    public ChannelGroup getChannelGroupByGroupId(Long groupId) {
        return groupChannelGroupMap.get(groupId);
    }

    /**
     * 获取指定用户的Channel (推送服务使用的方法名)
     *
     * @param userId 用户ID
     * @return Channel，如果用户不在线则返回null
     */
    public Channel getUserChannel(Long userId) {
        return userChannelMap.get(userId);
    }

    /**
     * 获取群组的所有Channel集合 (推送服务使用的方法名)
     *
     * @param groupId 群组ID
     * @return Set<Channel>，如果群组不存在则返回空Set
     */
    public Set<Channel> getGroupChannels(Long groupId) {
        ChannelGroup channelGroup = groupChannelGroupMap.get(groupId);
        return channelGroup != null ? channelGroup : Set.of();
    }

    /**
     * 获取所有在线用户的ID集合
     *
     * @return Set<Long> 在线用户ID集合
     */
    public Set<Long> getOnlineUsers() {
        return userChannelMap.keySet();
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return userChannelMap.size();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        Channel channel = userChannelMap.get(userId);
        return channel != null && channel.isActive();
    }

    /**
     * 移除用户离开的群组
     *
     * @param groupId 群组ID
     * @param channel 用户Channel
     */
    public void leaveGroup(Long groupId, Channel channel) {
        ChannelGroup channelGroup = groupChannelGroupMap.get(groupId);
        if (channelGroup != null) {
            channelGroup.remove(channel);
            // 如果群组没有成员了，可以选择移除群组
            if (channelGroup.isEmpty()) {
                groupChannelGroupMap.remove(groupId);
            }
        }
    }

    /**
     * 获取所有活跃的Channel
     *
     * @return Set<Channel> 所有活跃Channel集合
     */
    public Set<Channel> getAllActiveChannels() {
        return Set.copyOf(userChannelMap.values());
    }
}