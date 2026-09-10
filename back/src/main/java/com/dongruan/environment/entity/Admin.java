package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("admins")
public class Admin {

    @TableId("admin_id")
    private Integer adminId;

    @TableField("admin_code")
    private String adminCode;

    private String password;

    @TableField("role_code")
    private String roleCode;
}
