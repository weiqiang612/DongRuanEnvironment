<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getDashboard, getFeedbacks, type DispatchItem } from '@/api/nepm'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'

const emit = defineEmits<{
  openDetail: [row: DispatchItem]
  openDispatch: [row: DispatchItem, isRedispatch: boolean]
}>()

type TaskTab = 'all' | 'pending' | 'handling' | 'completed' | 'timeout'

// 页面上方状态选项卡（按参考图顺序：全部、待指派、处理中、已完成、已超时）
const activeTab = ref<TaskTab>('all')

// 各状态数量统计（黑色括弧纯文本）
const tabCounts = ref({
  all: 0,
  pending: 0,
  handling: 0,
  completed: 0,
  timeout: 0,
})

const searchProvince = ref('')
const searchCity = ref('')
const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])
const searchGrade = ref<number | ''>('')
const searchDateRange = ref<[string, string]>()
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<DispatchItem[]>([])
const loading = ref(false)

const gradeNames = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'] as const
const stateNames = ['待指派', '处理中', '已完成'] as const

function toItem(row: AqiFeedbackRow): DispatchItem {
  const status = row.timeoutFlag ? '已超时' : stateNames[row.state ?? 0]
  return {
    id: row.afId,
    sn: `FB${String(row.afId).padStart(10, '0')}`,
    address: row.address,
    submitTime: row.submittedAt?.replace('T', ' ').slice(0, 16) ?? '',
    status: status as DispatchItem['status'],
    estimatedAqi: (gradeNames[(row.estimatedGrade ?? 1) - 1] ?? '优') as DispatchItem['estimatedAqi'],
    handler: row.gmName ?? '-',
  }
}

function getQueryStates(): number[] | undefined {
  if (activeTab.value === 'pending') return [0]
  if (activeTab.value === 'handling') return [1]
  if (activeTab.value === 'completed') return [2]
  return undefined
}

async function loadCounts() {
  try {
    const res = await getDashboard()
    if (res.data?.data) {
      const d = res.data.data
      tabCounts.value.pending = d.pending || 0
      tabCounts.value.handling = d.assigned || 0
      tabCounts.value.timeout = d.timeout || 0
      tabCounts.value.completed = d.completed || 0
      tabCounts.value.all = (d.pending || 0) + (d.assigned || 0) + (d.completed || 0)
    }
  } catch {
    // 忽略指标统计异常
  }
}

async function loadProvinces() {
  try {
    const res = await getProvinceOptions()
    provinces.value = res.data.data
  } catch {
    // 忽略省份加载失败
  }
}

async function onProvinceChange() {
  searchCity.value = ''
  cities.value = []
  if (!searchProvince.value) return
  try {
    const res = await getCityOptions(Number(searchProvince.value))
    cities.value = res.data.data
  } catch {
    // 忽略城市加载失败
  }
}

async function loadList() {
  loading.value = true
  try {
    const response = await getFeedbacks({
      states: getQueryStates(),
      timeoutOnly: activeTab.value === 'timeout' ? true : undefined,
      estimatedGrade: typeof searchGrade.value === 'number' ? searchGrade.value : undefined,
      provinceId: searchProvince.value ? Number(searchProvince.value) : undefined,
      cityId: searchCity.value ? Number(searchCity.value) : undefined,
      keyword: searchKeyword.value.trim() || undefined,
      submittedFrom: searchDateRange.value?.[0],
      submittedTo: searchDateRange.value?.[1],
      page: page.value,
      pageSize: pageSize.value,
    })
    list.value = response.data.data.items.map(toItem)
    total.value = response.data.data.total
  } finally {
    loading.value = false
  }
}

function getAqiClass(aqi: string) {
  const map: Record<string, string> = {
    优: 'aqi-grade-1',
    良: 'aqi-grade-2',
    轻度污染: 'aqi-grade-3',
    中度污染: 'aqi-grade-4',
    重度污染: 'aqi-grade-5',
    严重污染: 'aqi-grade-6',
  }
  return map[aqi] || ''
}

function handleTabChange(tab: TaskTab) {
  activeTab.value = tab
  page.value = 1
  void loadList()
}

async function handleSearch() {
  page.value = 1
  try {
    await loadList()
    ElMessage.success('已按条件查询')
  } catch {
    ElMessage.error('查询失败，请确认后端服务已启动')
  }
}

function handleReset() {
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  searchGrade.value = ''
  searchDateRange.value = undefined
  searchKeyword.value = ''
  page.value = 1
  void loadList()
}

onMounted(() => {
  void loadList()
  void loadCounts()
  void loadProvinces()
})
</script>

<template>
  <div class="view-task-manage">
    <!-- 页面上方状态选项卡（纯净无底色徽标，括弧带数字，未选黑色/深灰） -->
    <div class="task-tabs-bar">
      <button
        type="button"
        :class="['task-tab-btn', { 'is-active': activeTab === 'all' }]"
        @click="handleTabChange('all')"
      >
        全部 ({{ tabCounts.all }})
      </button>
      <button
        type="button"
        :class="['task-tab-btn', { 'is-active': activeTab === 'pending' }]"
        @click="handleTabChange('pending')"
      >
        待指派 ({{ tabCounts.pending }})
      </button>
      <button
        type="button"
        :class="['task-tab-btn', { 'is-active': activeTab === 'handling' }]"
        @click="handleTabChange('handling')"
      >
        处理中 ({{ tabCounts.handling }})
      </button>
      <button
        type="button"
        :class="['task-tab-btn', { 'is-active': activeTab === 'completed' }]"
        @click="handleTabChange('completed')"
      >
        已完成 ({{ tabCounts.completed }})
      </button>
      <button
        type="button"
        :class="['task-tab-btn', { 'is-active': activeTab === 'timeout' }]"
        @click="handleTabChange('timeout')"
      >
        已超时 ({{ tabCounts.timeout }})
      </button>
    </div>

    <!-- 筛选面板（所属区域、AQI等级、提交时间、关键词、查询、重置） -->
    <div class="filter-panel mt-3">
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
          <el-select v-model="searchGrade" placeholder="全部等级" clearable style="width: 110px">
            <el-option label="全部等级" value="" />
            <el-option label="优 (一级)" :value="1" />
            <el-option label="良 (二级)" :value="2" />
            <el-option label="轻度污染 (三级)" :value="3" />
            <el-option label="中度污染 (四级)" :value="4" />
            <el-option label="重度污染 (五级)" :value="5" />
            <el-option label="严重污染 (六级)" :value="6" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>提交时间</label>
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
            placeholder="请输入地址、任务编号或反馈编号"
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

    <!-- 通栏任务列表表格卡片（右侧分派说明已去掉） -->
    <div class="panel-card mt-3">
      <el-table v-loading="loading" :data="list" stripe class="main-data-table">
        <el-table-column prop="sn" label="任务编号" width="175">
          <template #default="{ row }">
            <span class="sn-text">{{ row.sn }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="address-text">{{ row.address }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预估AQI" width="120" align="center">
          <template #default="{ row }">
            <span :class="['aqi-pill', getAqiClass(row.estimatedAqi)]">{{ row.estimatedAqi }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170" align="center" />
        <el-table-column label="当前负责人" width="130" align="center">
          <template #default="{ row }">
            <span :class="row.handler && row.handler !== '-' ? 'text-regular' : 'text-dash'">
              {{ row.handler && row.handler !== '-' ? row.handler : '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="115" align="center">
          <template #default="{ row }">
            <span :class="['state-pill', `state-pill--${row.status}`]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="125" align="center">
          <template #default="{ row }">
            <!-- 待指派：查看 | 指派 -->
            <template v-if="row.status === '待指派'">
              <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
              <span class="op-split">|</span>
              <el-button link type="primary" size="small" @click="emit('openDispatch', row, false)">指派</el-button>
            </template>
            <!-- 已超时：查看 | 重派 -->
            <template v-else-if="row.status === '已超时'">
              <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
              <span class="op-split">|</span>
              <el-button link type="primary" size="small" @click="emit('openDispatch', row, true)">重派</el-button>
            </template>
            <!-- 已完成：详情 -->
            <template v-else-if="row.status === '已完成'">
              <el-button link type="primary" size="small" @click="emit('openDetail', row)">详情</el-button>
            </template>
            <!-- 处理中：查看 -->
            <template v-else>
              <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部分页 -->
      <div class="pagination-bar">
        <span class="page-total">共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="prev, pager, next, sizes, jumper"
          @current-change="loadList"
          @size-change="loadList"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-task-manage {
  width: 100%;
}

/* 顶部状态选项卡 */
.task-tabs-bar {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 4px 6px 10px;
  border-bottom: 1px solid #e2e8f0;
}

.task-tab-btn {
  background: transparent;
  border: none;
  font-size: 14.5px;
  color: #1e293b;
  cursor: pointer;
  padding: 4px 0 10px;
  position: relative;
  outline: none;
  transition: color 0.2s ease;
  font-weight: 500;
}

.task-tab-btn:hover {
  color: #1890ff;
}

.task-tab-btn.is-active {
  color: #1890ff;
  font-weight: 600;
}

.task-tab-btn.is-active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2.5px;
  background: #1890ff;
  border-radius: 2px;
}

.mt-3 {
  margin-top: 14px;
}

/* 筛选面板 */
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

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.region-selects {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-item label {
  color: #475569;
  white-space: nowrap;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.flex-1 {
  flex: 1;
}

/* 表格卡片通栏 */
.panel-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 18px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  width: 100%;
  box-sizing: border-box;
}

.main-data-table {
  width: 100%;
}

.sn-text {
  font-size: 13px;
  color: #1e293b;
  letter-spacing: 0.2px;
}

.address-text {
  color: #1e293b;
  font-size: 13px;
}

.text-dash {
  color: #94a3b8;
}

.text-regular {
  color: #334155;
}

/* 状态标签胶囊 (对齐参考图) */
.state-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.state-pill--待指派 {
  background: #e6f4ff;
  color: #1677ff;
  border: 1px solid #bae0ff;
}

.state-pill--处理中 {
  background: #f0f7ff;
  color: #0284c7;
  border: 1px solid #bae6fd;
}

.state-pill--已完成 {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.state-pill--已超时 {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

/* AQI 等级胶囊 (HJ 633 国标色系，对齐参考图轻雅配色) */
.aqi-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}



.op-split {
  margin: 0 6px;
  color: #cbd5e1;
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
</style>

