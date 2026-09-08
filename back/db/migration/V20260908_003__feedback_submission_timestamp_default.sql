-- Keeps legacy feedback inserts compatible when they only provide af_date and af_time.
-- Execute through a UTF-8 client, for example: mysql --default-character-set=utf8mb4.
SET NAMES utf8mb4;

ALTER TABLE aqi_feedback
    MODIFY COLUMN submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '反馈提交时间';
