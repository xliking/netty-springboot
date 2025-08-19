package xlike.top.nettydemo.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息的统一数据模型
 * @author Administrator
 */
@Data
public class ChatMessage {

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT, IMAGE, FILE, VIDEO, LINK, SYSTEM
    }

    /**
     * 客户端请求或服务端推送的动作类型
     */
    public enum ActionType {
        // 客户端操作
        GET_GROUP_LIST,
        GET_PRIVATE_HISTORY,
        GET_GROUP_HISTORY,
        SEND_TO_USER,
        SEND_TO_GROUP,

        // 服务端推送操作
        PUSH_MESSAGE,           // 推送新消息
        PUSH_GROUP_LIST,        // 推送群组列表
        PUSH_HISTORY,           // 推送历史记录
        PUSH_NOTIFICATION,      // 推送通知消息
        PUSH_SYSTEM_MESSAGE,    // 推送系统消息
        PUSH_USER_STATUS,       // 推送用户状态变更
        PUSH_GROUP_UPDATE,      // 推送群组信息更新
        PUSH_ONLINE_USERS,      // 推送在线用户列表
        PUSH_BROADCAST          // 服务端广播消息
    }

    private ActionType action;
    private String messageId;
    private MessageType messageType;
    private String content;
    private Long senderId;
    private String senderNickname;
    private Long receiverId;
    private Long groupId;
    private LocalDateTime sendTime;
    // 用于承载历史记录或列表等数据
    private Object data;
}