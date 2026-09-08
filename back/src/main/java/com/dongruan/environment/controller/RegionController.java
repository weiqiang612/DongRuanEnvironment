package com.dongruan.environment.controller;

import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.dto.RegionOption;
import com.dongruan.environment.service.IRegionService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/region")
public class RegionController {

    private final IRegionService regionService;

    @GetMapping("/provinces")
    public ResultVO<?> listProvinces() {
        return new ResultVO<>(200, "查询成功", regionService.findProvinces());
    }

    @GetMapping("/cities")
    public ResultVO<?> listCities(@RequestParam @NotNull final Integer provinceId) {
        return new ResultVO<>(200, "查询成功", regionService.findCitiesByProvinceId(provinceId));
    }
}
