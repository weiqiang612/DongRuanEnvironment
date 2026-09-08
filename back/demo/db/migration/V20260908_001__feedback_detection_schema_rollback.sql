-- Use only when V20260908_001 was applied to an empty detection_result table
-- and no task_assign_log or alert_record data has been created.
-- This restores the legacy table shape while preserving the project-wide
-- logical-foreign-key policy (it does not recreate physical foreign keys).
-- Execute through a UTF-8 client, for example: mysql --default-character-set=utf8mb4.
SET NAMES utf8mb4;

DROP TABLE IF EXISTS alert_record;
DROP TABLE IF EXISTS task_assign_log;

ALTER TABLE detection_result
    DROP INDEX uk_detection_result_feedback, DROP INDEX idx_detection_result_detected_aqi,
    DROP COLUMN feedback_id, DROP COLUMN detected_at,
    MODIFY COLUMN so2_value INT NOT NULL COMMENT '实测空气二氧化硫浓度值(单位: ug/m3)',
    MODIFY COLUMN co_value INT NOT NULL COMMENT '实测空气一氧化碳浓度值(单位: ug/m3)',
    MODIFY COLUMN spm_value INT NOT NULL COMMENT '实测空气悬浮颗粒物浓度值(单位: ug/m3)',
    MODIFY COLUMN gm_id INT NOT NULL COMMENT '所属网格员编号',
    ADD COLUMN province_id INT NOT NULL COMMENT '所属省区域编号' AFTER id,
    ADD COLUMN city_id INT NOT NULL COMMENT '所属市区域编号' AFTER province_id,
    ADD COLUMN address VARCHAR(200) NOT NULL COMMENT '反馈信息所在区域详细地址' AFTER city_id,
    ADD COLUMN confirm_date VARCHAR(20) NOT NULL COMMENT '确认日期' AFTER aqi_id,
    ADD COLUMN confirm_time VARCHAR(20) NOT NULL COMMENT '确认时间' AFTER confirm_date,
    ADD COLUMN fd_id VARCHAR(20) NOT NULL COMMENT '反馈者编号(公众监督员电话号码)' AFTER gm_id,
    ADD COLUMN information VARCHAR(400) NOT NULL COMMENT '反馈信息描述' AFTER fd_id,
    ADD COLUMN remarks VARCHAR(200) NULL DEFAULT NULL COMMENT '备注' AFTER information,
    ADD INDEX fk_stat_province_id (province_id),
    ADD INDEX fk_stat_city_id (city_id);

RENAME TABLE detection_result TO statistics;

ALTER TABLE aqi_feedback
    DROP CHECK chk_aqi_feedback_state, DROP CHECK chk_aqi_feedback_timeout_flag,
    DROP INDEX idx_aqi_feedback_tel_submitted, DROP INDEX idx_aqi_feedback_state_timeout_assigned,
    DROP COLUMN timeout_at, DROP COLUMN timeout_flag, DROP COLUMN updated_at, DROP COLUMN completed_at, DROP COLUMN assigned_at, DROP COLUMN submitted_at,
    MODIFY COLUMN gm_id INT NULL DEFAULT 0 COMMENT '指派网格员编号',
    MODIFY COLUMN state INT NOT NULL DEFAULT 0 COMMENT '信息状态 0:未指派1:已指派 2:已确认';
