package xlike.top.nettydemo.constants;

/**
 * 项目常量类
 * @author Administrator
 */
public final class NettyConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private NettyConstants() {
    }

    /**
     * HTTP 健康检查路径
     */
    public static final String HEALTH_CHECK_PATH = "/health";

    /**
     * 心跳检测时发送的 ping 消息
     */
    public static final String PING_MESSAGE = "ping";
}