<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { logoutSession } from '@/api/session'

import NepmDispatchModal from '@/components/nepm/NepmDispatchModal.vue'
import NepmHeader from '@/components/nepm/NepmHeader.vue'
import NepmSidebar from '@/components/nepm/NepmSidebar.vue'

import NepmAnalyticsView from '@/views/nepm/NepmAnalyticsView.vue'
import NepmDashboardView from '@/views/nepm/NepmDashboardView.vue'
import NepmDetailView from '@/views/nepm/NepmDetailView.vue'
import NepmDispatchView from '@/views/nepm/NepmDispatchView.vue'
import NepmFeedbacksView from '@/views/nepm/NepmFeedbacksView.vue'
import NepmAqiAlertView from '@/views/nepm/NepmAqiAlertView.vue'
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
      return { title: '工作台', subtitle: '今日工作概览，快速查看待办任务、异常情况与近期趋势，高效处理公众反馈与任务调度。' }
    case 'feedbacks':
      return { title: '公众反馈列表', subtitle: '查看和处理公众提交的环境问题反馈，及时响应，推动问题解决。' }
    case 'feedbackDetail':
      return { title: '反馈详情', subtitle: '查看公众反馈的详细信息及处理进度' }
    case 'dispatch':
    case 'handle':
      return { title: '任务管理', subtitle: '对公众反馈任务进行全生命周期管理，支持任务查询、指派、处理、跟踪和完成。' }
    case 'results':
      return { title: '检测结果', subtitle: '查看网格员的污染物检测数据和最终AQI结果，支持多条件筛选与结果查询。' }
    case 'aqiAlert':
      return { title: 'AQI预警', subtitle: '集中监控并处置 4~6 级高等级空气质量超标预警，落实环境事件闭环处置。' }
    case 'timeout':
      return { title: '超时预警', subtitle: '实时监控公众反馈任务的超时情况，及时发出预警并督促处理，确保问题闭环。' }
    case 'analytics':
      return { title: '统计分析', subtitle: '对系统反馈数据进行多维度统计分析，支持区域对比、处理效率趋势和污染风险分析，为环境管理决策提供数据支持。' }
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
    <!-- 贯通式深海蓝侧边栏 -->
    <NepmSidebar :active="active" @change="handleMenuChange" />

    <!-- 右侧主体内容容器 -->
    <div class="layout-body">
      <NepmHeader @logout="openLogoutConfirm" />

      <main id="main-content" class="main-content">
        <!-- 页面主标题与水墨画卷通栏横幅 (无小框独立包装，大自然融入) -->
        <div v-if="active !== 'feedbackDetail'" class="page-heading">
          <div class="heading-left">
            <h1 class="heading-title">{{ pageHeader.title }}</h1>
            <p class="heading-subtitle">{{ pageHeader.subtitle }}</p>
          </div>
          <div v-if="active === 'dashboard' || active === 'analytics'" class="heading-art-wrap" aria-hidden="true">
            <img src="@/assets/header-mountain.png" alt="" class="heading-art-bg" />
            <div class="heading-art-slogan">
              <div class="slogan-line-1">绿水青山</div>
              <div class="slogan-line-2">就是金山银山 ——</div>
            </div>
          </div>
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
          :key="`dispatch-${contentRevision}`"
          @open-detail="openDetail"
          @open-dispatch="onOpenDispatch"
        />

        <NepmResultsView
          v-else-if="active === 'results'"
        />

        <NepmAqiAlertView
          v-else-if="active === 'aqiAlert'"
          :key="`aqiAlert-${contentRevision}`"
        />

        <NepmTimeoutView
          v-else-if="active === 'timeout'"
          :key="`timeout-${contentRevision}`"
          @open-detail="openDetail"
          @open-dispatch="onOpenDispatch"
          @navigate-to-aqi-alert="active = 'aqiAlert'"
        />

        <NepmAnalyticsView
          v-else-if="active === 'analytics'"
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
  height: 100vh;
  width: 100vw;
  background-color: #f1f5f9;
  color: #1f2937;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  display: flex;
  flex-direction: row;
  overflow: hidden;
}

.layout-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow-y: auto;
}

.main-content {
  flex: 1;
  min-width: 0;
  padding: 20px 28px 36px;
  max-width: 1720px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.page-heading {
  margin-bottom: 20px;
  position: relative;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 8px;
  overflow: hidden;
}

.heading-left {
  position: relative;
  z-index: 2;
  max-width: 60%;
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

/* 贯穿通栏的顶部水墨大画卷横幅 (无独立小盒子包装，大山水与背景自然融通) */
.heading-art-wrap {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 55%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 18px;
  pointer-events: none;
  z-index: 1;
}

.heading-art-bg {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: right center;
  opacity: 0.95;
  /* 柔和向左大跨度羽化消隐，无任何方块边缘线 */
  mask-image: linear-gradient(to right, transparent 0%, rgba(0, 0, 0, 0.35) 20%, rgba(0, 0, 0, 1) 50%);
  -webkit-mask-image: linear-gradient(to right, transparent 0%, rgba(0, 0, 0, 0.35) 20%, rgba(0, 0, 0, 1) 50%);
}

.heading-art-slogan {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
  text-align: right;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.slogan-line-1 {
  font-size: 13px;
  color: #334155;
  letter-spacing: 2px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
}

.slogan-line-2 {
  font-size: 14.5px;
  color: #1e293b;
  letter-spacing: 1.5px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
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
  .nepm-page {
    flex-direction: column;
    height: auto;
    overflow-y: auto;
  }
  .layout-body {
    height: auto;
    overflow-y: visible;
  }
}
</style>
