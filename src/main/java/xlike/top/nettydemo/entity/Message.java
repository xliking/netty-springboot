package xlike.top.nettydemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import xlike.top.nettydemo.model.ChatMessage;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long groupId;
    private Integer messageType;
    private String content;
    private LocalDateTime sendTime;
    private Integer isRead;
}