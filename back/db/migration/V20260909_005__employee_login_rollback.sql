-- PBKDF2 哈希无法安全缩回 VARCHAR(20)。回滚前必须从执行迁移前的备份恢复密码字段。
-- 仅在已恢复 admins.password 且确认不再需要角色值后，执行以下结构回退：
ALTER TABLE grid_member DROP INDEX uk_grid_member_gm_code;
ALTER TABLE admins DROP INDEX uk_admins_admin_code;
ALTER TABLE admins DROP COLUMN role_code;
