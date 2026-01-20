-- 实验室管理与设备基础模块示例数据
-- 使用前请确保已执行 lab_equipment_manager.sql 创建表结构

USE lab_equipment_manager;

-- 确保设备状态数据存在（如果已存在则忽略）
INSERT IGNORE INTO `equipment_status` (`name`, `code`, `description`) VALUES
('待入库', 'pending', '设备已采购但尚未入库'),
('已入库', 'instored', '设备已入库且可使用'),
('使用中', 'inuse', '设备正在使用中'),
('维修中', 'repairing', '设备正在维修中'),
('报废', 'scrapped', '设备已报废');

-- 插入实验室示例数据（5个实验室）
-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO `laboratory` (`name`, `code`, `location`, `area`, `capacity`, `type`, `status`, `manager_id`, `description`) VALUES
('计算机基础实验室', 'LAB001', '科技楼301', 120.50, 50, '计算机实验室', 1, NULL, '用于计算机基础课程教学，配备50台台式电脑'),
('计算机网络实验室', 'LAB002', '科技楼402', 150.00, 40, '计算机实验室', 1, NULL, '用于计算机网络课程教学，配备网络设备和服务器'),
('电子电路实验室', 'LAB003', '实验楼201', 100.00, 30, '电子实验室', 1, NULL, '用于电子电路实验，配备示波器、信号发生器等设备'),
('物理实验实验室', 'LAB004', '实验楼305', 80.00, 25, '物理实验室', 1, NULL, '用于大学物理实验教学'),
('机械工程实验室', 'LAB005', '工程楼101', 200.00, 20, '机械实验室', 2, NULL, '用于机械工程实验，目前维护中');

-- 插入设备示例数据（20台设备）
-- 注意：laboratory_id 使用子查询获取实验室ID，status_id 使用子查询获取状态ID

-- 计算机基础实验室（LAB001）的设备
-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO `equipment` (`name`, `model`, `specification`, `asset_code`, `unit_price`, `quantity`, `supplier`, `purchase_date`, `warranty_period`, `status_id`, `laboratory_id`, `description`) VALUES
('联想台式电脑', 'ThinkCentre M720t', 'i5-9500/8G/256G SSD/1T HDD', 'EQ20230001', 4500.00, 50, '联想科技有限公司', '2023-09-10', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '用于计算机基础课程教学'),
('戴尔显示器', 'Dell P2419H', '23.8英寸 IPS 1920x1080', 'EQ20230002', 1200.00, 50, '戴尔（中国）有限公司', '2023-09-10', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '配套台式电脑使用'),
('键盘鼠标套装', '罗技 MK270', '无线键鼠套装', 'EQ20230003', 150.00, 50, '罗技科技有限公司', '2023-09-10', 12, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '配套台式电脑使用'),
('投影仪', '爱普生 CB-X41', 'XGA 1024x768 3500流明', 'EQ20230004', 3500.00, 1, '爱普生（中国）有限公司', '2023-08-15', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '用于教学投影'),
('音响系统', '漫步者 R201T06', '2.1声道多媒体音箱', 'EQ20230005', 200.00, 1, '漫步者科技股份有限公司', '2023-08-15', 12, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB001'), '用于教学音频播放');

-- 计算机网络实验室（LAB002）的设备
-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO `equipment` (`name`, `model`, `specification`, `asset_code`, `unit_price`, `quantity`, `supplier`, `purchase_date`, `warranty_period`, `status_id`, `laboratory_id`, `description`) VALUES
('华为交换机', 'S5700-28C-EI', '24口千兆以太网交换机', 'EQ20230006', 8000.00, 5, '华为技术有限公司', '2023-10-01', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于网络实验'),
('思科路由器', 'Cisco 2911', '双WAN口企业级路由器', 'EQ20230007', 12000.00, 3, '思科系统（中国）网络技术有限公司', '2023-10-01', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于网络路由实验'),
('戴尔服务器', 'PowerEdge R740', 'E5-2620v4/32G/1T SSD', 'EQ20230008', 25000.00, 2, '戴尔（中国）有限公司', '2023-10-01', 36, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于服务器配置实验'),
('网络测试仪', 'Fluke LinkRunner AT', '网络链路测试仪', 'EQ20230009', 15000.00, 2, '福禄克测试仪器（上海）有限公司', '2023-10-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于网络故障诊断'),
('网线钳', 'AMP RJ45', 'RJ45网线制作工具', 'EQ20230010', 80.00, 10, '安普（中国）有限公司', '2023-10-01', 12, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB002'), '用于网线制作实验');

-- 电子电路实验室（LAB003）的设备
-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO `equipment` (`name`, `model`, `specification`, `asset_code`, `unit_price`, `quantity`, `supplier`, `purchase_date`, `warranty_period`, `status_id`, `laboratory_id`, `description`) VALUES
('数字示波器', 'Tektronix TBS1052B', '50MHz 双通道数字示波器', 'EQ20230011', 3500.00, 15, '泰克科技（中国）有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB003'), '用于电路信号测量'),
('函数信号发生器', 'RIGOL DG1022Z', '25MHz 双通道函数信号发生器', 'EQ20230012', 2800.00, 15, '普源精电科技股份有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB003'), '用于信号源输出'),
('数字万用表', 'Fluke 17B+', '数字万用表', 'EQ20230013', 500.00, 20, '福禄克测试仪器（上海）有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB003'), '用于电压电流测量'),
('直流稳压电源', 'RIGOL DP832', '双路可调直流电源 32V/3A', 'EQ20230014', 1500.00, 15, '普源精电科技股份有限公司', '2023-11-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB003'), '用于电路供电'),
('面包板', 'MB-102', '830孔面包板', 'EQ20230015', 25.00, 30, '深圳市优信电子有限公司', '2023-11-01', 0, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB003'), '用于电路搭建实验');

-- 物理实验实验室（LAB004）的设备
-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO `equipment` (`name`, `model`, `specification`, `asset_code`, `unit_price`, `quantity`, `supplier`, `purchase_date`, `warranty_period`, `status_id`, `laboratory_id`, `description`) VALUES
('光学实验台', 'GX-1', '光学实验平台 1200x600mm', 'EQ20230016', 5000.00, 5, '北京光学仪器厂', '2023-12-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB004'), '用于光学实验'),
('激光器', 'He-Ne激光器', '632.8nm 红色激光', 'EQ20230017', 2000.00, 5, '北京光学仪器厂', '2023-12-01', 12, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB004'), '用于光学实验光源'),
('电子天平', 'Sartorius BSA224S', '0.1mg精度 220g量程', 'EQ20230018', 3500.00, 3, '赛多利斯科学仪器（北京）有限公司', '2023-12-01', 24, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB004'), '用于质量测量'),
('温度计', 'PT100', '铂电阻温度计 -50~200℃', 'EQ20230019', 300.00, 10, '上海自动化仪表有限公司', '2023-12-01', 12, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB004'), '用于温度测量'),
('游标卡尺', 'Mitutoyo 500-196-30', '0.02mm精度 0-150mm', 'EQ20230020', 200.00, 15, '三丰精密量仪（上海）有限公司', '2023-12-01', 12, (SELECT id FROM equipment_status WHERE code = 'instored'), (SELECT id FROM laboratory WHERE code = 'LAB004'), '用于长度测量');

-- 说明：
-- 1. 本脚本使用子查询自动获取实验室ID和设备状态ID，避免硬编码ID
-- 2. 使用 INSERT IGNORE 避免重复插入，如果数据已存在则忽略
-- 3. 如果实验室或设备状态不存在，INSERT会失败，请先确保基础数据已插入
-- 4. 本脚本可以安全地重复执行，不会因为重复数据而报错
-- 5. 如果需要完全重新插入，可以先删除现有数据：
--    DELETE FROM equipment WHERE asset_code LIKE 'EQ2023%';
--    DELETE FROM laboratory WHERE code LIKE 'LAB%';

-- ============================================
-- 验证数据插入结果
-- ============================================

-- 查看设备状态数据
SELECT * FROM equipment_status ORDER BY id;

-- 查看实验室数据
SELECT id, name, code, location, status, type FROM laboratory ORDER BY id;

-- 查看设备数据（前10条）
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
ORDER BY e.id
LIMIT 10;

-- 统计各实验室的设备数量
SELECT 
    l.code AS lab_code,
    l.name AS lab_name,
    COUNT(e.id) AS equipment_count
FROM laboratory l
LEFT JOIN equipment e ON l.id = e.laboratory_id
GROUP BY l.id, l.code, l.name
ORDER BY l.id;

