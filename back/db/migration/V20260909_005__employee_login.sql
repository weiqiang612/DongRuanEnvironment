-- 执行前：备份 grid_member、admins，并核对 password 中不存在无法识别的历史编码。
-- 本脚本不插入账号、密码或角色；管理员 role_code 须由维护人员显式配置。
ALTER TABLE grid_member MODIFY COLUMN password VARCHAR(256) NOT NULL;
ALTER TABLE admins MODIFY COLUMN password VARCHAR(256) NOT NULL;
ALTER TABLE admins ADD COLUMN role_code VARCHAR(32) NULL COMMENT 'NEPM_ADMIN 或 NEPV_DECISION_MAKER';
ALTER TABLE grid_member ADD UNIQUE INDEX uk_grid_member_gm_code (gm_code);
ALTER TABLE admins ADD UNIQUE INDEX uk_admins_admin_code (admin_code);
