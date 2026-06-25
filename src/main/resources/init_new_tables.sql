-- ============================================
-- 在线家教服务平台 - 新增表结构
-- 数据库: tutor_db
-- 使用方法: 在 MySQL 中执行此脚本
-- ============================================

-- 1. 关注表
CREATE TABLE IF NOT EXISTS `follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT '学员用户ID',
  `teacher_id` BIGINT NOT NULL COMMENT '教员用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_student` (`student_id`),
  INDEX `idx_teacher` (`teacher_id`),
  UNIQUE KEY `uk_student_teacher` (`student_id`, `teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- 2. 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user1_id` BIGINT NOT NULL COMMENT '用户1 ID（较小的ID）',
  `user2_id` BIGINT NOT NULL COMMENT '用户2 ID（较大的ID）',
  `last_message` VARCHAR(500) DEFAULT '' COMMENT '最后一条消息预览',
  `last_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
  `unread_count_user1` INT DEFAULT 0 COMMENT '用户1未读数',
  `unread_count_user2` INT DEFAULT 0 COMMENT '用户2未读数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user1` (`user1_id`),
  INDEX `idx_user2` (`user2_id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 3. 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
  `content` TEXT COMMENT '消息内容',
  `is_read` INT DEFAULT 0 COMMENT '0-未读 1-已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_conversation` (`conversation_id`),
  INDEX `idx_sender` (`sender_id`),
  INDEX `idx_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 4. AI对话历史表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: user / assistant',
  `content` TEXT COMMENT '对话内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

-- 5. 修改 appointment 表：添加拒绝理由和取消理由
ALTER TABLE `appointment`
  ADD COLUMN IF NOT EXISTS `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '教员拒绝理由',
  ADD COLUMN IF NOT EXISTS `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '学员取消理由';

-- 6. 修改 demand 表：添加浏览量
ALTER TABLE `demand`
  ADD COLUMN IF NOT EXISTS `view_count` INT DEFAULT 0 COMMENT '浏览量';
