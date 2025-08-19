package xlike.top.nettydemo.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.handler.AuthHandler;
import xlike.top.nettydemo.handler.HttpRouterHandler;
import xlike.top.nettydemo.handler.WebSocketFrameHandler;
import xlike.top.nettydemo.properties.NettyProperties;

/**
 * Netty 服务器的 Channel 初始化器
 *
 * @author Administrator
 */
@Slf4j
@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyProperties props;
    private final AuthHandler authHandler;
    private final HttpRouterHandler httpRouterHandler;
    private final WebSocketFrameHandler wsHandler;

    public NettyServerInitializer(NettyProperties props, AuthHandler authHandler, HttpRouterHandler httpRouterHandler, WebSocketFrameHandler wsHandler) {
        this.props = props;
        this.authHandler = authHandler;
        this.httpRouterHandler = httpRouterHandler;
        this.wsHandler = wsHandler;
    }


    /**
     * 初始化新接受的 Channel
     *
     * @param ch SocketChannel
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        // HTTP 协议处理
        p.addLast("httpCodec", new HttpServerCodec());
        p.addLast("httpAggregator", new HttpObjectAggregator(65536));
        p.addLast("idleStateHandler", new IdleStateHandler(
                props.getReaderIdleSeconds(), props.getWriterIdleSeconds(), 0));
        // 认证 Handler-所有 HTTP 请求都会先经过这里 - 因为需要验证Token
        p.addLast("authHandler", authHandler);
        // 自定义 HTTP 路由处理器 - 处理非 WebSocket 的普通 HTTP 请求
        p.addLast("httpRouter", httpRouterHandler);
        // WebSocket 协议处理器 - 使用自定义配置来处理查询参数
        WebSocketServerProtocolConfig config = WebSocketServerProtocolConfig.newBuilder()
                .websocketPath(props.getWebsocketPath())
                // 允许路径以指定路径开始（处理查询参数） http请求握手 后面都会有 ?token=xxx
                .checkStartsWith(true)
                .maxFramePayloadLength(props.getMaxWsFrameSize())
                .build();
        WebSocketServerProtocolHandler wsProtocolHandler = new WebSocketServerProtocolHandler(config);
        p.addLast("webSocketProtocol", wsProtocolHandler);
        // 只有在 WebSocket 握手成功后，这个 Handler 才会收到消息（WebSocketFrame）
        p.addLast("webSocketFrameHandler", wsHandler);
    }
}