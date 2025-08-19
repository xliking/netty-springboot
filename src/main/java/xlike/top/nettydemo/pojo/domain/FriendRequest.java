package xlike.top.nettydemo.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请表
 * 存储用户之间的好友申请记录
 */
@Data
@TableName("chat_friend_request")
public class FriendRequest {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请人ID */
    private Long fromUserId;

    /** 接收人ID */
    private Long toUserId;

    /** 申请附言 */
    private String message;

    /** 状态 (0=待处理, 1=已同意, 2=已拒绝) */
    private Integer status;

    /** 申请时间 */
    private LocalDateTime createTime;

    /** 处理时间 */
    private LocalDateTime handleTime;
}
