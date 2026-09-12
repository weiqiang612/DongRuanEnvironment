package com.dongruan.environment.service;

import com.dongruan.environment.dto.MeasurementRequest;
import com.dongruan.environment.dto.NepgTaskDetailResponse;
import com.dongruan.environment.dto.NepgTaskResponse;
import java.util.List;

public interface INepgTaskService {
    List<NepgTaskResponse> findMyTasks(String accountCode);
    NepgTaskDetailResponse findMyTask(Integer feedbackId, String accountCode);
    NepgTaskDetailResponse submitMeasurement(Integer feedbackId, String accountCode, MeasurementRequest request);
}
