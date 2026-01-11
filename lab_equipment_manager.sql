-- 创建数据库
CREATE DATABASE IF NOT EXISTS lab_equipment_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_equipment_manager;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号/工号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `department` VARCHAR(100) DEFAULT NULL COMMENT '所属院系',
  `role_code` VARCHAR(50) DEFAULT NULL COMMENT '角色代码（冗余字段，减少连表查询）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色代码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `fk_user_role_user` (`user_id`),
  KEY `fk_user_role_role` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 实验室表
CREATE TABLE IF NOT EXISTS `laboratory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '实验室ID',
  `name` VARCHAR(100) NOT NULL COMMENT '实验室名称',
  `code` VARCHAR(50) NOT NULL COMMENT '实验室编号',
  `location` VARCHAR(200) NOT NULL COMMENT '实验室位置',
  `area` DECIMAL(10,2) DEFAULT NULL COMMENT '实验室面积',
  `capacity` INT DEFAULT NULL COMMENT '容纳人数',
  `type` VARCHAR(50) DEFAULT NULL COMMENT '实验室类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-不可用，1-可用，2-维护中',
  `manager_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
  `description` TEXT DEFAULT NULL COMMENT '实验室描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `fk_laboratory_user` (`manager_id`),
  CONSTRAINT `fk_laboratory_user` FOREIGN KEY (`manager_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室表';

-- 设备状态表
CREATE TABLE IF NOT EXISTS `equipment_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `name` VARCHAR(50) NOT NULL COMMENT '状态名称',
  `code` VARCHAR(50) NOT NULL COMMENT '状态代码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '状态描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态表';

-- 设备表
CREATE TABLE IF NOT EXISTS `equipment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `model` VARCHAR(100) NOT NULL COMMENT '设备型号',
  `specification` VARCHAR(200) DEFAULT NULL COMMENT '设备规格',
  `asset_code` VARCHAR(50) DEFAULT NULL COMMENT '资产编号/二维码标识',
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `supplier` VARCHAR(100) DEFAULT NULL COMMENT '供应商',
  `purchase_date` DATE NOT NULL COMMENT '购置日期',
  `warranty_period` INT DEFAULT NULL COMMENT '保修期（月）',
  `status_id` BIGINT NOT NULL COMMENT '设备状态ID',
  `laboratory_id` BIGINT NOT NULL COMMENT '所属实验室ID',
  `description` TEXT DEFAULT NULL COMMENT '设备描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `fk_equipment_status` (`status_id`),
  KEY `fk_equipment_laboratory` (`laboratory_id`),
  CONSTRAINT `fk_equipment_status` FOREIGN KEY (`status_id`) REFERENCES `equipment_status` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_equipment_laboratory` FOREIGN KEY (`laboratory_id`) REFERENCES `laboratory` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 设备借用记录表
CREATE TABLE IF NOT EXISTS `equipment_borrow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '借用记录ID',
  `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
  `user_id` BIGINT NOT NULL COMMENT '借用用户ID',
  `borrow_date` DATETIME NOT NULL COMMENT '借用日期',
  `plan_return_date` DATETIME NOT NULL COMMENT '计划归还日期',
  `actual_return_date` DATETIME DEFAULT NULL COMMENT '实际归还日期',
  `purpose` VARCHAR(200) DEFAULT NULL COMMENT '借用用途',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '借用数量',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝，3-已借出，4-已归还，5-已逾期',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_remark` VARCHAR(200) DEFAULT NULL COMMENT '审批备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_borrow_equipment` (`equipment_id`),
  KEY `fk_borrow_user` (`user_id`),
  KEY `fk_borrow_approver` (`approver_id`),
  CONSTRAINT `fk_borrow_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_borrow_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_borrow_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备借用记录表';

-- 设备维修记录表
CREATE TABLE IF NOT EXISTS `equipment_repair` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维修记录ID',
  `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
  `reporter_id` BIGINT NOT NULL COMMENT '报修人ID',
  `report_date` DATETIME NOT NULL COMMENT '报修日期',
  `fault_description` TEXT NOT NULL COMMENT '故障描述',
  `repair_result` TEXT DEFAULT NULL COMMENT '维修结果',
  `repair_date` DATETIME DEFAULT NULL COMMENT '维修日期',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待维修，1-维修中，2-已修好，3-无法修复',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_repair_equipment` (`equipment_id`),
  KEY `fk_repair_reporter` (`reporter_id`),
  CONSTRAINT `fk_repair_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_repair_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维修记录表';

-- 设备报废记录表
CREATE TABLE IF NOT EXISTS `equipment_scrap` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报废记录ID',
  `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `apply_date` DATETIME NOT NULL COMMENT '申请日期',
  `scrap_reason` TEXT NOT NULL COMMENT '报废原因',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_remark` VARCHAR(200) DEFAULT NULL COMMENT '审批备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_scrap_equipment` (`equipment_id`),
  KEY `fk_scrap_applicant` (`applicant_id`),
  KEY `fk_scrap_approver` (`approver_id`),
  CONSTRAINT `fk_scrap_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_scrap_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_scrap_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备报废记录表';

-- 实验室预约表（合并使用记录）
CREATE TABLE IF NOT EXISTS `laboratory_reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约记录ID',
  `laboratory_id` BIGINT NOT NULL COMMENT '实验室ID',
  `user_id` BIGINT NOT NULL COMMENT '预约人ID',
  `reserve_date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `purpose` VARCHAR(200) DEFAULT NULL COMMENT '预约目的',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝，3-已取消，4-已完成，5-已使用',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_remark` VARCHAR(200) DEFAULT NULL COMMENT '审批备注',
  `actual_start_time` TIME DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` TIME DEFAULT NULL COMMENT '实际结束时间',
  `usage_remark` VARCHAR(200) DEFAULT NULL COMMENT '使用备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `fk_reservation_laboratory` (`laboratory_id`),
  KEY `fk_reservation_user` (`user_id`),
  KEY `fk_reservation_approver` (`approver_id`),
  KEY `idx_lab_date_time` (`laboratory_id`, `reserve_date`, `start_time`, `end_time`),
  CONSTRAINT `fk_reservation_laboratory` FOREIGN KEY (`laboratory_id`) REFERENCES `laboratory` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reservation_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室预约表（合并使用记录）';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `code` VARCHAR(50) NOT NULL COMMENT '课程编号',
  `department` VARCHAR(100) NOT NULL COMMENT '所属院系',
  `teacher_id` BIGINT NOT NULL COMMENT '授课教师ID',
  `credits` DECIMAL(3,1) NOT NULL COMMENT '学分',
  `hours` INT NOT NULL COMMENT '学时',
  `type` VARCHAR(50) DEFAULT NULL COMMENT '课程类型',
  `description` TEXT DEFAULT NULL COMMENT '课程描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `fk_course_teacher` (`teacher_id`),
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 课程安排表
CREATE TABLE IF NOT EXISTS `course_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程安排ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `laboratory_id` BIGINT NOT NULL COMMENT '实验室ID',
  `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
  `weekday` TINYINT NOT NULL COMMENT '星期：1-周一，2-周二，...，7-周日',
  `section` TINYINT NOT NULL COMMENT '节次',
  `start_week` INT NOT NULL COMMENT '开始周',
  `end_week` INT NOT NULL COMMENT '结束周',
  `schedule_type` VARCHAR(20) NOT NULL COMMENT '排课类型：每周/单周/双周',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_time_schedule` (`laboratory_id`, `weekday`, `section`, `start_week`, `end_week`, `schedule_type`),
  KEY `fk_schedule_course` (`course_id`),
  KEY `fk_schedule_laboratory` (`laboratory_id`),
  KEY `fk_schedule_teacher` (`teacher_id`),
  CONSTRAINT `fk_schedule_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_schedule_laboratory` FOREIGN KEY (`laboratory_id`) REFERENCES `laboratory` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_schedule_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程安排表';

-- 课程资源表
CREATE TABLE IF NOT EXISTS `course_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程资源ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-不可用，1-可用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_resource_course` (`course_id`),
  KEY `fk_resource_equipment` (`equipment_id`),
  CONSTRAINT `fk_resource_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程资源表';

-- 选课记录表
CREATE TABLE IF NOT EXISTS `course_selection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '选课记录ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `selection_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `score` DECIMAL(5,2) DEFAULT NULL COMMENT '成绩',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
  KEY `fk_selection_student` (`student_id`),
  KEY `fk_selection_course` (`course_id`),
  CONSTRAINT `fk_selection_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_selection_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';

-- AI知识库表
CREATE TABLE IF NOT EXISTS `ai_knowledge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `question` VARCHAR(500) NOT NULL COMMENT '问题',
  `answer` TEXT NOT NULL COMMENT '答案',
  `category` VARCHAR(100) DEFAULT NULL COMMENT '分类',
  `keywords` VARCHAR(200) DEFAULT NULL COMMENT '关键词',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库表';

-- AI对话记录表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '对话记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_input` TEXT NOT NULL COMMENT '用户输入',
  `ai_output` TEXT NOT NULL COMMENT 'AI输出',
  `source` VARCHAR(50) NOT NULL COMMENT '来源：knowledge-知识库，api-外部API',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_conversation_user` (`user_id`),
  CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- 初始化角色数据
INSERT INTO `role` (`name`, `code`, `description`) VALUES
('系统管理员', 'admin', '系统最高权限角色'),
('教师', 'teacher', '教师角色'),
('学生', 'student', '学生角色');

-- 初始化设备状态数据
INSERT INTO `equipment_status` (`name`, `code`, `description`) VALUES
('待入库', 'pending', '设备已采购但尚未入库'),
('已入库', 'instored', '设备已入库且可使用'),
('使用中', 'inuse', '设备正在使用中'),
('维修中', 'repairing', '设备正在维修中'),
('报废', 'scrapped', '设备已报废');

-- 初始化管理员用户（密码：admin123）
INSERT INTO `user` (`username`, `password`, `name`, `email`, `phone`, `department`, `role_code`, `status`) VALUES
('admin', '$2a$10$7rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6Ee6S4Z8Q4rG6E', '系统管理员', 'admin@example.com', '13800138000', '信息中心', 'admin', 1);

-- 关联管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1);
