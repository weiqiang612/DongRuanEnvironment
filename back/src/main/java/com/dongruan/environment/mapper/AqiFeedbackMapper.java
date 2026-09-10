package com.dongruan.environment.mapper;

import com.dongruan.environment.entity.AqiFeedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
