package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.entity.ChatGroup;
import xlike.top.nettydemo.entity.Message;
import xlike.top.nettydemo.mapper.ChatGroupMapper;
import xlike.top.nettydemo.mapper.MessageMapper;
import xlike.top.nettydemo.model.ChatMessage;
import xlike.top.nettydemo.model.SessionManager;
import xlike.top.nettydemo.service.ChatService;
import xlike.top.nettydemo.service.WebSocketPushService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Administrator
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatGroupMapper chatGroupMapper;
    private final MessageMapper messageMapper;
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ObjectMapper objectMapper;
    private final WebSocketPushService pushService;

    public ChatServiceImpl(ChatGroupMapper chatGroupMapper, MessageMapper messageMapper, ObjectMapper objectMapper, WebSocketPushService pushService) {
        this.chatGroupMapper = chatGroupMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
        this.pushService = pushService;
    }

    @Override
    public List<ChatGroup> getGroupsForUser(Long userId) {
        return chatGroupMapper.findGroupsByUserId(userId);
    }

    @Override
    public List<Message> getPrivateChatHistory(Long userId1, Long userId2) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.nested(w -> w.eq(Message::getSenderId, userId1).eq(Message::getReceiverId, userId2))
                .or(w -> w.eq(Message::getSenderId, userId2).eq(Message::getReceiverId, userId1));
        wrapper.orderByAsc(Message::getSendTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    public List<Message> getGroupChatHistory(Long groupId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getGroupId, groupId).orderByAsc(Message::getSendTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    public void sendMessageToUser(ChatMessage chatMessage) {
        // 保存消息到数据库
        Message message = convertToMessageEntity(chatMessage);
        messageMapper.insert(message);
        
        // 使用推送服务发送消息
        chatMessage.setAction(ChatMessage.ActionType.PUSH_MESSAGE);
        boolean success = pushService.pushMessageToUser(chatMessage.getReceiverId(), chatMessage);
        
        if (success) {
            log.info("Private message sent from user {} to user {}", chatMessage.getSenderId(), chatMessage.getReceiverId());
        } else {
            log.warn("Failed to send private message to user {}, user may be offline", chatMessage.getReceiverId());
            // 如果不在线，可以考虑存储离线消息，或通过其他方式推送（如APNS, FCM）
        }
    }

    @Override
    public void sendMessageToGroup(ChatMessage chatMessage) {
        // 保存消息到数据库
        Message message = convertToMessageEntity(chatMessage);
        messageMapper.insert(message);
        
        // 使用推送服务发送群组消息
        chatMessage.setAction(ChatMessage.ActionType.PUSH_MESSAGE);
        int count = pushService.pushMessageToGroup(chatMessage.getGroupId(), chatMessage);
        
        log.info("Group message sent from user {} to group {}, delivered to {} users", 
                chatMessage.getSenderId(), chatMessage.getGroupId(), count);
    }

    private Message convertToMessageEntity(ChatMessage dto) {
        Message entity = new Message();
        entity.setSenderId(dto.getSenderId());
        entity.setReceiverId(dto.getReceiverId());
        entity.setGroupId(dto.getGroupId());
        entity.setContent(dto.getContent());
        entity.setMessageType(dto.getMessageType().ordinal());
        entity.setSendTime(LocalDateTime.now());
        entity.setIsRead(0);
        return entity;
    }
}