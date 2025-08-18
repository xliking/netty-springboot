package xlike.top.nettydemo.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.constants.NettyConstants;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 自定义 HTTP 路由处理器
 * 用于处理简单的 HTTP 请求，例如健康检查
 * @author Administrator
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HttpRouterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 读取并处理传入的 HTTP 请求
     *
     * @param ctx ChannelHandlerContext
     * @param req FullHttpRequest
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("HttpRouterHandler received request: {} {}", req.method(), req.uri());
        
        // 检查是否为 WebSocket 升级请求，如果是则不应该走到这里
        if (req.headers().contains(HttpHeaderNames.UPGRADE) && "websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) {
            log.warn("WebSocket request reached HttpRouterHandler - this should not happen!");
            ctx.fireChannelRead(req.retain());
            return;
        }
        
        // 检查是否为健康检查请求
        if (NettyConstants.HEALTH_CHECK_PATH.equals(req.uri())) {
            handleHealthCheck(ctx);
        } else {
            // 如果不是健康检查请求，返回 404
            sendNotFoundResponse(ctx);
        }
    }

    /**
     * 处理健康检查请求
     *
     * @param ctx ChannelHandlerContext
     */
    private void handleHealthCheck(ChannelHandlerContext ctx) {
        var content = ctx.alloc().buffer();
        content.writeCharSequence("OK", java.nio.charset.StandardCharsets.UTF_8);

        var resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        resp.headers().set(CONTENT_TYPE, "text/plain; charset=utf-8");
        resp.headers().setInt(CONTENT_LENGTH, content.readableBytes());

        // 发送响应并添加监听器，失败时关闭连接
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }
}