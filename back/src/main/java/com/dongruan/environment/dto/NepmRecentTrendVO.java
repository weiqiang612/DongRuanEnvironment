package com.dongruan.environment.dto;

import java.util.List;

public record NepmRecentTrendVO(
        List<String> dates,
        List<Long> newFeedbacks,
        List<Long> completedFeedbacks,
        List<Long> pendingFeedbacks
) {}
