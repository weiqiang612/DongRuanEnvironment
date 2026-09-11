package com.dongruan.environment.mapper;

import com.dongruan.environment.entity.AqiFeedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author weiqiang
 * @since 2026-09-03
 */
@Mapper
public interface AqiFeedbackMapper extends BaseMapper<AqiFeedback> {
    @Select("""
    SELECT
        aqi.*,
        p.province_name,
        c.city_name
    FROM aqi_feedback aqi
    JOIN grid_province p
        ON aqi.province_id = p.province_id
    JOIN grid_city c
        ON aqi.city_id = c.city_id
""")
    List<AqiFeedback> findAll();

    @Select("""
    <script>
    SELECT aqi.*, p.province_name, c.city_name, gm.gm_name
    FROM aqi_feedback aqi
    JOIN grid_province p ON aqi.province_id = p.province_id
    JOIN grid_city c ON aqi.city_id = c.city_id
    LEFT JOIN grid_member gm ON aqi.gm_id = gm.gm_id
    <where>
      <if test='provinceId != null'>AND aqi.province_id = #{provinceId}</if>
      <if test='cityId != null'>AND aqi.city_id = #{cityId}</if>
      <if test='states != null and states.size() > 0'>
        AND aqi.state IN
        <foreach collection='states' item='item' open='(' separator=',' close=')'>#{item}</foreach>
      </if>
      <if test='timeoutOnly != null and timeoutOnly'>AND aqi.timeout_flag = 1</if>
      <if test='estimatedGrade != null'>AND aqi.estimated_grade = #{estimatedGrade}</if>
      <if test='submittedFrom != null'>AND aqi.submitted_at &gt;= #{submittedFrom}</if>
      <if test='submittedToExclusive != null'>AND aqi.submitted_at &lt; #{submittedToExclusive}</if>
      <if test='keyword != null and keyword != ""'>
        AND (aqi.address LIKE CONCAT('%', #{keyword}, '%')
          OR aqi.information LIKE CONCAT('%', #{keyword}, '%'))
      </if>
    </where>
    ORDER BY aqi.submitted_at DESC, aqi.af_id DESC
    </script>
    """)
    List<AqiFeedback> findForNepm(
            @Param("provinceId") Integer provinceId,
            @Param("cityId") Integer cityId,
            @Param("states") List<Integer> states,
            @Param("timeoutOnly") Boolean timeoutOnly,
            @Param("estimatedGrade") Integer estimatedGrade,
            @Param("submittedFrom") LocalDateTime submittedFrom,
            @Param("submittedToExclusive") LocalDateTime submittedToExclusive,
            @Param("keyword") String keyword);

    @Select("""
    SELECT aqi.*, p.province_name, c.city_name, gm.gm_name
    FROM aqi_feedback aqi
    JOIN grid_province p ON aqi.province_id = p.province_id
    JOIN grid_city c ON aqi.city_id = c.city_id
    LEFT JOIN grid_member gm ON aqi.gm_id = gm.gm_id
    WHERE aqi.af_id = #{feedbackId}
    """)
    AqiFeedback findDetailForNepm(@Param("feedbackId") Integer feedbackId);

    @Select("""
    SELECT
        aqi.*,
        p.province_name,
        c.city_name
    FROM aqi_feedback aqi
    JOIN grid_province p
        ON aqi.province_id = p.province_id
    JOIN grid_city c
        ON aqi.city_id = c.city_id
    WHERE aqi.tel_id = #{telId}
    ORDER BY aqi.submitted_at DESC, aqi.af_id DESC
""")
    List<AqiFeedback> findByTelId(@Param("telId") String telId);

    @Select("""
    SELECT
        aqi.*,
        p.province_name,
        c.city_name
    FROM aqi_feedback aqi
    JOIN grid_province p
        ON aqi.province_id = p.province_id
    JOIN grid_city c
        ON aqi.city_id = c.city_id
    WHERE aqi.af_id = #{afId}
      AND aqi.tel_id = #{telId}
""")
    AqiFeedback findByIdAndTelId(@Param("afId") Integer afId, @Param("telId") String telId);
}
