package com.dongruan.environment.dto;

/** 管理端可指派网格员展示项。 */
public record NepmCandidateResponse(String gmId, String gmName, Integer provinceId, Integer cityId,
                                    String cityName, String sourceLevel) {
}
