package xlike.top.nettydemo.service;

import xlike.top.nettydemo.pojo.domain.ChatGroup;
import xlike.top.nettydemo.pojo.domain.Message;

import java.util.List;

/**
 * 聊天业务接口
 * 职责：
 *   - 提供聊天相关的数据操作
 *   - 不直接依赖 WebSocket 协议封装（WsEnvelope）
 * @author Administrator
 */
public interface ChatService {

    /**
     * 根据用户ID获取其群聊列表
     *
     * @param userId 用户ID
     * @return 群聊列表
     */
    List<ChatGroup> getGroupsForUser(Long userId);

    /**
     * 获取与某个用户的聊天记录（双向）
     *
     * @param userId1 用户1
     * @param userId2 用户2
     * @return 聊天记录
     */
    List<Message> getPrivateChatHistory(Long userId1, Long userId2);

    /**
     * 获取群聊的聊天记录
     *
     * @param groupId 群组ID
     * @return 聊天记录
     */
    List<Message> getGroupChatHistory(Long groupId);

    /**
     * 保存单聊消息
     *
     * @param message 单聊消息
     */
    void savePrivateMessage(Message message);

    /**
     * 保存群聊消息
     *
     * @param message 群聊消息
     */
    void saveGroupMessage(Message message);
}
