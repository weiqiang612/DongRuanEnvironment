<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import NepmDispatchModal from '@/components/nepm/NepmDispatchModal.vue'
import NepmHeader from '@/components/nepm/NepmHeader.vue'
import NepmSidebar from '@/components/nepm/NepmSidebar.vue'

import NepmAnalyticsView from '@/views/nepm/NepmAnalyticsView.vue'
import NepmDashboardView from '@/views/nepm/NepmDashboardView.vue'
import NepmDetailView from '@/views/nepm/NepmDetailView.vue'
import NepmDispatchView from '@/views/nepm/NepmDispatchView.vue'
import NepmFeedbacksView from '@/views/nepm/NepmFeedbacksView.vue'
import NepmResultsView from '@/views/nepm/NepmResultsView.vue'
import NepmTimeoutView from '@/views/nepm/NepmTimeoutView.vue'

const router = useRouter()

// 管理员登录后默认展示工作台概览
const active = ref('dashboard')

const dispatchModalVisible = ref(false)
const contentRevision = ref(0)
const selectedFeedbackId = ref<number | null>(null)
interface DispatchTask {
  id: number
  sn: string
  address: string
  estimatedAqi?: string
  isRedispatch?: boolean
  handler?: string
}

const currentDispatchTask = ref<DispatchTask | null>(null)

const pageHeader = computed(() => {
  switch (active.value) {
    case 'dashboard':
      return { title: '工作台', subtitle: '今日工作概览 · 这是您处理公众反馈、任务调度等日常工作的入口' }
    case 'feedbacks':
      return { title: '公众反馈列表', subtitle: '查看和处理公众提交的环境问题反馈，及时响应，推动问题解决。' }
    case 'feedbackDetail':
      return { title: '反馈详情', subtitle: '查看公众反馈的详细信息及处理进度' }
    case 'dispatch':
      return { title: '任务分派', subtitle: '将公众反馈的环境问题任务分派给网格巡查员，确保问题及时处理。' }
    case 'handle':
      return { title: '任务处理', subtitle: '跟踪已分派任务的执行进度，对超时或异常任务进行催办、重派和闭环处理。' }
    case 'results':
      return { title: '检测结果', subtitle: '查看网格员的污染物检测数据和最终AQI结果，支持多条件筛选与结果查询。' }
    case 'timeout':
      return { title: '超时预警', subtitle: '实时监控公众反馈任务的超时情况，及时发出预警并督促处理，确保问题闭环。' }
    case 'analytics':
    case 'regional':
      return { title: '统计分析', subtitle: '对系统反馈数据进行多维度统计分析，支持历史趋势、区域分布和问题类型分析' }
    default:
      return { title: '工作台', subtitle: '' }
  }
})

function handleMenuChange(key: string) {
  active.value = key
}

function onOpenDispatch(row: DispatchTask, isRedispatch = false) {
  currentDispatchTask.value = {
    id: row.id,
    sn: row.sn,
    address: row.address,
    estimatedAqi: row.estimatedAqi,
    isRedispatch,
    handler: row.handler,
  }
  dispatchModalVisible.value = true
}

function openDetail(row: { id: number }) {
  selectedFeedbackId.value = row.id
  active.value = 'feedbackDetail'
}

function onTaskDispatched() {
  contentRevision.value += 1
}
</script>

<template>
  <div class="nepm-page">
    <NepmHeader @logout="router.push('/')" />

    <div class="shell">
      <NepmSidebar :active="active" @change="handleMenuChange" />

      <main id="main-content" class="main-content">
        <!-- 页面主标题与副标题 -->
        <div v-if="active !== 'feedbackDetail'" class="page-heading">
          <h1 class="heading-title">{{ pageHeader.title }}</h1>
          <p class="heading-subtitle">{{ pageHeader.subtitle }}</p>
        </div>

        <!-- 7 个高质量业务子视图切换 -->
        <NepmDashboardView
          v-if="active === 'dashboard'"
          :key="`dashboard-${contentRevision}`"
          @navigate="active = $event"
          @open-dispatch="onOpenDispatch"
        />

        <NepmFeedbacksView
          v-else-if="active === 'feedbacks'"
          :key="`feedbacks-${contentRevision}`"
          @open-detail="openDetail"
          @open-dispatch="onOpenDispatch"
        />

        <NepmDetailView
          v-else-if="active === 'feedbackDetail'"
          :key="`detail-${selectedFeedbackId}-${contentRevision}`"
          :feedback-id="selectedFeedbackId"
          @back="active = 'feedbacks'"
          @open-dispatch="onOpenDispatch"
        />

        <NepmDispatchView
          v-else-if="active === 'dispatch' || active === 'handle'"
          :key="`${active}-${contentRevision}`"
          :mode="active === 'dispatch' ? 'dispatch' : 'handle'"
          @open-detail="openDetail"
          @open-dispatch="onOpenDispatch"
        />

        <NepmResultsView
          v-else-if="active === 'results'"
          @open-detail="active = 'feedbackDetail'"
        />

        <NepmTimeoutView
          v-else-if="active === 'timeout'"
          :key="`timeout-${contentRevision}`"
          @open-detail="openDetail"
          @open-dispatch="onOpenDispatch"
        />

        <NepmAnalyticsView
          v-else-if="active === 'analytics' || active === 'regional'"
        />
      </main>
    </div>

    <!-- 全局任务指派/重派通用弹窗 -->
    <NepmDispatchModal
      v-model:visible="dispatchModalVisible"
      :task-data="currentDispatchTask"
      @dispatched="onTaskDispatched"
    />
  </div>
</template>

<style scoped>
.nepm-page {
  min-height: 100vh;
  background-color: #f5f8fc;
  color: #1f2937;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  display: flex;
  flex-direction: column;
}

.shell {
  display: flex;
  flex: 1;
}

.main-content {
  flex: 1;
  min-width: 0;
  padding: 20px 24px 36px;
  max-width: 1720px;
  margin: 0 auto;
  width: 100%;
}

.page-heading {
  margin-bottom: 16px;
}

.heading-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #102f5e;
  line-height: 1.25;
}

.heading-subtitle {
  margin: 0;
  font-size: 13.5px;
  color: #64748b;
}

@media (max-width: 900px) {
  .shell {
    flex-direction: column;
  }
}
</style>
