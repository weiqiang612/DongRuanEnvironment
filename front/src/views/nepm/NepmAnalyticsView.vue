<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'
import {
  DocumentChecked,
  Histogram,
  InfoFilled,
  Refresh,
  Search,
  Tickets,
  Timer,
  Top,
  Bottom,
  WarningFilled,
  TrendCharts,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getAnalyticsStats,
  type AnalyticsStats,
  type RegionRiskItem,
  type EfficiencyTrendItem,
  type PollutionTrendItem,
} from '@/api/nepm'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'

const searchProvince = ref('')
const searchCity = ref('')
const searchDateRange = ref<[string, string]>(['2026-04-01', '2026-09-13'])
const loading = ref(false)

const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])

// 统计核心数据
const stats = ref<AnalyticsStats>({
  totalDetections: 127,
  aqiDistribution: [],
  monthlyTrends: [],
  highAlertCount: 26,
  pendingAlertCount: 0,
  handledAlertCount: 26,
  totalFeedbacks: 328,
  completionRate: 92.3,
  completionRateChange: 2.8,
  timeoutRate: 5.1,
  timeoutRateChange: -1.5,
  highPollutionRate: 18.6,
  highPollutionRateChange: 3.2,
  periodInsight: '',
})

// 标准缺省基准集（原型对齐）
const defaultRegionRisks: RegionRiskItem[] = [
  { regionName: '丰台区', feedbackCount: 186, highPollutionRate: 36.7 },
  { regionName: '朝阳区', feedbackCount: 280, highPollutionRate: 24.3 },
  { regionName: '唐山市', feedbackCount: 95, highPollutionRate: 25.0 },
  { regionName: '石家庄市', feedbackCount: 135, highPollutionRate: 18.0 },
  { regionName: '海淀区', feedbackCount: 120, highPollutionRate: 18.1 },
  { regionName: '西湖区', feedbackCount: 185, highPollutionRate: 12.5 },
]

const defaultEfficiencyTrends: EfficiencyTrendItem[] = [
  { month: '2026-04', feedbackCount: 140, completionRate: 78.5 },
  { month: '2026-05', feedbackCount: 195, completionRate: 82.1 },
  { month: '2026-06', feedbackCount: 175, completionRate: 85.6 },
  { month: '2026-07', feedbackCount: 220, completionRate: 88.9 },
  { month: '2026-08', feedbackCount: 245, completionRate: 91.2 },
  { month: '2026-09', feedbackCount: 280, completionRate: 92.3 },
]

const defaultPollutionTrends: PollutionTrendItem[] = [
  { month: '2026-04', highPollutionRate: 12.3 },
  { month: '2026-05', highPollutionRate: 14.6 },
  { month: '2026-06', highPollutionRate: 16.8 },
  { month: '2026-07', highPollutionRate: 20.1 },
  { month: '2026-08', highPollutionRate: 22.4 },
  { month: '2026-09', highPollutionRate: 18.6 },
]

// 计算属性：核心数据展示
const completionRateDisplay = computed(() => (stats.value.completionRate ?? 92.3).toFixed(1))
const timeoutRateDisplay = computed(() => (stats.value.timeoutRate ?? 5.1).toFixed(1))
const highPollutionRateDisplay = computed(() => (stats.value.highPollutionRate ?? 18.6).toFixed(1))

const regionRiskList = computed<RegionRiskItem[]>(() => {
  if (stats.value.regionRisks && stats.value.regionRisks.length > 0) {
    return stats.value.regionRisks
  }
  return defaultRegionRisks
})

// 第二层：区域重点关注排行（Top 5 直观横向对比列表）
interface RankedRegionItem {
  rank: number
  regionName: string
  feedbackCount: number
  highPollutionRate: number
  riskLevel: '极高风险' | '高风险' | '中风险' | '正常关注'
  percentage: number
}

const rankedRegionList = computed<RankedRegionItem[]>(() => {
  const list = [...regionRiskList.value]
  // 综合严重度排序
  list.sort((a, b) => (b.feedbackCount * 0.6 + b.highPollutionRate * 3) - (a.feedbackCount * 0.6 + a.highPollutionRate * 3))
  const maxFeedback = Math.max(...list.map((i) => i.feedbackCount), 300)

  return list.slice(0, 5).map((item, index) => {
    let riskLevel: '极高风险' | '高风险' | '中风险' | '正常关注' = '正常关注'
    if (item.highPollutionRate >= 30) {
      riskLevel = '极高风险'
    } else if (item.feedbackCount >= 200 || item.highPollutionRate >= 20) {
      riskLevel = '高风险'
    } else if (item.highPollutionRate >= 15 || item.feedbackCount >= 120) {
      riskLevel = '中风险'
    }

    return {
      rank: index + 1,
      regionName: item.regionName,
      feedbackCount: item.feedbackCount,
      highPollutionRate: item.highPollutionRate,
      riskLevel,
      percentage: Math.min(Math.round((item.feedbackCount / maxFeedback) * 100), 100),
    }
  })
})

const efficiencyTrendList = computed<EfficiencyTrendItem[]>(() => {
  if (stats.value.efficiencyTrends && stats.value.efficiencyTrends.length > 0) {
    return stats.value.efficiencyTrends
  }
  return defaultEfficiencyTrends
})

const pollutionTrendList = computed<PollutionTrendItem[]>(() => {
  if (stats.value.pollutionTrends && stats.value.pollutionTrends.length > 0) {
    return stats.value.pollutionTrends
  }
  return defaultPollutionTrends
})

// 第一层：根据当前真实数据动态生成分析结论
const dynamicConclusion = computed(() => {
  const list = [...regionRiskList.value]
  const topPollution = [...list].sort((a, b) => b.highPollutionRate - a.highPollutionRate)[0]
  const topFeedback = [...list].sort((a, b) => b.feedbackCount - a.feedbackCount)[0]

  return {
    compRate: completionRateDisplay.value,
    pName: topPollution?.regionName || '部分重点区域',
    pRate: topPollution ? topPollution.highPollutionRate.toFixed(1) : '0.0',
    fName: topFeedback?.regionName || '部分区域',
    fCount: topFeedback ? topFeedback.feedbackCount : 0,
  }
})

// ECharts DOM 引用与实例
const scatterChartRef = ref<HTMLDivElement>()
const efficiencyChartRef = ref<HTMLDivElement>()
const pollutionChartRef = ref<HTMLDivElement>()

const scatterChartInstance = shallowRef<echarts.ECharts>()
const efficiencyChartInstance = shallowRef<echarts.ECharts>()
const pollutionChartInstance = shallowRef<echarts.ECharts>()

type BuiltinPos = 'top' | 'bottom' | 'left' | 'right'

// 初始化四象限散点图 (深度分析)
function initScatterChart() {
  if (!scatterChartRef.value) return
  if (!scatterChartInstance.value) {
    scatterChartInstance.value = echarts.init(scatterChartRef.value)
  }

  const items = regionRiskList.value
  const data = items.map((item) => {
    const isMajorFocus = item.regionName === '丰台区' || item.regionName === '朝阳区'
    let color = '#94a3b8'
    let category = '正常处理'
    let labelPos: BuiltinPos = 'right'
    let symbolSize = 12

    if (item.regionName === '丰台区') {
      color = '#f97316'
      category = '重点关注区'
      labelPos = 'right'
      symbolSize = 22
    } else if (item.regionName === '朝阳区') {
      color = '#ef4444'
      category = '重点关注区'
      labelPos = 'right'
      symbolSize = 22
    } else if (item.regionName === '唐山市') {
      color = '#94a3b8'
      category = '保持关注'
      labelPos = 'top'
    } else if (item.regionName === '石家庄市') {
      color = '#94a3b8'
      category = '正常关注'
      labelPos = 'bottom'
    } else if (item.regionName === '海淀区') {
      color = '#94a3b8'
      category = '正常关注'
      labelPos = 'top'
    } else if (item.regionName === '西湖区') {
      color = '#94a3b8'
      category = '正常处理'
      labelPos = 'right'
    }

    return {
      name: item.regionName,
      value: [item.feedbackCount, item.highPollutionRate],
      category,
      symbolSize,
      itemStyle: {
        color,
        opacity: isMajorFocus ? 1 : 0.45,
      },
      label: {
        show: true,
        formatter: '{b}',
        position: labelPos,
        distance: 6,
        color: isMajorFocus ? '#0f172a' : '#94a3b8',
        fontSize: isMajorFocus ? 12 : 10,
        fontWeight: isMajorFocus ? 600 : 400,
      },
    }
  })

  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      top: 45,
      right: 35,
      bottom: 45,
      left: 45,
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: '#ffffff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#0f172a', fontSize: 12 },
      formatter: (params: unknown) => {
        const p = params as {
          name: string
          value: [number, number]
          data: { category: string }
        }
        return `
          <div style="font-weight:600;margin-bottom:4px;color:#0f172a">${p.name} · <span style="font-weight:normal;color:#64748b">${p.data.category}</span></div>
          <div style="color:#64748b;font-size:11px">反馈总量: <b style="color:#0284c7">${p.value[0]}</b> 条</div>
          <div style="color:#64748b;font-size:11px">高等级污染占比: <b style="color:#ef4444">${p.value[1]}%</b></div>
        `
      },
    },
    xAxis: {
      type: 'value',
      name: '反馈量 (条)',
      nameLocation: 'middle',
      nameGap: 24,
      nameTextStyle: { color: '#94a3b8', fontSize: 10 },
      min: 0,
      max: 320,
      interval: 50,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      name: '高等级污染占比',
      nameLocation: 'end',
      nameTextStyle: { color: '#94a3b8', fontSize: 10, align: 'left', padding: [0, 0, 0, -10] },
      min: 0,
      max: 50,
      interval: 10,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: {
        formatter: '{value}%',
        color: '#94a3b8',
        fontSize: 10,
      },
      splitLine: { lineStyle: { color: '#f8fafc' } },
    },
    graphic: [
      {
        type: 'text',
        right: 45,
        top: 60,
        style: {
          text: '重点关注区\n(高反馈 · 高风险)',
          fill: 'rgba(239, 68, 68, 0.40)',
          font: 'bold 10px sans-serif',
          align: 'center',
        },
      },
      {
        type: 'text',
        left: 95,
        top: 60,
        style: {
          text: '低反馈 · 高风险\n(潜在风险)',
          fill: '#e2e8f0',
          font: '10px sans-serif',
          align: 'center',
        },
      },
      {
        type: 'text',
        right: 45,
        bottom: 75,
        style: {
          text: '高反馈 · 低风险\n(正常处理)',
          fill: '#e2e8f0',
          font: '10px sans-serif',
          align: 'center',
        },
      },
      {
        type: 'text',
        left: 95,
        bottom: 75,
        style: {
          text: '低反馈 · 低风险\n(保持关注)',
          fill: '#e2e8f0',
          font: '10px sans-serif',
          align: 'center',
        },
      },
    ],
    series: [
      {
        type: 'scatter',
        data,
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: {
            color: '#e2e8f0',
            type: 'dashed',
            width: 1,
          },
          data: [
            {
              xAxis: 150,
              label: {
                formatter: '中位线 150条',
                position: 'end',
                color: '#cbd5e1',
                fontSize: 9,
              },
            },
            {
              yAxis: 30,
              label: {
                formatter: '警戒线 30%',
                position: 'end',
                color: '#cbd5e1',
                fontSize: 9,
              },
            },
          ],
        },
      },
    ],
  }

  scatterChartInstance.value.setOption(option)
}

// 初始化效率趋势柱线混合图 (第二层核心问题)
function initEfficiencyChart() {
  if (!efficiencyChartRef.value) return
  if (!efficiencyChartInstance.value) {
    efficiencyChartInstance.value = echarts.init(efficiencyChartRef.value)
  }

  const items = efficiencyTrendList.value
  const months = items.map((item) => item.month)
  const counts = items.map((item) => item.feedbackCount)
  const rates = items.map((item) => item.completionRate)

  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      top: 35,
      right: 42,
      bottom: 25,
      left: 42,
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#0f172a' },
    },
    legend: {
      top: 0,
      left: 'center',
      textStyle: { color: '#7a8ca5', fontSize: 11 },
      data: ['反馈量 (条)', '完成率 (%)'],
      itemWidth: 10,
      itemHeight: 10,
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisTick: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#7a8ca5', fontSize: 11 },
    },
    yAxis: [
      {
        type: 'value',
        max: 400,
        interval: 100,
        splitLine: { lineStyle: { color: '#f1f5f9' } },
        axisLabel: { color: '#7a8ca5', fontSize: 11 },
      },
      {
        type: 'value',
        min: 0,
        max: 100,
        interval: 20,
        axisLabel: {
          formatter: '{value}%',
          color: '#7a8ca5',
          fontSize: 11,
        },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '反馈量 (条)',
        type: 'bar',
        barWidth: 16,
        itemStyle: {
          color: '#38bdf8',
          borderRadius: [3, 3, 0, 0],
        },
        data: counts,
      },
      {
        name: '完成率 (%)',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#10b981' },
        lineStyle: { width: 2.2, color: '#10b981' },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%',
          color: '#059669',
          fontSize: 10,
          fontWeight: 600,
        },
        data: rates,
      },
    ],
  }

  efficiencyChartInstance.value.setOption(option)
}

// 初始化污染风险趋势面积折线图 (第三层深度分析)
function initPollutionChart() {
  if (!pollutionChartRef.value) return
  if (!pollutionChartInstance.value) {
    pollutionChartInstance.value = echarts.init(pollutionChartRef.value)
  }

  const items = pollutionTrendList.value
  const months = items.map((item) => item.month)
  const rates = items.map((item) => item.highPollutionRate)

  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      top: 35,
      right: 25,
      bottom: 25,
      left: 42,
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#0f172a' },
    },
    legend: {
      top: 0,
      left: 'center',
      textStyle: { color: '#7a8ca5', fontSize: 11 },
      data: ['高等级污染占比 (4~6级AQI)'],
      itemWidth: 10,
      itemHeight: 10,
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisTick: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#7a8ca5', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 40,
      interval: 10,
      axisLabel: {
        formatter: '{value}%',
        color: '#7a8ca5',
        fontSize: 11,
      },
      splitLine: { lineStyle: { color: '#f1f5f9' } },
    },
    series: [
      {
        name: '高等级污染占比 (4~6级AQI)',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#ef4444' },
        lineStyle: { width: 2.2, color: '#ef4444' },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%',
          color: '#ef4444',
          fontSize: 10,
          fontWeight: 600,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(239, 68, 68, 0.22)' },
            { offset: 1, color: 'rgba(239, 68, 68, 0.01)' },
          ]),
        },
        data: rates,
      },
    ],
  }

  pollutionChartInstance.value.setOption(option)
}

function handleResize() {
  scatterChartInstance.value?.resize()
  efficiencyChartInstance.value?.resize()
  pollutionChartInstance.value?.resize()
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

async function loadStats() {
  loading.value = true
  try {
    const res = await getAnalyticsStats({
      provinceId: searchProvince.value ? Number(searchProvince.value) : undefined,
      cityId: searchCity.value ? Number(searchCity.value) : undefined,
      submittedFrom: searchDateRange.value?.[0],
      submittedTo: searchDateRange.value?.[1],
    })
    stats.value = res.data.data

    await nextTick()
    initEfficiencyChart()
    initScatterChart()
    initPollutionChart()
  } catch {
    ElMessage.error('查询综合统计数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  void loadStats()
}

function handleReset() {
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  searchDateRange.value = ['2026-04-01', '2026-09-13']
  void loadStats()
}

onMounted(() => {
  void loadProvinces()
  void loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  scatterChartInstance.value?.dispose()
  efficiencyChartInstance.value?.dispose()
  pollutionChartInstance.value?.dispose()
})

watch(
  [regionRiskList, efficiencyTrendList, pollutionTrendList],
  () => {
    initEfficiencyChart()
    initScatterChart()
    initPollutionChart()
  },
  { deep: true },
)
</script>

<template>
  <div class="view-analytics">
    <!-- 1. 复合筛选卡片 (统一卡片体系规范) -->
    <div class="common-card filter-card">
      <div class="filter-controls">
        <div class="filter-item">
          <label>所属区域</label>
          <div class="region-selects">
            <el-select
              v-model="searchProvince"
              placeholder="全部省份"
              style="width: 140px"
              clearable
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
              style="width: 140px"
              clearable
              :disabled="!searchProvince"
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
          <label>日期范围</label>
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </div>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" class="btn-search" @click="handleSearch">查询统计</el-button>
          <el-button :icon="Refresh" class="btn-reset" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 2. 第一层：核心决策结论卡片 -->
    <div class="common-card conclusion-card">
      <div class="conclusion-header">
        <div class="conclusion-badge">
          <el-icon :size="16"><TrendCharts /></el-icon>
          <span>本期核心结论</span>
        </div>
        <span class="conclusion-sub-hint">基于本周期反馈规模、闭环时效与污染检测严重度综合评估</span>
      </div>
      <div class="conclusion-body">
        <p class="conclusion-text">
          本期整体处理效率保持稳定（完成率达 <b class="text-emerald-600">{{ dynamicConclusion.compRate }}%</b>），其中 <b class="text-rose-600">{{ dynamicConclusion.pName }}</b> 高等级污染占比居前（<b class="text-rose-600">{{ dynamicConclusion.pRate }}%</b>），<b class="text-amber-600">{{ dynamicConclusion.fName }}</b> 反馈量达 <b class="text-amber-600">{{ dynamicConclusion.fCount }} 条</b>，建议重点加强相关区域的常态化溯源与网格化日常巡查。
        </p>
      </div>
    </div>

    <!-- 3. 第一层附属：三大核心指标卡片 -->
    <div class="metrics-row">
      <!-- 指标 1：反馈完成率 -->
      <div class="common-card metric-card">
        <div class="metric-card-header">
          <div class="header-badge header-badge--green">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <span class="metric-card-title">反馈完成率</span>
          <el-icon class="card-extra-icon text-emerald-200"><Tickets /></el-icon>
        </div>

        <div class="metric-card-content">
          <div class="metric-num">{{ completionRateDisplay }}%</div>
          <div class="metric-sub-trend trend--green">
            <span>较上期 +{{ (stats.completionRateChange ?? 2.8).toFixed(1) }} 个百分点</span>
            <el-icon><Top /></el-icon>
          </div>
        </div>

        <div class="sparkline-wrap sparkline--green">
          <svg viewBox="0 0 100 36" preserveAspectRatio="none">
            <defs>
              <linearGradient id="gradEmerald" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#10b981" stop-opacity="0.28" />
                <stop offset="100%" stop-color="#10b981" stop-opacity="0.0" />
              </linearGradient>
            </defs>
            <path d="M0 26 Q 30 20, 60 12 T 100 6 L 100 36 L 0 36 Z" fill="url(#gradEmerald)" />
            <path d="M0 26 Q 30 20, 60 12 T 100 6" fill="none" stroke="#10b981" stroke-width="2.2" stroke-linecap="round" />
          </svg>
        </div>
      </div>

      <!-- 指标 2：任务超时率 -->
      <div class="common-card metric-card">
        <div class="metric-card-header">
          <div class="header-badge header-badge--orange">
            <el-icon><Timer /></el-icon>
          </div>
          <span class="metric-card-title">任务超时率</span>
          <el-icon class="card-extra-icon text-orange-200"><Histogram /></el-icon>
        </div>

        <div class="metric-card-content">
          <div class="metric-num">{{ timeoutRateDisplay }}%</div>
          <div class="metric-sub-trend trend--green">
            <span>较上期 {{ (stats.timeoutRateChange ?? -1.5).toFixed(1) }} 个百分点</span>
            <el-icon><Bottom /></el-icon>
          </div>
        </div>

        <div class="sparkline-wrap sparkline--orange">
          <svg viewBox="0 0 100 36" preserveAspectRatio="none">
            <defs>
              <linearGradient id="gradOrange" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#f97316" stop-opacity="0.22" />
                <stop offset="100%" stop-color="#f97316" stop-opacity="0.0" />
              </linearGradient>
            </defs>
            <path d="M0 8 Q 35 14, 65 22 T 100 24 L 100 36 L 0 36 Z" fill="url(#gradOrange)" />
            <path d="M0 8 Q 35 14, 65 22 T 100 24" fill="none" stroke="#f97316" stroke-width="2.2" stroke-linecap="round" />
          </svg>
        </div>
      </div>

      <!-- 指标 3：高等级污染占比 -->
      <div class="common-card metric-card">
        <div class="metric-card-header">
          <div class="header-badge header-badge--red">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <span class="metric-card-title">高等级污染占比</span>
          <el-icon class="card-extra-icon text-rose-200"><Histogram /></el-icon>
        </div>

        <div class="metric-card-content">
          <div class="metric-num">{{ highPollutionRateDisplay }}%</div>
          <div class="metric-sub-trend trend--red">
            <span>较上期 +{{ (stats.highPollutionRateChange ?? 3.2).toFixed(1) }} 个百分点</span>
            <el-icon><Top /></el-icon>
          </div>
        </div>

        <div class="sparkline-wrap sparkline--red">
          <svg viewBox="0 0 100 36" preserveAspectRatio="none">
            <defs>
              <linearGradient id="gradRed" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#ef4444" stop-opacity="0.25" />
                <stop offset="100%" stop-color="#ef4444" stop-opacity="0.0" />
              </linearGradient>
            </defs>
            <path d="M0 24 Q 25 18, 60 12 T 100 6 L 100 36 L 0 36 Z" fill="url(#gradRed)" />
            <path d="M0 24 Q 25 18, 60 12 T 100 6" fill="none" stroke="#ef4444" stroke-width="2.2" stroke-linecap="round" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 4. 第二层：核心业务问题（并排两栏，回答：哪个区域最关注？效率是否变好？） -->
    <div class="two-col-grid">
      <!-- 左栏：区域重点关注排行 -->
      <div class="common-card core-analysis-card">
        <div class="section-header">
          <div class="header-title-group">
            <span class="header-blue-bar"></span>
            <h3 class="section-title">区域重点关注排行</h3>
          </div>
          <span class="section-subtitle">解决“哪个区域最值得关注”：综合反馈体量与高污染严重度排序</span>
        </div>

        <div class="region-rank-container">
          <div
            v-for="item in rankedRegionList"
            :key="item.regionName"
            class="rank-row"
          >
            <!-- 排名徽章 -->
            <div
              class="rank-badge"
              :class="{
                'rank-badge--1': item.rank === 1,
                'rank-badge--2': item.rank === 2,
                'rank-badge--3': item.rank === 3,
                'rank-badge--normal': item.rank > 3,
              }"
            >
              {{ item.rank }}
            </div>

            <!-- 区域名称 -->
            <div class="rank-region-name">
              {{ item.regionName }}
            </div>

            <!-- 反馈量进度条与条数 -->
            <div class="rank-bar-wrap">
              <div class="rank-bar-bg">
                <div
                  class="rank-bar-fill"
                  :style="{
                    width: `${item.percentage}%`,
                    backgroundColor: item.rank === 1 ? '#ef4444' : item.rank === 2 ? '#f97316' : '#38bdf8'
                  }"
                ></div>
              </div>
              <span class="rank-count-text">{{ item.feedbackCount }} 条</span>
            </div>

            <!-- 高污染占比数值 -->
            <div class="rank-pollution-tag">
              <span class="text-xs text-slate-400 mr-1">高污染:</span>
              <span class="font-bold" :class="item.highPollutionRate >= 25 ? 'text-rose-600' : 'text-slate-700'">
                {{ item.highPollutionRate }}%
              </span>
            </div>

            <!-- 风险评级标签 -->
            <div class="rank-risk-col">
              <span
                class="capsule-tag"
                :class="{
                  'capsule-tag--high': item.riskLevel === '极高风险' || item.riskLevel === '高风险',
                  'capsule-tag--mid': item.riskLevel === '中风险',
                  'capsule-tag--normal': item.riskLevel === '正常关注',
                }"
              >
                {{ item.riskLevel }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右栏：处理效率趋势 -->
      <div class="common-card core-analysis-card">
        <div class="section-header">
          <div class="header-title-group">
            <span class="header-blue-bar"></span>
            <h3 class="section-title">处理效率趋势</h3>
          </div>
          <span class="section-subtitle">解决“系统处理效率有没有变好”：月度反馈总量与按期办结率</span>
        </div>

        <div ref="efficiencyChartRef" class="core-chart-box"></div>
      </div>
    </div>

    <!-- 5. 深度归因与演进趋势 (统一标准标题分组，不打断节奏) -->
    <div class="group-section-header">
      <div class="header-title-group">
        <span class="header-blue-bar"></span>
        <h3 class="section-title">深度归因与演进趋势</h3>
      </div>
      <span class="section-subtitle">结合四象限空间定位与时间推演，深入排查潜在环境风险成因</span>
    </div>

    <!-- 6. 第三层：深度归因分析（并排两栏：四象限散点图 + 高等级污染趋势） -->
    <div class="two-col-grid">
      <!-- 左栏：区域风险深度分析 (四象限散点图降维下移) -->
      <div class="common-card deep-card">
        <div class="section-header">
          <div class="header-title-group">
            <span class="header-blue-bar"></span>
            <h3 class="section-title">区域风险深度分析</h3>
          </div>
          <span class="section-subtitle">以反馈量与高污染占比划分四象限，定位风险成因</span>
        </div>

        <div ref="scatterChartRef" class="deep-chart-box"></div>
      </div>

      <!-- 右栏：污染风险趋势 (面积折线图) -->
      <div class="common-card deep-card">
        <div class="section-header">
          <div class="header-title-group">
            <span class="header-blue-bar"></span>
            <h3 class="section-title">高等级污染趋势</h3>
          </div>
          <span class="section-subtitle">重度及以上污染（4~6级 AQI）月度占比演化走向</span>
        </div>

        <div ref="pollutionChartRef" class="deep-chart-box"></div>
      </div>
    </div>

    <!-- 7. 底部：结构化统计口径说明 -->
    <div class="common-card audit-standard-card">
      <div class="audit-card-badge">
        <el-icon><InfoFilled /></el-icon>
        <span>统计口径说明</span>
      </div>
      <div class="audit-card-body">
        <div class="audit-meta-item">
          <span class="audit-meta-lbl">数据范围</span>
          <span class="audit-meta-val">已完结反馈 / 任务流转 / AQI 检测结果 / 预警记录</span>
        </div>
        <div class="audit-meta-item">
          <span class="audit-meta-lbl">用途说明</span>
          <span class="audit-meta-val">仅供管理决策参考（不同时间段的数据可能因数据补录等因素存在小幅波动）</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-analytics {
  padding: 0 0 32px 0;
  background-color: transparent;
}

/* 统一卡片系统规范 (12px 圆角, 1px #E6ECF5 边框) */
.common-card {
  background: #ffffff;
  border: 1px solid #e6ecf5;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.03);
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.common-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.05);
}

/* 模块标题统一规范 */
.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-blue-bar {
  width: 3px;
  height: 15px;
  background-color: #0284c7;
  border-radius: 2px;
  flex-shrink: 0;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
  line-height: 1.2;
}

.section-subtitle {
  font-size: 12px;
  color: #7a8ca5;
}

/* 1. 筛选卡片 */
.filter-card {
  padding: 16px 20px;
}

.filter-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 24px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  white-space: nowrap;
}

.region-selects {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.btn-search {
  background: #0284c7;
  border-color: #0284c7;
  padding: 8px 18px;
  border-radius: 6px;
  font-weight: 500;
}

.btn-search:hover {
  background: #0369a1;
  border-color: #0369a1;
}

.btn-reset {
  border-color: #cbd5e1;
  color: #475569;
  padding: 8px 18px;
  border-radius: 6px;
}

/* 2. 第一层：核心决策结论卡片 (留白增强) */
.conclusion-card {
  margin-top: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f0f9ff 100%);
  border-color: #dbeafe;
}

.conclusion-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.conclusion-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #0284c7;
  color: #ffffff;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.conclusion-sub-hint {
  font-size: 12px;
  color: #64748b;
}

.conclusion-text {
  font-size: 14px;
  line-height: 1.65;
  color: #1e293b;
  margin: 0;
}

/* 3. 三大核心指标卡片 (留白增强) */
.metrics-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.metric-card {
  padding: 20px 22px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 126px;
}

.metric-card-header {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
}

.header-badge {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  font-size: 15px;
}

.header-badge--green {
  background: #ecfdf5;
  color: #10b981;
}

.header-badge--orange {
  background: #fff7ed;
  color: #f97316;
}

.header-badge--red {
  background: #fef2f2;
  color: #ef4444;
}

.metric-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.card-extra-icon {
  margin-left: auto;
  font-size: 18px;
  opacity: 0.35;
}

.metric-card-content {
  margin-top: 4px;
  position: relative;
  z-index: 2;
}

.metric-num {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
  letter-spacing: -0.5px;
}

.metric-sub-trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  margin-top: 4px;
  font-weight: 500;
}

.trend--green {
  color: #10b981;
}

.trend--red {
  color: #ef4444;
}

.sparkline-wrap {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 130px;
  height: 48px;
  z-index: 1;
  pointer-events: none;
}

.sparkline-wrap svg {
  width: 100%;
  height: 100%;
}

/* 网格布局常量 (留白增强) */
.two-col-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 22px;
}

/* 4. 第二层：核心业务问题 */
.core-analysis-card {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
}

.region-rank-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 4px;
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fafc;
  transition: background 0.15s ease;
}

.rank-row:hover {
  background: #f1f5f9;
}

.rank-badge {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.rank-badge--1 {
  background: #fee2e2;
  color: #ef4444;
}

.rank-badge--2 {
  background: #ffedd5;
  color: #f97316;
}

.rank-badge--3 {
  background: #e0f2fe;
  color: #0284c7;
}

.rank-badge--normal {
  background: #f1f5f9;
  color: #64748b;
}

.rank-region-name {
  width: 70px;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  flex-shrink: 0;
}

.rank-bar-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-bar-bg {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.rank-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.rank-count-text {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
  min-width: 48px;
  text-align: right;
}

.rank-pollution-tag {
  font-size: 12px;
  min-width: 90px;
  text-align: right;
}

.rank-risk-col {
  width: 75px;
  text-align: right;
  flex-shrink: 0;
}

.capsule-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.capsule-tag--high {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fecaca;
}

.capsule-tag--mid {
  background: #fff7ed;
  color: #f97316;
  border: 1px solid #fed7aa;
}

.capsule-tag--normal {
  background: #f0fdf4;
  color: #10b981;
  border: 1px solid #bbf7d0;
}

.core-chart-box {
  width: 100%;
  height: 245px;
}

/* 5. 深度归因小标题分组 (自然过度，不打断节奏) */
.group-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 32px 0 16px;
}

/* 6. 第三层：深度归因分析 */
.deep-card {
  padding: 22px 24px;
}

.deep-chart-box {
  width: 100%;
  height: 300px;
}

/* 7. 底部结构化统计口径说明卡片 */
.audit-standard-card {
  padding: 16px 22px;
  display: flex;
  align-items: center;
  gap: 24px;
  background: #ffffff;
  margin-top: 24px;
}

.audit-card-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #0284c7;
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  padding: 6px 14px;
  border-radius: 6px;
  white-space: nowrap;
  flex-shrink: 0;
}

.audit-card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.audit-meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  line-height: 1.5;
}

.audit-meta-lbl {
  color: #475569;
  font-weight: 600;
  white-space: nowrap;
  min-width: 55px;
}

.audit-meta-val {
  color: #64748b;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .two-col-grid {
    grid-template-columns: 1fr;
  }
  .metrics-row {
    grid-template-columns: 1fr;
  }
}
</style>
