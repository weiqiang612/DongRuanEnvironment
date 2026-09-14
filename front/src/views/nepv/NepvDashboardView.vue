<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'
import chinaGeoJson from '@/assets/maps/china.geo.json'
import { getNepvDashboard, type NepvDashboard } from '@/api/nepv'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'
import { getAqiGradeChartColor, getAqiGradeColor } from '@/utils/aqiColors'
import { normalizeProvinceName } from '@/utils/provinceMap'
import { loadProvinceCityGeoJson } from '@/utils/provinceMaps'

echarts.registerMap('china-nepv', chinaGeoJson as never)
const router = useRouter()
const provinceOptions = ref<RegionOption[]>([])
const cityOptions = ref<RegionOption[]>([])
const provinceId = ref<number>()
const cityId = ref<number>()
const dateRange = ref<[string, string]>()
const dashboard = ref<NepvDashboard>()
const loading = ref(false)
const loadError = ref('')
const cityMapUnavailable = ref(false)
const mapZooming = ref(false)
const mapRef = ref<HTMLDivElement>()
const trendRef = ref<HTMLDivElement>()
const distributionRef = ref<HTMLDivElement>()
let mapChart: echarts.ECharts | undefined
let trendChart: echarts.ECharts | undefined
let distributionChart: echarts.ECharts | undefined
let shouldPlayProvinceDrilldown = false
let resizeAnimationFrame: number | undefined

const provinceRisks = computed(() => dashboard.value?.provinceRisks ?? [])
const cityRisks = computed(() => dashboard.value?.cityRisks ?? [])
const selectedProvince = computed(() => provinceOptions.value.find((item) => item.id === provinceId.value))
const isProvinceMap = computed(() => Boolean(selectedProvince.value))
const selectedCityRiskId = ref<number>()
const regionalRisks = computed(() =>
  isProvinceMap.value
    ? cityRisks.value.map((item) => ({
        id: item.cityId,
        name: item.cityName,
        detectionCount: item.detectionCount,
        highPollutionCount: item.highPollutionCount,
        pendingAlertCount: item.pendingAlertCount,
      }))
    : provinceRisks.value.map((item) => ({
        id: item.provinceId,
        name: item.provinceName,
        detectionCount: item.detectionCount,
        highPollutionCount: item.highPollutionCount,
        pendingAlertCount: item.pendingAlertCount,
      })),
)
const topRisks = computed(() => regionalRisks.value.slice(0, 5))
const mapRiskData = computed(() =>
  regionalRisks.value.map((item) => ({
    name: item.name,
    value: item.highPollutionCount,
    detectionCount: item.detectionCount,
    pendingAlertCount: item.pendingAlertCount,
  })),
)
const mapRiskMaximum = computed(() =>
  Math.max(1, ...mapRiskData.value.map((item) => Number(item.value))),
)
const mapTitle = computed(() =>
  isProvinceMap.value ? `${selectedProvince.value?.name ?? ''}市级 AQI 风险分布` : '中国省级污染风险分布',
)
const mapHint = computed(() =>
  isProvinceMap.value ? '悬停查看城市指标，点击城市高亮下方数据' : '悬停查看，点击省份进入市级地图',
)
const scopeLabel = computed(() => {
  if (cityId.value) return cityOptions.value.find((item) => item.id === cityId.value)?.name ?? '当前城市'
  return selectedProvince.value?.name ?? '全国'
})

async function changeProvince() {
  cityId.value = undefined
  selectedCityRiskId.value = undefined
  cityOptions.value = provinceId.value ? (await getCityOptions(provinceId.value)).data.data : []
}
function changeCity(value: number | undefined) {
  selectedCityRiskId.value = value
  void loadDashboard()
}
async function loadDashboard() {
  loading.value = true
  loadError.value = ''
  try {
    const response = await getNepvDashboard({
      provinceId: provinceId.value,
      cityId: cityId.value,
      submittedFrom: dateRange.value?.[0],
      submittedTo: dateRange.value?.[1],
    })
    dashboard.value = response.data.data
    await nextTick()
    renderCharts()
  } catch {
    dashboard.value = undefined
    loadError.value = '决策数据暂时无法加载，请确认登录状态和后端服务后重试。'
  } finally {
    loading.value = false
  }
}
function resetFilters() {
  provinceId.value = undefined
  cityId.value = undefined
  cityOptions.value = []
  selectedCityRiskId.value = undefined
  dateRange.value = undefined
  void loadDashboard()
}
function returnToNationalMap() {
  provinceId.value = undefined
  cityId.value = undefined
  cityOptions.value = []
  selectedCityRiskId.value = undefined
  void loadDashboard()
}
function cityRiskRowClass({ row }: { row: { cityId: number } }) {
  return row.cityId === selectedCityRiskId.value ? 'city-risk-row-selected' : ''
}
function selectCityRisk(row: { cityId: number }) {
  selectedCityRiskId.value = row.cityId
  if (cityId.value !== row.cityId) {
    cityId.value = row.cityId
    void loadDashboard()
  }
}
function renderCharts() {
  renderMap()
  renderTrend()
  renderDistribution()
}
async function renderMap() {
  if (!mapRef.value) return
  mapChart ??= echarts.init(mapRef.value)
  const requestedProvinceId = provinceId.value
  const provinceGeoJson = selectedProvince.value
    ? await loadProvinceCityGeoJson(selectedProvince.value.name)
    : undefined
  if (requestedProvinceId !== provinceId.value) return
  cityMapUnavailable.value = Boolean(isProvinceMap.value && !provinceGeoJson)
  if (cityMapUnavailable.value) {
    shouldPlayProvinceDrilldown = false
    mapChart.clear()
    return
  }
  const detailMapAvailable = isProvinceMap.value && provinceGeoJson
  const mapName = detailMapAvailable ? `nepv-province-${provinceId.value}` : 'china-nepv'
  if (detailMapAvailable) echarts.registerMap(mapName, provinceGeoJson as never)
  mapChart.setOption(
    {
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(4,23,52,.94)',
        borderColor: '#248bce',
        textStyle: { color: '#edf8ff' },
        formatter: (params: {
          name: string
          data?: { detectionCount: number; pendingAlertCount: number; value: number }
        }) => {
          const item =
            params.data ??
            mapRiskData.value.find((risk) =>
              detailMapAvailable
                ? risk.name === params.name
                : normalizeProvinceName(risk.name) === normalizeProvinceName(params.name),
            )
          return `${params.name}<br/>检测量：${item?.detectionCount ?? 0}<br/>高等级污染：${item?.value ?? 0}<br/>待处置预警：${item?.pendingAlertCount ?? 0}`
        },
      },
      visualMap: {
        min: 0,
        max: mapRiskMaximum.value,
        show: false,
        inRange: { color: ['#144f82', '#2087bf', '#f0b843', '#ed5e5e'] },
      },
      series: [
        {
          type: 'map',
          map: mapName,
          roam: true,
          layoutCenter: ['50%', '48%'],
          layoutSize: '90%',
          data: mapRiskData.value,
          label: { show: false },
          itemStyle: { areaColor: '#123f6c', borderColor: '#58b8ed', borderWidth: 0.8 },
          emphasis: {
            label: { show: true, color: '#fff', fontSize: 12, fontWeight: 'bold' },
            itemStyle: {
              areaColor: '#2aa7eb',
              shadowBlur: 16,
              shadowColor: 'rgba(36,178,255,.75)',
            },
          },
          select: {
            label: { show: true, color: '#fff', fontSize: 12, fontWeight: 'bold' },
            itemStyle: { areaColor: '#4cc3ff', borderColor: '#d6f5ff', borderWidth: 1.5 },
          },
        },
      ],
    },
    true,
  )
  if (shouldPlayProvinceDrilldown && detailMapAvailable) {
    shouldPlayProvinceDrilldown = false
    mapZooming.value = false
    requestAnimationFrame(() => {
      mapZooming.value = true
      window.setTimeout(() => {
        mapZooming.value = false
      }, 1000)
    })
  }
  mapChart.off('click')
  mapChart.on('click', (params) => {
    if (detailMapAvailable) {
      const matchedCity = cityOptions.value.find((option) => option.name === params.name)
      if (matchedCity) {
        selectedCityRiskId.value = matchedCity.id
        if (cityId.value !== matchedCity.id) {
          cityId.value = matchedCity.id
          void loadDashboard()
        }
      }
      return
    }
    const matched = provinceOptions.value.find(
      (option) => normalizeProvinceName(option.name) === normalizeProvinceName(params.name),
    )
    if (!matched) return
    shouldPlayProvinceDrilldown = true
    provinceId.value = matched.id
    void changeProvince().then(loadDashboard)
  })
}
function renderTrend() {
  if (!trendRef.value) return
  trendChart ??= echarts.init(trendRef.value)
  const trends = dashboard.value?.monthlyTrends ?? []
  trendChart.setOption(
    {
      color: ['#1ea4ff', '#66e8b2'],
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(4,23,52,.94)', borderColor: '#248bce' },
      legend: { data: ['检测量', '预警量'], top: 4, textStyle: { color: '#b9d9ef', fontSize: 11 } },
      grid: { left: 45, right: 36, top: 45, bottom: 28 },
      xAxis: {
        type: 'category',
        data: trends.map((item) => item.month),
        axisLine: { lineStyle: { color: '#2a648e' } },
        axisLabel: { color: '#a4c5dd', fontSize: 11 },
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        splitLine: { lineStyle: { color: 'rgba(74,135,178,.25)' } },
        axisLabel: { color: '#8fb6d0', fontSize: 11 },
      },
      series: [
        {
          name: '检测量',
          type: 'bar',
          barMaxWidth: 20,
          data: trends.map((item) => item.detectionCount),
          itemStyle: { color: '#2496e8' },
        },
        {
          name: '预警量',
          type: 'line',
          smooth: true,
          data: trends.map((item) => item.alertCount),
          symbolSize: 7,
          lineStyle: { width: 2 },
          itemStyle: { color: '#66e8b2' },
        },
      ],
    },
    true,
  )
}
function renderDistribution() {
  if (!distributionRef.value) return
  distributionChart ??= echarts.init(distributionRef.value)
  const distribution = dashboard.value?.aqiDistribution ?? []
  distributionChart.setOption(
    {
      tooltip: { trigger: 'item', backgroundColor: 'rgba(4,23,52,.94)', borderColor: '#248bce' },
      series: [
        {
          type: 'pie',
          radius: ['48%', '70%'],
          center: ['34%', '52%'],
          label: { show: false },
          labelLine: { show: false },
          data: distribution.map((item) => ({
            value: item.count,
            name: item.label,
            itemStyle: { color: getAqiGradeChartColor(item.aqiId) },
          })),
        },
      ],
      graphic: [
        {
          type: 'text',
          left: '27%',
          top: '42%',
          style: {
            text: `${dashboard.value?.totalDetections ?? 0}`,
            fill: '#f2fbff',
            font: '700 22px Microsoft YaHei',
          },
        },
        {
          type: 'text',
          left: '25%',
          top: '56%',
          style: { text: '总检测数', fill: '#93bad5', font: '12px Microsoft YaHei' },
        },
      ],
    },
    true,
  )
}
function resizeChart(chart: echarts.ECharts | undefined, container: HTMLDivElement | undefined) {
  if (!chart || chart.isDisposed() || !container?.isConnected || !container.clientWidth || !container.clientHeight) {
    return
  }
  chart.resize()
}
function resizeCharts() {
  if (resizeAnimationFrame !== undefined) cancelAnimationFrame(resizeAnimationFrame)
  resizeAnimationFrame = requestAnimationFrame(() => {
    resizeAnimationFrame = undefined
    resizeChart(mapChart, mapRef.value)
    resizeChart(trendChart, trendRef.value)
    resizeChart(distributionChart, distributionRef.value)
  })
}
onMounted(async () => {
  try {
    provinceOptions.value = (await getProvinceOptions()).data.data
  } catch {
    loadError.value = '地区筛选项暂时无法加载，请确认后端服务后重试。'
  }
  await loadDashboard()
  window.addEventListener('resize', resizeCharts)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (resizeAnimationFrame !== undefined) cancelAnimationFrame(resizeAnimationFrame)
  mapChart?.dispose()
  trendChart?.dispose()
  distributionChart?.dispose()
})
</script>

<template>
  <main class="decision-screen" v-loading="loading">
    <header class="screen-header">
      <div class="brand-lockup">
        <img :src="logo" alt="东软环保公众监督系统" />
        <div>
          <h1>东软环保公众监督系统</h1>
          <p>NEPV 决策者可视化驾驶舱</p>
        </div>
      </div>
      <p class="environment-slogan">绿水青山　共建美好未来</p>
      <div class="header-actions">
        <span>数据范围：{{ scopeLabel }}</span
        ><el-button text class="entry-button" @click="router.push('/')">返回入口</el-button>
      </div>
    </header>
    <section class="filter-bar" aria-label="决策数据筛选">
      <div class="filter-label">区域筛选</div>
      <el-select
        v-model="provinceId"
        clearable
        placeholder="全部省份"
        aria-label="省份"
        @change="changeProvince"
        ><el-option
          v-for="item in provinceOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
      /></el-select>
      <el-select
        v-model="cityId"
        clearable
        placeholder="全部城市"
        aria-label="城市"
        :disabled="!provinceId"
        @change="changeCity"
        ><el-option v-for="item in cityOptions" :key="item.id" :label="item.name" :value="item.id"
      /></el-select>
      <span class="date-label">时间范围</span
      ><el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <el-button type="primary" @click="loadDashboard">查询</el-button
      ><el-button class="reset-button" @click="resetFilters">重置</el-button>
    </section>
    <p v-if="loadError" class="error-state" role="alert">{{ loadError }}</p>
    <template v-else>
      <section class="metric-grid" aria-label="核心决策指标">
        <article class="metric-card metric-blue">
          <span>累计检测量</span><strong>{{ dashboard?.totalDetections ?? 0 }}</strong
          ><small>单位：条</small>
        </article>
        <article class="metric-card metric-red">
          <span>高等级污染</span><strong>{{ dashboard?.highPollutionDetections ?? 0 }}</strong
          ><small>AQI 4 至 6 级</small>
        </article>
        <article class="metric-card metric-orange">
          <span>待处置预警</span><strong>{{ dashboard?.pendingAlerts ?? 0 }}</strong
          ><small>高等级预警记录</small>
        </article>
        <article class="metric-card metric-green">
          <span>城市网格覆盖</span
          ><strong>{{ dashboard?.gridCoverage.coverageRate ?? 0 }}<em>%</em></strong
          ><small
            >{{ dashboard?.gridCoverage.coveredCities ?? 0 }} /
            {{ dashboard?.gridCoverage.totalCities ?? 0 }} 城市</small
          >
        </article>
      </section>
      <section class="dashboard-grid">
        <article class="panel trend-panel">
          <div class="panel-heading">
            <h2>月度检测与预警趋势</h2>
            <span>按反馈提交月归集</span>
          </div>
          <div ref="trendRef" class="chart trend-chart" />
          <p v-if="!dashboard?.monthlyTrends.length" class="chart-empty">暂无月度趋势数据</p>
        </article>
        <article class="panel map-panel">
          <div class="panel-heading">
            <h2>{{ mapTitle }}</h2>
            <span class="map-heading-actions"
              >{{ mapHint
              }}<el-button v-if="isProvinceMap" text class="return-map-button" @click="returnToNationalMap"
                >返回全国</el-button
              ></span
            >
          </div>
          <div ref="mapRef" :class="['chart', 'map-chart', { 'map-chart--drilldown': mapZooming }]" />
          <div v-if="!cityMapUnavailable" class="map-risk-scale" aria-label="高等级污染数颜色说明，从低到高">
            <span>风险热度（高等级污染数）</span><b>低 0</b><i aria-hidden="true" /><b>高 {{ mapRiskMaximum }}</b>
          </div>
          <p v-if="cityMapUnavailable" class="chart-empty map-empty"
            >当前省份暂无市级地图资源，请返回全国地图或通过城市筛选查看数据</p
          ><p v-else-if="!regionalRisks.length" class="chart-empty map-empty"
            >{{ isProvinceMap ? '该省暂无可映射的城市统计数据' : '暂无可映射的省级统计数据' }}</p
          >
        </article>
        <aside class="right-column">
          <article class="panel ranking-panel">
            <div class="panel-heading">
              <h2>{{ isProvinceMap ? '重点城市' : '重点省份' }}</h2>
              <span>高等级污染数</span>
            </div>
            <ol v-if="topRisks.length" class="ranking-list">
              <li v-for="(item, index) in topRisks" :key="item.id">
                <b>{{ index + 1 }}</b
                ><strong>{{ item.name }}</strong
                ><span>{{ item.highPollutionCount }}</span
                ><small>检测 {{ item.detectionCount }}</small>
              </li>
            </ol>
            <p v-else class="panel-empty">暂无区域统计数据</p>
          </article>
          <article class="panel distribution-panel">
            <div class="panel-heading">
              <h2>AQI 等级分布</h2>
              <span>当前筛选范围</span>
            </div>
            <div ref="distributionRef" class="chart distribution-chart" />
            <ul class="aqi-legend">
              <li v-for="item in dashboard?.aqiDistribution ?? []" :key="item.aqiId">
                <i :style="{ backgroundColor: getAqiGradeChartColor(item.aqiId) }" /><span>{{ item.label }}</span
                ><b>{{ item.count }}</b>
              </li>
            </ul>
          </article>
        </aside>
      </section>
      <section class="panel alert-panel">
        <div class="panel-heading">
          <h2>{{ isProvinceMap ? '省内城市 AQI 风险数据' : '最新高等级预警' }}</h2>
          <span>{{ isProvinceMap ? '点击地图城市可高亮对应行' : `只读展示 · 共 ${dashboard?.totalAlerts ?? 0} 条` }}</span>
        </div>
        <el-table
          v-if="isProvinceMap"
          :data="cityRisks"
          size="small"
          empty-text="该省暂无城市检测数据"
          row-key="cityId"
          :row-class-name="cityRiskRowClass"
          @row-click="selectCityRisk"
          aria-label="省内城市 AQI 风险数据"
          ><el-table-column prop="cityName" label="城市" min-width="130" /><el-table-column prop="detectionCount" label="检测量" width="100" /><el-table-column
            prop="highPollutionCount"
            label="高等级污染"
            width="130"
          /><el-table-column prop="pendingAlertCount" label="待处置预警" width="130" /><el-table-column label="主导 AQI" width="140"
            ><template #default="scope"
              ><span
                v-if="scope.row.dominantAqiId"
                class="aqi-tag nepv-aqi-tag"
                :style="{
                  '--nepv-aqi-fill': getAqiGradeChartColor(scope.row.dominantAqiId),
                  '--nepv-aqi-text': getAqiGradeColor(scope.row.dominantAqiId),
                }"
                >{{ scope.row.dominantAqiId }}级</span
              ><span v-else>—</span></template
            ></el-table-column
          ></el-table
        >
        <el-table
          v-else
          :data="dashboard?.recentAlerts ?? []"
          size="small"
          empty-text="暂无预警记录"
          aria-label="最新高等级预警列表"
          ><el-table-column label="等级" width="92"
            ><template #default="scope"
              ><span
                class="aqi-tag nepv-aqi-tag"
                :style="{
                  '--nepv-aqi-fill': getAqiGradeChartColor(scope.row.alertLevel),
                  '--nepv-aqi-text': getAqiGradeColor(scope.row.alertLevel),
                }"
                >{{ scope.row.alertLevel }}级</span
              ></template
            ></el-table-column
          ><el-table-column label="区域" min-width="160"
            ><template #default="scope"
              >{{ scope.row.provinceName }} {{ scope.row.cityName }}</template
            ></el-table-column
          ><el-table-column
            prop="address"
            label="地址"
            min-width="280"
            show-overflow-tooltip
          /><el-table-column prop="createdAt" label="生成时间" width="180" /><el-table-column
            label="处置状态"
            width="130"
            ><template #default="scope"
              ><span
                :class="[
                  'alert-status',
                  scope.row.alertStatus === 'PENDING' ? 'pending' : 'handled',
                ]"
                >{{ scope.row.alertStatus === 'PENDING' ? '待处置' : '已处置' }}</span
              ></template
            ></el-table-column
          ></el-table
        >
      </section>
    </template>
  </main>
</template>

<style>
.decision-screen {
  --line: rgb(79 182 247 / 0.48);
  --muted: #9bc7e6;
  min-width: 1280px;
  min-height: 100vh;
  box-sizing: border-box;
  padding: 0 18px 18px;
  background: #061a32 radial-gradient(circle at 50% -10%, rgb(28 102 162 / 0.58), transparent 43%);
  color: #eef8ff;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}
.screen-header {
  height: 70px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  border-bottom: 1px solid #1688ca;
  background: linear-gradient(
    90deg,
    rgb(2 37 73 / 0.86),
    rgb(8 85 139 / 0.26),
    rgb(2 37 73 / 0.86)
  );
}
.brand-lockup {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-lockup img {
  width: 56px;
  height: 56px;
  object-fit: contain;
}
.brand-lockup h1 {
  margin: 0;
  font-size: 25px;
  letter-spacing: 2px;
  line-height: 1.05;
}
.brand-lockup p {
  margin: 3px 0 0;
  color: #9fd2ee;
  font-size: 12px;
  letter-spacing: 5px;
}
.environment-slogan {
  margin: 0;
  color: #c8eeff;
  font-size: 16px;
  letter-spacing: 5px;
}
.header-actions {
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #a6cbe2;
  font-size: 12px;
}
.entry-button {
  color: #bceaff !important;
}
.filter-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 62px;
  margin-top: 14px;
  padding: 0 18px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: rgb(4 43 80 / 0.82);
}
.filter-label,
.date-label {
  position: relative;
  z-index: 1;
  color: #b9dcf2;
  font-size: 13px;
  white-space: nowrap;
}
.filter-label {
  padding-left: 18px;
}
.filter-label::before {
  position: absolute;
  left: 0;
  top: 3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #35b9ff;
  box-shadow: 0 0 12px #35b9ff;
  content: '';
}
.filter-bar .el-select {
  position: relative;
  z-index: 1;
  width: 155px;
}
.filter-bar .el-date-editor {
  position: relative;
  z-index: 1;
  width: 295px;
}
.filter-bar .el-input__wrapper {
  background: rgb(4 26 59 / 0.86);
  box-shadow: 0 0 0 1px rgb(89 181 241 / 0.55) inset;
}
.filter-bar .el-input__inner,
.filter-bar .el-range-input,
.filter-bar .el-range-separator {
  color: #d6edff;
}
.filter-bar .el-button {
  position: relative;
  z-index: 1;
  min-width: 76px;
}
.reset-button {
  color: #d7efff;
  background: rgb(4 35 71 / 0.8);
  border-color: #329ee0;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 12px;
}
.metric-card {
  position: relative;
  min-height: 90px;
  padding: 15px 18px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: linear-gradient(135deg, rgb(5 50 91 / 0.96), rgb(5 31 63 / 0.82));
  box-shadow: inset 0 0 24px rgb(22 139 219 / 0.13);
}
.metric-card::after {
  position: absolute;
  right: -16px;
  bottom: -26px;
  width: 90px;
  height: 90px;
  border: 1px solid currentColor;
  border-radius: 50%;
  opacity: 0.17;
  content: '';
}
.metric-card span,
.metric-card small {
  display: block;
  color: var(--muted);
  font-size: 13px;
}
.metric-card strong {
  display: inline-block;
  margin: 5px 0 4px;
  font-size: 29px;
  line-height: 1;
  color: #eaf8ff;
}
.metric-card em {
  font-size: 15px;
  font-style: normal;
}
.metric-red strong {
  color: #ff867b;
}
.metric-orange strong {
  color: #ffd277;
}
.metric-green strong {
  color: #6be7bd;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(300px, 0.9fr) minmax(600px, 1.45fr) minmax(315px, 0.78fr);
  gap: 12px;
  margin-top: 12px;
}
.panel {
  position: relative;
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: linear-gradient(135deg, rgb(5 46 83 / 0.9), rgb(4 29 60 / 0.9));
  box-shadow: inset 0 0 30px rgb(18 122 203 / 0.08);
}
.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  padding: 0 14px;
  border-bottom: 1px solid rgb(87 176 232 / 0.34);
  background: rgb(4 49 89 / 0.54);
}
.panel-heading h2 {
  position: relative;
  margin: 0;
  padding-left: 11px;
  color: #edf9ff;
  font-size: 16px;
}
.panel-heading h2::before {
  position: absolute;
  top: 1px;
  bottom: 1px;
  left: 0;
  width: 3px;
  background: #47d2ff;
  box-shadow: 0 0 9px #47d2ff;
  content: '';
}
.panel-heading span {
  color: #9ec9e4;
  font-size: 11px;
}
.map-heading-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.return-map-button {
  min-height: 24px;
  padding: 2px 6px;
  color: #8fe7ff !important;
}
.chart {
  width: 100%;
}
.trend-panel,
.map-panel {
  height: 448px;
}
.trend-chart {
  height: 392px;
}
.map-chart {
  height: 350px;
  transform-origin: 50% 52%;
  will-change: transform, opacity;
}
.map-chart--drilldown {
  animation: province-map-drilldown 1000ms cubic-bezier(0.16, 0.85, 0.27, 1) both;
}
@keyframes province-map-drilldown {
  from {
    transform: scale(0.7);
    opacity: 0.28;
  }
  58% {
    transform: scale(1.04);
    opacity: 0.94;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.map-risk-scale {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 42px;
  padding: 0 16px;
  border-top: 1px solid rgb(87 176 232 / 0.2);
  color: #9fc7e2;
  font-size: 11px;
}
.map-risk-scale span {
  margin-right: 4px;
  color: #dcefff;
}
.map-risk-scale b {
  color: #a8d1ee;
  font-weight: 500;
}
.map-risk-scale i {
  width: min(22vw, 180px);
  min-width: 96px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(90deg, #144f82, #2087bf, #f0b843, #ed5e5e);
  box-shadow: 0 0 10px rgb(54 171 237 / 0.42);
}
.chart-empty {
  position: absolute;
  inset: 52% 0 auto;
  text-align: center;
  color: #a4c7de;
  font-size: 13px;
  pointer-events: none;
}
.map-empty {
  inset: 47% 0 auto;
}
.right-column {
  display: grid;
  grid-template-rows: 200px 236px;
  gap: 12px;
}
.ranking-list {
  margin: 0;
  padding: 4px 14px;
  list-style: none;
}
.ranking-list li {
  display: grid;
  grid-template-columns: 25px 1fr 46px 66px;
  gap: 7px;
  align-items: center;
  min-height: 32px;
  border-bottom: 1px solid rgb(104 184 237 / 0.18);
  font-size: 12px;
}
.ranking-list li:last-child {
  border-bottom: 0;
}
.ranking-list b {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #1c79ba;
  color: #fff;
}
.ranking-list li:nth-child(1) b {
  background: #e95157;
}
.ranking-list li:nth-child(2) b {
  background: #ee8d38;
}
.ranking-list li:nth-child(3) b {
  background: #c3a43d;
}
.ranking-list strong {
  font-weight: 500;
}
.ranking-list span {
  color: #ffd689;
  font-size: 15px;
  font-weight: 700;
  text-align: right;
}
.ranking-list small {
  color: #94bad4;
  text-align: right;
}
.panel-empty {
  padding: 38px 12px;
  text-align: center;
  color: #a4c7de;
  font-size: 13px;
}
.distribution-panel {
  display: grid;
  grid-template-rows: 40px 196px;
}
.distribution-chart {
  height: 196px;
}
.aqi-legend {
  position: absolute;
  right: 12px;
  bottom: 9px;
  width: 43%;
  margin: 0;
  padding: 0;
  list-style: none;
}
.aqi-legend li {
  display: grid;
  grid-template-columns: 9px 1fr auto;
  gap: 6px;
  align-items: center;
  min-height: 21px;
  color: #b7d6ea;
  font-size: 11px;
}
.aqi-legend i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.aqi-legend b {
  color: #eff9ff;
  font-weight: 500;
}
.alert-panel {
  margin-top: 12px;
}
.alert-panel .el-table,
.alert-panel .el-table tr,
.alert-panel .el-table th.el-table__cell {
  background: transparent;
  color: #dceeff;
}
.alert-panel .el-table td.el-table__cell,
.alert-panel .el-table th.el-table__cell.is-leaf {
  border-color: rgb(113 185 230 / 0.2);
}
.alert-panel .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: rgb(28 118 182 / 0.24) !important;
}
.alert-panel .el-table .city-risk-row-selected > td.el-table__cell {
  background: rgb(46 159 223 / 0.34) !important;
  box-shadow: inset 3px 0 0 #79e5ff;
}
.nepv-aqi-tag {
  min-width: 40px;
  background-color: var(--nepv-aqi-fill) !important;
  color: var(--nepv-aqi-text) !important;
  border: 1px solid var(--nepv-aqi-fill) !important;
}
.alert-status {
  position: relative;
  padding-left: 13px;
}
.alert-status::before {
  position: absolute;
  top: 5px;
  left: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  content: '';
}
.pending {
  color: #ffb0a8;
}
.pending::before {
  background: #ff6671;
  box-shadow: 0 0 7px #ff6671;
}
.handled {
  color: #78dfb5;
}
.handled::before {
  background: #54dca5;
}
.error-state {
  margin: 16px 0;
  padding: 18px;
  border: 1px solid rgb(245 105 105 / 0.65);
  border-radius: 6px;
  background: rgb(104 24 43 / 0.42);
  color: #ffd2cf;
  text-align: center;
}
@media (max-width: 1279px) {
  .decision-screen {
    min-width: 0;
    padding: 0 14px 14px;
  }
  .screen-header {
    grid-template-columns: 1fr auto;
    height: auto;
    min-height: 66px;
  }
  .environment-slogan {
    display: none;
  }
  .dashboard-grid {
    grid-template-columns: 1fr 1fr;
  }
  .map-panel {
    grid-column: span 2;
  }
  .right-column {
    grid-column: span 2;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 250px;
  }
  .right-column .panel {
    height: 250px;
  }
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .filter-bar {
    height: auto;
    min-height: 62px;
    flex-wrap: wrap;
    padding-block: 10px;
  }
}
@media (max-width: 720px) {
  .screen-header {
    display: block;
    padding: 10px;
  }
  .header-actions {
    justify-content: space-between;
    margin-top: 8px;
  }
  .brand-lockup h1 {
    font-size: 20px;
  }
  .metric-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  .map-panel,
  .right-column {
    grid-column: auto;
  }
  .right-column {
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  .trend-panel,
  .map-panel {
    height: 390px;
  }
  .trend-chart,
  .map-chart {
    height: 334px;
  }
  .map-panel {
    height: 430px;
  }
  .map-chart {
    height: 346px;
  }
  .filter-bar .el-select,
  .filter-bar .el-date-editor {
    width: 100%;
  }
  .alert-panel {
    overflow: auto;
  }
  .alert-panel .el-table {
    min-width: 700px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .decision-screen,
  .decision-screen * {
    transition-duration: 0.01ms !important;
    animation-duration: 0.01ms !important;
  }
}
</style>
