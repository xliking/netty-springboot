package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.pojo.domain.ChatGroup;
import xlike.top.nettydemo.pojo.domain.ChatMessage;
import xlike.top.nettydemo.mapper.ChatGroupMapper;
import xlike.top.nettydemo.mapper.ChatMessageMapper;
import xlike.top.nettydemo.model.WsEnvelope;
import xlike.top.nettydemo.service.ChatService;
import xlike.top.nettydemo.service.WebSocketPushService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天业务实现类
 * - 职责：处理消息存储和调用推送服务
 * @author Administrator
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatGroupMapper chatGroupMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final WebSocketPushService pushService;

    public ChatServiceImpl(ChatGroupMapper chatGroupMapper,
                           ChatMessageMapper chatMessageMapper,
                           WebSocketPushService pushService) {
        this.chatGroupMapper = chatGroupMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.pushService = pushService;
    }

    @Override
    public List<ChatGroup> getGroupsForUser(Long userId) {
        return chatGroupMapper.findGroupsByUserId(userId);
    }

    @Override
    public List<ChatMessage> getPrivateChatHistory(Long userId1, Long userId2) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.nested(w -> w.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
                .or(w -> w.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1));
        wrapper.orderByAsc(ChatMessage::getSendTime);
        return chatMessageMapper.selectList(wrapper);
    }

    @Override
    public List<ChatMessage> getGroupChatHistory(Long groupId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getGroupId, groupId).orderByAsc(ChatMessage::getSendTime);
        return chatMessageMapper.selectList(wrapper);
    }

    @Override
    public void savePrivateMessage(ChatMessage chatMessage) {
        chatMessage.setSendTime(LocalDateTime.now());
        chatMessage.setIsRead(0);
        chatMessageMapper.insert(chatMessage);

        WsEnvelope<ChatMessage> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_MESSAGE, chatMessage);
        boolean success = pushService.pushMessageToUser(chatMessage.getReceiverId(), envelope);

        if (success) {
            log.info("Private message sent from user {} to user {}", chatMessage.getSenderId(), chatMessage.getReceiverId());
        } else {
            log.warn("Failed to send private message to user {}, user may be offline", chatMessage.getReceiverId());
            // TODO: 离线消息存储 或 接入第三方推送（APNS, FCM）
        }
    }

    @Override
    public void saveGroupMessage(ChatMessage chatMessage) {

        chatMessage.setSendTime(LocalDateTime.now());
        chatMessage.setIsRead(0);
        chatMessageMapper.insert(chatMessage);

        WsEnvelope<ChatMessage> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_MESSAGE, chatMessage);
        int count = pushService.pushMessageToGroup(chatMessage.getGroupId(), envelope);

        log.info("Group message sent from user {} to group {}, delivered to {} users",
                chatMessage.getSenderId(), chatMessage.getGroupId(), count);
    }
}
