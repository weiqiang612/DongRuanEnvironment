package com.dongruan.environment.dto;

import java.util.List;

/** 管理端列表统一分页响应。 */
public record NepmPageResponse<T>(List<T> items, long total, int page, int pageSize) {
}
