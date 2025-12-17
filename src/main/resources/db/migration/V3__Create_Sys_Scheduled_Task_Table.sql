CREATE TABLE `sys_scheduled_task` (
  `id` BIGINT(20) NOT NULL COMMENT '主键',
  `task_name` VARCHAR(255) NOT NULL COMMENT '任务唯一标识',
  `cron_expression` VARCHAR(255) NOT NULL COMMENT 'cron表达式',
  `bean_name` VARCHAR(255) DEFAULT NULL COMMENT 'Spring Bean名称',
  `method_name` VARCHAR(255) NOT NULL COMMENT '执行方法',
  `params` TEXT DEFAULT NULL COMMENT '方法参数 list内部必须承载的都是可序列化对象',
  `parameter_types` TEXT DEFAULT NULL COMMENT '方法参数类型数组，用逗号分隔例如：String.class,Integer.class',
  `status` INT(11) DEFAULT NULL COMMENT '状态',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `is_deleted` INT(11) DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态定时任务表';