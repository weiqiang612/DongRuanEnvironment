package com.dongruan.environment.service;

import com.dongruan.environment.dto.NepvDashboardVO;
import java.time.LocalDate;

public interface INepvDashboardService {
    NepvDashboardVO getDashboard(Integer provinceId, Integer cityId, LocalDate submittedFrom, LocalDate submittedTo);
}
