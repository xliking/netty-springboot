package xlike.top.nettydemo.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import xlike.top.nettydemo.enums.MessageType;
import xlike.top.nettydemo.handler.MessageTypeHandler;

import java.time.LocalDateTime;

/**
 * @author Administrator
 */
@Data
@TableName("chat_message")
public class ChatMessage {


    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long groupId;

    @TableField(typeHandler = MessageTypeHandler.class)
    private MessageType messageType;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime sendTime;

    private Integer isRead;

    @TableField(exist = false)
    private String senderNickname;

}