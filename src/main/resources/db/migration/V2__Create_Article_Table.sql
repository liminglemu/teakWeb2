CREATE TABLE `article` (
  `id` BIGINT NOT NULL COMMENT '文章ID',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '文章分标题',
  `cate_id` BIGINT DEFAULT NULL COMMENT '文章分类Id',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户Id',
  `status` INT DEFAULT NULL COMMENT '状态',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `is_deleted` INT DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';