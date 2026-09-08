-- Run through a UTF-8 client after backing up the supervisor table.
-- MySQL DDL implicitly commits. Existing plaintext values are upgraded only
-- after a successful login by the application.
SET NAMES utf8mb4;

ALTER TABLE supervisor
    MODIFY COLUMN password VARCHAR(256) NOT NULL COMMENT 'PBKDF2 密码哈希；历史明文首次登录成功后升级';
