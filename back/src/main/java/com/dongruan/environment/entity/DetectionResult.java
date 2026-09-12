package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("detection_result")
public class DetectionResult {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer feedbackId;
    private BigDecimal so2Value;
    private Integer so2Level;
    private BigDecimal coValue;
    private Integer coLevel;
    private BigDecimal spmValue;
    private Integer spmLevel;
    private Integer aqiId;
    private LocalDateTime detectedAt;
    private String gmId;
}
