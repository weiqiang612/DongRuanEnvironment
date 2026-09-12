package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("alert_record")
public class AlertRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer feedbackId;
    private Integer resultId;
    private Integer alertLevel;
    private String alertStatus;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
