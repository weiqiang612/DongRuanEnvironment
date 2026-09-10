-- 为缺少市级选项的直辖市补齐同名城市记录，供现有省市级联选择复用。
INSERT INTO grid_city (city_name, province_id, remarks)
SELECT p.province_name, p.province_id, '直辖市城市选项'
FROM grid_province p
LEFT JOIN grid_city c ON c.province_id = p.province_id
WHERE p.province_name IN ('上海市', '重庆市')
  AND c.city_id IS NULL;
