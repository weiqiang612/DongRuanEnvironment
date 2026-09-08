package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("supervisor")
public class Supervisor {

    @TableId("tel_id")
    private String telId;

    private String password;

    private String realName;
}
