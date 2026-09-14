package com.dongruan.environment.mapper;

import com.dongruan.environment.dto.RegionOption;
import com.dongruan.environment.dto.NepvGridCoverageStat;
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

    @Select("""
    <script>
    SELECT COUNT(*) AS totalCities,
           COUNT(DISTINCT CASE WHEN gm.gm_id IS NOT NULL THEN c.city_id END) AS coveredCities
    FROM grid_city c
    LEFT JOIN grid_member gm ON gm.city_id = c.city_id
    <where>
      <if test='provinceId != null'>AND c.province_id = #{provinceId}</if>
      <if test='cityId != null'>AND c.city_id = #{cityId}</if>
    </where>
    </script>
    """)
    NepvGridCoverageStat findGridCoverage(
            @Param("provinceId") Integer provinceId,
            @Param("cityId") Integer cityId);
}
