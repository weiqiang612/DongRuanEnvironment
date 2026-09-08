package com.dongruan.environment.mapper;

import com.dongruan.environment.dto.RegionOption;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RegionMapper {

    @Select("SELECT province_id AS id, province_name AS name FROM grid_province ORDER BY province_id")
    List<RegionOption> findProvinces();

    @Select("SELECT city_id AS id, city_name AS name FROM grid_city WHERE province_id = #{provinceId} ORDER BY city_id")
    List<RegionOption> findCitiesByProvinceId(@Param("provinceId") Integer provinceId);
}
