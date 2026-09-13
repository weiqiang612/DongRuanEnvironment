package com.dongruan.environment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongruan.environment.dto.AlertRecordStatItem;
import com.dongruan.environment.dto.NepmAqiAlertVO;
import com.dongruan.environment.entity.AlertRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {

    @Select("""
    <script>
    SELECT
        ar.id,
        ar.feedback_id,
        ar.result_id,
        ar.alert_level,
        ar.alert_status,
        ar.created_at,
        ar.handled_at,
        af.province_id,
        p.province_name,
        af.city_id,
        c.city_name,
        af.address,
        dr.aqi_id,
        gm.gm_name
    FROM alert_record ar
    JOIN aqi_feedback af ON ar.feedback_id = af.af_id
    JOIN grid_province p ON af.province_id = p.province_id
    JOIN grid_city c ON af.city_id = c.city_id
    LEFT JOIN detection_result dr ON ar.result_id = dr.id
    LEFT JOIN grid_member gm ON dr.gm_id = gm.gm_id
    <where>
      <if test='status != null and status != ""'>AND ar.alert_status = #{status}</if>
      <if test='provinceId != null'>AND af.province_id = #{provinceId}</if>
      <if test='cityId != null'>AND af.city_id = #{cityId}</if>
    </where>
    ORDER BY ar.created_at DESC, ar.id DESC
    </script>
    """)
    List<NepmAqiAlertVO> findForNepm(
            @Param("status") String status,
            @Param("provinceId") Integer provinceId,
            @Param("cityId") Integer cityId);

    @Select("""
    SELECT
        ar.id,
        ar.feedback_id,
        ar.result_id,
        ar.alert_level,
        ar.alert_status,
        ar.created_at,
        ar.handled_at,
        af.province_id,
        p.province_name,
        af.city_id,
        c.city_name,
        af.address,
        dr.aqi_id,
        gm.gm_name
    FROM alert_record ar
    JOIN aqi_feedback af ON ar.feedback_id = af.af_id
    JOIN grid_province p ON af.province_id = p.province_id
    JOIN grid_city c ON af.city_id = c.city_id
    LEFT JOIN detection_result dr ON ar.result_id = dr.id
    LEFT JOIN grid_member gm ON dr.gm_id = gm.gm_id
    WHERE ar.id = #{id}
    """)
    NepmAqiAlertVO findDetailForNepm(@Param("id") Long id);

    @Update("""
    UPDATE alert_record
    SET alert_status = 'HANDLED', handled_at = #{handledAt}
    WHERE id = #{id} AND alert_status = 'PENDING'
    """)
    int handleAlert(@Param("id") Long id, @Param("handledAt") LocalDateTime handledAt);

    @Select("""
    <script>
    SELECT
        ar.id,
        ar.alert_status,
        ar.alert_level,
        af.submitted_at
    FROM alert_record ar
    JOIN aqi_feedback af ON ar.feedback_id = af.af_id
    <where>
      <if test='provinceId != null'>AND af.province_id = #{provinceId}</if>
      <if test='cityId != null'>AND af.city_id = #{cityId}</if>
      <if test='submittedFrom != null'>AND af.submitted_at &gt;= #{submittedFrom}</if>
      <if test='submittedToExclusive != null'>AND af.submitted_at &lt; #{submittedToExclusive}</if>
    </where>
    </script>
    """)
    List<AlertRecordStatItem> findAlertsForStats(
            @Param("provinceId") Integer provinceId,
            @Param("cityId") Integer cityId,
            @Param("submittedFrom") LocalDateTime submittedFrom,
            @Param("submittedToExclusive") LocalDateTime submittedToExclusive);
}

