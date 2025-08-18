package xlike.top.nettydemo.handler;

import cn.dev33.satoken.stp.StpUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.model.SessionManager;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手认证处理器
 * 在
 * WebSocket 协议升级前，通过 HTTP 请求头进行 sa-token 认证
 * @author Administrator
 */
@Component
@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);

    /**
     * 只处理 HTTP 请求，进行认证
     *
     * @param ctx ChannelHandlerContext
     * @param req FullHttpRequest
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("AuthHandler received request: {} {}", req.method(), req.uri());
        log.info("Headers: {}", req.headers());
        
        // 只关心 WebSocket 的升级请求
        if (req.headers().contains(HttpHeaderNames.UPGRADE) && "websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) {
            
            // 获取干净的 URI（去掉查询参数）
            String cleanUri = req.uri().split("\\?")[0];
            log.info("Clean URI for WebSocket upgrade: {}", cleanUri);
            try {
                log.info("Processing WebSocket upgrade request");
                
                // 从请求头中获取 token
                String token = req.headers().get("Authorization");
                log.info("Token from Authorization header: {}", token);
                
                // 如果请求头中没有，则尝试从URL查询参数中获取 (为了兼容浏览器JS)
                if (token == null || token.isEmpty()) {
                    QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
                    Map<String, List<String>> parameters = decoder.parameters();
                    log.info("URL parameters: {}", parameters);
                    
                    if (parameters.containsKey("token") && !parameters.get("token").isEmpty()) {
                        token = parameters.get("token").get(0);
                        log.info("Token from URL parameter: {}", token);
                    }
                }
                if (token == null) {
                    throw new SecurityException("令牌不能为空");
                }

                log.info("Validating token: {}", token);
                
                //  验证 token
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                     throw new SecurityException("无效的Token");
                }
                
                log.info("Token validated successfully for user: {}", loginId);
                
                // 认证成功，将用户ID附加到Channel上
                ctx.channel().attr(SessionManager.USER_ID_KEY).set(Long.valueOf(loginId.toString()));
                log.info("User ID attached to channel: {}", loginId);

                // 必须调用 retain()，因为 fireChannelRead 会释放 req
                ctx.fireChannelRead(req.retain());
                log.info("Request passed to next handler");

            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage(), e);
                // 认证失败，返回 401 Unauthorized
                sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED));
                return; // 结束处理，不传递给后续handler
            }
        } else {
             log.info("Not a WebSocket upgrade request, passing to next handler");
             // 如果不是 ws 升级请求，直接传递给下一个 handler (例如 HttpRouterHandler)
             ctx.fireChannelRead(req.retain());
        }
    }

    /**
     * 发送HTTP响应
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpResponse res) {
        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }
}