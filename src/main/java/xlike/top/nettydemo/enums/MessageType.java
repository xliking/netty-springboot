package xlike.top.nettydemo.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author Administrator
 */
@Getter
public enum MessageType {

    TEXT(0, "文本消息"),
    IMAGE(1, "图片消息"),
    FILE(2, "文件消息"),
    VIDEO(3, "视频消息"),
    LINK(4, "链接消息"),
    SYSTEM(5, "系统消息");

    private final Integer code;
    private final String message;

    MessageType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static MessageType fromValue(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
