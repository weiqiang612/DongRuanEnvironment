# 中国省级地图资源

`china.geo.json` 于 2026-09-14 从阿里云 DataV GeoAtlas 公共边界地址 `https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json` 获取，作为本项目的静态中国省级底图使用。

`provinces/*.geo.json` 同日从 `https://geo.datav.aliyun.com/areas_v3/bound/{adcode}_full.json` 获取，用于点击省份后的市级边界下钻。前端仅在进入对应省份时按需加载该资源，不在页面运行时请求外部地图服务。公开源未提供台湾（`710000`）的对应边界文件，因此该区域保持现有的资源缺失降级提示，不伪造市级地图。

前端通过省份名称与国家 adcode 的静态映射关联项目区域。项目区域名称无法匹配地图时不补造地图数据，保留列表统计与空状态。
