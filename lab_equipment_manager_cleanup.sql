-- 清理示例数据脚本
-- 注意：此脚本会删除所有示例数据，但保留基础配置数据（角色、设备状态等）
-- 使用前请确保已备份重要数据

USE lab_equipment_manager;

-- 禁用外键检查（临时）
SET FOREIGN_KEY_CHECKS = 0;

-- 删除业务数据（按依赖关系顺序删除）
DELETE FROM `ai_conversation`;
DELETE FROM `ai_knowledge`;
DELETE FROM `course_selection`;
DELETE FROM `course_resource`;
DELETE FROM `course_schedule`;
DELETE FROM `course`;
DELETE FROM `laboratory_reservation`;
DELETE FROM `equipment_scrap`;
DELETE FROM `equipment_repair`;
DELETE FROM `equipment_borrow`;
DELETE FROM `equipment`;
DELETE FROM `laboratory`;

-- 删除示例用户（保留管理员）
DELETE FROM `user_role` WHERE `user_id` > 1;
DELETE FROM `user` WHERE `id` > 1;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 重置自增ID（可选，如果需要从1开始）
-- ALTER TABLE `laboratory` AUTO_INCREMENT = 1;
-- ALTER TABLE `equipment` AUTO_INCREMENT = 1;
-- ALTER TABLE `equipment_borrow` AUTO_INCREMENT = 1;
-- ALTER TABLE `equipment_repair` AUTO_INCREMENT = 1;
-- ALTER TABLE `equipment_scrap` AUTO_INCREMENT = 1;
-- ALTER TABLE `laboratory_reservation` AUTO_INCREMENT = 1;
-- ALTER TABLE `course` AUTO_INCREMENT = 1;
-- ALTER TABLE `course_schedule` AUTO_INCREMENT = 1;
-- ALTER TABLE `course_resource` AUTO_INCREMENT = 1;
-- ALTER TABLE `course_selection` AUTO_INCREMENT = 1;
-- ALTER TABLE `ai_knowledge` AUTO_INCREMENT = 1;
-- ALTER TABLE `ai_conversation` AUTO_INCREMENT = 1;

-- 验证清理结果
SELECT '清理完成！' AS message;
SELECT COUNT(*) AS remaining_laboratories FROM `laboratory`;
SELECT COUNT(*) AS remaining_equipment FROM `equipment`;
SELECT COUNT(*) AS remaining_users FROM `user`;

