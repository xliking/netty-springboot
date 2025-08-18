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

        // 服务端操作 - 通用消息推送
        PUSH_MESSAGE,
        PUSH_GROUP_LIST,
        PUSH_HISTORY
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