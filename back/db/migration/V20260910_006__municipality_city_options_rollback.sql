-- 仅删除本迁移新增且未被业务反馈引用的直辖市城市选项。
DELETE c
FROM grid_city c
JOIN grid_province p ON p.province_id = c.province_id
LEFT JOIN aqi_feedback f ON f.city_id = c.city_id
WHERE p.province_name IN ('上海市', '重庆市')
  AND c.city_name = p.province_name
  AND c.remarks = '直辖市城市选项'
  AND f.af_id IS NULL;
