<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Location, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getDetectionResult,
  getDetectionResults,
  type DetectionResultItem,
} from '@/api/nepm'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'

const searchProvince = ref('')
const searchCity = ref('')
const searchAqiId = ref<number | ''>('')
const searchDateRange = ref<[string, string]>()
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])
const list = ref<DetectionResultItem[]>([])

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const activeDetail = ref<DetectionResultItem | null>(null)

// 统一复用系统公共 AQI 6级标准业务语义配色类 (aqi-grade-1 ~ aqi-grade-6)
const aqiGradeMap: Record<number, { name: string; tagClass: string; color: string; desc: string }> = {
  1: { name: '一级（优）', tagClass: 'aqi-grade-1', color: '#1b6a3e', desc: '优' },
  2: { name: '二级（良）', tagClass: 'aqi-grade-2', color: '#1b6a3e', desc: '良' },
  3: { name: '三级（轻度污染）', tagClass: 'aqi-grade-3', color: '#7d5514', desc: '轻度污染' },
  4: { name: '四级（中度污染）', tagClass: 'aqi-grade-4', color: '#914216', desc: '中度污染' },
  5: { name: '五级（重度污染）', tagClass: 'aqi-grade-5', color: '#962828', desc: '重度污染' },
  6: { name: '六级（严重污染）', tagClass: 'aqi-grade-6', color: '#59258a', desc: '严重污染' },
}

function getAqiMeta(grade?: number) {
  if (!grade || !aqiGradeMap[grade]) {
    return { name: '未知', tagClass: '', color: '#94a3b8', desc: '-' }
  }
  return aqiGradeMap[grade]
}

function formatSn(prefix: string, id: number, padLen = 8): string {
  return `${prefix}${String(id).padStart(padLen, '0')}`
}

function formatTime(val?: string): string {
  if (!val) return '-'
  return val.replace('T', ' ').slice(0, 19)
}

async function loadProvinces() {
  try {
    const res = await getProvinceOptions()
    provinces.value = res.data.data
  } catch {
    ElMessage.error('加载省份列表失败')
  }
}

async function onProvinceChange() {
  searchCity.value = ''
  if (!searchProvince.value) {
    cities.value = []
    return
  }
  try {
    const res = await getCityOptions(Number(searchProvince.value))
    cities.value = res.data.data
  } catch {
    ElMessage.error('加载城市选项失败')
  }
}

async function loadResults() {
  loading.value = true
  try {
    const res = await getDetectionResults({
      provinceId: searchProvince.value ? Number(searchProvince.value) : undefined,
      cityId: searchCity.value ? Number(searchCity.value) : undefined,
      aqiId: searchAqiId.value !== '' ? Number(searchAqiId.value) : undefined,
      submittedFrom: searchDateRange.value?.[0],
      submittedTo: searchDateRange.value?.[1],
      keyword: searchKeyword.value.trim() || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    list.value = res.data.data.items ?? []
    total.value = res.data.data.total ?? 0
  } catch {
    ElMessage.error('查询检测结果失败，请确认网络或服务状态')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  void loadResults()
}

function handleReset() {
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  searchAqiId.value = ''
  searchDateRange.value = undefined
  searchKeyword.value = ''
  page.value = 1
  void loadResults()
}

async function openDetail(row: DetectionResultItem) {
  detailVisible.value = true
  detailLoading.value = true
  activeDetail.value = row
  try {
    const res = await getDetectionResult(row.id)
    if (res.data.data) {
      activeDetail.value = res.data.data
    }
  } catch {
    // 降级使用行内数据
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  void loadProvinces()
  void loadResults()
})
</script>

<template>
  <div class="view-results">
    <!-- 复合筛选卡片（单行复用任务管理排版） -->
    <div class="filter-panel">
      <div class="filter-row">
        <div class="filter-item">
          <label>省市</label>
          <div class="region-selects">
            <el-select
              v-model="searchProvince"
              placeholder="全部省份"
              clearable
              style="width: 106px"
              @change="onProvinceChange"
            >
              <el-option
                v-for="item in provinces"
                :key="item.id"
                :label="item.name"
                :value="String(item.id)"
              />
            </el-select>
            <el-select
              v-model="searchCity"
              placeholder="全部城市"
              clearable
              :disabled="!searchProvince"
              style="width: 106px"
            >
              <el-option
                v-for="item in cities"
                :key="item.id"
                :label="item.name"
                :value="String(item.id)"
              />
            </el-select>
          </div>
        </div>

        <div class="filter-item">
          <label>AQI等级</label>
          <el-select
            v-model="searchAqiId"
            placeholder="全部等级"
            clearable
            style="width: 110px"
          >
            <el-option label="全部等级" value="" />
            <el-option :value="1" label="优 (一级)" />
            <el-option :value="2" label="良 (二级)" />
            <el-option :value="3" label="轻度污染 (三级)" />
            <el-option :value="4" label="中度污染 (四级)" />
            <el-option :value="5" label="重度污染 (五级)" />
            <el-option :value="6" label="严重污染 (六级)" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>反馈日期</label>
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 215px"
          />
        </div>

        <div class="filter-item">
          <label>关键词</label>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入地址、检测编号或反馈编号"
            clearable
            style="width: 255px"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 数据列表表格 -->
    <div class="panel-card mt-3">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        class="main-data-table"
        empty-text="暂无符合条件的检测结果数据"
      >
        <el-table-column label="区域与地址" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="loc-address-cell">
              <el-icon class="loc-icon"><Location /></el-icon>
              <div class="loc-content">
                <div class="loc-title">{{ row.address }}</div>
                <div class="loc-subtitle">
                  <span>{{ row.provinceName }} {{ row.cityName }}</span>
                  <span class="sub-sep">|</span>
                  <span>{{ formatSn('JC', row.id, 8) }}</span>
                  <span class="sub-sep">|</span>
                  <span>{{ formatSn('FB', row.feedbackId, 10) }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 拆分 3 个独立 AQI 指标列 -->
        <el-table-column width="105" align="center">
          <template #header>
            <div class="th-stack">
              <span class="th-name">SO₂</span>
              <span class="th-unit">(μg/m³)</span>
            </div>
          </template>
          <template #default="{ row }">
            <span class="measure-num">{{ row.so2Value ?? '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column width="105" align="center">
          <template #header>
            <div class="th-stack">
              <span class="th-name">CO</span>
              <span class="th-unit">(mg/m³)</span>
            </div>
          </template>
          <template #default="{ row }">
            <span class="measure-num">{{ row.coValue ?? '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column width="105" align="center">
          <template #header>
            <div class="th-stack">
              <span class="th-name">PM2.5</span>
              <span class="th-unit">(μg/m³)</span>
            </div>
          </template>
          <template #default="{ row }">
            <span class="measure-num">{{ row.spmValue ?? '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="最终AQI等级" width="135" align="center">
          <template #default="{ row }">
            <span :class="['aqi-pill', `aqi-grade-${row.aqiId}`]">
              {{ getAqiMeta(row.aqiId).name }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="gmName" label="检测网格员" width="110" align="center">
          <template #default="{ row }">
            <span class="text-regular">{{ row.gmName || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="检测时间" width="165" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.detectedAt) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="85" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="View" @click="openDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-bar">
        <span class="page-total">共 {{ total }} 条检测记录</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          layout="sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="loadResults"
          @size-change="loadResults"
        />
      </div>
    </div>

    <!-- 只读详情模态弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="检测结果详情（只读归档）"
      width="780px"
      destroy-on-close
      class="detection-detail-dialog"
    >
      <div v-loading="detailLoading" class="detail-container">
        <div v-if="activeDetail" class="detail-content">
          <!-- 状态提示横幅 -->
          <div class="dialog-banner">
            <div class="banner-title">
              <span class="banner-label">最终空气质量评级：</span>
              <span :class="['aqi-pill', 'aqi-pill-large', getAqiMeta(activeDetail.aqiId).tagClass]">
                {{ getAqiMeta(activeDetail.aqiId).name }}
              </span>
            </div>
            <div class="banner-desc">
              系统依据网格员实测数值（SO₂、CO、PM2.5）自动核算生成最终 AQI，数据客观真实，只读归档留痕。
            </div>
          </div>

          <!-- 反馈事实信息 -->
          <div class="section-block">
            <h4 class="section-title">关联反馈事实</h4>
            <div class="info-grid">
              <div class="info-cell">
                <span class="label">反馈编号：</span>
                <span class="value font-mono">{{ formatSn('FB', activeDetail.feedbackId, 12) }}</span>
              </div>
              <div class="info-cell">
                <span class="label">反馈时间：</span>
                <span class="value">{{ formatTime(activeDetail.submittedAt) }}</span>
              </div>
              <div class="info-cell full-width">
                <span class="label">发生区域：</span>
                <span class="value">{{ activeDetail.provinceName }} {{ activeDetail.cityName }}</span>
              </div>
              <div class="info-cell full-width">
                <span class="label">详细地址：</span>
                <span class="value">{{ activeDetail.address }}</span>
              </div>
              <div class="info-cell full-width">
                <span class="label">公众预估等级：</span>
                <span :class="['aqi-pill', getAqiMeta(activeDetail.estimatedGrade).tagClass]">
                  {{ getAqiMeta(activeDetail.estimatedGrade).name }}
                </span>
              </div>
              <div class="info-cell full-width">
                <span class="label">反馈描述：</span>
                <div class="value-text-box">{{ activeDetail.information || '暂无详细描述' }}</div>
              </div>
            </div>
          </div>

          <!-- 网格员检测信息 -->
          <div class="section-block mt-4">
            <h4 class="section-title">网格巡查员信息</h4>
            <div class="info-grid">
              <div class="info-cell">
                <span class="label">网格员姓名：</span>
                <span class="value text-bold">{{ activeDetail.gmName || '-' }}</span>
              </div>
              <div class="info-cell">
                <span class="label">网格员工号：</span>
                <span class="value font-mono">{{ activeDetail.gmId || '-' }}</span>
              </div>
              <div class="info-cell full-width">
                <span class="label">检测完成时间：</span>
                <span class="value">{{ formatTime(activeDetail.detectedAt) }}</span>
              </div>
            </div>
          </div>

          <!-- 实测污染物数值卡片 -->
          <div class="section-block mt-4">
            <h4 class="section-title">实测污染物浓度与分项等级</h4>
            <div class="pollutants-cards">
              <!-- SO2 -->
              <div class="pollutant-card">
                <div class="card-name">二氧化硫 (SO₂)</div>
                <div class="card-num">
                  <span class="num">{{ activeDetail.so2Value }}</span>
                  <span class="unit">μg/m³</span>
                </div>
                <div class="card-level">
                  分项评级：
                  <span :class="['aqi-pill', getAqiMeta(activeDetail.so2Level).tagClass]">
                    {{ getAqiMeta(activeDetail.so2Level).name }}
                  </span>
                </div>
              </div>

              <!-- CO -->
              <div class="pollutant-card">
                <div class="card-name">一氧化碳 (CO)</div>
                <div class="card-num">
                  <span class="num">{{ activeDetail.coValue }}</span>
                  <span class="unit">mg/m³</span>
                </div>
                <div class="card-level">
                  分项评级：
                  <span :class="['aqi-pill', getAqiMeta(activeDetail.coLevel).tagClass]">
                    {{ getAqiMeta(activeDetail.coLevel).name }}
                  </span>
                </div>
              </div>

              <!-- PM2.5 -->
              <div class="pollutant-card">
                <div class="card-name">细颗粒物 (PM2.5)</div>
                <div class="card-num">
                  <span class="num">{{ activeDetail.spmValue }}</span>
                  <span class="unit">μg/m³</span>
                </div>
                <div class="card-level">
                  分项评级：
                  <span :class="['aqi-pill', getAqiMeta(activeDetail.spmLevel).tagClass]">
                    {{ getAqiMeta(activeDetail.spmLevel).name }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-panel {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}
.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.mt-3 {
  margin-top: 14px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.filter-item label {
  color: #475569;
  white-space: nowrap;
}
.region-selects {
  display: flex;
  align-items: center;
  gap: 6px;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}
.panel-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 18px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  width: 100%;
  box-sizing: border-box;
}
.font-mono {
  font-family: monospace;
}
.text-regular {
  color: #334155;
  font-size: 13px;
}

/* 区域与地址复合展示 (对齐参考图) */
.loc-address-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 2px 0;
}
.loc-icon {
  color: #1677ff;
  font-size: 16px;
  margin-top: 3px;
  flex-shrink: 0;
}
.loc-content {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.loc-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
  word-break: break-all;
}
.loc-subtitle {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}
.sub-sep {
  color: #cbd5e1;
  font-weight: normal;
}

/* 拆分后的指标列双行表头与数值 */
.th-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.25;
}
.th-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
.th-unit {
  font-size: 11px;
  color: #64748b;
  font-weight: normal;
}
.measure-num {
  font-size: 13.5px;
  color: #1e293b;
  font-weight: 500;
}
.time-text {
  font-size: 13px;
  color: #334155;
}

.aqi-pill-large {
  font-size: 14px;
  padding: 4px 12px;
  font-weight: 600;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}
.page-total {
  font-size: 13px;
  color: #64748b;
}

/* 详情弹窗样式 */
.detail-container {
  min-height: 200px;
}
.dialog-banner {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #1F5A94;
  border-radius: 6px;
  padding: 14px 18px;
  margin-bottom: 20px;
}
.banner-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.banner-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.banner-desc {
  margin-top: 6px;
  font-size: 12.5px;
  color: #64748b;
  line-height: 1.5;
}
.section-block {
  background: #fafbfc;
  border: 1px solid #eaeff5;
  border-radius: 8px;
  padding: 16px;
}
.section-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #1F5A94;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}
.section-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  background: #1F5A94;
  border-radius: 2px;
  margin-right: 8px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 20px;
  font-size: 13px;
}
.info-cell {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}
.info-cell.full-width {
  grid-column: 1 / -1;
}
.info-cell .label {
  color: #64748b;
  white-space: nowrap;
  min-width: 90px;
}
.info-cell .value {
  color: #1e293b;
  word-break: break-all;
}
.value-text-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
  width: 100%;
}
.pollutants-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.pollutant-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  text-align: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.card-name {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}
.card-num {
  margin-bottom: 10px;
}
.card-num .num {
  font-size: 24px;
  font-weight: 700;
  color: #1F5A94;
  font-family: monospace;
}
.card-num .unit {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 4px;
}
.card-level {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
@media (max-width: 768px) {
  .pollutants-cards {
    grid-template-columns: 1fr;
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
