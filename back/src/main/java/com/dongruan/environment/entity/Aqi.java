package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("aqi")
public class Aqi {
    @TableId("aqi_id")
    private Integer aqiId;
    private String chineseExplain;
    private String aqiExplain;
    private String color;
    private Integer so2Min;
    private Integer so2Max;
    private Integer coMin;
    private Integer coMax;
    private Integer spmMin;
    private Integer spmMax;
}
