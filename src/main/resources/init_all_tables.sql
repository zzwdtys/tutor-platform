-- 完整建表脚本（原始表 + 新增表）
CREATE DATABASE IF NOT EXISTS tutor_db DEFAULT CHARSET utf8mb4;
USE tutor_db;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `openid` VARCHAR(100) DEFAULT NULL,
  `role` INT DEFAULT 0 COMMENT '0学员 1教员 2管理员',
  `nickname` VARCHAR(100) DEFAULT NULL,
  `avatar` VARCHAR(500) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `real_name` VARCHAR(50) DEFAULT NULL,
  `id_card` VARCHAR(20) DEFAULT NULL,
  `school` VARCHAR(200) DEFAULT NULL,
  `grade` VARCHAR(50) DEFAULT NULL,
  `teach_exp` VARCHAR(200) DEFAULT NULL,
  `status` INT DEFAULT 1 COMMENT '1正常',
  `username` VARCHAR(50) DEFAULT NULL,
  `password` VARCHAR(200) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `is_deleted` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 需求表
CREATE TABLE IF NOT EXISTS `demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `subject` VARCHAR(50) DEFAULT NULL,
  `grade` VARCHAR(50) DEFAULT NULL,
  `location` VARCHAR(200) DEFAULT NULL,
  `budget_min` INT DEFAULT NULL,
  `budget_max` INT DEFAULT NULL,
  `teacher_gender` INT DEFAULT 0,
  `description` TEXT,
  `status` INT DEFAULT 0 COMMENT '0待匹配 1已匹配 3已关闭',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 简历表
CREATE TABLE IF NOT EXISTS `resume` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `subjects` VARCHAR(200) DEFAULT NULL,
  `grades` VARCHAR(200) DEFAULT NULL,
  `price` INT DEFAULT NULL,
  `location` VARCHAR(200) DEFAULT NULL,
  `teaching_years` INT DEFAULT 0,
  `certificate` VARCHAR(500) DEFAULT NULL,
  `self_intro` TEXT,
  `status` INT DEFAULT 1 COMMENT '1已发布',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 预约表
CREATE TABLE IF NOT EXISTS `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `demand_id` BIGINT DEFAULT NULL,
  `resume_id` BIGINT DEFAULT NULL,
  `student_id` BIGINT DEFAULT NULL,
  `teacher_id` BIGINT DEFAULT NULL,
  `status` INT DEFAULT 0 COMMENT '0待接单 1已接单 2已拒绝 3已授课 4已完成 5已取消',
  `appointment_time` DATETIME DEFAULT NULL,
  `actual_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_student` (`student_id`),
  INDEX `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 评价表
CREATE TABLE IF NOT EXISTS `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `appointment_id` BIGINT DEFAULT NULL,
  `student_id` BIGINT DEFAULT NULL,
  `teacher_id` BIGINT DEFAULT NULL,
  `score` INT DEFAULT 0,
  `professionalism` INT DEFAULT 0,
  `patience` INT DEFAULT 0,
  `communication` INT DEFAULT 0,
  `comment_text` TEXT,
  `sentiment` INT DEFAULT 1,
  `tags` VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 关注表
CREATE TABLE IF NOT EXISTS `follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `teacher_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_student` (`student_id`),
  INDEX `idx_teacher` (`teacher_id`),
  UNIQUE KEY `uk_student_teacher` (`student_id`, `teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user1_id` BIGINT NOT NULL,
  `user2_id` BIGINT NOT NULL,
  `last_message` VARCHAR(500) DEFAULT '',
  `last_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `unread_count_user1` INT DEFAULT 0,
  `unread_count_user2` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user1` (`user1_id`),
  INDEX `idx_user2` (`user2_id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `content` TEXT,
  `is_read` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_conversation` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. AI对话历史表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `content` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
