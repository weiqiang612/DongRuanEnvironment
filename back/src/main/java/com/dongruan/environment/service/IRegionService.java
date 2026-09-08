package com.dongruan.environment.service;

import com.dongruan.environment.dto.RegionOption;
import java.util.List;

public interface IRegionService {

    List<RegionOption> findProvinces();

    List<RegionOption> findCitiesByProvinceId(Integer provinceId);
}
