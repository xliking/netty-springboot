package xlike.top.nettydemo.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import xlike.top.nettydemo.initializer.NettyServerInitializer;
import xlike.top.nettydemo.properties.NettyProperties;

/**
 * Netty 服务器，负责启动和停止
 * 实现 SmartLifecycle 接口，以便由 Spring 容器管理其生命周期
 * @author Administrator
 */
@Component
public class NettyServer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    /**
     * Netty 配置属性
     */
    private final NettyProperties props;

    /**
     * Netty Channel 初始化器
     */
    private final NettyServerInitializer initializer;

    /**
     * Boss 线程组，用于接受客户端连接
     */
    private EventLoopGroup bossGroup;

    /**
     * Worker 线程组，用于处理 I/O 事件
     */
    private EventLoopGroup workerGroup;

    /**
     * 服务器端的 Channel
     */
    private Channel serverChannel;

    /**
     * 运行状态标志
     */
    private volatile boolean running = false;


    /**
     * 构造函数
     *
     * @param props       Netty 配置
     * @param initializer Channel 初始化器
     */
    public NettyServer(NettyProperties props, NettyServerInitializer initializer) {
        this.props = props;
        this.initializer = initializer;
    }

    /**
     * 启动 Netty 服务器
     */
    @Override
    public void start() {
        if (running) {
            return;
        }

        bossGroup = newEventLoopGroup(props.getBossThreads());
        workerGroup = newEventLoopGroup(props.getWorkerThreads());

        Class<? extends ServerChannel> channelClass = resolveServerChannelClass();

        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(channelClass)
                    .option(ChannelOption.SO_BACKLOG, props.getBacklog())
                    .childOption(ChannelOption.TCP_NODELAY, props.isTcpNoDelay())
                    .childOption(ChannelOption.SO_KEEPALIVE, props.isSoKeepAlive())
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(props.getWriteBufferWatermarkLow(), props.getWriteBufferWatermarkHigh()))
                    .childHandler(initializer);

            serverChannel = b.bind(props.getPort()).syncUninterruptibly().channel();
            running = true;
            log.info("Netty server started on port {} (implementation: {})", props.getPort(), channelClass.getSimpleName());
        } catch (Exception e) {
            log.error("Failed to start Netty server", e);
            throw new IllegalStateException("Failed to start Netty", e);
        }
    }

    /**
     * 停止 Netty 服务器，并优雅地关闭线程组
     */
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } catch (Exception e) {
            log.warn("Exception while closing server channel", e);
        }

        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Exception e) {
            log.warn("Exception while shutting down boss group", e);
        }

        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Exception e) {
            log.warn("Exception while shutting down worker group", e);
        }
        running = false;
        log.info("Netty server stopped.");
    }

    /**
     * 检查服务器是否正在运行
     *
     * @return 如果正在运行，则返回 true
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 创建并返回一个 EventLoopGroup
     * 此方法会根据当前操作系统的特性，选择性能最优的 EventLoopGroup 实现。
     * 在 Linux 上优先使用 Epoll，在 macOS 上优先使用 KQueue，其他情况使用 NIO。
     *
     * @param threads 线程数
     * @return EventLoopGroup 实例
     */
    private static EventLoopGroup newEventLoopGroup(int threads) {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(threads);
        }
        if (KQueue.isAvailable()) {
            return new KQueueEventLoopGroup(threads);
        }
        return new NioEventLoopGroup(threads);
    }

    /**
     * 解析并返回服务器 Channel 的 Class 类型
     *
     * @return ServerChannel 的 Class 类型
     */
    private static Class<? extends ServerChannel> resolveServerChannelClass() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        }
        if (KQueue.isAvailable()) {
            return KQueueServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }
}