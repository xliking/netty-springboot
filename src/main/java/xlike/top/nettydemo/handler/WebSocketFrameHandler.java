package xlike.top.nettydemo.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.entity.ChatGroup;
import xlike.top.nettydemo.entity.Message;
import xlike.top.nettydemo.model.ChatMessage;
import xlike.top.nettydemo.model.SessionManager;
import xlike.top.nettydemo.service.ChatService;

import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ChatService chatService;

    public WebSocketFrameHandler(ObjectMapper objectMapper, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("WebSocketFrameHandler userEventTriggered: {}", evt.getClass().getSimpleName());
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete handshake = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            log.info("WebSocket 握手成功 from: {}", ctx.channel().remoteAddress());
            log.info("Request URI: {}", handshake.requestUri());
            log.info("Selected subprotocol: {}", handshake.selectedSubprotocol());
            log.info("Handshake headers: {}", handshake.requestHeaders());
            
            Long userId = ctx.channel().attr(SessionManager.USER_ID_KEY).get();
            log.info("User ID from channel attribute: {}", userId);
            
            if (userId != null) {
                sessionManager.userLogin(userId, ctx.channel());
                // 登录成功后，获取该用户的所有群组，并让其Channel加入
                List<ChatGroup> groups = chatService.getGroupsForUser(userId);
                for (ChatGroup group : groups) {
                    sessionManager.joinGroup(group.getId(), ctx.channel());
                }
                log.info("User {} session is now active and joined {} groups.", userId, groups.size());
            } else {
                log.error("握手完成，但未找到用户ID --- 关闭连接");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String jsonMessage = ((TextWebSocketFrame) frame).text();
            try {
                ChatMessage chatMessage = objectMapper.readValue(jsonMessage, ChatMessage.class);
                Long currentUserId = ctx.channel().attr(SessionManager.USER_ID_KEY).get();
                // 设置发送者为当前用户
                chatMessage.setSenderId(currentUserId);
                // 根据action路由到不同业务
                switch (chatMessage.getAction()) {
                    case GET_GROUP_LIST:
                        handleGetGroupList(ctx, currentUserId);
                        break;
                    case GET_PRIVATE_HISTORY:
                        handleGetPrivateHistory(ctx, currentUserId, chatMessage.getReceiverId());
                        break;
                    case GET_GROUP_HISTORY:
                        handleGetGroupHistory(ctx, chatMessage.getGroupId());
                        break;
                    case SEND_TO_USER:
                        chatService.sendMessageToUser(chatMessage);
                        break;
                    case SEND_TO_GROUP:
                        chatService.sendMessageToGroup(chatMessage);
                        break;
                    default:
                        log.warn("Unknown action received: {}", chatMessage.getAction());
                }
            } catch (Exception e) {
                log.error("Failed to process WebSocket frame: {}", jsonMessage, e);
            }
        }
    }

    private void handleGetGroupList(ChannelHandlerContext ctx, Long userId) throws JsonProcessingException {
        List<ChatGroup> groups = chatService.getGroupsForUser(userId);
        ChatMessage response = new ChatMessage();
        response.setAction(ChatMessage.ActionType.PUSH_GROUP_LIST);
        response.setData(groups);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(response)));
    }

    private void handleGetPrivateHistory(ChannelHandlerContext ctx, Long userId1, Long userId2) throws JsonProcessingException {
        List<Message> messages = chatService.getPrivateChatHistory(userId1, userId2);
        ChatMessage response = new ChatMessage();
        response.setAction(ChatMessage.ActionType.PUSH_HISTORY);
        response.setData(messages);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(response)));
    }

    private void handleGetGroupHistory(ChannelHandlerContext ctx, Long groupId) throws JsonProcessingException {
        List<Message> messages = chatService.getGroupChatHistory(groupId);
        ChatMessage response = new ChatMessage();
        response.setAction(ChatMessage.ActionType.PUSH_HISTORY);
        response.setData(messages);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.userLogout(ctx.channel());
        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
}