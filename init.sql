-- ----------------------------
-- 1. 用户表 (user)
-- 存储用户信息
-- ----------------------------
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(64) NOT NULL COMMENT '用户名/登录账号',
                        `password` varchar(128) NOT NULL COMMENT '密码 (经过加密存储)',
                        `nickname` varchar(64) NOT NULL COMMENT '用户昵称',
                        `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像URL',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 群组表 (chat_group)
-- 存储群组信息
-- ----------------------------
CREATE TABLE `chat_group` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群组ID',
                              `group_name` varchar(64) NOT NULL COMMENT '群组名称',
                              `owner_id` bigint(20) NOT NULL COMMENT '群主的用户ID',
                              `avatar` varchar(255) DEFAULT NULL COMMENT '群头像URL',
                              `announcement` text COMMENT '群公告',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

-- ----------------------------
-- 3. 群组成员表 (group_member)
-- 存储用户和群组的多对多关系
-- ----------------------------
CREATE TABLE `group_member` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `group_id` bigint(20) NOT NULL COMMENT '群组ID',
                                `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                `member_nickname` varchar(64) DEFAULT NULL COMMENT '在群里的昵称',
                                `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_group_user` (`group_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';

-- ----------------------------
-- 4. 消息记录表 (message)
-- 存储单聊和群聊的消息
-- ----------------------------
CREATE TABLE `message` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                           `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
                           `receiver_id` bigint(20) DEFAULT NULL COMMENT '接收者ID (单聊时使用)',
                           `group_id` bigint(20) DEFAULT NULL COMMENT '接收群组ID (群聊时使用)',
                           `message_type` tinyint(4) NOT NULL COMMENT '消息类型 (1:文本, 2:图片, 3:文件, 4:视频, 5:链接)',
                           `content` text NOT NULL COMMENT '消息内容 (根据类型不同，存储不同格式，如文本内容、文件URL等)',
                           `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                           `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读 (仅单聊有效, 0:未读, 1:已读)',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';