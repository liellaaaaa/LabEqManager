-- 实验室管理与设备基础模块完整示例数据
-- 包含所有业务流程的各个状态示例
-- 使用前请确保已执行 lab_equipment_manager.sql 创建表结构

USE lab_equipment_manager;

-- ============================================
-- 1. 基础配置数据
-- ============================================

-- 确保设备状态数据存在（如果已存在则忽略）
INSERT IGNORE INTO `equipment_status` (`name`, `code`, `description`) VALUES
('待入库', 'pending', '设备已采购但尚未入库'),
('已入库', 'instored', '设备已入库，可投入使用'),
('使用中', 'inuse', '设备正在使用中'),
('维修中', 'repairing', '设备正在维修中'),
('报废', 'scrapped', '设备已报废');

-- ============================================
-- 2. 测试用户数据
-- ============================================

-- 插入测试用户（密码都是：123456，使用BCrypt加密后的值）
-- 注意：实际使用时需要根据你的BCrypt配置生成正确的密码哈希
-- 这里使用占位符，实际使用时需要替换为真实的BCrypt哈希值
INSERT IGNORE INTO `user` (`username`, `password`, `name`, `email`, `phone`, `department`, `role_code`, `status`) VALUES
('admin', '$2a$10$7rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6E', '系统管理员', 'admin@example.com', '13800138000', '信息中心', 'admin', 1),
('teacher001', '$2a$10$7rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6E', '张老师', 'teacher001@example.com', '13800138001', '计算机学院', 'teacher', 1),
('teacher002', '$2a$10$7rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6E', '李老师', 'teacher002@example.com', '13800138002', '电子工程学院', 'teacher', 1),
('student001', '$2a$10$7rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6E', '王同学', 'student001@example.com', '13800138003', '计算机学院', 'student', 1),
('student002', '$2a$10$7rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6E', '赵同学', 'student002@example.com', '13800138004', '电子工程学院', 'student', 1);

-- 关联用户角色（假设角色ID：1-管理员，2-教师，3-学生）
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES
((SELECT id FROM user WHERE username = 'admin'), (SELECT id FROM role WHERE code = 'admin')),
((SELECT id FROM user WHERE username = 'teacher001'), (SELECT id FROM role WHERE code = 'teacher')),
((SELECT id FROM user WHERE username = 'teacher002'), (SELECT id FROM role WHERE code = 'teacher')),
((SELECT id FROM user WHERE username = 'student001'), (SELECT id FROM role WHERE code = 'student')),
((SELECT id FROM user WHERE username = 'student002'), (SELECT id FROM role WHERE code = 'student'));

-- ============================================
-- 3. 实验室数据
-- ============================================

-- 插入实验室示例数据（2个实验室）
INSERT IGNORE INTO `laboratory` (`name`, `code`, `location`, `capacity`, `type`, `status`, `manager_id`, `description`) VALUES
('计算机基础实验室', 'LAB001', '科技楼301', 50, '计算机实验室', 1, (SELECT id FROM user WHERE username = 'teacher001'), '用于计算机基础课程教学，配备50台台式电脑'),
('电子电路实验室', 'LAB002', '实验楼201', 30, '电子实验室', 1, (SELECT id FROM user WHERE username = 'teacher002'), '用于电子电路实验，配备示波器、信号发生器等设备');

-- ============================================
-- 4. 设备数据
-- ============================================

-- 计算机基础实验室（LAB001）的设备
INSERT IGNORE INTO `equipment` (`name`, `model`, `specification`, `asset_code`, `unit_price`, `quantity`, `supplier`, `purchase_date`, `warranty_period`, `status_id`, `laboratory_id`, `description`) VALUES
('联想台式电脑', 'ThinkCentre M720t', 'i5-9500/8G/256G SSD/1T HDD', 'EQ20230001', 4500.00, 50, '联想科技有限公司', '2023-09-10', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '用于计算机基础课程教学'),
('戴尔显示器', 'Dell P2419H', '23.8英寸 IPS 1920x1080', 'EQ20230002', 1200.00, 50, '戴尔（中国）有限公司', '2023-09-10', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '配套台式电脑使用'),
('投影仪', '爱普生 CB-X41', 'XGA 1024x768 3500流明', 'EQ20230003', 3500.00, 1, '爱普生（中国）有限公司', '2023-08-15', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '用于教学投影');

-- 电子电路实验室（LAB002）的设备
INSERT IGNORE INTO `equipment` (`name`, `model`, `specification`, `asset_code`, `unit_price`, `quantity`, `supplier`, `purchase_date`, `warranty_period`, `status_id`, `laboratory_id`, `description`) VALUES
('数字示波器', 'Tektronix TBS1052B', '50MHz 双通道数字示波器', 'EQ20230004', 3500.00, 15, '泰克科技（中国）有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于电路信号测量'),
('函数信号发生器', 'RIGOL DG1022Z', '25MHz 双通道函数信号发生器', 'EQ20230005', 2800.00, 15, '普源精电科技股份有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于信号源输出'),
('数字万用表', 'Fluke 17B+', '数字万用表', 'EQ20230006', 500.00, 20, '福禄克测试仪器（上海）有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于电压电流测量');

-- ============================================
-- 5. 设备借用记录（6个状态各一条）
-- ============================================
-- 状态：0-待审批，1-已通过，2-已拒绝，3-已借出，4-已归还，5-已逾期

-- 0-待审批
INSERT IGNORE INTO `equipment_borrow` (`equipment_id`, `user_id`, `borrow_date`, `plan_return_date`, `actual_return_date`, `purpose`, `quantity`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230001' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student001' LIMIT 1),
    '2024-01-15 09:00:00',
    '2024-01-20 17:00:00',
    NULL,
    '课程实验需要',
    1,
    0,
    NULL,
    NULL,
    NULL
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student001');

-- 1-已通过
INSERT IGNORE INTO `equipment_borrow` (`equipment_id`, `user_id`, `borrow_date`, `plan_return_date`, `actual_return_date`, `purpose`, `quantity`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230002' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student001' LIMIT 1),
    '2024-01-10 09:00:00',
    '2024-01-15 17:00:00',
    NULL,
    '课程实验需要',
    1,
    1,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-10 10:00:00',
    '同意借用'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 2-已拒绝
INSERT IGNORE INTO `equipment_borrow` (`equipment_id`, `user_id`, `borrow_date`, `plan_return_date`, `actual_return_date`, `purpose`, `quantity`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230003' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student002' LIMIT 1),
    '2024-01-12 09:00:00',
    '2024-01-18 17:00:00',
    NULL,
    '个人项目使用',
    1,
    2,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-12 10:30:00',
    '设备正在维护中，暂不外借'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230003')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 3-已借出
INSERT IGNORE INTO `equipment_borrow` (`equipment_id`, `user_id`, `borrow_date`, `plan_return_date`, `actual_return_date`, `purpose`, `quantity`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230004' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student002' LIMIT 1),
    '2024-01-08 09:00:00',
    '2024-01-13 17:00:00',
    NULL,
    '课程实验需要',
    1,
    3,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-08 10:00:00',
    '同意借用，已借出'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230004')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 4-已归还
INSERT IGNORE INTO `equipment_borrow` (`equipment_id`, `user_id`, `borrow_date`, `plan_return_date`, `actual_return_date`, `purpose`, `quantity`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230005' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student001' LIMIT 1),
    '2024-01-05 09:00:00',
    '2024-01-10 17:00:00',
    '2024-01-10 16:30:00',
    '课程实验需要',
    1,
    4,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-05 10:00:00',
    '同意借用'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230005')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 5-已逾期
INSERT IGNORE INTO `equipment_borrow` (`equipment_id`, `user_id`, `borrow_date`, `plan_return_date`, `actual_return_date`, `purpose`, `quantity`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230006' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student002' LIMIT 1),
    '2024-01-01 09:00:00',
    '2024-01-05 17:00:00',
    NULL,
    '课程实验需要',
    1,
    5,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-01 10:00:00',
    '同意借用，已逾期未归还'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230006')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- ============================================
-- 6. 设备维修记录（4个状态各一条）
-- ============================================
-- 状态：0-待维修，1-维修中，2-已修好，3-无法修复

-- 0-待维修
INSERT IGNORE INTO `equipment_repair` (`equipment_id`, `reporter_id`, `report_date`, `fault_description`, `repair_result`, `repair_date`, `status`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230001' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher001' LIMIT 1),
    '2024-01-15 14:00:00',
    '电脑无法开机，按电源键无反应',
    NULL,
    NULL,
    0
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher001');

-- 1-维修中
INSERT IGNORE INTO `equipment_repair` (`equipment_id`, `reporter_id`, `report_date`, `fault_description`, `repair_result`, `repair_date`, `status`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230002' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher001' LIMIT 1),
    '2024-01-12 10:00:00',
    '显示器屏幕出现花屏，显示异常',
    '正在更换显示面板',
    '2024-01-13 09:00:00',
    1
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher001');

-- 2-已修好
INSERT IGNORE INTO `equipment_repair` (`equipment_id`, `reporter_id`, `report_date`, `fault_description`, `repair_result`, `repair_date`, `status`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230003' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher001' LIMIT 1),
    '2024-01-08 11:00:00',
    '投影仪灯泡不亮，无法投影',
    '已更换新灯泡，测试正常',
    '2024-01-09 15:00:00',
    2
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230003')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher001');

-- 3-无法修复
INSERT IGNORE INTO `equipment_repair` (`equipment_id`, `reporter_id`, `report_date`, `fault_description`, `repair_result`, `repair_date`, `status`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230004' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher002' LIMIT 1),
    '2024-01-05 09:00:00',
    '示波器主板损坏，无法修复',
    '主板严重损坏，维修成本过高，建议报废',
    '2024-01-06 16:00:00',
    3
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230004')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher002');

-- ============================================
-- 7. 设备报废记录（3个状态各一条）
-- ============================================
-- 状态：0-待审批，1-已通过，2-已拒绝

-- 0-待审批
INSERT IGNORE INTO `equipment_scrap` (`equipment_id`, `applicant_id`, `apply_date`, `scrap_reason`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230005' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher002' LIMIT 1),
    '2024-01-15 10:00:00',
    '设备使用年限过长，性能已无法满足教学需求，且维修成本过高',
    0,
    NULL,
    NULL,
    NULL
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230005')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher002');

-- 1-已通过
INSERT IGNORE INTO `equipment_scrap` (`equipment_id`, `applicant_id`, `apply_date`, `scrap_reason`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230006' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher002' LIMIT 1),
    '2024-01-10 09:00:00',
    '设备严重损坏，无法修复，已确认无维修价值',
    1,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-10 14:00:00',
    '同意报废申请'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230006')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 2-已拒绝
INSERT IGNORE INTO `equipment_scrap` (`equipment_id`, `applicant_id`, `apply_date`, `scrap_reason`, `status`, `approver_id`, `approve_time`, `approve_remark`)
SELECT 
    (SELECT id FROM equipment WHERE asset_code = 'EQ20230001' LIMIT 1),
    (SELECT id FROM user WHERE username = 'teacher001' LIMIT 1),
    '2024-01-08 11:00:00',
    '设备使用年限较长，但功能正常，建议继续使用',
    2,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-08 15:00:00',
    '设备功能正常，建议继续使用，暂不批准报废'
WHERE EXISTS (SELECT 1 FROM equipment WHERE asset_code = 'EQ20230001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'teacher001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- ============================================
-- 8. 实验室预约记录（6个状态各一条）
-- ============================================
-- 状态：0-待审批，1-已通过，2-已拒绝，3-已取消，4-已完成，5-已使用

-- 0-待审批
INSERT IGNORE INTO `laboratory_reservation` (`laboratory_id`, `user_id`, `reserve_date`, `start_time`, `end_time`, `purpose`, `status`, `approver_id`, `approve_time`, `approve_remark`, `actual_start_time`, `actual_end_time`, `usage_remark`)
SELECT 
    (SELECT id FROM laboratory WHERE code = 'LAB001' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student001' LIMIT 1),
    '2024-01-20',
    '08:00:00',
    '12:00:00',
    '课程实验',
    0,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL
WHERE EXISTS (SELECT 1 FROM laboratory WHERE code = 'LAB001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student001');

-- 1-已通过
INSERT IGNORE INTO `laboratory_reservation` (`laboratory_id`, `user_id`, `reserve_date`, `start_time`, `end_time`, `purpose`, `status`, `approver_id`, `approve_time`, `approve_remark`, `actual_start_time`, `actual_end_time`, `usage_remark`)
SELECT 
    (SELECT id FROM laboratory WHERE code = 'LAB001' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student002' LIMIT 1),
    '2024-01-18',
    '14:00:00',
    '18:00:00',
    '课程实验',
    1,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-17 10:00:00',
    '同意预约',
    NULL,
    NULL,
    NULL
WHERE EXISTS (SELECT 1 FROM laboratory WHERE code = 'LAB001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 2-已拒绝
INSERT IGNORE INTO `laboratory_reservation` (`laboratory_id`, `user_id`, `reserve_date`, `start_time`, `end_time`, `purpose`, `status`, `approver_id`, `approve_time`, `approve_remark`, `actual_start_time`, `actual_end_time`, `usage_remark`)
SELECT 
    (SELECT id FROM laboratory WHERE code = 'LAB002' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student001' LIMIT 1),
    '2024-01-19',
    '08:00:00',
    '12:00:00',
    '个人项目',
    2,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-17 11:00:00',
    '该时间段已有课程安排',
    NULL,
    NULL,
    NULL
WHERE EXISTS (SELECT 1 FROM laboratory WHERE code = 'LAB002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 3-已取消
INSERT IGNORE INTO `laboratory_reservation` (`laboratory_id`, `user_id`, `reserve_date`, `start_time`, `end_time`, `purpose`, `status`, `approver_id`, `approve_time`, `approve_remark`, `actual_start_time`, `actual_end_time`, `usage_remark`)
SELECT 
    (SELECT id FROM laboratory WHERE code = 'LAB002' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student002' LIMIT 1),
    '2024-01-16',
    '14:00:00',
    '18:00:00',
    '课程实验',
    3,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-15 10:00:00',
    '同意预约',
    NULL,
    NULL,
    NULL
WHERE EXISTS (SELECT 1 FROM laboratory WHERE code = 'LAB002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 4-已完成
INSERT IGNORE INTO `laboratory_reservation` (`laboratory_id`, `user_id`, `reserve_date`, `start_time`, `end_time`, `purpose`, `status`, `approver_id`, `approve_time`, `approve_remark`, `actual_start_time`, `actual_end_time`, `usage_remark`)
SELECT 
    (SELECT id FROM laboratory WHERE code = 'LAB001' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student001' LIMIT 1),
    '2024-01-14',
    '08:00:00',
    '12:00:00',
    '课程实验',
    4,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-13 10:00:00',
    '同意预约',
    '08:10:00',
    '11:50:00',
    '实验顺利完成'
WHERE EXISTS (SELECT 1 FROM laboratory WHERE code = 'LAB001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student001')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- 5-已使用
INSERT IGNORE INTO `laboratory_reservation` (`laboratory_id`, `user_id`, `reserve_date`, `start_time`, `end_time`, `purpose`, `status`, `approver_id`, `approve_time`, `approve_remark`, `actual_start_time`, `actual_end_time`, `usage_remark`)
SELECT 
    (SELECT id FROM laboratory WHERE code = 'LAB002' LIMIT 1),
    (SELECT id FROM user WHERE username = 'student002' LIMIT 1),
    '2024-01-17',
    '14:00:00',
    '18:00:00',
    '课程实验',
    5,
    (SELECT id FROM user WHERE username = 'admin' LIMIT 1),
    '2024-01-16 10:00:00',
    '同意预约',
    '14:05:00',
    NULL,
    '实验进行中'
WHERE EXISTS (SELECT 1 FROM laboratory WHERE code = 'LAB002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'student002')
  AND EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- ============================================
-- 验证数据插入结果
-- ============================================

-- 查看设备状态数据
SELECT * FROM equipment_status ORDER BY id;

-- 查看用户数据
SELECT id, username, name, role_code, department FROM user ORDER BY id;

-- 查看实验室数据
SELECT id, name, code, location, status, type FROM laboratory ORDER BY id;

-- 查看设备数据
SELECT 
    e.id,
    e.name AS equipment_name,
    e.asset_code,
    e.quantity,
    l.name AS laboratory_name,
    es.name AS status_name
FROM equipment e
LEFT JOIN laboratory l ON e.laboratory_id = l.id
LEFT JOIN equipment_status es ON e.status_id = es.id
ORDER BY e.id;

-- 统计各实验室的设备数量
SELECT 
    l.code AS lab_code,
    l.name AS lab_name,
    COUNT(e.id) AS equipment_count
FROM laboratory l
LEFT JOIN equipment e ON l.id = e.laboratory_id
GROUP BY l.id, l.code, l.name
ORDER BY l.id;

-- 查看设备借用记录（按状态分组）
SELECT 
    status,
    CASE status
        WHEN 0 THEN '待审批'
        WHEN 1 THEN '已通过'
        WHEN 2 THEN '已拒绝'
        WHEN 3 THEN '已借出'
        WHEN 4 THEN '已归还'
        WHEN 5 THEN '已逾期'
    END AS status_name,
    COUNT(*) AS count
FROM equipment_borrow
GROUP BY status
ORDER BY status;

-- 查看设备维修记录（按状态分组）
SELECT 
    status,
    CASE status
        WHEN 0 THEN '待维修'
        WHEN 1 THEN '维修中'
        WHEN 2 THEN '已修好'
        WHEN 3 THEN '无法修复'
    END AS status_name,
    COUNT(*) AS count
FROM equipment_repair
GROUP BY status
ORDER BY status;

-- 查看设备报废记录（按状态分组）
SELECT 
    status,
    CASE status
        WHEN 0 THEN '待审批'
        WHEN 1 THEN '已通过'
        WHEN 2 THEN '已拒绝'
    END AS status_name,
    COUNT(*) AS count
FROM equipment_scrap
GROUP BY status
ORDER BY status;

-- 查看实验室预约记录（按状态分组）
SELECT 
    status,
    CASE status
        WHEN 0 THEN '待审批'
        WHEN 1 THEN '已通过'
        WHEN 2 THEN '已拒绝'
        WHEN 3 THEN '已取消'
        WHEN 4 THEN '已完成'
        WHEN 5 THEN '已使用'
    END AS status_name,
    COUNT(*) AS count
FROM laboratory_reservation
GROUP BY status
ORDER BY status;

-- 说明：
-- 1. 本脚本包含所有业务流程的各个状态示例数据
-- 2. 设备借用：6个状态各一条记录（0-待审批，1-已通过，2-已拒绝，3-已借出，4-已归还，5-已逾期）
-- 3. 设备维修：4个状态各一条记录（0-待维修，1-维修中，2-已修好，3-无法修复）
-- 4. 设备报废：3个状态各一条记录（0-待审批，1-已通过，2-已拒绝）
-- 5. 实验室预约：6个状态各一条记录（0-待审批，1-已通过，2-已拒绝，3-已取消，4-已完成，5-已使用）
-- 6. 使用 INSERT IGNORE 避免重复插入，可以安全地重复执行
-- 7. 所有日期时间使用子查询获取用户ID和设备ID，避免硬编码