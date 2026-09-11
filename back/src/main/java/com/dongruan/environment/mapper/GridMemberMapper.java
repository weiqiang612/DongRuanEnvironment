package com.dongruan.environment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongruan.environment.entity.GridMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GridMemberMapper extends BaseMapper<GridMember> {
    @Select("""
    SELECT gm.*, c.city_name
    FROM grid_member gm
    JOIN grid_city c ON gm.city_id = c.city_id
    WHERE gm.state = 0
    """)
    List<GridMember> findAvailableForNepm();
}
