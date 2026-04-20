CREATE TABLE `web_user`
(
    `id`            BIGINT       NOT NULL COMMENT 'ID',
    `user_name`     VARCHAR(255) NOT NULL COMMENT '用户名称',
    `user_password` VARCHAR(255) NOT NULL COMMENT '用户密码',
    `status`        INT      DEFAULT NULL COMMENT '状态',
    `create_time`   DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted`    INT      DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户名称表';