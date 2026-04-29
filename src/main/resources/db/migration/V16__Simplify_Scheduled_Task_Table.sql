-- 简化定时任务表结构，移除冗余字段，优化设计
-- 1. 将params和parameter_types数据迁移到task_args（如果存在）
-- 2. 删除params和parameter_types字段
-- 3. 添加必要的约束和注释

-- 第一步：将现有的params+parameter_types数据迁移到task_args（如果task_args为空且params不为空）
UPDATE sys_scheduled_task 
SET task_args = JSON_OBJECT('__legacy_params', params, '__legacy_param_types', parameter_types)
WHERE (task_args IS NULL OR task_args = '') 
  AND (params IS NOT NULL AND params != '');

-- 第二步：删除params和parameter_types列
ALTER TABLE sys_scheduled_task 
DROP COLUMN params,
DROP COLUMN parameter_types;

-- 第三步：添加状态约束（0-停用，1-启用）
ALTER TABLE sys_scheduled_task 
MODIFY COLUMN status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用';

-- 第四步：添加任务名称唯一约束，防止重复
ALTER TABLE sys_scheduled_task 
ADD UNIQUE INDEX uk_task_name (task_name);

-- 第五步：添加cron表达式校验注释（应用层校验，这里只加注释）
-- cron表达式格式校验由应用层保证

-- 第六步：优化task_args字段注释
ALTER TABLE sys_scheduled_task 
MODIFY COLUMN task_args TEXT DEFAULT NULL COMMENT '任务参数(JSON格式)。支持两种模式：
1. 位置参数模式：["value1", "value2"] - 按位置匹配方法参数
2. 命名参数模式：{"param1":"value1","param2":"value2"} - 按参数名匹配（需方法参数使用@Param注解）';