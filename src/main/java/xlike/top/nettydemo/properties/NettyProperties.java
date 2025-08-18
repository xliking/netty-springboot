package xlike.top.nettydemo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Netty 服务器的配置属性类
 * 通过 @ConfigurationProperties 注解将 application.yml 中的 netty 前缀的配置项绑定到此类
 * @author Administrator
 */
@Component
@Data
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

    /**
     * Netty 监听的端口
     */
    private int port = 8090;

    /**
     * Boss 线程组的线程数
     */
    private int bossThreads = 1;

    /**
     * Worker 线程组的线程数, 0 表示自动设置
     */
    private int workerThreads = 0;

    /**
     * WebSocket 的访问路径
     */
    private String websocketPath = "/ws";

    /**
     * 读空闲超时时间（秒）
     */
    private int readerIdleSeconds = 45;

    /**
     * 写空闲超时时间（秒）
     */
    private int writerIdleSeconds = 20;

    /**
     * WebSocket 帧的最大长度
     */
    private int maxWsFrameSize = 1024 * 1024;

    /**
     * 服务端接受连接的队列长度
     */
    private int backlog = 1024;

    /**
     * 是否启用 TCP Nagle 算法
     */
    private boolean tcpNoDelay = true;

    /**
     * 是否开启 TCP 底层的 SO_KEEPALIVE 机制
     */
    private boolean soKeepAlive = true;

    /**
     * 写缓冲区低水位线
     */
    private int writeBufferWatermarkLow = 32 * 1024;

    /**
     * 写缓冲区高水位线
     */
    private int writeBufferWatermarkHigh = 64 * 1024;
}