-- 向user_C表添加示例数据
INSERT INTO user_C (PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, MAX_INACTIVE_INTERVAL, EXPIRY_TIME, PRINCIPAL_NAME, user_agent, client_ip, login_time) 
VALUES 
('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'sess001', 1672502400000, 1672506000000, 1800, 1672513200000, 'test_user1', 'Mozilla/5.0', '192.168.1.100', NOW()),
('ffffffff-gggg-hhhh-iiii-jjjjjjjjjjjj', 'sess002', 1672502500000, 1672506100000, 1800, 1672513300000, 'test_user2', 'Chrome/98.0', '192.168.1.101', NOW());