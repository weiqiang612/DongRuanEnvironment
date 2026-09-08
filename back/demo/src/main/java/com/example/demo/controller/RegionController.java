package com.example.demo.controller;

import com.example.demo.common.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 为反馈表单提供省、市选择项。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/region")
public class RegionController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/provinces")
    public ResultVO<List<RegionOption>> listProvinces() {
        List<RegionOption> provinces = jdbcTemplate.query(
                "SELECT province_id, province_name FROM grid_province ORDER BY province_id",
                (resultSet, rowNum) -> new RegionOption(
                        resultSet.getInt("province_id"),
                        resultSet.getString("province_name")
                )
        );
        return new ResultVO<>(200, "查询成功", provinces);
    }

    @GetMapping("/cities")
    public ResultVO<List<RegionOption>> listCities(@RequestParam Integer provinceId) {
        List<RegionOption> cities = jdbcTemplate.query(
                "SELECT city_id, city_name FROM grid_city WHERE province_id = ? ORDER BY city_id",
                (resultSet, rowNum) -> new RegionOption(
                        resultSet.getInt("city_id"),
                        resultSet.getString("city_name")
                ),
                provinceId
        );
        return new ResultVO<>(200, "查询成功", cities);
    }

    public record RegionOption(Integer id, String name) {
    }
}
