package xlike.top.nettydemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.model.ChatMessage;
import xlike.top.nettydemo.model.SessionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * WebSocket 服务端主动推送服务
 * 提供各种推送消息给客户端的方法
 * @author Administrator
 */
@Slf4j
@Service
public class WebSocketPushService {

    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;

    public WebSocketPushService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * 推送消息给指定用户
     * @param userId 用户ID
     * @param message 聊天消息
     * @return 是否推送成功
     */
    public boolean pushMessageToUser(Long userId, ChatMessage message) {
        try {
            Channel channel = sessionManager.getUserChannel(userId);
            if (channel != null && channel.isActive()) {
                message.setMessageId(UUID.randomUUID().toString());
                message.setSendTime(LocalDateTime.now());
                String json = objectMapper.writeValueAsString(message);
                channel.writeAndFlush(new TextWebSocketFrame(json));
                log.info("Message pushed to user {}: {}", userId, message.getAction());
                return true;
            } else {
                log.warn("User {} is not online, message not sent", userId);
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for user {}", userId, e);
            return false;
        }
    }

    /**
     * 推送通知消息给指定用户
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @return 是否推送成功
     */
    public boolean pushNotificationToUser(Long userId, String title, String content) {
        ChatMessage notification = new ChatMessage();
        notification.setAction(ChatMessage.ActionType.PUSH_NOTIFICATION);
        notification.setMessageType(ChatMessage.MessageType.SYSTEM);
        notification.setContent(content);
        notification.setSenderNickname(title);
        return pushMessageToUser(userId, notification);
    }

    /**
     * 推送系统消息给指定用户
     * @param userId 用户ID
     * @param content 系统消息内容
     * @return 是否推送成功
     */
    public boolean pushSystemMessageToUser(Long userId, String content) {
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setAction(ChatMessage.ActionType.PUSH_SYSTEM_MESSAGE);
        systemMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
        systemMessage.setContent(content);
        systemMessage.setSenderNickname("系统消息");
        return pushMessageToUser(userId, systemMessage);
    }

    /**
     * 推送数据给指定用户
     * @param userId 用户ID
     * @param action 动作类型
     * @param data 数据内容
     * @return 是否推送成功
     */
    public boolean pushDataToUser(Long userId, ChatMessage.ActionType action, Object data) {
        ChatMessage message = new ChatMessage();
        message.setAction(action);
        message.setData(data);
        return pushMessageToUser(userId, message);
    }

    /**
     * 广播消息给所有在线用户
     * @param message 聊天消息
     * @return 成功推送的用户数量
     */
    public int broadcastMessageToAllUsers(ChatMessage message) {
        Set<Long> onlineUsers = sessionManager.getOnlineUsers();
        int successCount = 0;
        
        message.setMessageId(UUID.randomUUID().toString());
        message.setSendTime(LocalDateTime.now());
        
        for (Long userId : onlineUsers) {
            try {
                Channel channel = sessionManager.getUserChannel(userId);
                if (channel != null && channel.isActive()) {
                    String json = objectMapper.writeValueAsString(message);
                    channel.writeAndFlush(new TextWebSocketFrame(json));
                    successCount++;
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to broadcast message to user {}", userId, e);
            }
        }
        
        log.info("Broadcast message sent to {}/{} users", successCount, onlineUsers.size());
        return successCount;
    }

    /**
     * 广播通知给所有在线用户
     * @param title 通知标题
     * @param content 通知内容
     * @return 成功推送的用户数量
     */
    public int broadcastNotificationToAllUsers(String title, String content) {
        ChatMessage notification = new ChatMessage();
        notification.setAction(ChatMessage.ActionType.PUSH_BROADCAST);
        notification.setMessageType(ChatMessage.MessageType.SYSTEM);
        notification.setContent(content);
        notification.setSenderNickname(title);
        return broadcastMessageToAllUsers(notification);
    }

    /**
     * 推送消息给群组内所有用户
     * @param groupId 群组ID
     * @param message 聊天消息
     * @return 成功推送的用户数量
     */
    public int pushMessageToGroup(Long groupId, ChatMessage message) {
        Set<Channel> groupChannels = sessionManager.getGroupChannels(groupId);
        int successCount = 0;
        
        message.setMessageId(UUID.randomUUID().toString());
        message.setSendTime(LocalDateTime.now());
        message.setGroupId(groupId);
        
        for (Channel channel : groupChannels) {
            if (channel.isActive()) {
                try {
                    String json = objectMapper.writeValueAsString(message);
                    channel.writeAndFlush(new TextWebSocketFrame(json));
                    successCount++;
                } catch (JsonProcessingException e) {
                    log.error("Failed to send message to group {} channel", groupId, e);
                }
            }
        }
        
        log.info("Message sent to {}/{} users in group {}", successCount, groupChannels.size(), groupId);
        return successCount;
    }

    /**
     * 推送群组通知
     * @param groupId 群组ID
     * @param title 通知标题
     * @param content 通知内容
     * @return 成功推送的用户数量
     */
    public int pushGroupNotification(Long groupId, String title, String content) {
        ChatMessage notification = new ChatMessage();
        notification.setAction(ChatMessage.ActionType.PUSH_GROUP_UPDATE);
        notification.setMessageType(ChatMessage.MessageType.SYSTEM);
        notification.setContent(content);
        notification.setSenderNickname(title);
        return pushMessageToGroup(groupId, notification);
    }

    /**
     * 推送在线用户列表给指定用户
     * @param userId 用户ID
     * @return 是否推送成功
     */
    public boolean pushOnlineUsersToUser(Long userId) {
        Set<Long> onlineUsers = sessionManager.getOnlineUsers();
        ChatMessage message = new ChatMessage();
        message.setAction(ChatMessage.ActionType.PUSH_ONLINE_USERS);
        message.setData(onlineUsers);
        return pushMessageToUser(userId, message);
    }

    /**
     * 推送用户状态变更消息
     * @param userId 状态变更的用户ID
     * @param status 用户状态 (online/offline/busy等)
     * @param excludeUserId 排除的用户ID（通常是状态变更的用户自己）
     * @return 成功推送的用户数量
     */
    public int pushUserStatusChange(Long userId, String status, Long excludeUserId) {
        ChatMessage statusMessage = new ChatMessage();
        statusMessage.setAction(ChatMessage.ActionType.PUSH_USER_STATUS);
        statusMessage.setSenderId(userId);
        statusMessage.setContent(status);
        
        Set<Long> onlineUsers = sessionManager.getOnlineUsers();
        int successCount = 0;
        
        for (Long targetUserId : onlineUsers) {
            if (!targetUserId.equals(excludeUserId)) {
                if (pushMessageToUser(targetUserId, statusMessage)) {
                    successCount++;
                }
            }
        }
        
        log.info("User {} status change ({}) sent to {} users", userId, status, successCount);
        return successCount;
    }

    /**
     * 推送指定用户列表
     * @param userIds 用户ID列表
     * @param message 聊天消息
     * @return 成功推送的用户数量
     */
    public int pushMessageToUsers(List<Long> userIds, ChatMessage message) {
        int successCount = 0;
        
        message.setMessageId(UUID.randomUUID().toString());
        message.setSendTime(LocalDateTime.now());
        
        for (Long userId : userIds) {
            if (pushMessageToUser(userId, message)) {
                successCount++;
            }
        }
        
        log.info("Message sent to {}/{} specified users", successCount, userIds.size());
        return successCount;
    }

    /**
     * 获取当前在线用户数量
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return sessionManager.getOnlineUsers().size();
    }

    /**
     * 获取当前在线用户列表
     * @return 在线用户ID集合
     */
    public Set<Long> getOnlineUsers() {
        return sessionManager.getOnlineUsers();
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        Channel channel = sessionManager.getUserChannel(userId);
        return channel != null && channel.isActive();
    }
}