CREATE TABLE `device_fault_records` (
  `record_id` BIGINT(20) NOT NULL COMMENT '主键ID（非自增，需手动赋值）',
  `fault_id` VARCHAR(64) NOT NULL COMMENT '故障ID（业务唯一标识）',
  `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
  `fault_type` INT(11) NOT NULL COMMENT '故障类型枚举（1-硬件，2-软件）',
  `fault_level` INT(11) NOT NULL COMMENT '故障级别枚举（1-低，2-中，3-高）',
  `occur_time` DATETIME(3) NOT NULL COMMENT '事件发生时间（含毫秒精度）',
  `op_type` INT(11) NOT NULL COMMENT '操作类型枚举（1-故障产生，2-派单，3-修复）',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障记录表';