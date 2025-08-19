package xlike.top.nettydemo.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xlike.top.nettydemo.pojo.domain.ChatMessage;
import xlike.top.nettydemo.enums.MessageType;
import xlike.top.nettydemo.model.WsEnvelope;
import xlike.top.nettydemo.service.WebSocketPushService;

import java.util.List;
import java.util.Set;

/**
 * WebSocket 推送测试控制器
 * @author Administrator
 */
@Slf4j
@RestController
@RequestMapping("/api/push")
@CrossOrigin("*")
public class WebSocketPushController {

    private final WebSocketPushService pushService;

    public WebSocketPushController(WebSocketPushService pushService) {
        this.pushService = pushService;
    }

    /**
     * 推送消息给指定用户
     */
    @PostMapping("/user/{userId}")
    public ApiResponse pushToUser(@PathVariable Long userId, @RequestBody PushMessageRequest request) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageType(MessageType.TEXT);
        chatMessage.setContent(request.getContent());
        chatMessage.setSenderNickname(request.getSenderName());

        WsEnvelope<ChatMessage> envelope = new WsEnvelope<>();
        envelope.setAction(WsEnvelope.ActionType.PUSH_MESSAGE);
        envelope.setData(chatMessage);

        boolean success = pushService.pushMessageToUser(userId, envelope);
        return success ? ApiResponse.success("消息推送成功") : ApiResponse.error("用户不在线或推送失败");
    }

    /**
     * 推送通知给指定用户
     */
    @PostMapping("/notification/user/{userId}")
    public ApiResponse pushNotificationToUser(@PathVariable Long userId, @RequestBody NotificationRequest request) {
        boolean success = pushService.pushNotificationToUser(userId, request.getTitle(), request.getContent());
        return success ? ApiResponse.success("通知推送成功") : ApiResponse.error("用户不在线或推送失败");
    }

    /**
     * 推送系统消息给指定用户
     */
    @PostMapping("/system/user/{userId}")
    public ApiResponse pushSystemMessageToUser(@PathVariable Long userId, @RequestBody SystemMessageRequest request) {
        boolean success = pushService.pushSystemMessageToUser(userId, request.getContent());
        return success ? ApiResponse.success("系统消息推送成功") : ApiResponse.error("用户不在线或推送失败");
    }

    /**
     * 广播消息给所有在线用户
     */
    @PostMapping("/broadcast")
    public ApiResponse broadcastToAllUsers(@RequestBody PushMessageRequest request) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageType(MessageType.TEXT);
        chatMessage.setContent(request.getContent());
        chatMessage.setSenderNickname(request.getSenderName());

        WsEnvelope<ChatMessage> envelope = new WsEnvelope<>();
        envelope.setAction(WsEnvelope.ActionType.PUSH_BROADCAST);
        envelope.setData(chatMessage);

        int count = pushService.broadcastMessageToAllUsers(envelope);
        return ApiResponse.success("广播消息已发送给 " + count + " 个用户");
    }

    /**
     * 推送消息给群组
     */
    @PostMapping("/group/{groupId}")
    public ApiResponse pushToGroup(@PathVariable Long groupId, @RequestBody PushMessageRequest request) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageType(MessageType.TEXT);
        chatMessage.setContent(request.getContent());
        chatMessage.setSenderNickname(request.getSenderName());

        WsEnvelope<ChatMessage> envelope = new WsEnvelope<>();
        envelope.setAction(WsEnvelope.ActionType.PUSH_MESSAGE);
        envelope.setData(chatMessage);

        int count = pushService.pushMessageToGroup(groupId, envelope);
        return ApiResponse.success("群组消息已发送给 " + count + " 个用户");
    }

    /**
     * 推送群组通知
     */
    @PostMapping("/notification/group/{groupId}")
    public ApiResponse pushGroupNotification(@PathVariable Long groupId, @RequestBody NotificationRequest request) {
        int count = pushService.pushGroupNotification(groupId, request.getTitle(), request.getContent());
        return ApiResponse.success("群组通知已发送给 " + count + " 个用户");
    }

    /**
     * 推送消息给指定用户列表
     */
    @PostMapping("/users")
    public ApiResponse pushToUsers(@RequestBody PushToUsersRequest request) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageType(MessageType.TEXT);
        chatMessage.setContent(request.getContent());
        chatMessage.setSenderNickname(request.getSenderName());

        WsEnvelope<ChatMessage> envelope = new WsEnvelope<>();
        envelope.setAction(WsEnvelope.ActionType.PUSH_MESSAGE);
        envelope.setData(chatMessage);

        int count = pushService.pushMessageToUsers(request.getUserIds(), envelope);
        return ApiResponse.success("消息已发送给 " + count + "/" + request.getUserIds().size() + " 个用户");
    }

    /**
     * 推送用户状态变更
     */
    @PostMapping("/user-status")
    public ApiResponse pushUserStatusChange(@RequestBody UserStatusRequest request) {
        int count = pushService.pushUserStatusChange(request.getUserId(), request.getStatus(), request.getExcludeUserId());
        return ApiResponse.success("用户状态变更通知已发送给 " + count + " 个用户");
    }

    /**
     * 获取在线用户信息
     */
    @GetMapping("/online-users")
    public ApiResponse getOnlineUsers() {
        Set<Long> onlineUsers = pushService.getOnlineUsers();
        return ApiResponse.success("当前在线用户", onlineUsers);
    }

    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-count")
    public ApiResponse getOnlineUserCount() {
        int count = pushService.getOnlineUserCount();
        return ApiResponse.success("当前在线用户数量: " + count);
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/user/{userId}/online")
    public ApiResponse checkUserOnline(@PathVariable Long userId) {
        boolean online = pushService.isUserOnline(userId);
        return ApiResponse.success("用户 " + userId + " " + (online ? "在线" : "离线"));
    }

    // ================= DTO 内部类 =================

    @Data
    public static class PushMessageRequest {
        private String content;
        private String messageType = "TEXT";
        private String senderName = "系统";
    }

    @Data
    public static class NotificationRequest {
        private String title;
        private String content;
    }

    @Data
    public static class SystemMessageRequest {
        private String content;
    }

    @Data
    public static class PushToUsersRequest {
        private List<Long> userIds;
        private String content;
        private String messageType = "TEXT";
        private String senderName = "系统";
    }

    @Data
    public static class UserStatusRequest {
        private Long userId;
        private String status;
        private Long excludeUserId;
    }

    @Data
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public static ApiResponse success(String message) {
            ApiResponse response = new ApiResponse();
            response.success = true;
            response.message = message;
            return response;
        }

        public static ApiResponse success(String message, Object data) {
            ApiResponse response = new ApiResponse();
            response.success = true;
            response.message = message;
            response.data = data;
            return response;
        }

        public static ApiResponse error(String message) {
            ApiResponse response = new ApiResponse();
            response.success = false;
            response.message = message;
            return response;
        }
    }
}
