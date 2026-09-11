package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("grid_member")
public class GridMember {

    @TableId("gm_id")
    private String gmId;

    @TableField("gm_name")
    private String gmName;

    @TableField("gm_code")
    private String gmCode;

    private String password;

    @TableField("province_id")
    private Integer provinceId;

    @TableField("city_id")
    private Integer cityId;

    private Integer state;

    @TableField(exist = false)
    private String cityName;
}
