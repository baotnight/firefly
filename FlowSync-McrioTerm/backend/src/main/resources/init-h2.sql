-- H2 初始化脚本（自动执行，与 MySQL init.sql 结构一致）

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    real_name   VARCHAR(50)  NOT NULL,
    role        VARCHAR(30)  NOT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    email       VARCHAR(100) DEFAULT NULL,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS project_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    status      VARCHAR(20)  NOT NULL,
    priority    VARCHAR(20)  NOT NULL,
    owner_id    BIGINT       NOT NULL,
    start_date  DATE         DEFAULT NULL,
    end_date    DATE         DEFAULT NULL,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (owner_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS task_info (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    project_id    BIGINT        NOT NULL,
    parent_id     BIGINT        DEFAULT NULL,
    title         VARCHAR(100)  NOT NULL,
    description   VARCHAR(1000) DEFAULT NULL,
    assignee_id   BIGINT        DEFAULT NULL,
    creator_id    BIGINT        DEFAULT NULL,
    status        VARCHAR(20)   NOT NULL,
    priority      VARCHAR(20)   NOT NULL,
    due_date      DATE          DEFAULT NULL,
    ai_suggestion TEXT          DEFAULT NULL,
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (project_id)  REFERENCES project_info(id),
    FOREIGN KEY (parent_id)   REFERENCES task_info(id),
    FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    FOREIGN KEY (creator_id)  REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS task_log (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    task_id          BIGINT        NOT NULL,
    progress_percent INT           NOT NULL,
    content          VARCHAR(1000) NOT NULL,
    operator_id      BIGINT        DEFAULT NULL,
    create_time      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (task_id)    REFERENCES task_info(id),
    FOREIGN KEY (operator_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS task_summary (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    project_id   BIGINT        NOT NULL,
    task_id      BIGINT        DEFAULT NULL,
    summary_type VARCHAR(30)   NOT NULL,
    content      VARCHAR(2000) NOT NULL,
    created_by   BIGINT        DEFAULT NULL,
    create_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (project_id) REFERENCES project_info(id),
    FOREIGN KEY (task_id)    REFERENCES task_info(id),
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS operation_log (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    operator_id  BIGINT        DEFAULT NULL,
    action       VARCHAR(50)   NOT NULL,
    target_type  VARCHAR(30)   DEFAULT NULL,
    target_id    BIGINT        DEFAULT NULL,
    detail       VARCHAR(500)  DEFAULT NULL,
    create_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (operator_id) REFERENCES sys_user(id)
);

-- 预置用户（密码均为 123456 的 BCrypt 哈希）
INSERT INTO sys_user (username, password, real_name, role) VALUES
('leader',  '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '项目负责人', '负责人'),
('member1', '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '王小明',     '组员'),
('member2', '$2b$10$JTDbxruQLYGoMzrRbApPlej53AIE9CrFwZM/qZGhdv0h9mXlagGCO', '李小华',     '组员');
