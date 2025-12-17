CREATE TABLE `order` (
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `user_id` VARCHAR(255) NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status` VARCHAR(50) NOT NULL COMMENT '订单状态',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';