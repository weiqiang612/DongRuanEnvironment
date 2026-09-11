<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFeedback, getLogs, type AssignLog, type DispatchItem } from '@/api/nepm'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'

const props = defineProps<{ feedbackId: number | null }>()
const emit = defineEmits<{ back: []; openDispatch: [row: DispatchItem] }>()
const feedback = ref<AqiFeedbackRow | null>(null)
const logs = ref<AssignLog[]>([])
const loading = ref(false)
const gradeNames = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'] as const
const stateNames = ['待指派', '处理中', '已完成'] as const

const status = computed(() => feedback.value?.timeoutFlag ? '已超时' : stateNames[feedback.value?.state ?? 0] ?? '待指派')
const estimatedGrade = computed(() => gradeNames[(feedback.value?.estimatedGrade ?? 1) - 1] ?? '优')
const task = computed<DispatchItem | null>(() => feedback.value ? {
  id: feedback.value.afId,
  sn: `FB${String(feedback.value.afId).padStart(12, '0')}`,
  address: feedback.value.address,
  submitTime: feedback.value.submittedAt?.replace('T', ' ').slice(0, 16) ?? '',
  status: status.value,
  estimatedAqi: estimatedGrade.value,
  handler: feedback.value.gmName ?? '-',
} : null)

function formatTime(value?: string) {
  return value?.replace('T', ' ').slice(0, 19) ?? '-'
}

function actionLabel(action: string) {
  return ({ ASSIGN: '首次指派', REASSIGN: '重新指派', CONTINUE: '继续处理' } as Record<string, string>)[action] ?? action
}

async function loadDetail() {
  if (props.feedbackId === null) return
  loading.value = true
  try {
    const [feedbackResponse, logsResponse] = await Promise.all([
      getFeedback(props.feedbackId),
      getLogs(props.feedbackId),
    ])
    feedback.value = feedbackResponse.data.data
    logs.value = logsResponse.data.data
  } catch {
    feedback.value = null
    logs.value = []
    ElMessage.error('反馈详情加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { void loadDetail() })
watch(() => props.feedbackId, () => { void loadDetail() })
</script>

<template>
  <div class="view-detail" v-loading="loading">
    <div class="detail-top-bar"><el-button :icon="ArrowLeft" @click="emit('back')">返回列表</el-button></div>
    <template v-if="feedback && task">
      <div class="detail-two-column">
        <section class="panel-card">
          <div class="panel-header"><span class="blue-block"></span><h3>反馈基本信息</h3></div>
          <dl class="info-grid">
            <div><dt>反馈编号</dt><dd class="font-mono">{{ task.sn }}</dd></div>
            <div><dt>地址</dt><dd>{{ feedback.address }}</dd></div>
            <div><dt>提交时间</dt><dd>{{ formatTime(feedback.submittedAt) }}</dd></div>
            <div><dt>所属区域</dt><dd>{{ feedback.provinceName ?? '-' }} {{ feedback.cityName ?? '' }}</dd></div>
            <div><dt>问题描述</dt><dd class="description">{{ feedback.information }}</dd></div>
            <div><dt>预估AQI等级</dt><dd>{{ estimatedGrade }}</dd></div>
            <div><dt>当前状态</dt><dd><span :class="['state-pill', `state-pill--${status}`]">{{ status }}</span></dd></div>
            <div><dt>当前处理人</dt><dd>{{ feedback.gmName ?? '-' }}</dd></div>
          </dl>
          <div class="action-row"><el-button v-if="status !== '已完成'" type="primary" @click="emit('openDispatch', task)">{{ task.handler === '-' ? '指派网格员' : '重新指派' }}</el-button></div>
        </section>
        <section class="panel-card">
          <div class="panel-header"><span class="blue-block"></span><h3>指派记录</h3></div>
          <el-table :data="logs" empty-text="暂无指派记录">
            <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column>
            <el-table-column label="操作" width="110"><template #default="{ row }">{{ actionLabel(row.actionType) }}</template></el-table-column>
            <el-table-column prop="fromGmId" label="原网格员" min-width="110" />
            <el-table-column prop="toGmId" label="目标网格员" min-width="110" />
          </el-table>
        </section>
      </div>
      <section class="panel-card mt-4 unavailable-card"><span class="blue-block"></span><div><h3>检测结果</h3><p>实测数据和最终 AQI 将在检测结果模块实现后展示。</p></div></section>
    </template>
    <el-empty v-else-if="!loading" description="未选择反馈或反馈不存在" />
  </div>
</template>

<style scoped>
.detail-top-bar { margin-bottom: 12px; }.detail-two-column { display: grid; grid-template-columns: minmax(0, 1fr) minmax(420px, 0.9fr); gap: 16px; }.panel-card { padding: 16px 20px; background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; box-shadow: 0 1px 3px rgb(0 0 0 / 2%); }.panel-header, .unavailable-card { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }.panel-header h3, .unavailable-card h3 { margin: 0; color: #1f2937; font-size: 16px; }.blue-block { width: 12px; height: 12px; border-radius: 2px; background: #1677ff; flex: 0 0 auto; }.info-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px 28px; margin: 0; }.info-grid div { display: grid; gap: 4px; }.info-grid dt { color: #64748b; font-size: 13px; }.info-grid dd { margin: 0; color: #1f2937; font-size: 14px; }.description { line-height: 1.6; }.font-mono { font-family: monospace; }.state-pill { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; }.state-pill--待指派 { color: #1677ff; border: 1px solid #bae0ff; background: #e6f4ff; }.state-pill--处理中 { color: #0284c7; border: 1px solid #bae6fd; background: #f0f7ff; }.state-pill--已完成 { color: #52c41a; border: 1px solid #b7eb8f; background: #f6ffed; }.state-pill--已超时 { color: #ff4d4f; border: 1px solid #ffa39e; background: #fff1f0; }.action-row { margin-top: 20px; }.mt-4 { margin-top: 16px; }.unavailable-card p { margin: 6px 0 0; color: #64748b; font-size: 13px; }@media (max-width: 1100px) { .detail-two-column { grid-template-columns: 1fr; }.info-grid { grid-template-columns: 1fr; } }
</style>
