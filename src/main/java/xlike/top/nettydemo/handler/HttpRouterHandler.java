package xlike.top.nettydemo.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.constants.NettyConstants;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 自定义 HTTP 路由处理器
 * 用于处理简单的 HTTP 请求，例如健康检查
 *
 * @author Administrator
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HttpRouterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ObjectMapper objectMapper;

    public HttpRouterHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : createDefaultObjectMapper();
    }

    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    /**
     * 读取并处理传入的 HTTP 请求
     *
     * @param ctx ChannelHandlerContext
     * @param req FullHttpRequest
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("HttpRouterHandler received request: {} {}", req.method(), req.uri());
        // 检查是否为 WebSocket 升级请求，如果是则传递给下一个处理器
        if (req.headers().contains(HttpHeaderNames.UPGRADE) && "websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) {
            ctx.fireChannelRead(req.retain());
            return;
        }
        // 检查是否为健康检查请求
        if (NettyConstants.HEALTH_CHECK_PATH.equals(req.uri())) {
            handleHealthCheck(ctx);
        } else {
            sendNotFoundResponse(ctx);
        }
    }

    /**
     * 处理健康检查请求
     */
    private void handleHealthCheck(ChannelHandlerContext ctx) {
        try {
            // 创建 R 响应对象
            R<String> result = R.ok("服务运行正常", "ok");
            String jsonResponse = objectMapper.writeValueAsString(result);
            
            var content = ctx.alloc().buffer();
            content.writeCharSequence(jsonResponse, java.nio.charset.StandardCharsets.UTF_8);
            
            var resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            resp.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
            resp.headers().setInt(CONTENT_LENGTH, content.readableBytes());
            
            // 发送响应并添加监听器，失败时关闭连接
            ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } catch (JsonProcessingException e) {
            sendErrorResponse(ctx);
        }
    }


    /**
     * 发送 404 Not Found 响应
     */
    private void sendNotFoundResponse(ChannelHandlerContext ctx) {
        try {
            R<String> result = R.fail(404, "资源不存在");
            String jsonResponse = objectMapper.writeValueAsString(result);
            
            var content = ctx.alloc().buffer();
            content.writeCharSequence(jsonResponse, java.nio.charset.StandardCharsets.UTF_8);
            
            var resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, content);
            resp.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
            resp.headers().setInt(CONTENT_LENGTH, content.readableBytes());
            
            ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } catch (JsonProcessingException e) {
            sendRawErrorResponse(ctx);
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx) {
        try {
            R<String> result = R.fail("内部服务器错误");
            String jsonResponse = objectMapper.writeValueAsString(result);
            
            var content = ctx.alloc().buffer();
            content.writeCharSequence(jsonResponse, java.nio.charset.StandardCharsets.UTF_8);
            
            var resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
            resp.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
            resp.headers().setInt(CONTENT_LENGTH, content.readableBytes());
            
            ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } catch (JsonProcessingException e) {
            sendRawErrorResponse(ctx);
        }
    }

    /**
     * 发送原始错误响应（当 JSON 序列化失败时使用）
     */
    private void sendRawErrorResponse(ChannelHandlerContext ctx) {
        var content = ctx.alloc().buffer();
        content.writeCharSequence("内部服务器错误", java.nio.charset.StandardCharsets.UTF_8);
        
        var resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
        resp.headers().set(CONTENT_TYPE, "text/plain; charset=utf-8");
        resp.headers().setInt(CONTENT_LENGTH, content.readableBytes());
        
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }


}