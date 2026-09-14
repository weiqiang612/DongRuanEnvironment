package com.dongruan.environment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/** Read-only aggregate returned to the NEPV decision dashboard. */
public record NepvDashboardVO(
        long totalDetections,
        long highPollutionDetections,
        long totalAlerts,
        long pendingAlerts,
        GridCoverage gridCoverage,
        List<AqiDistributionItem> aqiDistribution,
        List<MonthlyTrendItem> monthlyTrends,
        List<ProvinceRiskItem> provinceRisks,
        List<CityRiskItem> cityRisks,
        List<RecentAlertItem> recentAlerts
) {
    public record GridCoverage(long totalCities, long coveredCities, double coverageRate) {
    }

    public record AqiDistributionItem(int aqiId, String label, long count) {
    }

    public record MonthlyTrendItem(String month, long detectionCount, long alertCount) {
    }

    public record ProvinceRiskItem(
            Integer provinceId, String provinceName, long detectionCount,
            long highPollutionCount, long pendingAlertCount) {
    }

    /** City-level risk aggregate returned for a selected province, optionally narrowed to one city. */
    public record CityRiskItem(
            Integer cityId, String cityName, long detectionCount,
            long highPollutionCount, long pendingAlertCount, Integer dominantAqiId) {
    }

    public record RecentAlertItem(
            Long id, Integer alertLevel, String alertStatus, String provinceName,
            String cityName, String address,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt) {
    }
}
