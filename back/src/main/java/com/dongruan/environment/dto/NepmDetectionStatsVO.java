package com.dongruan.environment.dto;

import java.util.List;

public record NepmDetectionStatsVO(
        Long totalDetections,
        List<AqiDistributionItem> aqiDistribution,
        List<MonthlyTrendItem> monthlyTrends,
        Long highAlertCount,
        Long pendingAlertCount,
        Long handledAlertCount,
        Long totalFeedbacks,
        Double completionRate,
        Double completionRateChange,
        Double timeoutRate,
        Double timeoutRateChange,
        Double highPollutionRate,
        Double highPollutionRateChange,
        String periodInsight,
        List<RegionRiskItem> regionRisks,
        List<EfficiencyTrendItem> efficiencyTrends,
        List<PollutionTrendItem> pollutionTrends,
        List<KeyRegionItem> keyRegions
) {
    public NepmDetectionStatsVO(
            Long totalDetections,
            List<AqiDistributionItem> aqiDistribution,
            List<MonthlyTrendItem> monthlyTrends,
            Long highAlertCount,
            Long pendingAlertCount,
            Long handledAlertCount
    ) {
        this(totalDetections, aqiDistribution, monthlyTrends, highAlertCount, pendingAlertCount, handledAlertCount,
                0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", List.of(), List.of(), List.of(), List.of());
    }

    public record AqiDistributionItem(Integer aqiId, String aqiName, Long count) {
    }

    public record MonthlyTrendItem(String month, Long detectionCount, Long alertCount) {
    }

    public record RegionRiskItem(String regionName, Long feedbackCount, Double highPollutionRate) {
    }

    public record EfficiencyTrendItem(String month, Long feedbackCount, Double completionRate) {
    }

    public record PollutionTrendItem(String month, Double highPollutionRate) {
    }

    public record KeyRegionItem(
            Integer rank,
            String regionName,
            Long feedbackCount,
            Double highPollutionRate,
            String changeRate,
            String priorityLevel,
            String advice
    ) {
    }
}
