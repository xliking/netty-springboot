package xlike.top.nettydemo.service;

import xlike.top.nettydemo.entity.ChatGroup;
import xlike.top.nettydemo.entity.Message;
import xlike.top.nettydemo.model.ChatMessage;
import java.util.List;

public interface ChatService {

    /**
     * 根据用户ID获取其群聊列表
     */
    List<ChatGroup> getGroupsForUser(Long userId);

    /**
     * 获取与某个用户的聊天记录
     */
    List<Message> getPrivateChatHistory(Long userId1, Long userId2);

    /**
     * 获取群聊的聊天记录
     */
    List<Message> getGroupChatHistory(Long groupId);

    /**
     * 发送单聊消息
     */
    void sendMessageToUser(ChatMessage chatMessage);

    /**
     * 发送群聊消息
     */
    void sendMessageToGroup(ChatMessage chatMessage);
}