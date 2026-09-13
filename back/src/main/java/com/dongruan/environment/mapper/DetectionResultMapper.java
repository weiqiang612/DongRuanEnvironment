package com.dongruan.environment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongruan.environment.dto.NepmDetectionResultVO;
import com.dongruan.environment.entity.DetectionResult;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DetectionResultMapper extends BaseMapper<DetectionResult> {

    @Select("""
    <script>
    SELECT
        dr.id,
        dr.feedback_id,
        af.province_id,
        p.province_name,
        af.city_id,
        c.city_name,
        af.address,
        af.information,
        af.estimated_grade,
        dr.gm_id,
        gm.gm_name,
        dr.so2_value,
        dr.so2_level,
        dr.co_value,
        dr.co_level,
        dr.spm_value,
        dr.spm_level,
        dr.aqi_id,
        dr.detected_at,
        af.submitted_at
    FROM detection_result dr
    JOIN aqi_feedback af ON dr.feedback_id = af.af_id
    JOIN grid_province p ON af.province_id = p.province_id
    JOIN grid_city c ON af.city_id = c.city_id
    LEFT JOIN grid_member gm ON dr.gm_id = gm.gm_id
    <where>
      <if test='provinceId != null'>AND af.province_id = #{provinceId}</if>
      <if test='cityId != null'>AND af.city_id = #{cityId}</if>
      <if test='aqiId != null'>AND dr.aqi_id = #{aqiId}</if>
      <if test='submittedFrom != null'>AND af.submitted_at &gt;= #{submittedFrom}</if>
      <if test='submittedToExclusive != null'>AND af.submitted_at &lt; #{submittedToExclusive}</if>
      <if test='keyword != null and keyword != ""'>
        AND (af.address LIKE CONCAT('%', #{keyword}, '%')
          OR af.information LIKE CONCAT('%', #{keyword}, '%')
          OR gm.gm_name LIKE CONCAT('%', #{keyword}, '%'))
      </if>
    </where>
    ORDER BY dr.detected_at DESC, dr.id DESC
    </script>
    """)
    List<NepmDetectionResultVO> findForNepm(
            @Param("provinceId") Integer provinceId,
            @Param("cityId") Integer cityId,
            @Param("aqiId") Integer aqiId,
            @Param("submittedFrom") LocalDateTime submittedFrom,
            @Param("submittedToExclusive") LocalDateTime submittedToExclusive,
            @Param("keyword") String keyword);

    @Select("""
    SELECT
        dr.id,
        dr.feedback_id,
        af.province_id,
        p.province_name,
        af.city_id,
        c.city_name,
        af.address,
        af.information,
        af.estimated_grade,
        dr.gm_id,
        gm.gm_name,
        dr.so2_value,
        dr.so2_level,
        dr.co_value,
        dr.co_level,
        dr.spm_value,
        dr.spm_level,
        dr.aqi_id,
        dr.detected_at,
        af.submitted_at
    FROM detection_result dr
    JOIN aqi_feedback af ON dr.feedback_id = af.af_id
    JOIN grid_province p ON af.province_id = p.province_id
    JOIN grid_city c ON af.city_id = c.city_id
    LEFT JOIN grid_member gm ON dr.gm_id = gm.gm_id
    WHERE dr.id = #{id}
    """)
    NepmDetectionResultVO findDetailForNepm(@Param("id") Integer id);
}

