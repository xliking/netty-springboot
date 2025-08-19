package xlike.top.nettydemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xlike.top.nettydemo.pojo.domain.ChatGroup;
import xlike.top.nettydemo.pojo.domain.Message;
import xlike.top.nettydemo.mapper.ChatGroupMapper;
import xlike.top.nettydemo.mapper.MessageMapper;
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
    private final MessageMapper messageMapper;
    private final WebSocketPushService pushService;

    public ChatServiceImpl(ChatGroupMapper chatGroupMapper,
                           MessageMapper messageMapper,
                           WebSocketPushService pushService) {
        this.chatGroupMapper = chatGroupMapper;
        this.messageMapper = messageMapper;
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
    public void savePrivateMessage(Message message) {
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0);
        messageMapper.insert(message);

        WsEnvelope<Message> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_MESSAGE, message);
        boolean success = pushService.pushMessageToUser(message.getReceiverId(), envelope);

        if (success) {
            log.info("Private message sent from user {} to user {}", message.getSenderId(), message.getReceiverId());
        } else {
            log.warn("Failed to send private message to user {}, user may be offline", message.getReceiverId());
            // TODO: 离线消息存储 或 接入第三方推送（APNS, FCM）
        }
    }

    @Override
    public void saveGroupMessage(Message message) {
        message.setSendTime(LocalDateTime.now());
        message.setIsRead(0);
        messageMapper.insert(message);

        WsEnvelope<Message> envelope = new WsEnvelope<>(WsEnvelope.ActionType.PUSH_MESSAGE, message);
        int count = pushService.pushMessageToGroup(message.getGroupId(), envelope);

        log.info("Group message sent from user {} to group {}, delivered to {} users",
                message.getSenderId(), message.getGroupId(), count);
    }
}
