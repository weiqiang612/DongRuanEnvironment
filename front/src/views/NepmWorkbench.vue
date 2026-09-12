<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { logoutSession } from '@/api/session'

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
const confirmLogoutVisible = ref(false)
const loggingOut = ref(false)
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

function openLogoutConfirm() {
  confirmLogoutVisible.value = true
}

function closeLogoutConfirm() {
  confirmLogoutVisible.value = false
}

async function handleConfirmLogout() {
  loggingOut.value = true
  try {
    await logoutSession()
  } catch {
    // 忽略异常
  } finally {
    confirmLogoutVisible.value = false
    loggingOut.value = false
    router.push('/nepm/login')
  }
}
</script>

<template>
  <div class="nepm-page">
    <NepmHeader @logout="openLogoutConfirm" />

    <div class="shell">
      <NepmSidebar :active="active" @change="handleMenuChange" />

      <main id="main-content" class="main-content">
        <!-- 页面主标题与副标题 -->
        <div v-if="active !== 'feedbackDetail'" class="page-heading">
          <div class="heading-left">
            <h1 class="heading-title">{{ pageHeader.title }}</h1>
            <p class="heading-subtitle">{{ pageHeader.subtitle }}</p>
          </div>
          <button type="button" class="heading-logout-btn" @click="openLogoutConfirm">
            <el-icon class="btn-icon"><SwitchButton /></el-icon>
            <span>退出登录</span>
          </button>
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
          @open-dispatch="onOpenDispatch"
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

    <!-- 管理员退出系统二次确认弹窗 -->
    <Teleport to="body">
      <div v-if="confirmLogoutVisible" class="nepm-modal-backdrop" @click="closeLogoutConfirm">
        <div class="nepm-modal-box" role="dialog" aria-modal="true" @click.stop>
          <div class="nepm-modal-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#dc2626" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
          </div>
          <h3 class="nepm-modal-title">退出系统确认</h3>
          <p class="nepm-modal-desc">确定要退出系统管理端吗？退出后需重新登录。</p>
          <div class="nepm-modal-actions">
            <button type="button" class="btn-modal-cancel" @click="closeLogoutConfirm">取消</button>
            <button
              type="button"
              class="btn-modal-confirm"
              :disabled="loggingOut"
              @click="handleConfirmLogout"
            >
              {{ loggingOut ? '退出中…' : '确认退出' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
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
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.heading-logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 16px;
  background: #ffffff;
  border: 1.5px solid #bfdbfe;
  border-radius: 8px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(37, 99, 235, 0.08);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.heading-logout-btn .btn-icon {
  font-size: 16px;
}

.heading-logout-btn:hover {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #1d4ed8;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.15);
  transform: translateY(-1px);
}

.heading-logout-btn:active {
  transform: translateY(0);
}

/* 管理端退出弹窗 */
.nepm-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background-color: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  animation: modalFadeIn 0.2s ease-out;
}

.nepm-modal-box {
  width: 100%;
  max-width: 380px;
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  animation: modalScaleUp 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.nepm-modal-icon {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #fee2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.nepm-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 8px;
}

.nepm-modal-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px;
  line-height: 1.5;
}

.nepm-modal-actions {
  display: flex;
  gap: 12px;
}

.btn-modal-cancel,
.btn-modal-confirm {
  flex: 1;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-modal-cancel {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
}
.btn-modal-cancel:hover {
  background: #e2e8f0;
}

.btn-modal-confirm {
  background: #dc2626;
  border: 1px solid #dc2626;
  color: #ffffff;
}
.btn-modal-confirm:hover {
  background: #b91c1c;
}
.btn-modal-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes modalScaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@media (max-width: 900px) {
  .shell {
    flex-direction: column;
  }
}
</style>
