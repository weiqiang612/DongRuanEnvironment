<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import NepmDispatchTips from '@/components/nepm/NepmDispatchTips.vue'
import { getFeedbacks, type DispatchItem } from '@/api/nepm'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'

const props = withDefaults(defineProps<{ mode?: 'dispatch' | 'handle' }>(), { mode: 'dispatch' })

const emit = defineEmits<{
  openDetail: [row: DispatchItem]
  openDispatch: [row: DispatchItem, isRedispatch: boolean]
}>()

const searchState = ref('')
const searchDistrict = ref('')
const searchDateRange = ref<[string, string]>()
const searchKeyword = ref('')
const page = ref(1)

const list = ref<DispatchItem[]>([])
const total = ref(0)
const pageSize = ref(10)
const visibleList = computed(() => props.mode === 'dispatch'
  ? list.value.filter((item) => item.status === '待指派')
  : list.value.filter((item) => item.status === '处理中' || item.status === '已超时' || item.status === '已完成'))

const gradeNames = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'] as const
const stateNames = ['待指派', '处理中', '已完成'] as const

function toItem(row: AqiFeedbackRow): DispatchItem {
  const status = row.timeoutFlag ? '已超时' : stateNames[row.state ?? 0]
  return {
    id: row.afId,
    sn: `FB${String(row.afId).padStart(12, '0')}`,
    address: row.address,
    submitTime: row.submittedAt?.replace('T', ' ').slice(0, 16) ?? '',
    status: status as DispatchItem['status'],
    estimatedAqi: (gradeNames[(row.estimatedGrade ?? 1) - 1] ?? '优') as DispatchItem['estimatedAqi'],
    handler: row.gmName ?? '-',
  }
}

function selectedStates() {
  if (props.mode === 'dispatch') return [0]
  if (searchState.value === '已超时') return undefined
  const state = ['处理中', '已完成'].indexOf(searchState.value)
  return state >= 0 ? [state + 1] : [1, 2]
}

async function loadList() {
  const response = await getFeedbacks({
    states: selectedStates(),
    timeoutOnly: searchState.value === '已超时' ? true : undefined,
    submittedFrom: searchDateRange.value?.[0],
    submittedTo: searchDateRange.value?.[1],
    keyword: searchKeyword.value || undefined,
    page: page.value,
    pageSize: pageSize.value,
  })
  list.value = response.data.data.items.map(toItem)
  total.value = response.data.data.total
}

function getAqiClass(aqi: string) {
  const map: Record<string, string> = {
    优: 'aqi-tag--excellent',
    良: 'aqi-tag--good',
    轻度污染: 'aqi-tag--light',
    中度污染: 'aqi-tag--medium',
    重度污染: 'aqi-tag--heavy',
  }
  return map[aqi] || ''
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
  searchDistrict.value = ''
  searchDateRange.value = undefined
  searchKeyword.value = ''
  page.value = 1
  void loadList()
}

onMounted(() => { void loadList() })

watch(
  () => props.mode,
  () => {
    page.value = 1
    void loadList()
  },
)
</script>

<template>
  <div class="view-dispatch">
    <!-- 筛选面板 -->
    <div class="filter-panel">
      <div class="filter-row">
        <div class="filter-item">
          <label>{{ mode === 'dispatch' ? '待分派状态' : '处理状态' }}</label>
          <el-select v-model="searchState" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="全部状态" value="" />
            <el-option v-if="mode === 'dispatch'" label="待指派" value="待指派" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已超时" value="已超时" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>所属区域</label>
          <el-select v-model="searchDistrict" placeholder="全部区域" clearable style="width: 130px">
            <el-option label="全部区域" value="" />
            <el-option label="朝阳区" value="朝阳区" />
            <el-option label="海淀区" value="海淀区" />
            <el-option label="丰台区" value="丰台区" />
            <el-option label="通州区" value="通州区" />
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
            style="width: 240px"
          />
        </div>

        <div class="filter-item flex-1">
          <label>关键词</label>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入地址、反馈编号或问题描述"
            clearable
            style="max-width: 320px"
          />
        </div>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 双栏布局: 任务列表 + 分派说明 -->
    <div class="dispatch-two-column mt-4">
      <div class="panel-card">
        <div class="panel-header">
          <div class="header-left">
            <span class="blue-block"></span>
            <h3 class="panel-title">{{ mode === 'dispatch' ? '待分派任务列表' : '任务处理列表' }}（共 {{ visibleList.length }} 条）</h3>
          </div>
        </div>

        <el-table :data="visibleList" stripe class="main-data-table">
          <el-table-column prop="id" label="序号" width="60" align="center" />
          <el-table-column prop="sn" label="反馈编号" width="150">
            <template #default="{ row }">
              <span class="font-mono text-bold">{{ row.sn }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="230" show-overflow-tooltip />
          <el-table-column prop="submitTime" label="提交时间" width="160" />
          <el-table-column label="当前状态" width="95" align="center">
            <template #default="{ row }">
              <span :class="['state-pill', `state-pill--${row.status}`]">{{ row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column label="预估AQI" width="100" align="center">
            <template #default="{ row }">
              <span :class="['aqi-pill', getAqiClass(row.estimatedAqi)]">{{ row.estimatedAqi }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="handler" label="当前负责人" width="105" align="center">
            <template #default="{ row }">
              <span :class="row.handler !== '-' ? 'text-regular font-medium' : 'text-disabled'">
                {{ row.handler }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看详情</el-button>
              <span class="op-split">|</span>
              <el-button
                v-if="mode === 'dispatch' || row.status !== '已完成'"
                link
                type="primary"
                size="small"
                @click="emit('openDispatch', row, row.status === '处理中' || row.status === '已超时')"
              >
                {{ mode === 'dispatch' ? '指派' : '重派' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <span class="page-total">共 {{ visibleList.length }} 条</span>
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            @current-change="loadList"
            @size-change="loadList"
            layout="prev, pager, next, sizes, jumper"
            :page-sizes="[10, 20, 50]"
            :total="total"
          />
        </div>
      </div>

      <!-- 右侧分派说明 -->
      <NepmDispatchTips />
    </div>
  </div>
</template>

<style scoped>
.filter-panel { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
.filter-row { display: flex; align-items: center; flex-wrap: wrap; gap: 16px; }
.filter-item { display: flex; align-items: center; gap: 8px; font-size: 13.5px; }
.filter-item label { color: #4b5563; white-space: nowrap; }
.filter-actions { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.flex-1 { flex: 1; }
.mt-4 { margin-top: 16px; }
.dispatch-two-column { display: grid; grid-template-columns: minmax(0, 1fr) 310px; gap: 16px; }
.panel-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.header-left { display: flex; align-items: center; gap: 8px; }
.blue-block { width: 12px; height: 12px; background-color: #1677ff; border-radius: 2px; }
.panel-title { margin: 0; font-size: 16px; font-weight: 600; color: #1f2937; }
.font-mono { font-family: monospace; }
.text-bold { font-weight: 600; }
.text-disabled { color: #94a3b8; }
.text-regular { color: #374151; }
.font-medium { font-weight: 500; }
.state-pill { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.state-pill--待指派 { background: #e6f4ff; color: #1677ff; border: 1px solid #bae0ff; }
.state-pill--处理中 { background: #f0f7ff; color: #0284c7; border: 1px solid #bae6fd; }
.state-pill--已完成 { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.state-pill--已超时 { background: #fff1f0; color: #ff4d4f; border: 1px solid #ffa39e; }
.aqi-pill { display: inline-block; padding: 2px 7px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.aqi-tag--excellent { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.aqi-tag--good { background: #fefce8; color: #ca8a04; border: 1px solid #fef08a; }
.aqi-tag--light { background: #fff7ed; color: #ea580c; border: 1px solid #fed7aa; }
.aqi-tag--medium { background: #fff1f0; color: #e11d48; border: 1px solid #fecdd3; }
.aqi-tag--heavy { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.op-split { margin: 0 6px; color: #cbd5e1; }
.pagination-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; padding-top: 12px; border-top: 1px solid #f1f5f9; }
.page-total { font-size: 13px; color: #64748b; }
@media (max-width: 1200px) { .dispatch-two-column { grid-template-columns: 1fr; } }
</style>
