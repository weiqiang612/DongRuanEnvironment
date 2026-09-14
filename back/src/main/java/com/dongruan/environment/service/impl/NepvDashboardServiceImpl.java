package com.dongruan.environment.service.impl;

import com.dongruan.environment.dto.AlertRecordStatItem;
import com.dongruan.environment.dto.NepmAqiAlertVO;
import com.dongruan.environment.dto.NepmDetectionResultVO;
import com.dongruan.environment.dto.NepvDashboardVO;
import com.dongruan.environment.dto.NepvGridCoverageStat;
import com.dongruan.environment.mapper.AlertRecordMapper;
import com.dongruan.environment.mapper.DetectionResultMapper;
import com.dongruan.environment.mapper.RegionMapper;
import com.dongruan.environment.service.INepvDashboardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NepvDashboardServiceImpl implements INepvDashboardService {

    private final DetectionResultMapper detectionResultMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final RegionMapper regionMapper;

    @Override
    public NepvDashboardVO getDashboard(final Integer provinceId, final Integer cityId,
            final LocalDate submittedFrom, final LocalDate submittedTo) {
        if (submittedFrom != null && submittedTo != null && submittedFrom.isAfter(submittedTo)) {
            throw new IllegalArgumentException("提交开始日期不能晚于结束日期");
        }
        final LocalDateTime from = submittedFrom == null ? null : submittedFrom.atStartOfDay();
        final LocalDateTime toExclusive = submittedTo == null ? null : submittedTo.plusDays(1).atStartOfDay();
        final List<NepmDetectionResultVO> results = detectionResultMapper.findForNepm(
                provinceId, cityId, null, from, toExclusive, null);
        final List<AlertRecordStatItem> alerts = alertRecordMapper.findAlertsForStats(
                provinceId, cityId, from, toExclusive);
        final List<NepmAqiAlertVO> recentAlertDetails = alertRecordMapper.findForNepm(null, provinceId, cityId);

        final Map<Integer, Long> aqiCounts = results.stream().filter(item -> item.aqiId() != null)
                .collect(Collectors.groupingBy(NepmDetectionResultVO::aqiId, Collectors.counting()));
        final List<NepvDashboardVO.AqiDistributionItem> distribution = List.of(
                new NepvDashboardVO.AqiDistributionItem(1, "一级（优）", aqiCounts.getOrDefault(1, 0L)),
                new NepvDashboardVO.AqiDistributionItem(2, "二级（良）", aqiCounts.getOrDefault(2, 0L)),
                new NepvDashboardVO.AqiDistributionItem(3, "三级（轻度污染）", aqiCounts.getOrDefault(3, 0L)),
                new NepvDashboardVO.AqiDistributionItem(4, "四级（中度污染）", aqiCounts.getOrDefault(4, 0L)),
                new NepvDashboardVO.AqiDistributionItem(5, "五级（重度污染）", aqiCounts.getOrDefault(5, 0L)),
                new NepvDashboardVO.AqiDistributionItem(6, "六级（严重污染）", aqiCounts.getOrDefault(6, 0L)));

        final DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("yyyy-MM");
        final Map<String, Long> detectionByMonth = results.stream().filter(item -> item.submittedAt() != null)
                .collect(Collectors.groupingBy(item -> item.submittedAt().format(monthFormat), Collectors.counting()));
        final Map<String, Long> alertByMonth = alerts.stream().filter(item -> item.submittedAt() != null)
                .collect(Collectors.groupingBy(item -> item.submittedAt().format(monthFormat), Collectors.counting()));
        final Set<String> months = new TreeSet<>();
        months.addAll(detectionByMonth.keySet());
        months.addAll(alertByMonth.keySet());
        final List<NepvDashboardVO.MonthlyTrendItem> monthlyTrends = months.stream()
                .map(month -> new NepvDashboardVO.MonthlyTrendItem(month,
                        detectionByMonth.getOrDefault(month, 0L), alertByMonth.getOrDefault(month, 0L))).toList();

        final Map<Integer, List<NepmDetectionResultVO>> resultsByProvince = results.stream()
                .filter(item -> item.provinceId() != null)
                .collect(Collectors.groupingBy(NepmDetectionResultVO::provinceId));
        final Map<Integer, Long> pendingAlertsByProvince = recentAlertDetails.stream()
                .filter(item -> "PENDING".equalsIgnoreCase(item.alertStatus()) && item.provinceId() != null)
                .filter(item -> inRange(item.createdAt(), from, toExclusive))
                .collect(Collectors.groupingBy(NepmAqiAlertVO::provinceId, Collectors.counting()));
        final List<NepvDashboardVO.ProvinceRiskItem> provinceRisks = resultsByProvince.entrySet().stream()
                .map(entry -> {
                    final List<NepmDetectionResultVO> provinceResults = entry.getValue();
                    final NepmDetectionResultVO first = provinceResults.get(0);
                    final long highCount = provinceResults.stream()
                            .filter(item -> item.aqiId() != null && item.aqiId() >= 4).count();
                    return new NepvDashboardVO.ProvinceRiskItem(entry.getKey(), first.provinceName(),
                            provinceResults.size(), highCount, pendingAlertsByProvince.getOrDefault(entry.getKey(), 0L));
                }).sorted(Comparator.comparing(NepvDashboardVO.ProvinceRiskItem::highPollutionCount).reversed()
                        .thenComparing(NepvDashboardVO.ProvinceRiskItem::detectionCount, Comparator.reverseOrder()))
                .toList();

        final List<NepvDashboardVO.CityRiskItem> cityRisks = provinceId == null
                ? List.of()
                : buildCityRisks(results, recentAlertDetails, from, toExclusive);

        final NepvGridCoverageStat coverage = regionMapper.findGridCoverage(provinceId, cityId);
        final long totalCities = coverage == null || coverage.totalCities() == null ? 0 : coverage.totalCities();
        final long coveredCities = coverage == null || coverage.coveredCities() == null ? 0 : coverage.coveredCities();
        final double coverageRate = totalCities == 0 ? 0D : Math.round((coveredCities * 1000D / totalCities)) / 10D;

        final List<NepvDashboardVO.RecentAlertItem> recentAlerts = recentAlertDetails.stream()
                .filter(item -> inRange(item.createdAt(), from, toExclusive)).limit(5)
                .map(item -> new NepvDashboardVO.RecentAlertItem(item.id(), item.alertLevel(), item.alertStatus(),
                        item.provinceName(), item.cityName(), item.address(), item.createdAt())).toList();
        final long highPollutionDetections = results.stream()
                .filter(item -> item.aqiId() != null && item.aqiId() >= 4).count();
        return new NepvDashboardVO(results.size(), highPollutionDetections, alerts.size(),
                alerts.stream().filter(item -> "PENDING".equalsIgnoreCase(item.alertStatus())).count(),
                new NepvDashboardVO.GridCoverage(totalCities, coveredCities, coverageRate), distribution,
                monthlyTrends, provinceRisks, cityRisks, recentAlerts);
    }

    private List<NepvDashboardVO.CityRiskItem> buildCityRisks(final List<NepmDetectionResultVO> results,
            final List<NepmAqiAlertVO> alerts, final LocalDateTime from, final LocalDateTime toExclusive) {
        final Map<Integer, List<NepmDetectionResultVO>> resultsByCity = results.stream()
                .filter(item -> item.cityId() != null)
                .collect(Collectors.groupingBy(NepmDetectionResultVO::cityId));
        final Map<Integer, Long> pendingAlertsByCity = alerts.stream()
                .filter(item -> "PENDING".equalsIgnoreCase(item.alertStatus()) && item.cityId() != null)
                .filter(item -> inRange(item.createdAt(), from, toExclusive))
                .collect(Collectors.groupingBy(NepmAqiAlertVO::cityId, Collectors.counting()));
        return resultsByCity.entrySet().stream()
                .map(entry -> {
                    final List<NepmDetectionResultVO> cityResults = entry.getValue();
                    final NepmDetectionResultVO first = cityResults.get(0);
                    final long highCount = cityResults.stream()
                            .filter(item -> item.aqiId() != null && item.aqiId() >= 4).count();
                    return new NepvDashboardVO.CityRiskItem(entry.getKey(), first.cityName(), cityResults.size(),
                            highCount, pendingAlertsByCity.getOrDefault(entry.getKey(), 0L),
                            dominantAqiId(cityResults));
                })
                .sorted(Comparator.comparing(NepvDashboardVO.CityRiskItem::highPollutionCount).reversed()
                        .thenComparing(NepvDashboardVO.CityRiskItem::detectionCount, Comparator.reverseOrder())
                        .thenComparing(NepvDashboardVO.CityRiskItem::cityName))
                .toList();
    }

    private Integer dominantAqiId(final List<NepmDetectionResultVO> results) {
        return results.stream().filter(item -> item.aqiId() != null)
                .collect(Collectors.groupingBy(NepmDetectionResultVO::aqiId, Collectors.counting())).entrySet().stream()
                .max(Map.Entry.<Integer, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey).orElse(null);
    }

    private boolean inRange(final LocalDateTime value, final LocalDateTime from, final LocalDateTime toExclusive) {
        return value != null && (from == null || !value.isBefore(from))
                && (toExclusive == null || value.isBefore(toExclusive));
    }
}
