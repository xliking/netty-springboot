package xlike.top.nettydemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.pojo.domain.Message;
import xlike.top.nettydemo.enums.MessageType;
import xlike.top.nettydemo.model.SessionManager;
import xlike.top.nettydemo.model.WsEnvelope;
import xlike.top.nettydemo.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 需要自己处理后续保存数据入库的功能
 * WebSocket 服务端主动推送服务
 * 提供各种推送消息给客户端的方法
 *
 * @author Administrator
 */
@Slf4j
@Service
public class WebSocketPushService {

    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;
    private final IdGenerator idGenerator = new IdGenerator(1, 1);

    public WebSocketPushService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * 核心统一封装方法
     */
    private <T> void sendResponse(Channel channel, WsEnvelope.ActionType action, T data) throws JsonProcessingException {
        try {
            R<T> response = R.ok(action.name(), data);
            String json = objectMapper.writeValueAsString(response);
            channel.writeAndFlush(new TextWebSocketFrame(json));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response : {}", e.getMessage());
        }
    }

    public <T> void sendResponse(ChannelHandlerContext ctx, WsEnvelope.ActionType action, T data) {
        try {
            R<T> response = R.ok(action.name(), data);
            String json = objectMapper.writeValueAsString(response);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
        } catch (JsonProcessingException e) {
            sendError(ctx, "发送消息失败，未知异常");
        }
    }

    public void sendError(ChannelHandlerContext ctx, String errorMsg) {
        try {
            R<String> response = R.fail(errorMsg);
            String json = objectMapper.writeValueAsString(response);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
        }
    }


    /**
     * 推送消息给指定用户
     */
    public boolean pushMessageToUser(Long userId, WsEnvelope<?> envelope) {
        try {
            Channel channel = sessionManager.getUserChannel(userId);
            if (channel != null && channel.isActive()) {
                if (envelope.getData() instanceof Message msg) {
                    msg.setId(idGenerator.nextId());
                    msg.setSendTime(LocalDateTime.now());
                }
                sendResponse(channel, envelope.getAction(), envelope.getData());
                return true;
            } else {
                log.warn("User {} is not online, message not sent", userId);
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for user {},{}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 推送通知消息给指定用户
     */
    public boolean pushNotificationToUser(Long userId, String title, String content) {
        Message notification = new Message();
        notification.setMessageType(MessageType.SYSTEM);
        notification.setContent(content);
        notification.setSenderNickname(title);

        WsEnvelope<Message> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_NOTIFICATION, notification);
        return pushMessageToUser(userId, envelope);
    }

    /**
     * 推送系统消息给指定用户
     */
    public boolean pushSystemMessageToUser(Long userId, String content) {
        Message systemMessage = new Message();
        systemMessage.setMessageType(MessageType.SYSTEM);
        systemMessage.setContent(content);
        systemMessage.setSenderNickname("系统消息");

        WsEnvelope<Message> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_SYSTEM_MESSAGE, systemMessage);
        return pushMessageToUser(userId, envelope);
    }

    /**
     * 推送任意数据给指定用户
     */
    public boolean pushDataToUser(Long userId, WsEnvelope.ActionType action, Object data) {
        WsEnvelope<Object> envelope = new WsEnvelope<>(action, data);
        return pushMessageToUser(userId, envelope);
    }

    /**
     * 广播消息给所有在线用户
     */
    public int broadcastMessageToAllUsers(WsEnvelope<?> envelope) {
        Set<Long> onlineUsers = sessionManager.getOnlineUsers();
        int successCount = 0;

        if (envelope.getData() instanceof Message msg) {
            msg.setId(idGenerator.nextId());
            msg.setSendTime(LocalDateTime.now());
        }

        for (Long userId : onlineUsers) {
            try {
                Channel channel = sessionManager.getUserChannel(userId);
                if (channel != null && channel.isActive()) {
                    sendResponse(channel, envelope.getAction(), envelope.getData());
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
     * 推送消息给群组
     */
    public int pushMessageToGroup(Long groupId, WsEnvelope<?> envelope) {
        Set<Channel> groupChannels = sessionManager.getGroupChannels(groupId);
        int successCount = 0;

        if (envelope.getData() instanceof Message msg) {
            msg.setId(idGenerator.nextId());
            msg.setSendTime(LocalDateTime.now());
            msg.setGroupId(groupId);
        }

        for (Channel channel : groupChannels) {
            if (channel.isActive()) {
                try {
                    sendResponse(channel, envelope.getAction(), envelope.getData());
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
     */
    public int pushGroupNotification(Long groupId, String title, String content) {
        Message notification = new Message();
        notification.setMessageType(MessageType.SYSTEM);
        notification.setContent(content);
        notification.setSenderNickname(title);

        WsEnvelope<Message> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_GROUP_UPDATE, notification);
        return pushMessageToGroup(groupId, envelope);
    }

    /**
     * 推送在线用户列表
     */
    public boolean pushOnlineUsersToUser(Long userId) {
        Set<Long> onlineUsers = sessionManager.getOnlineUsers();
        WsEnvelope<Set<Long>> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_ONLINE_USERS, onlineUsers);
        return pushMessageToUser(userId, envelope);
    }

    /**
     * 推送用户状态变更
     */
    public int pushUserStatusChange(Long userId, String status, Long excludeUserId) {
        Message statusMessage = new Message();
        statusMessage.setSenderId(userId);
        statusMessage.setContent(status);

        WsEnvelope<Message> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_USER_STATUS, statusMessage);

        Set<Long> onlineUsers = sessionManager.getOnlineUsers();
        int successCount = 0;

        for (Long targetUserId : onlineUsers) {
            if (!targetUserId.equals(excludeUserId)) {
                if (pushMessageToUser(targetUserId, envelope)) {
                    successCount++;
                }
            }
        }

        log.info("User {} status change ({}) sent to {} users", userId, status, successCount);
        return successCount;
    }

    /**
     * 推送消息给指定用户列表
     */
    public int pushMessageToUsers(List<Long> userIds, WsEnvelope<?> envelope) {
        int successCount = 0;

        if (envelope.getData() instanceof Message msg) {
            msg.setId(idGenerator.nextId());
            msg.setSendTime(LocalDateTime.now());
        }

        for (Long userId : userIds) {
            if (pushMessageToUser(userId, envelope)) {
                successCount++;
            }
        }

        log.info("Message sent to {}/{} specified users", successCount, userIds.size());
        return successCount;
    }

    /**
     * 获取当前在线用户数量
     */
    public int getOnlineUserCount() {
        return sessionManager.getOnlineUsers().size();
    }

    /**
     * 获取当前在线用户列表
     */
    public Set<Long> getOnlineUsers() {
        return sessionManager.getOnlineUsers();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        Channel channel = sessionManager.getUserChannel(userId);
        return channel != null && channel.isActive();
    }
}
