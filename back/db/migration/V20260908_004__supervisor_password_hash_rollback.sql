-- Run only after confirming no PBKDF2 hashes remain in supervisor.password.
-- MySQL DDL implicitly commits; restore from backup if hashed account data exists.
SET NAMES utf8mb4;

ALTER TABLE supervisor
    MODIFY COLUMN password VARCHAR(20) NOT NULL COMMENT '密码';
