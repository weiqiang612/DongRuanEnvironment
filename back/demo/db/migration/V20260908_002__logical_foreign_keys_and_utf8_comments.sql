-- Converts all current nepmdb relationships to logical foreign keys.
-- Execute through a UTF-8 client, for example: mysql --default-character-set=utf8mb4.
SET NAMES utf8mb4;

-- Retain the relation columns and their indexes. Existence validation and
-- deletion handling belong to the service layer, not to MySQL FOREIGN KEYs.
-- Each conditional drop makes this safe after a fresh V001 run as well as
-- after the earlier local V001 version that created physical foreign keys.
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'alert_record' AND constraint_name = 'fk_alert_record_feedback' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE alert_record DROP FOREIGN KEY fk_alert_record_feedback', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'alert_record' AND constraint_name = 'fk_alert_record_result' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE alert_record DROP FOREIGN KEY fk_alert_record_result', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'aqi_feedback' AND constraint_name = 'fk_api_city_id' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE aqi_feedback DROP FOREIGN KEY fk_api_city_id', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'aqi_feedback' AND constraint_name = 'fk_api_province_id' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE aqi_feedback DROP FOREIGN KEY fk_api_province_id', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'detection_result' AND constraint_name = 'fk_detection_result_feedback' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE detection_result DROP FOREIGN KEY fk_detection_result_feedback', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'grid_city' AND constraint_name = 'fk_city_province_id' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE grid_city DROP FOREIGN KEY fk_city_province_id', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'grid_member' AND constraint_name = 'fk_city_id' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE grid_member DROP FOREIGN KEY fk_city_id', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'grid_member' AND constraint_name = 'fk_province_id' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE grid_member DROP FOREIGN KEY fk_province_id', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'task_assign_log' AND constraint_name = 'fk_task_assign_log_admin' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE task_assign_log DROP FOREIGN KEY fk_task_assign_log_admin', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'task_assign_log' AND constraint_name = 'fk_task_assign_log_feedback' AND constraint_type = 'FOREIGN KEY'), 'ALTER TABLE task_assign_log DROP FOREIGN KEY fk_task_assign_log_feedback', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Repair comments written with a non-UTF-8 client during the earlier run.
ALTER TABLE aqi_feedback
    MODIFY COLUMN submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '反馈提交时间',
    MODIFY COLUMN gm_id VARCHAR(11) NULL DEFAULT NULL COMMENT '指派网格员编号',
    MODIFY COLUMN assigned_at DATETIME NULL COMMENT '最近一次指派时间',
    MODIFY COLUMN completed_at DATETIME NULL COMMENT '任务完成时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    MODIFY COLUMN timeout_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否超时 0:否 1:是',
    MODIFY COLUMN timeout_at DATETIME NULL COMMENT '首次标记超时时间',
    MODIFY COLUMN state TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '任务状态 0:待指派 1:已指派 2:已完成';

ALTER TABLE detection_result
    MODIFY COLUMN feedback_id INT NOT NULL COMMENT '关联公众反馈编号',
    MODIFY COLUMN so2_value DECIMAL(10, 2) NOT NULL COMMENT 'SO2 实测浓度',
    MODIFY COLUMN co_value DECIMAL(10, 2) NOT NULL COMMENT 'CO 实测浓度',
    MODIFY COLUMN spm_value DECIMAL(10, 2) NOT NULL COMMENT 'PM2.5 实测浓度',
    MODIFY COLUMN detected_at DATETIME NOT NULL COMMENT '检测时间',
    MODIFY COLUMN gm_id VARCHAR(11) NOT NULL COMMENT '检测网格员编号';

ALTER TABLE task_assign_log
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '指派日志主键',
    MODIFY COLUMN feedback_id INT NOT NULL COMMENT '关联公众反馈编号',
    MODIFY COLUMN operator_id INT NOT NULL COMMENT '操作管理员编号',
    MODIFY COLUMN from_gm_id VARCHAR(11) NULL COMMENT '原网格员编号',
    MODIFY COLUMN to_gm_id VARCHAR(11) NULL COMMENT '新网格员编号',
    MODIFY COLUMN action_type VARCHAR(20) NOT NULL COMMENT 'ASSIGN、REASSIGN、CONTINUE',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    COMMENT = '反馈指派操作日志';

ALTER TABLE alert_record
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警记录主键',
    MODIFY COLUMN feedback_id INT NOT NULL COMMENT '关联公众反馈编号',
    MODIFY COLUMN result_id INT NOT NULL COMMENT '关联检测结果编号',
    MODIFY COLUMN alert_level TINYINT UNSIGNED NOT NULL COMMENT '预警 AQI 等级 4 至 6',
    MODIFY COLUMN alert_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING、HANDLED',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    MODIFY COLUMN handled_at DATETIME NULL COMMENT '处置时间',
    COMMENT = '高等级 AQI 预警记录';
