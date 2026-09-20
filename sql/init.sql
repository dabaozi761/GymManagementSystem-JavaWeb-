-- =============================================
-- 健身房管理系统 数据库初始化脚本(MySQL 8.0)
-- 执行方式:Navicat 中直接运行,或命令行:
--   mysql -root -p1234 -e "source D:/idea/GymManagementSystem-JavaWeb-/sql/init.sql"
-- 注意:脚本含 DROP TABLE,重复执行会清空已有数据
-- =============================================

CREATE DATABASE IF NOT EXISTS `gym_management_system`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `gym_management_system`;

DROP TABLE IF EXISTS `course_enrollment`;
DROP TABLE IF EXISTS `training_log`;
DROP TABLE IF EXISTS `training_plan`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `membership_card`;
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `equipment`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `admin`;

-- ========== 1. 管理员表 ==========
CREATE TABLE `admin` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50) unique  NOT NULL COMMENT '管理员用户名(唯一)',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(加密存储)',
    `name`        VARCHAR(50)  NOT NULL COMMENT '姓名',
    `phone`       VARCHAR(20) unique  DEFAULT NULL COMMENT '手机号(可空)',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1 正常 0 禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表';

-- ========== 2. 用户表 ==========
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50) unique  NOT NULL COMMENT '用户名(唯一)',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(加密存储)',
    `phone`       VARCHAR(20) unique  DEFAULT NULL COMMENT '手机号(可空)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1 正常 0 禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表';

-- ========== 3. 会员表 ==========
CREATE TABLE `member` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id`   VARCHAR(32) unique  NOT NULL COMMENT '会员编号(业务号,如 M202609200001)',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '关联 user.id(逻辑外键,可空:线下会员可无账号)',
    `name`        VARCHAR(50)  NOT NULL COMMENT '姓名',
    `gender`      TINYINT      DEFAULT NULL COMMENT '性别:1 男 2 女',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `birthday`    DATE         DEFAULT NULL COMMENT '出生日期',
    `join_date`   DATE         NOT NULL COMMENT '入会日期',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1 正常(在籍) 0 已退会',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员表';

-- ========== 4. 会员卡表 ==========
CREATE TABLE `membership_card` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `card_no`      VARCHAR(32) unique   NOT NULL COMMENT '卡号(唯一)',
    `member_id`    BIGINT        NOT NULL COMMENT '所属会员 id(逻辑外键 member.id)',
    `card_type`    TINYINT       NOT NULL COMMENT '卡类型:1 月卡 2 季卡 3 年卡 4 次卡 5 储值卡',
    `balance`      DECIMAL(10,2) DEFAULT NULL COMMENT '余额(储值卡使用)',
    `total_count`  INT           DEFAULT NULL COMMENT '总次数(次卡使用)',
    `remain_count` INT           DEFAULT NULL COMMENT '剩余次数(次卡使用)',
    `start_date`   DATE          DEFAULT NULL COMMENT '有效期开始(月/季/年卡使用)',
    `end_date`     DATE          DEFAULT NULL COMMENT '有效期结束(月/季/年卡使用)',
    `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:1 正常 2 冻结 3 过期 4 注销',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员卡表';

-- ========== 5. 器材表 ==========
CREATE TABLE `equipment` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `equipment_no`   VARCHAR(32) unique   NOT NULL COMMENT '器材编号(唯一)',
    `name`           VARCHAR(100)  NOT NULL COMMENT '器材名称(如:跑步机)',
    `type`           TINYINT       NOT NULL COMMENT '类型:1 有氧 2 力量 3 自由重量 4 其他',
    `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:1 可用 2 维修中 3 报废',
    `purchase_date`  DATE          DEFAULT NULL COMMENT '购入日期',
    `purchase_price` DECIMAL(10,2) DEFAULT NULL COMMENT '购入价格',
    `remark`         VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '器材表';

-- ========== 6. 课程表 ==========
CREATE TABLE `course` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_no`   VARCHAR(32) unique   NOT NULL COMMENT '课程编号(唯一)',
    `name`        VARCHAR(100)  NOT NULL COMMENT '课程名称',
    `type`        TINYINT       NOT NULL COMMENT '类型:1 团课 2 私教 3 其他',
    `coach_name`  VARCHAR(50)   DEFAULT NULL COMMENT '教练姓名(未建教练表,暂存姓名)',
    `price`       DECIMAL(10,2) DEFAULT NULL COMMENT '课程价格',
    `capacity`    INT           DEFAULT NULL COMMENT '报名人数上限(可空表示不限)',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:1 开课 0 停课',
    `remark`      VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程表';

-- ========== 7. 课程报名表 ==========
CREATE TABLE `course_enrollment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_id`   BIGINT       NOT NULL COMMENT '课程 id(逻辑外键 course.id)',
    `member_id`   BIGINT       NOT NULL COMMENT '报名会员 id(逻辑外键 member.id)',
    `enroll_date` DATE         NOT NULL DEFAULT (CURDATE()) COMMENT '报名日期',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1 已报名 2 已取消 3 已完成',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程报名表';

-- ========== 8. 训练计划表 ==========
CREATE TABLE `training_plan` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id`   BIGINT       NOT NULL COMMENT '会员 id(逻辑外键 member.id)',
    `admin_id`    BIGINT       NOT NULL COMMENT '制定计划的管理员 id(逻辑外键 admin.id)',
    `title`       VARCHAR(100) NOT NULL COMMENT '计划名称',
    `goal`        VARCHAR(50)  DEFAULT NULL COMMENT '训练目标(如:减脂/增肌/塑形)',
    `content`     TEXT         DEFAULT NULL COMMENT '计划内容描述',
    `start_date`  DATE         DEFAULT NULL COMMENT '计划开始日期',
    `end_date`    DATE         DEFAULT NULL COMMENT '计划结束日期',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1 进行中 2 已完成 3 已取消',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_admin_id` (`admin_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '训练计划表';

-- ========== 9. 训练日志表 ==========
CREATE TABLE `training_log` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `training_plan_id` BIGINT       NOT NULL COMMENT '训练计划 id(逻辑外键 training_plan.id)',
    `train_date`       DATE         NOT NULL DEFAULT (CURDATE()) COMMENT '训练日期',
    `duration_minutes` INT          DEFAULT NULL COMMENT '训练时长(分钟)',
    `content`          TEXT         DEFAULT NULL COMMENT '训练内容记录',
    `feeling`          VARCHAR(255) DEFAULT NULL COMMENT '身体感受',
    `remark`           VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除:0 未删除 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_training_plan_id` (`training_plan_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '训练日志表';
