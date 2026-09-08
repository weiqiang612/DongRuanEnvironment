package com.dongruan.environment.service.impl;

import com.dongruan.environment.dto.RegionOption;
import com.dongruan.environment.mapper.RegionMapper;
import com.dongruan.environment.service.IRegionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements IRegionService {

    private final RegionMapper regionMapper;

    @Override
    public List<RegionOption> findProvinces() {
        return regionMapper.findProvinces();
    }

    @Override
    public List<RegionOption> findCitiesByProvinceId(final Integer provinceId) {
        return regionMapper.findCitiesByProvinceId(provinceId);
    }
}
