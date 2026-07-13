-- =====================================================
-- FlowSync 数据库初始化脚本
-- 数据库: flowsync_simple
-- 字符集: utf8mb4
-- =====================================================

-- 确保客户端使用 UTF-8 编码传输数据
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS flowsync_simple
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE flowsync_simple;

-- -----------------------------------------------------
-- 1. 用户表 sys_user
-- -----------------------------------------------------
DROP TABLE IF EXISTS invite_code;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS task_summary;
DROP TABLE IF EXISTS task_log;
DROP TABLE IF EXISTS task_info;
DROP TABLE IF EXISTS project_info;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username    VARCHAR(50)  NOT NULL                 COMMENT '用户名（登录账号）',
    password    VARCHAR(100) NOT NULL                 COMMENT '密码（明文存储，教学简化版）',
    real_name   VARCHAR(100) NOT NULL                 COMMENT '真实姓名',
    role        VARCHAR(30)  NOT NULL                 COMMENT '角色：负责人/组员',
    phone       VARCHAR(20)  DEFAULT NULL             COMMENT '联系电话',
    email       VARCHAR(100) DEFAULT NULL             COMMENT '电子邮箱',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------
-- 2. 项目表 project_info
-- -----------------------------------------------------
CREATE TABLE project_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name        VARCHAR(100) NOT NULL                 COMMENT '项目名称',
    description VARCHAR(500) DEFAULT NULL             COMMENT '项目说明',
    status      VARCHAR(20)  NOT NULL                 COMMENT '项目状态：未开始/进行中/已完成',
    priority    VARCHAR(20)  NOT NULL                 COMMENT '优先级：低/中/高',
    owner_id    BIGINT       NOT NULL                 COMMENT '项目负责人 ID',
    start_date  DATE         DEFAULT NULL             COMMENT '开始日期',
    end_date    DATE         DEFAULT NULL             COMMENT '结束日期',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- -----------------------------------------------------
-- 3. 任务表 task_info
-- -----------------------------------------------------
CREATE TABLE task_info (
    id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    project_id    BIGINT        NOT NULL                 COMMENT '所属项目 ID',
    parent_id     BIGINT        DEFAULT NULL             COMMENT '父任务 ID（自关联，支持多层级）',
    title         VARCHAR(100)  NOT NULL                 COMMENT '任务标题',
    description   VARCHAR(1000) DEFAULT NULL             COMMENT '任务说明',
    assignee_id   BIGINT        DEFAULT NULL             COMMENT '负责人 ID',
    creator_id    BIGINT        DEFAULT NULL             COMMENT '创建人 ID',
    status        VARCHAR(20)   NOT NULL                 COMMENT '任务状态：未开始/进行中/已完成',
    priority      VARCHAR(20)   NOT NULL                 COMMENT '优先级：低/中/高',
    due_date      DATE          DEFAULT NULL             COMMENT '截止日期',
    ai_suggestion TEXT          DEFAULT NULL             COMMENT '千问 AI 建议内容',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_task_project  FOREIGN KEY (project_id)  REFERENCES project_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_parent   FOREIGN KEY (parent_id)   REFERENCES task_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_creator  FOREIGN KEY (creator_id)  REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- -----------------------------------------------------
-- 4. 进度记录表 task_log
-- -----------------------------------------------------
CREATE TABLE task_log (
    id               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    task_id          BIGINT        NOT NULL                 COMMENT '所属任务 ID',
    progress_percent INT           NOT NULL                 COMMENT '进度百分比（0-100）',
    content          VARCHAR(1000) NOT NULL                 COMMENT '进度说明内容',
    operator_id      BIGINT        DEFAULT NULL             COMMENT '记录人 ID',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_log_task   FOREIGN KEY (task_id)    REFERENCES task_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进度记录表';

-- -----------------------------------------------------
-- 5. 总结表 task_summary
-- -----------------------------------------------------
CREATE TABLE task_summary (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    project_id   BIGINT        NOT NULL                 COMMENT '所属项目 ID',
    task_id      BIGINT        DEFAULT NULL             COMMENT '关联任务 ID（可选）',
    summary_type VARCHAR(30)   NOT NULL                 COMMENT '总结类型：阶段总结/最终总结',
    content      VARCHAR(2000) NOT NULL                 COMMENT '总结内容',
    created_by   BIGINT        DEFAULT NULL             COMMENT '创建人 ID',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_summary_project FOREIGN KEY (project_id) REFERENCES project_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_summary_task    FOREIGN KEY (task_id)    REFERENCES task_info(id) ON DELETE SET NULL,
    CONSTRAINT fk_summary_creator FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='总结表';

-- -----------------------------------------------------
-- 6. 操作日志表 operation_log
-- -----------------------------------------------------
CREATE TABLE operation_log (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    operator_id  BIGINT        DEFAULT NULL             COMMENT '操作人 ID',
    action       VARCHAR(50)   NOT NULL                 COMMENT '操作类型（登录/创建项目/编辑任务等）',
    target_type  VARCHAR(30)   DEFAULT NULL             COMMENT '操作对象类型（用户/项目/任务）',
    target_id    BIGINT        DEFAULT NULL             COMMENT '操作对象 ID',
    detail       VARCHAR(500)  DEFAULT NULL             COMMENT '操作详情',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_oplog_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------------------
-- 7. 邀请码表 invite_code
-- -----------------------------------------------------
CREATE TABLE invite_code (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    code         VARCHAR(20)  NOT NULL                 COMMENT '邀请码',
    created_by   BIGINT       DEFAULT NULL             COMMENT '创建人（管理员）',
    used         TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '是否已使用',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_invite_creator FOREIGN KEY (created_by) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请码表';

-- -----------------------------------------------------
-- 预置用户数据
-- -----------------------------------------------------
-- 密码统一为 123456 的 BCrypt 哈希（cost=10）
INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin',   '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '系统管理员', '管理员'),
('leader',  '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '项目负责人', '负责人'),
('member1', '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '王小明',     '组员'),
('member2', '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '李小华',     '组员');
