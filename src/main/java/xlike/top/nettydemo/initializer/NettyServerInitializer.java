package xlike.top.nettydemo.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.handler.AuthHandler;
import xlike.top.nettydemo.handler.HttpRouterHandler;
import xlike.top.nettydemo.handler.WebSocketFrameHandler;
import xlike.top.nettydemo.properties.NettyProperties;

/**
 * Netty 服务器的 Channel 初始化器
 * @author Administrator
 */
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
        // 64KB
        p.addLast("httpAggregator", new HttpObjectAggregator(65536));
        // 自定义 HTTP 路由
        p.addLast("httpRouter", httpRouterHandler);
        // 添加认证处理器
        p.addLast("authHandler", authHandler);
        // 心跳检测
        p.addLast("idleStateHandler", new IdleStateHandler(
                props.getReaderIdleSeconds(), props.getWriterIdleSeconds(), 0));
        // WebSocket 协议处理器
        p.addLast("webSocketProtocol", new WebSocketServerProtocolHandler(
                props.getWebsocketPath(), null, true, props.getMaxWsFrameSize()));
        // 自定义 WebSocket 消息帧处理器
        p.addLast("webSocketFrameHandler", wsHandler);
    }
}