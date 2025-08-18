package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.entity.ChatGroup;
import xlike.top.nettydemo.entity.Message;
import xlike.top.nettydemo.mapper.ChatGroupMapper;
import xlike.top.nettydemo.mapper.MessageMapper;
import xlike.top.nettydemo.model.ChatMessage;
import xlike.top.nettydemo.model.SessionManager;
import xlike.top.nettydemo.service.ChatService;
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

    public ChatServiceImpl(ChatGroupMapper chatGroupMapper, MessageMapper messageMapper, ObjectMapper objectMapper) {
        this.chatGroupMapper = chatGroupMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
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
        // 查找接收者的Channel
        Channel receiverChannel = sessionManager.getChannelByUserId(chatMessage.getReceiverId());
        // 如果接收者在线，实时推送消息
        if (receiverChannel != null) {
            try {
                receiverChannel.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(chatMessage)));
            } catch (JsonProcessingException e) {
                log.error("Error converting ChatMessage to JSON: {}", e.getMessage());
            }
        }
        // 如果不在线，可以考虑存储离线消息，或通过其他方式推送（如APNS, FCM）
    }

    @Override
    public void sendMessageToGroup(ChatMessage chatMessage) {
        // 保存消息到数据库
        Message message = convertToMessageEntity(chatMessage);
        messageMapper.insert(message);
        // 查找群组的ChannelGroup
        ChannelGroup channelGroup = sessionManager.getChannelGroupByGroupId(chatMessage.getGroupId());
        // 如果群组存在且有在线成员，广播消息
        if (channelGroup != null) {
            try {
                // 注意：这里会将消息也发给发送者自己，客户端需要根据senderId判断是否是自己发的消息
                channelGroup.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(chatMessage)));
            } catch (JsonProcessingException e) {
                log.error("Error converting ChatMessage to JSON: {}", e.getMessage());
            }
        }
    }

    // DTO to Entity converter
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