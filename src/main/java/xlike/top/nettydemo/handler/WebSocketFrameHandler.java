package xlike.top.nettydemo.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.pojo.domain.ChatGroup;
import xlike.top.nettydemo.pojo.domain.Message;
import xlike.top.nettydemo.model.SessionManager;
import xlike.top.nettydemo.model.WsEnvelope;
import xlike.top.nettydemo.pojo.dto.GroupHistoryRequest;
import xlike.top.nettydemo.pojo.dto.PrivateHistoryRequest;
import xlike.top.nettydemo.service.ChatService;
import xlike.top.nettydemo.service.WebSocketPushService;

import java.util.List;

/**
 * WebSocket Frame Handler
 * 职责：
 * - 处理客户端发来的 WS 消息
 * - 路由到 ChatService
 * - 返回 WsEnvelope 封装的响应
 *
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
@ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ChatService chatService;
    private final WebSocketPushService pushService;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("WebSocketFrameHandler userEventTriggered: {}", evt.getClass().getSimpleName());
        // HTTP握手认证
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("WebSocket 握手成功 from: {}", ctx.channel().remoteAddress());
            Long userId = ctx.channel().attr(SessionManager.USER_ID_KEY).get();
            if (userId != null) {
                sessionManager.userLogin(userId, ctx.channel());
                // 登录成功后，获取该用户的所有群组，并让其 Channel 加入
                List<ChatGroup> groups = chatService.getGroupsForUser(userId);
                for (ChatGroup group : groups) {
                    sessionManager.joinGroup(group.getId(), ctx.channel());
                }
                log.info("User {} session is now active and joined {} groups.", userId, groups.size());
            } else {
                log.error("握手完成，但未找到用户ID --- 关闭连接");
                ctx.close();
            }
        } else if (evt instanceof IdleStateEvent idleStateEvent) {
            // 心跳检测事件
            switch (idleStateEvent.state()) {
                case READER_IDLE -> {
                    log.warn("用户 {} 超过指定时间未发送消息", ctx.channel().remoteAddress());
                    R<String> pong = R.ok(WsEnvelope.ActionType.PONG.name(), "alive");
                    String json = objectMapper.writeValueAsString(pong);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
                }
                case WRITER_IDLE -> {
                    log.debug("服务器一段时间未向 {} 发送消息", ctx.channel().remoteAddress());
                }
                case ALL_IDLE -> {
                    log.warn("用户 {} 长时间无读写", ctx.channel().remoteAddress());
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame textFrame) {
            String jsonMessage = textFrame.text();
            try {
                WsEnvelope<?> envelope = objectMapper.readValue(jsonMessage, WsEnvelope.class);
                Long currentUserId = ctx.channel().attr(SessionManager.USER_ID_KEY).get();
                if (envelope.getAction() == null) {
                    pushService.sendError(ctx, "消息处理失败: " + "操作不能为空");
                    return;
                }
                switch (envelope.getAction()) {
                    case GET_GROUP_LIST -> handleGetGroupList(ctx, currentUserId);

                    case GET_PRIVATE_HISTORY -> {
                        // data 结构: { "receiverId": 2 }
                        PrivateHistoryRequest req =
                                objectMapper.convertValue(envelope.getData(), PrivateHistoryRequest.class);
                        handleGetPrivateHistory(ctx, currentUserId, req.getReceiverId());
                    }

                    case GET_GROUP_HISTORY -> {
                        // data 结构: { "groupId": 1 }
                        GroupHistoryRequest req =
                                objectMapper.convertValue(envelope.getData(), GroupHistoryRequest.class);
                        handleGetGroupHistory(ctx, req.getGroupId());
                    }

                    case SEND_TO_USER -> {
                        // data 结构: { "receiverId": 2, "messageType": "TEXT", "content": "xxx" }
                        Message msg = objectMapper.convertValue(envelope.getData(), Message.class);
                        msg.setSenderId(currentUserId);
                        chatService.savePrivateMessage(msg);
                    }

                    case SEND_TO_GROUP -> {
                        // data 结构: { "groupId": 1, "messageType": "TEXT", "content": "xxx" }
                        Message msg = objectMapper.convertValue(envelope.getData(), Message.class);
                        msg.setSenderId(currentUserId);
                        chatService.saveGroupMessage(msg);
                    }
                    default -> {
                        log.warn("Unknown action received: {}", envelope.getAction());
                        pushService.sendError(ctx, "消息处理失败: 未知操作");
                    }
                }
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage());
                pushService.sendError(ctx, "消息处理失败: " + e.getMessage());
            }
        }
    }


    private void handleGetGroupList(ChannelHandlerContext ctx, Long userId) {
        List<ChatGroup> groups = chatService.getGroupsForUser(userId);
        pushService.sendResponse(ctx, WsEnvelope.ActionType.PUSH_GROUP_LIST, groups);
    }

    private void handleGetPrivateHistory(ChannelHandlerContext ctx, Long userId1, Long userId2) {
        List<Message> messages = chatService.getPrivateChatHistory(userId1, userId2);
        pushService.sendResponse(ctx, WsEnvelope.ActionType.PUSH_HISTORY, messages);
    }

    private void handleGetGroupHistory(ChannelHandlerContext ctx, Long groupId) {
        List<Message> messages = chatService.getGroupChatHistory(groupId);
        pushService.sendResponse(ctx, WsEnvelope.ActionType.PUSH_HISTORY, messages);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.userLogout(ctx.channel());
        super.channelInactive(ctx);
    }
}
