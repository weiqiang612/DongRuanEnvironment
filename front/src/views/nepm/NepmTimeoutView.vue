<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Filter, Refresh, Search } from '@element-plus/icons-vue'
import NepmAlertTips from '@/components/nepm/NepmAlertTips.vue'
import NepmStatCard from '@/components/nepm/NepmStatCard.vue'
import { getDashboard, getTimeoutAlerts, type DashboardSummary } from '@/api/nepm'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'

const emit = defineEmits<{
  openDetail: [row: TimeoutAlertItem]
  openDispatch: [row: { id: number; sn: string; address: string; handler: string }, isRedispatch?: boolean]
}>()

const searchDateRange = ref<[string, string]>()
const searchKeyword = ref('')
const searchAlertType = ref('')
const searchHandleStatus = ref('')
const searchProvince = ref('')
const searchCity = ref('')
const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])
const page = ref(1)

interface TimeoutAlertItem {
  id: number
  alertSn: string
  feedbackSn: string
  address: string
  alertType: '反馈超时' | '处理超时'
  triggerTime: string
  statusText: string
  handler: string
  handleStatus: '未处理' | '处理中' | '已处理'
}

const list = ref<TimeoutAlertItem[]>([])
const total = ref(0)
const pageSize = ref(10)
const summary = ref<DashboardSummary>({ pending: 0, assigned: 0, completed: 0, timeout: 0 })

const timeoutStats = computed(() => [
  { title: '当前超时任务', value: summary.value.timeout, tone: 'red-light' as const },
  { title: '待指派任务', value: summary.value.pending, tone: 'amber' as const },
  { title: '处理中任务', value: summary.value.assigned, tone: 'blue' as const },
  { title: '已完成任务', value: summary.value.completed, tone: 'green' as const },
])

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const selectedStates = computed<number[] | undefined>(() => {
  const alertStates = searchAlertType.value === '反馈超时'
    ? [0]
    : searchAlertType.value === '处理超时'
      ? [1, 2]
      : undefined
  const handleStates = searchHandleStatus.value === '未处理'
    ? [0]
    : searchHandleStatus.value === '处理中'
      ? [1]
      : searchHandleStatus.value === '已处理'
        ? [2]
        : undefined

  if (!alertStates) return handleStates
  if (!handleStates) return alertStates
  return alertStates.filter((state) => handleStates.includes(state))
})

function toItem(row: AqiFeedbackRow): TimeoutAlertItem {
  const type = row.state === 0 ? '反馈超时' : '处理超时'
  return {
    id: row.afId,
    alertSn: `YJ${String(row.afId).padStart(11, '0')}`,
    feedbackSn: `FB${String(row.afId).padStart(12, '0')}`,
    address: row.address,
    alertType: type,
    triggerTime: (row.timeoutAt ?? row.submittedAt ?? '').replace('T', ' ').slice(0, 16),
    statusText: '已超时',
    handler: row.gmName ?? '-',
    handleStatus: row.state === 2 ? '已处理' : row.state === 1 ? '处理中' : '未处理',
  }
}

async function loadList() {
  if (selectedStates.value?.length === 0) {
    list.value = []
    total.value = 0
    return
  }
  const response = await getTimeoutAlerts({
    provinceId: searchProvince.value ? Number(searchProvince.value) : undefined,
    cityId: searchCity.value ? Number(searchCity.value) : undefined,
    states: selectedStates.value,
    submittedFrom: searchDateRange.value?.[0],
    submittedTo: searchDateRange.value?.[1],
    keyword: searchKeyword.value || undefined,
    page: page.value,
    pageSize: pageSize.value,
  })
  list.value = response.data.data.items.map(toItem)
  total.value = response.data.data.total
}

async function loadProvinces() {
  const response = await getProvinceOptions()
  provinces.value = response.data.data
}

async function onProvinceChange() {
  searchCity.value = ''
  cities.value = []
  if (!searchProvince.value) return
  const response = await getCityOptions(Number(searchProvince.value))
  cities.value = response.data.data
}

async function loadSummary() {
  const response = await getDashboard()
  summary.value = response.data.data
}

async function handleSearch() {
  try {
    page.value = 1
    await loadList()
  } catch {
    // 查询失败时保留当前结果，避免误显示原型模拟数据。
  }
}

function handleReset() {
  searchDateRange.value = undefined
  searchKeyword.value = ''
  searchAlertType.value = ''
  searchHandleStatus.value = ''
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  page.value = 1
  void loadList()
}

onMounted(() => {
  void loadList()
  void loadSummary()
  void loadProvinces()
})
</script>

<template>
  <div class="view-timeout">
    <!-- 4 预警统计卡片 -->
    <div class="stat-grid-4">
      <NepmStatCard
        v-for="stat in timeoutStats"
        :key="stat.title"
        v-bind="stat"
      />
    </div>

    <!-- 筛选条件卡片 -->
    <div class="panel-card mt-4">
      <div class="card-title-row mb-3">
        <el-icon class="filter-icon"><Filter /></el-icon>
        <h3 class="filter-title">筛选条件</h3>
      </div>
      <div class="filter-row">
        <div class="filter-item">
          <label>预警类型</label>
          <el-select v-model="searchAlertType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="反馈超时" value="反馈超时" />
            <el-option label="处理超时" value="处理超时" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>处理状态</label>
          <el-select v-model="searchHandleStatus" placeholder="请选择" clearable style="width: 140px">
            <el-option label="未处理" value="未处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已处理" value="已处理" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>所属省份</label>
          <el-select v-model="searchProvince" placeholder="全部省份" clearable style="width: 140px" @change="onProvinceChange">
            <el-option v-for="item in provinces" :key="item.id" :label="item.name" :value="String(item.id)" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>所属城市</label>
          <el-select v-model="searchCity" placeholder="全部城市" clearable :disabled="!searchProvince" style="width: 140px">
            <el-option v-for="item in cities" :key="item.id" :label="item.name" :value="String(item.id)" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>预警时间</label>
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
            value-format="YYYY-MM-DD"
          />
        </div>
      </div>

      <div class="filter-row mt-3">
        <div class="filter-item flex-1">
          <label>关键词</label>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入预警编号、反馈编号或地址关键词"
            clearable
          />
        </div>
        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 超时预警列表 + 处理建议双栏 -->
    <div class="dispatch-two-column mt-4">
      <div class="panel-card">
        <div class="panel-header">
          <div class="header-left">
            <span class="blue-block"></span>
            <h3 class="panel-title">超时预警列表</h3>
          </div>
          <div class="table-pager-indicator">
            <span>共 {{ total }} 条数据</span>
            <span class="indicator-divider">|</span>
            <span>{{ page }}/{{ pageCount }} 页</span>
          </div>
        </div>

        <el-table :data="list" stripe class="compact-table">
          <el-table-column prop="id" label="序号" width="55" align="center" />
          <el-table-column prop="alertSn" label="预警编号" width="130">
            <template #default="{ row }">
              <span class="font-mono text-bold">{{ row.alertSn }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="feedbackSn" label="反馈编号" width="130">
            <template #default="{ row }">
              <span class="font-mono">{{ row.feedbackSn }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="190" show-overflow-tooltip />
          <el-table-column label="预警类型" width="95" align="center">
            <template #default="{ row }">
              <span :class="['type-pill', row.alertType === '反馈超时' ? 'type-pill--feedback' : 'type-pill--handle']">
                {{ row.alertType }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerTime" label="触发时间" width="145" />
          <el-table-column label="当前状态" width="95" align="center">
            <template #default="{ row }">
              <span class="timeout-pill">{{ row.statusText }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="handler" label="责任人" width="80" align="center" />
          <el-table-column label="处理状态" width="85" align="center">
            <template #default="{ row }">
              <span :class="['handle-pill', `handle-pill--${row.handleStatus}`]">{{ row.handleStatus }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="125" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
              <span class="op-split">|</span>
              <el-button link type="primary" size="small" @click="emit('openDispatch', { id: row.id, sn: row.feedbackSn, address: row.address, handler: row.handler }, true)">
                处理
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <span class="page-total">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            @current-change="loadList"
            @size-change="loadList"
            layout="prev, pager, next"
            :total="total"
          />
        </div>
      </div>

      <!-- 右栏: 处理建议 -->
      <NepmAlertTips />
    </div>
  </div>
</template>

<style scoped>
.stat-grid-4 { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.panel-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
.card-title-row { display: flex; align-items: center; gap: 8px; }
.filter-icon { color: #1677ff; font-size: 16px; }
.filter-title { margin: 0; font-size: 15px; font-weight: 600; color: #1f2937; }
.mb-3 { margin-bottom: 12px; }
.filter-row { display: flex; align-items: center; flex-wrap: wrap; gap: 16px; }
.mt-3 { margin-top: 12px; }
.mt-4 { margin-top: 16px; }
.filter-item { display: flex; align-items: center; gap: 8px; font-size: 13.5px; }
.filter-item label { color: #4b5563; white-space: nowrap; }
.filter-actions { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.flex-1 { flex: 1; }
.dispatch-two-column { display: grid; grid-template-columns: minmax(0, 1fr) 310px; gap: 16px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.blue-block { width: 12px; height: 12px; background-color: #1677ff; border-radius: 2px; }
.panel-title { margin: 0; font-size: 16px; font-weight: 600; color: #1f2937; }
.table-pager-indicator { font-size: 12px; color: #94a3b8; display: flex; align-items: center; gap: 6px; }
.indicator-divider { color: #e2e8f0; }
.font-mono { font-family: monospace; }
.text-bold { font-weight: 600; }
.type-pill { font-size: 11.5px; padding: 1px 6px; border-radius: 4px; }
.type-pill--feedback { background: #fff1f0; color: #ef4444; }
.type-pill--handle { background: #fff7ed; color: #f97316; }
.timeout-pill { background: #fef2f2; color: #ef4444; font-size: 11.5px; padding: 1px 6px; border-radius: 4px; }
.handle-pill--未处理 { color: #ef4444; }
.handle-pill--处理中 { color: #1677ff; }
.handle-pill--已处理 { color: #10b981; }
.op-split { margin: 0 6px; color: #cbd5e1; }
.pagination-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; padding-top: 12px; border-top: 1px solid #f1f5f9; }
.page-total { font-size: 13px; color: #64748b; }
@media (max-width: 1400px) { .stat-grid-4 { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 1200px) { .dispatch-two-column { grid-template-columns: 1fr; } }
</style>
