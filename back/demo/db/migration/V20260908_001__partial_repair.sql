-- Applies only to the local database after the interrupted first run of
-- V20260908_001. At this point aqi_feedback has been migrated and
-- statistics has been renamed to detection_result, but no detection columns
-- have been removed and no new tables have been created.
-- Execute through a UTF-8 client, for example: mysql --default-character-set=utf8mb4.
SET NAMES utf8mb4;

ALTER TABLE detection_result
    DROP FOREIGN KEY fk_stat_city_id,
    DROP FOREIGN KEY fk_stat_province_id,
    DROP INDEX fk_stat_city_id,
    DROP INDEX fk_stat_province_id,
    DROP COLUMN province_id, DROP COLUMN city_id, DROP COLUMN address,
    DROP COLUMN confirm_date, DROP COLUMN confirm_time, DROP COLUMN fd_id,
    DROP COLUMN information, DROP COLUMN remarks,
    MODIFY COLUMN so2_value DECIMAL(10, 2) NOT NULL COMMENT 'SO2 实测浓度',
    MODIFY COLUMN co_value DECIMAL(10, 2) NOT NULL COMMENT 'CO 实测浓度',
    MODIFY COLUMN spm_value DECIMAL(10, 2) NOT NULL COMMENT 'PM2.5 实测浓度',
    MODIFY COLUMN gm_id VARCHAR(11) NOT NULL COMMENT '检测网格员编号',
    ADD COLUMN feedback_id INT NOT NULL COMMENT '关联公众反馈编号' AFTER id,
    ADD COLUMN detected_at DATETIME NOT NULL COMMENT '检测时间' AFTER aqi_id,
    ADD UNIQUE INDEX uk_detection_result_feedback (feedback_id),
    ADD INDEX idx_detection_result_detected_aqi (detected_at, aqi_id);

CREATE TABLE task_assign_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '指派日志主键', feedback_id INT NOT NULL COMMENT '关联公众反馈编号', operator_id INT NOT NULL COMMENT '操作管理员编号',
    from_gm_id VARCHAR(11) NULL COMMENT '原网格员编号', to_gm_id VARCHAR(11) NULL COMMENT '新网格员编号',
    action_type VARCHAR(20) NOT NULL COMMENT 'ASSIGN、REASSIGN、CONTINUE', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id), INDEX idx_task_assign_log_feedback_created (feedback_id, created_at),
    CONSTRAINT chk_task_assign_log_action CHECK (action_type IN ('ASSIGN', 'REASSIGN', 'CONTINUE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '反馈指派操作日志';

CREATE TABLE alert_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警记录主键', feedback_id INT NOT NULL COMMENT '关联公众反馈编号', result_id INT NOT NULL COMMENT '关联检测结果编号',
    alert_level TINYINT UNSIGNED NOT NULL COMMENT '预警 AQI 等级 4 至 6', alert_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING、HANDLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间', handled_at DATETIME NULL COMMENT '处置时间',
    PRIMARY KEY (id), UNIQUE INDEX uk_alert_record_result (result_id), INDEX idx_alert_record_status_created (alert_status, created_at),
    CONSTRAINT chk_alert_record_level CHECK (alert_level BETWEEN 4 AND 6),
    CONSTRAINT chk_alert_record_status CHECK (alert_status IN ('PENDING', 'HANDLED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '高等级 AQI 预警记录';
