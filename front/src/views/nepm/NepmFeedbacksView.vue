<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFeedbacks, type FeedbackItem } from '@/api/nepm'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'

const emit = defineEmits<{
  openDetail: [row: FeedbackItem]
  openDispatch: [row: FeedbackItem]
}>()

const searchState = ref('')
const searchAqi = ref('')
const searchProvince = ref('')
const searchCity = ref('')
const searchDateRange = ref<[string, string]>()
const searchKeyword = ref('')
const page = ref(1)

const list = ref<FeedbackItem[]>([])
const total = ref(0)
const pageSize = ref(10)
const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])

const gradeNames = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'] as const
const stateNames = ['待指派', '处理中', '已完成'] as const

function toItem(row: AqiFeedbackRow): FeedbackItem {
  const isTimeout = Boolean(row.timeoutFlag)
  return {
    id: row.afId,
    sn: `FB${String(row.afId).padStart(12, '0')}`,
    address: row.address,
    submitTime: row.submittedAt?.replace('T', ' ').slice(0, 16) ?? '',
    status: (isTimeout ? '已超时' : stateNames[row.state ?? 0]) as FeedbackItem['status'],
    estimatedAqi: (gradeNames[(row.estimatedGrade ?? 1) - 1] ?? '优') as FeedbackItem['estimatedAqi'],
    finalAqi: '-',
    handler: row.gmName ?? '-',
  }
}

function stateQuery() {
  if (searchState.value === '已超时') return { timeoutOnly: true }
  const value = ['待指派', '处理中', '已完成'].indexOf(searchState.value)
  return value >= 0 ? { states: [value] } : {}
}

function gradeQuery() {
  const value = gradeNames.indexOf(searchAqi.value as typeof gradeNames[number])
  return value >= 0 ? { estimatedGrade: value + 1 } : {}
}

async function loadList() {
  const response = await getFeedbacks({
    ...stateQuery(),
    ...gradeQuery(),
    provinceId: searchProvince.value ? Number(searchProvince.value) : undefined,
    cityId: searchCity.value ? Number(searchCity.value) : undefined,
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
  provinces.value = (await getProvinceOptions()).data.data
}

async function onProvinceChange() {
  searchCity.value = ''
  cities.value = searchProvince.value ? (await getCityOptions(Number(searchProvince.value))).data.data : []
}

function getAqiClass(aqi?: string) {
  const map: Record<string, string> = {
    优: 'aqi-grade-1',
    良: 'aqi-grade-2',
    轻度污染: 'aqi-grade-3',
    中度污染: 'aqi-grade-4',
    重度污染: 'aqi-grade-5',
    严重污染: 'aqi-grade-6',
  }
  return aqi && map[aqi] ? map[aqi] : ''
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
  searchState.value = ''
  searchAqi.value = ''
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  searchDateRange.value = undefined
  searchKeyword.value = ''
  page.value = 1
  void loadList()
}

onMounted(() => {
  void loadList()
  void loadProvinces()
})
</script>

<template>
  <div class="view-feedbacks">
    <!-- 复合筛选卡片 -->
    <div class="filter-panel">
      <div class="filter-row">
        <div class="filter-item">
          <label>反馈状态</label>
          <el-select v-model="searchState" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="待指派" value="待指派" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已超时" value="已超时" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>预估AQI等级</label>
          <el-select v-model="searchAqi" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="优" value="优" />
            <el-option label="良" value="良" />
            <el-option label="轻度污染" value="轻度污染" />
            <el-option label="中度污染" value="中度污染" />
            <el-option label="重度污染" value="重度污染" />
            <el-option label="严重污染" value="严重污染" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>所属区域</label>
          <div class="region-selects">
            <el-select v-model="searchProvince" placeholder="全部省份" style="width: 130px" clearable @change="onProvinceChange">
              <el-option v-for="item in provinces" :key="item.id" :label="item.name" :value="String(item.id)" />
            </el-select>
            <el-select v-model="searchCity" placeholder="全部城市" style="width: 130px" clearable :disabled="!searchProvince">
              <el-option v-for="item in cities" :key="item.id" :label="item.name" :value="String(item.id)" />
            </el-select>
          </div>
        </div>
      </div>

      <div class="filter-row mt-3">
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
            style="width: 250px"
          />
        </div>

        <div class="filter-item flex-1">
          <label>关键词</label>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入地址、问题描述等关键词"
            clearable
            style="max-width: 380px"
          />
        </div>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 反馈列表表格 -->
    <div class="panel-card mt-4">
      <el-table :data="list" stripe class="main-data-table">
        <el-table-column prop="id" label="序号" width="60" align="center" />
        <el-table-column prop="sn" label="反馈编号" width="150">
          <template #default="{ row }">
            <span class="font-mono text-bold">{{ row.sn }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="240" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="165" />
        <el-table-column label="当前状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['state-pill', `state-pill--${row.status}`]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预估AQI" width="105" align="center">
          <template #default="{ row }">
            <span :class="['aqi-pill', getAqiClass(row.estimatedAqi)]">{{ row.estimatedAqi }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最终AQI" width="105" align="center">
          <template #default="{ row }">
            <span v-if="row.finalAqi && row.finalAqi !== '-'" :class="['aqi-pill', getAqiClass(row.finalAqi)]">
              {{ row.finalAqi }}
            </span>
            <span v-else class="text-disabled">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="95" align="center">
          <template #default="{ row }">
            <span :class="row.handler !== '-' ? 'text-regular' : 'text-disabled'">{{ row.handler }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="135" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
            <span class="op-split">|</span>
            <el-button
              link
              type="primary"
              size="small"
              @click="row.status === '已完成' ? emit('openDetail', row) : emit('openDispatch', row)"
            >
              {{ row.status === '已完成' ? '详情' : '处理' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span class="page-total">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          @current-change="loadList"
          @size-change="loadList"
          layout="sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
        />
      </div>
    </div>
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
.filter-row { display: flex; align-items: center; flex-wrap: wrap; gap: 16px; }
.mt-3 { margin-top: 12px; }
.mt-4 { margin-top: 16px; }
.filter-item { display: flex; align-items: center; gap: 8px; font-size: 13.5px; }
.filter-item label { color: #4b5563; font-size: 13.5px; white-space: nowrap; }
.region-selects { display: flex; gap: 6px; }
.filter-actions { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.flex-1 { flex: 1; }
.panel-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px 20px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02); }
.font-mono { font-family: monospace; }
.text-bold { font-weight: 600; color: #1f2937; }
.text-disabled { color: #94a3b8; }
.text-regular { color: #374151; font-weight: 500; }
.state-pill { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.state-pill--待指派 { background: #e6f4ff; color: #1677ff; border: 1px solid #bae0ff; }
.state-pill--处理中 { background: #f0f7ff; color: #0284c7; border: 1px solid #bae6fd; }
.state-pill--已完成 { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.state-pill--已超时 { background: #fff1f0; color: #ff4d4f; border: 1px solid #ffa39e; }
.op-split { margin: 0 6px; color: #cbd5e1; }
.pagination-bar {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 16px; padding-top: 12px; border-top: 1px solid #f1f5f9;
}
.page-total { font-size: 13px; color: #64748b; }
</style>
