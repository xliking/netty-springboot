package xlike.top.nettydemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 协议统一封装对象
 *
 * 使用场景：
 *   - 所有 WebSocket 消息都必须封装成 WsEnvelope，保证前后端协议一致。
 *   - 包含一个 action 表示操作类型（请求 or 推送），以及一个 data 表示具体载荷。
 *
 * 示例：
 *   客户端请求群组列表：
 *     {
 *       "action": "GET_GROUP_LIST",
 *       "data": null
 *     }
 *
 *   服务端推送群组列表：
 *     {
 *       "action": "PUSH_GROUP_LIST",
 *       "data": [
 *         { "id": 1, "name": "技术群" },
 *         { "id": 2, "name": "吹水群" }
 *       ]
 *     }
 *
 * @param <T> 泛型，表示数据载荷的类型，可以是单个对象，也可以是列表
 * @author
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsEnvelope<T> {

    /**
     * 客户端请求或服务端推送的动作类型
     *
     * - 客户端请求：
     *   GET_GROUP_LIST      -> 获取当前用户的群组列表
     *   GET_PRIVATE_HISTORY -> 获取与某个用户的历史聊天记录
     *   GET_GROUP_HISTORY   -> 获取某个群组的历史消息
     *   SEND_TO_USER        -> 发送单聊消息
     *   SEND_TO_GROUP       -> 发送群聊消息
     *
     * - 服务端推送：
     *   PONG                -> 心跳响应（客户端发送 PING 后返回）
     *   PUSH_MESSAGE        -> 推送单聊/群聊消息
     *   PUSH_GROUP_LIST     -> 推送群组列表
     *   PUSH_HISTORY        -> 推送聊天历史（单聊或群聊）
     *   PUSH_NOTIFICATION   -> 推送通知消息（带标题）
     *   PUSH_SYSTEM_MESSAGE -> 推送系统消息（例如：公告）
     *   PUSH_USER_STATUS    -> 推送用户状态变更（上线/下线/忙碌）
     *   PUSH_GROUP_UPDATE   -> 推送群组信息更新（新增成员/修改群名等）
     *   PUSH_ONLINE_USERS   -> 推送当前在线用户列表
     *   PUSH_BROADCAST      -> 推送广播消息（所有人可见）
     */
    public enum ActionType {
        // ===== 客户端请求 =====
        GET_GROUP_LIST,
        GET_PRIVATE_HISTORY,
        GET_GROUP_HISTORY,
        SEND_TO_USER,
        SEND_TO_GROUP,

        // ===== 服务端推送 =====
        PONG,
        PUSH_MESSAGE,
        PUSH_GROUP_LIST,
        PUSH_HISTORY,
        PUSH_NOTIFICATION,
        PUSH_SYSTEM_MESSAGE,
        PUSH_USER_STATUS,
        PUSH_GROUP_UPDATE,
        PUSH_ONLINE_USERS,
        PUSH_BROADCAST
    }

    /**
     * 操作类型 (请求 or 推送)
     */
    private ActionType action;

    /**
     * 消息数据载荷
     *
     * - 可能是：
     *   Message             -> 单条消息
     *   List<Message>       -> 多条消息记录
     *   ChatGroup           -> 单个群组
     *   List<ChatGroup>     -> 群组列表
     *   Long / String       -> 简单参数
     */
    private T data;
}
