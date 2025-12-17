-- 向user_C表添加新字段
ALTER TABLE user_C 
ADD COLUMN user_agent VARCHAR(255) COMMENT '用户代理信息',
ADD COLUMN client_ip VARCHAR(45) COMMENT '客户端IP地址',
ADD COLUMN login_time DATETIME COMMENT '登录时间';