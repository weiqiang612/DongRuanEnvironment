package com.dongruan.environment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("task_assign_log")
public class TaskAssignLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer feedbackId;
    private Integer operatorId;
    private String fromGmId;
    private String toGmId;
    private String actionType;
    private LocalDateTime createdAt;
}
