package xlike.top.nettydemo.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友分组表
 * 用户可以将好友分类管理
 */
@Data
@TableName("chat_friend_group")
public class FriendGroup {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 分组名称 */
    private String groupName;

    /** 创建时间 */
    private LocalDateTime createTime;
}
