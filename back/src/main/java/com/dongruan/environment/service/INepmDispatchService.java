package com.dongruan.environment.service;

import com.dongruan.environment.dto.NepmPageResponse;
import com.dongruan.environment.dto.NepmCandidateResponse;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.entity.GridMember;
import com.dongruan.environment.entity.TaskAssignLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface INepmDispatchService {
    NepmPageResponse<AqiFeedback> findFeedbacks(Integer provinceId, Integer cityId, List<Integer> states,
                                                 Boolean timeoutOnly, Integer estimatedGrade,
                                                 LocalDate submittedFrom, LocalDate submittedTo,
                                                 String keyword, Integer page, Integer pageSize);
    Optional<AqiFeedback> findFeedback(Integer feedbackId);
    List<NepmCandidateResponse> findCandidates(Integer feedbackId);
    List<TaskAssignLog> findLogs(Integer feedbackId);
    AqiFeedback dispatch(Integer feedbackId, String gridMemberId, String accountCode);
    Map<String, Long> dashboard();
    Map<String, Long> overview(Integer provinceId, Integer cityId, LocalDate submittedFrom, LocalDate submittedTo);
}
