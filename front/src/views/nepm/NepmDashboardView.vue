<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import NepmStatCard from '@/components/nepm/NepmStatCard.vue'
import { getDashboard, getFeedbacks, getTimeoutAlerts, type DashboardSummary, type DispatchItem } from '@/api/nepm'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'

const emit = defineEmits<{ navigate: [target: string]; openDispatch: [row: DispatchItem] }>()
const summary = ref<DashboardSummary>({ pending: 0, assigned: 0, completed: 0, timeout: 0 })
const pendingFeedbacks = ref<DispatchItem[]>([])
const timeoutFeedbacks = ref<DispatchItem[]>([])
const stats = computed(() => [
  { title: '待指派任务', value: summary.value.pending, tone: 'blue' as const },
  { title: '处理中任务', value: summary.value.assigned, tone: 'amber' as const },
  { title: '超时任务', value: summary.value.timeout, tone: 'red' as const },
  { title: '已完成任务', value: summary.value.completed, tone: 'green' as const },
])
const gradeNames = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'] as const
const stateNames = ['待指派', '处理中', '已完成'] as const

function toItem(row: AqiFeedbackRow): DispatchItem {
  return {
    id: row.afId,
    sn: `FB${String(row.afId).padStart(12, '0')}`,
    address: row.address,
    submitTime: row.submittedAt?.replace('T', ' ').slice(0, 16) ?? '',
    status: row.timeoutFlag ? '已超时' : stateNames[row.state ?? 0] ?? '待指派',
    estimatedAqi: gradeNames[(row.estimatedGrade ?? 1) - 1] ?? '优',
    handler: row.gmName ?? '-',
  }
}

function getAqiClass(aqi: string) {
  return `aqi-tag--${({ 优: 'excellent', 良: 'good', 轻度污染: 'light', 中度污染: 'medium', 重度污染: 'heavy', 严重污染: 'severe' } as Record<string, string>)[aqi] ?? 'excellent'}`
}

async function loadDashboard() {
  const [summaryResponse, pendingResponse, timeoutResponse] = await Promise.all([
    getDashboard(),
    getFeedbacks({ states: [0], page: 1, pageSize: 5 }),
    getTimeoutAlerts({ page: 1, pageSize: 5 }),
  ])
  summary.value = summaryResponse.data.data
  pendingFeedbacks.value = pendingResponse.data.data.items.map(toItem)
  timeoutFeedbacks.value = timeoutResponse.data.data.items.map(toItem)
}

onMounted(() => { void loadDashboard() })
</script>

<template>
  <div class="view-dashboard">
    <div class="stat-grid-4"><NepmStatCard v-for="stat in stats" :key="stat.title" v-bind="stat" /></div>
    <div class="two-col-grid mt-4">
      <div class="panel-card">
        <div class="panel-header"><div class="header-left"><span class="blue-block"></span><h3 class="panel-title">待指派公众反馈</h3></div><a href="javascript:void(0)" class="panel-more" @click="emit('navigate', 'feedbacks')">查看更多 &gt;</a></div>
        <el-table :data="pendingFeedbacks" stripe empty-text="暂无待指派反馈">
          <el-table-column prop="id" label="序号" width="60" align="center" />
          <el-table-column prop="address" label="地址" min-width="190" show-overflow-tooltip />
          <el-table-column prop="submitTime" label="提交时间" width="150" />
          <el-table-column label="预估AQI" width="100" align="center"><template #default="{ row }"><span :class="['aqi-pill', getAqiClass(row.estimatedAqi)]">{{ row.estimatedAqi }}</span></template></el-table-column>
          <el-table-column label="操作" width="80" align="center"><template #default="{ row }"><el-button link type="primary" size="small" @click="emit('openDispatch', row)">指派</el-button></template></el-table-column>
        </el-table>
      </div>
      <div class="panel-card">
        <div class="panel-header"><div class="header-left"><el-icon class="warn-icon"><WarningFilled /></el-icon><h3 class="panel-title">超时任务</h3></div><a href="javascript:void(0)" class="panel-more" @click="emit('navigate', 'timeout')">查看更多 &gt;</a></div>
        <el-table :data="timeoutFeedbacks" stripe empty-text="暂无超时任务">
          <el-table-column prop="sn" label="反馈编号" width="145" />
          <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
          <el-table-column prop="submitTime" label="提交时间" width="150" />
          <el-table-column label="状态" width="90" align="center"><template #default="{ row }"><span class="state-pill state-pill--timeout">{{ row.status }}</span></template></el-table-column>
        </el-table>
      </div>
    </div>
    <div class="panel-card mt-4 unavailable-card"><span class="blue-block"></span><div><h3 class="panel-title">趋势与 AQI 分布</h3><p>真实趋势、检测结果和 AQI 分布接口将在后续模块开放后展示。</p></div></div>
  </div>
</template>

<style scoped>
.stat-grid-4, .two-col-grid { display: grid; gap: 16px; }.stat-grid-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }.two-col-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }.mt-4 { margin-top: 16px; }.panel-card { padding: 16px 20px; background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; box-shadow: 0 1px 3px rgb(0 0 0 / 2%); }.panel-header, .header-left, .unavailable-card { display: flex; align-items: center; }.panel-header { justify-content: space-between; margin-bottom: 14px; }.header-left, .unavailable-card { gap: 8px; }.blue-block { width: 12px; height: 12px; background: #1677ff; border-radius: 2px; flex: 0 0 auto; }.panel-title { margin: 0; color: #1f2937; font-size: 16px; font-weight: 600; }.panel-more { color: #1677ff; font-size: 12.5px; text-decoration: none; }.warn-icon { color: #ef4444; font-size: 17px; }.state-pill, .aqi-pill { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; }.state-pill--timeout { color: #ff4d4f; border: 1px solid #ffa39e; background: #fff1f0; }.aqi-tag--excellent { color: #52c41a; border: 1px solid #b7eb8f; background: #f6ffed; }.aqi-tag--good { color: #ca8a04; border: 1px solid #fef08a; background: #fefce8; }.aqi-tag--light { color: #ea580c; border: 1px solid #fed7aa; background: #fff7ed; }.aqi-tag--medium { color: #e11d48; border: 1px solid #fecdd3; background: #fff1f0; }.aqi-tag--heavy { color: #dc2626; border: 1px solid #fecaca; background: #fef2f2; }.aqi-tag--severe { color: #9333ea; border: 1px solid #e9d5ff; background: #faf5ff; }.unavailable-card p { margin: 6px 0 0; color: #64748b; font-size: 13px; }@media (max-width: 1200px) { .stat-grid-4 { grid-template-columns: repeat(2, minmax(0, 1fr)); }.two-col-grid { grid-template-columns: 1fr; } }
</style>
