package xlike.top.nettydemo.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

/**
 * 好友关系表
 * 存储两个用户之间的好友关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_friend")
public class Friend {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 好友用户ID */
    private Long friendId;

    /** 备注名 */
    private String remark;

    /** 分组ID */
    private Long groupId;

    /** 成为好友时间 */
    private LocalDateTime createTime;
}
