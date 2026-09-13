<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import {
  Clock,
  Grid,
  Histogram,
  WarningFilled,
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import NepmStatCard from '@/components/nepm/NepmStatCard.vue'
import {
  getAqiAlerts,
  getAnalyticsStats,
  getDashboard,
  getFeedbacks,
  getRecentTrend,
  getTimeoutAlerts,
  type AnalyticsStats,
  type AqiAlertItem,
  type AqiGrade,
  type DashboardSummary,
  type DispatchItem,
  type RecentTrendData,
} from '@/api/nepm'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'

const emit = defineEmits<{
  navigate: [target: string]
  openDispatch: [row: DispatchItem]
}>()

// 真实汇总指标
const summary = ref<DashboardSummary>({ pending: 0, assigned: 0, completed: 0, timeout: 0 })
// 真实近7日趋势
const recentTrendData = ref<RecentTrendData | null>(null)
// 真实待处理公众反馈
const recentFeedbacks = ref<
  (DispatchItem & { estimatedGrade: number; aqiLevelName: string; aqiClass: string })[]
>([])
// 真实超时任务
const timeoutList = ref<DispatchItem[]>([])
// 真实高等级预警
const highAqiAlerts = ref<AqiAlertItem[]>([])
// 真实检测统计与分布
const analyticsData = ref<AnalyticsStats | null>(null)
const loading = ref(false)

const trendChartRef = ref<HTMLDivElement | null>(null)
const donutChartRef = ref<HTMLDivElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null
let donutChartInstance: echarts.ECharts | null = null

function formatChange(val?: number) {
  if (val === undefined || val === null) return undefined
  if (val > 0) return `+${val}`
  return `${val}`
}

function getTrendDirection(val?: number): boolean | null {
  if (val === undefined || val === null || val === 0) return null
  return val > 0
}

// 4个核心指标卡配置（对齐参考图细节与环比变动）
const stats = computed(() => [
  {
    title: '待指派',
    value: summary.value.pending,
    tone: 'coral' as const,
    iconType: 'doc' as const,
    subtext: '较昨日',
    change: formatChange(summary.value.pendingChange),
    isIncrease: getTrendDirection(summary.value.pendingChange),
  },
  {
    title: '处理中',
    value: summary.value.assigned,
    tone: 'blue' as const,
    iconType: 'setting' as const,
    subtext: '较昨日',
    change: formatChange(summary.value.assignedChange),
    isIncrease: getTrendDirection(summary.value.assignedChange),
  },
  {
    title: '超时任务',
    value: summary.value.timeout,
    tone: 'red' as const,
    iconType: 'warn' as const,
    subtext: '较昨日',
    change: formatChange(summary.value.timeoutChange),
    isIncrease: getTrendDirection(summary.value.timeoutChange),
  },
  {
    title: '今日完成',
    value: summary.value.todayCompleted ?? summary.value.completed,
    tone: 'green' as const,
    iconType: 'check' as const,
    subtext: '较昨日',
    change: formatChange(summary.value.completedChange),
    isIncrease: getTrendDirection(summary.value.completedChange),
  },
])

interface GradeMeta {
  name: AqiGrade
  label: string
  class: string
  color: string
}

const defaultMeta: GradeMeta = {
  name: '优',
  label: '优 (一级)',
  class: 'aqi-grade-1',
  color: '#1b6a3e',
}

const gradeMetaMap: Record<number, GradeMeta> = {
  1: { name: '优', label: '优 (一级)', class: 'aqi-grade-1', color: '#1b6a3e' },
  2: { name: '良', label: '良 (二级)', class: 'aqi-grade-2', color: '#1b6a3e' },
  3: { name: '轻度污染', label: '轻度 (三级)', class: 'aqi-grade-3', color: '#7d5514' },
  4: { name: '中度污染', label: '中度 (四级)', class: 'aqi-grade-4', color: '#914216' },
  5: { name: '重度污染', label: '重度 (五级)', class: 'aqi-grade-5', color: '#962828' },
  6: { name: '严重污染', label: '严重 (六级)', class: 'aqi-grade-6', color: '#59258a' },
}

function getGradeMeta(grade?: number | null): GradeMeta {
  const g = grade ?? 1
  return gradeMetaMap[g] ?? defaultMeta
}

// 真实公众反馈转换
function toFeedbackItem(row: AqiFeedbackRow) {
  const meta = getGradeMeta(row.estimatedGrade)
  const isPending = row.state === 0 || row.state === undefined
  const statusStr = row.timeoutFlag ? '已超时' : isPending ? '待指派' : '处理中'

  return {
    id: row.afId,
    sn: `FB${String(row.afId).padStart(10, '0')}`,
    address: row.address || '-',
    submitTime: row.submittedAt?.replace('T', ' ').slice(0, 16) || '-',
    status: statusStr as '待指派' | '处理中' | '已超时',
    estimatedAqi: meta.name,
    estimatedGrade: row.estimatedGrade ?? 1,
    aqiLevelName: meta.name,
    aqiClass: meta.class,
    handler: row.gmName || '-',
  }
}

// 真实超时任务转换
function toTimeoutItem(row: AqiFeedbackRow): DispatchItem {
  const meta = getGradeMeta(row.estimatedGrade)
  return {
    id: row.afId,
    sn: `FB${String(row.afId).padStart(10, '0')}`,
    address: row.address || '-',
    submitTime: row.submittedAt?.replace('T', ' ').slice(0, 16) || '-',
    status: '已超时',
    estimatedAqi: meta.name,
    handler: row.gmName || '-',
  }
}

// 计算真实的 6 级分布列表
const formattedDistribution = computed(() => {
  if (!analyticsData.value) return []
  const total = analyticsData.value.totalDetections || 0
  const countMap = new Map<number, number>()
  for (const d of analyticsData.value.aqiDistribution || []) {
    countMap.set(d.aqiId, d.count)
  }

  return [1, 2, 3, 4, 5, 6].map((aqiId) => {
    const meta = getGradeMeta(aqiId)
    const count = countMap.get(aqiId) ?? 0
    const percent = total > 0 ? ((count / total) * 100).toFixed(1) + '%' : '0.0%'
    return {
      aqiId,
      name: meta.label,
      count,
      percent,
      color: meta.color,
    }
  })
})

// 初始化近7日反馈流入、办结与待办趋势图（双柱一折线）
function initTrendChart() {
  if (!trendChartRef.value) return
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
  trendChartInstance = echarts.init(trendChartRef.value)

  const dates = recentTrendData.value?.dates || []
  const newFeedbacks = recentTrendData.value?.newFeedbacks || []
  const completedFeedbacks = recentTrendData.value?.completedFeedbacks || []
  const pendingFeedbacks = recentTrendData.value?.pendingFeedbacks || []

  if (dates.length === 0) {
    return
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#334155', fontSize: 12 },
      formatter: (params: unknown) => {
        const list = (Array.isArray(params) ? params : [params]) as Array<{
          axisValue?: string
          seriesName?: string
          value?: number | string
          color?: string
        }>
        let tip = `<div style="font-weight:600;margin-bottom:6px;color:#1e293b;">${list[0]?.axisValue || ''}</div>`
        for (const item of list) {
          const isPending = item.seriesName === '日终待处理'
          const unit = '件'
          tip += `<div style="display:flex;align-items:center;justify-content:space-between;gap:12px;margin:3px 0;">
            <span style="display:flex;align-items:center;gap:6px;">
              <span style="display:inline-block;width:8px;height:8px;border-radius:${isPending ? '50%' : '2px'};background:${item.color};"></span>
              <span style="color:#64748b;">${item.seriesName}</span>
            </span>
            <span style="font-weight:600;color:#0f172a;">${item.value}${unit}</span>
          </div>`
        }
        return tip
      },
    },
    legend: {
      top: 4,
      right: 10,
      itemGap: 16,
      textStyle: { color: '#64748b', fontSize: 12 },
      data: ['新增反馈', '当日办结', '日终待处理'],
    },
    grid: {
      top: 42,
      left: 36,
      right: 42,
      bottom: 24,
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisTick: { show: false },
    },
    yAxis: [
      {
        type: 'value',
        minInterval: 1,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#94a3b8', fontSize: 11 },
        splitLine: { lineStyle: { color: '#f1f5f9' } },
      },
      {
        type: 'value',
        minInterval: 1,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#94a3b8', fontSize: 11 },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '新增反馈',
        type: 'bar',
        barWidth: 12,
        itemStyle: {
          color: '#1890ff',
          borderRadius: [2, 2, 0, 0],
        },
        label: {
          show: true,
          position: 'top',
          color: '#475569',
          fontSize: 10,
        },
        data: newFeedbacks,
      },
      {
        name: '当日办结',
        type: 'bar',
        barWidth: 12,
        itemStyle: {
          color: '#52c41a',
          borderRadius: [2, 2, 0, 0],
        },
        label: {
          show: true,
          position: 'top',
          color: '#475569',
          fontSize: 10,
        },
        data: completedFeedbacks,
      },
      {
        name: '日终待处理',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#0284c7',
          borderColor: '#ffffff',
          borderWidth: 1.5,
        },
        lineStyle: {
          type: 'dashed',
          width: 1.8,
          color: '#0284c7',
        },
        data: pendingFeedbacks,
      },
    ],
  }

  trendChartInstance.setOption(option)
}

// 初始化 AQI 真实环形图
function initDonutChart() {
  if (!donutChartRef.value) return
  if (donutChartInstance) {
    donutChartInstance.dispose()
  }
  donutChartInstance = echarts.init(donutChartRef.value)

  const total = analyticsData.value?.totalDetections || 0
  const distList = formattedDistribution.value

  const chartData = distList.map((d) => ({
    value: d.count,
    name: d.name,
    itemStyle: { color: d.color },
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 份 ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#334155', fontSize: 12 },
    },
    title: {
      text: String(total),
      subtext: '总检测数',
      left: 'center',
      top: '36%',
      textStyle: {
        fontSize: 22,
        fontWeight: 'bold',
        color: '#0f172a',
      },
      subtextStyle: {
        fontSize: 11,
        color: '#64748b',
      },
    },
    series: [
      {
        type: 'pie',
        radius: ['58%', '78%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false },
        labelLine: { show: false },
        data: chartData,
      },
    ],
  }

  donutChartInstance.setOption(option)
}

function handleResize() {
  trendChartInstance?.resize()
  donutChartInstance?.resize()
}

// 100% 从后端加载真实数据，绝不使用假数据覆盖
async function loadDashboardData() {
  loading.value = true
  try {
    const [summaryRes, feedbackRes, timeoutRes, alertRes, analyticsRes, trendRes] = await Promise.allSettled([
      getDashboard(),
      getFeedbacks({ page: 1, pageSize: 5 }),
      getTimeoutAlerts({ page: 1, pageSize: 3 }),
      getAqiAlerts({ status: 'PENDING', page: 1, pageSize: 3 }),
      getAnalyticsStats({}),
      getRecentTrend(),
    ])

    if (summaryRes.status === 'fulfilled') {
      summary.value = summaryRes.value.data.data
    }

    if (feedbackRes.status === 'fulfilled') {
      const items = feedbackRes.value.data.data.items || []
      recentFeedbacks.value = items.map(toFeedbackItem)
    }

    if (timeoutRes.status === 'fulfilled') {
      const items = timeoutRes.value.data.data.items || []
      timeoutList.value = items.map(toTimeoutItem)
    }

    if (alertRes.status === 'fulfilled') {
      highAqiAlerts.value = alertRes.value.data.data.items || []
    }

    if (analyticsRes.status === 'fulfilled') {
      analyticsData.value = analyticsRes.value.data.data
    }

    if (trendRes.status === 'fulfilled') {
      recentTrendData.value = trendRes.value.data.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadDashboardData()
  await nextTick()
  if (recentTrendData.value && recentTrendData.value.dates.length > 0) {
    initTrendChart()
  }
  if (analyticsData.value && analyticsData.value.totalDetections > 0) {
    initDonutChart()
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance?.dispose()
  donutChartInstance?.dispose()
})
</script>

<template>
  <div class="view-dashboard">
    <!-- 4 大真实核心指标卡片 -->
    <section class="stat-grid-4" aria-label="核心业务指标">
      <NepmStatCard v-for="stat in stats" :key="stat.title" v-bind="stat" />
    </section>

    <!-- 中层双栏：左侧待处理公众反馈 + 右侧异常关注 -->
    <section class="mid-section mt-4">
      <!-- 左栏：待处理公众反馈（真实数据与真实预估等级） -->
      <div class="panel-card table-panel">
        <header class="panel-header">
          <div class="header-left">
            <span class="custom-title-icon custom-title-icon--ticket">
              <svg viewBox="0 0 16 16" width="16" height="16" fill="none">
                <rect width="16" height="16" rx="3.5" fill="#1890ff"/>
                <line x1="3.5" y1="4.5" x2="12.5" y2="4.5" stroke="#ffffff" stroke-width="1.8" stroke-linecap="round"/>
                <line x1="3.5" y1="8" x2="12.5" y2="8" stroke="#ffffff" stroke-width="1.8" stroke-linecap="round"/>
                <line x1="3.5" y1="11.5" x2="9" y2="11.5" stroke="#ffffff" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </span>
            <h3 class="panel-title">待处理公众反馈</h3>
          </div>
          <a href="javascript:void(0)" class="panel-more" @click="emit('navigate', 'feedbacks')">
            查看更多 &gt;
          </a>
        </header>

        <el-table
          :data="recentFeedbacks"
          stripe
          empty-text="暂无待处理公众反馈"
          class="custom-feedback-table"
        >
          <el-table-column prop="sn" label="反馈编号" min-width="135" />
          <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
          <el-table-column prop="submitTime" label="提交时间" width="135" />

          <!-- 真实预估AQI国标等级标签展示 -->
          <el-table-column label="预估AQI" width="105" align="center">
            <template #default="{ row }">
              <span :class="['aqi-badge', row.aqiClass]">{{ row.aqiLevelName }}</span>
            </template>
          </el-table-column>

          <!-- 真实当前状态 -->
          <el-table-column label="当前状态" width="85" align="center">
            <template #default="{ row }">
              <span
                :class="[
                  'status-pill',
                  row.status === '待指派'
                    ? 'status-pill--pending'
                    : row.status === '已超时'
                    ? 'status-pill--timeout'
                    : 'status-pill--assigned',
                ]"
              >
                {{ row.status }}
              </span>
            </template>
          </el-table-column>

          <!-- 状态驱动操作列：消除状态冲突 -->
          <el-table-column label="操作" width="105" align="center">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="emit('navigate', 'feedbacks')"
                >
                  查看
                </el-button>
                <!-- 待指派状态才显示指派，处理中只显示查看 -->
                <template v-if="row.status === '待指派'">
                  <span class="action-divider">|</span>
                  <el-button
                    link
                    type="primary"
                    size="small"
                    @click="emit('openDispatch', row)"
                  >
                    指派
                  </el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 右栏：异常关注（真实超时单与真实高等级预警） -->
      <div class="panel-card alert-panel">
        <header class="panel-header">
          <div class="header-left">
            <el-icon class="alert-icon"><WarningFilled /></el-icon>
            <h3 class="panel-title">异常关注</h3>
          </div>
          <a href="javascript:void(0)" class="panel-more" @click="emit('navigate', 'timeout')">
            查看更多 &gt;
          </a>
        </header>

        <div class="alert-content-flow">
          <!-- 1. 真实超时任务 -->
          <div class="alert-group">
            <div class="alert-group-title">
              <el-icon class="group-icon group-icon--clock"><Clock /></el-icon>
              <span class="group-name">超时任务</span>
              <span class="group-count">({{ timeoutList.length }})</span>
            </div>

            <div v-if="timeoutList.length > 0" class="alert-list">
              <div v-for="t in timeoutList" :key="t.id" class="alert-item-card">
                <div class="item-top">
                  <span class="item-sn">{{ t.sn }}</span>
                  <span class="badge-timeout">已超时</span>
                  <span class="item-time">{{ t.submitTime }}</span>
                  <el-button
                    link
                    type="primary"
                    size="small"
                    class="btn-item-view"
                    @click="emit('navigate', 'timeout')"
                  >
                    查看
                  </el-button>
                </div>
                <div class="item-bottom">
                  <span class="item-address">{{ t.address }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-hint">当前暂无超时任务</div>
          </div>

          <!-- 2. 真实高等级AQI预警（真实 aqiId 国标映射） -->
          <div class="alert-group mt-3">
            <div class="alert-group-title">
              <el-icon class="group-icon group-icon--wave"><Histogram /></el-icon>
              <span class="group-name">高等级AQI预警</span>
              <span class="group-count">({{ highAqiAlerts.length }})</span>
            </div>

            <div v-if="highAqiAlerts.length > 0" class="alert-list">
              <div v-for="alert in highAqiAlerts" :key="alert.id" class="alert-item-card">
                <div class="item-top">
                  <span class="item-point-name">{{ alert.address }}</span>
                  <span
                    :class="[
                      'badge-aqi-alert',
                      `aqi-grade-${alert.aqiId}`,
                    ]"
                  >
                    {{ getGradeMeta(alert.aqiId).label }}
                  </span>
                  <span class="item-time">{{ alert.createdAt?.replace('T', ' ').slice(0, 16) }}</span>
                  <el-button
                    link
                    type="primary"
                    size="small"
                    class="btn-item-view"
                    @click="emit('navigate', 'aqiAlert')"
                  >
                    查看
                  </el-button>
                </div>
                <div class="item-bottom">
                  <span class="item-desc">
                    污染物实测已达到{{ getGradeMeta(alert.aqiId).name }}水平，落实闭环处置。
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="empty-hint">当前暂无高等级未处置预警</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底层数据可视化：真实趋势与 AQI 等级分布 -->
    <section class="bottom-section mt-4">
      <!-- 近7日反馈与办结趋势图 -->
      <div class="panel-card chart-card">
        <header class="panel-header">
          <div class="header-left">
            <span class="custom-title-icon custom-title-icon--histogram">
              <svg viewBox="0 0 18 18" width="18" height="18" fill="none">
                <rect x="1.5" y="8" width="3.5" height="9" rx="1" fill="#1890ff"/>
                <rect x="7.25" y="2.5" width="3.5" height="14.5" rx="1" fill="#1890ff"/>
                <rect x="13" y="5.5" width="3.5" height="11.5" rx="1" fill="#1890ff"/>
              </svg>
            </span>
            <h3 class="panel-title">近7日反馈流入、办结与待办趋势</h3>
          </div>
          <a href="javascript:void(0)" class="panel-more" @click="emit('navigate', 'analytics')">
            查看详情 &gt;
          </a>
        </header>

        <div v-if="recentTrendData?.dates && recentTrendData.dates.length > 0" ref="trendChartRef" class="echarts-box"></div>
        <div v-else class="chart-empty-box">
          <el-empty description="系统暂无近7日反馈流入与待办趋势数据" :image-size="70" />
        </div>
      </div>

      <!-- 真实 AQI 等级分布环形图 -->
      <div class="panel-card chart-card">
        <header class="panel-header">
          <div class="header-left">
            <el-icon class="chart-icon"><Grid /></el-icon>
            <h3 class="panel-title">AQI空气质量等级分布</h3>
          </div>
          <span class="time-filter-tag">真实汇总数据</span>
        </header>

        <div v-if="analyticsData && analyticsData.totalDetections > 0" class="donut-chart-wrapper">
          <div ref="donutChartRef" class="donut-box"></div>
          <div class="aqi-legend-list">
            <div v-for="item in formattedDistribution" :key="item.aqiId" class="legend-row">
              <div class="legend-left">
                <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
                <span class="legend-name">{{ item.name }}</span>
              </div>
              <span class="legend-count">{{ item.count }}</span>
              <span class="legend-percent">{{ item.percent }}</span>
            </div>
          </div>
        </div>
        <div v-else class="chart-empty-box">
          <el-empty description="暂无已归档污染物实测检测数据" :image-size="70" />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.view-dashboard {
  width: 100%;
}

.stat-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.mid-section,
.bottom-section {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
}

.bottom-section {
  grid-template-columns: 1.1fr 1fr;
}

.mt-4 {
  margin-top: 16px;
}
.mt-3 {
  margin-top: 12px;
}

/* 卡片通用容器 */
.panel-card {
  padding: 18px 20px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.custom-title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.alert-icon {
  font-size: 16px;
  color: #ef4444;
}

.chart-icon {
  font-size: 17px;
  color: #1890ff;
}

.panel-title {
  margin: 0;
  color: #1e293b;
  font-size: 15px;
  font-weight: 600;
}

.panel-more {
  color: #1890ff;
  font-size: 12px;
  text-decoration: none;
  transition: opacity 0.2s;
}

.panel-more:hover {
  opacity: 0.8;
}

.time-filter-tag {
  font-size: 12px;
  color: #64748b;
}

/* 待处理表格细节 */
.custom-feedback-table {
  font-size: 13px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f8fafc;
  color: #64748b;
  font-weight: 500;
  height: 40px;
  padding: 4px 0;
}

:deep(.el-table td.el-table__cell) {
  padding: 8px 0;
}

/* 预估AQI胶囊标签 (HJ 633 色系) */
.aqi-badge {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}



/* 状态标签 */
.status-pill {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.status-pill--pending {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}
.status-pill--assigned {
  background: #e6f4ff;
  color: #1677ff;
  border: 1px solid #91caff;
}
.status-pill--timeout {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.table-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-divider {
  font-size: 11px;
  color: #cbd5e1;
}

/* 异常关注卡片流 */
.alert-content-flow {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alert-group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.group-icon {
  font-size: 15px;
}
.group-icon--clock {
  color: #ef4444;
}
.group-icon--wave {
  color: #ea580c;
}

.group-count {
  color: #ef4444;
  font-size: 13px;
  font-weight: 700;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alert-item-card {
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 10px 14px;
  transition: all 0.2s;
}

.alert-item-card:hover {
  background: #f1f5f9;
}

.item-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-sn {
  font-size: 12.5px;
  font-weight: 600;
  color: #334155;
}

.item-point-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.badge-timeout {
  font-size: 11px;
  background: #fee2e2;
  color: #dc2626;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.badge-aqi-alert {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.item-time {
  font-size: 11.5px;
  color: #94a3b8;
  margin-left: auto;
}

.btn-item-view {
  font-size: 12.5px;
  padding: 0;
  margin-left: 8px;
}

.item-bottom {
  margin-top: 4px;
}

.item-address,
.item-desc {
  font-size: 12px;
  color: #64748b;
}

.empty-hint {
  padding: 12px;
  text-align: center;
  color: #94a3b8;
  font-size: 12.5px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px dashed #e2e8f0;
}

/* 图表容器 */
.echarts-box {
  width: 100%;
  height: 220px;
}

.chart-empty-box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 220px;
}

.donut-chart-wrapper {
  display: flex;
  align-items: center;
  height: 220px;
}

.donut-box {
  width: 50%;
  height: 100%;
}

.aqi-legend-list {
  width: 50%;
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding-left: 12px;
}

.legend-row {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.legend-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.legend-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  color: #475569;
}

.legend-count {
  font-weight: 600;
  color: #1e293b;
  width: 32px;
  text-align: right;
  margin-right: 12px;
}

.legend-percent {
  color: #94a3b8;
  width: 42px;
  text-align: right;
}

/* 响应式断点适配 */
@media (max-width: 1280px) {
  .stat-grid-4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .mid-section,
  .bottom-section {
    grid-template-columns: 1fr;
  }
}
</style>
