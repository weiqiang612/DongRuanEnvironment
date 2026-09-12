<script setup lang="ts">
import type { AxiosError } from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNepgTask, type NepgTaskDetail } from '@/api/nepg'
import NepgHeader from '@/components/nepg/NepgHeader.vue'

const props = defineProps<{ afId: string }>()
const router = useRouter()

const task = ref<NepgTaskDetail | null>(null)
const loading = ref(true)
const errorMessage = ref('')

const formatTime = (value: string | null) => (value ? value.replace('T', ' ').slice(0, 16) : '—')

const gradeName = (grade: number | null) =>
  ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'][(grade || 0) - 1] || '—'

const gradeBadgeClass = (grade: number | null) => {
  if (!grade) return 'grade-good'
  if (grade <= 2) return 'grade-good'
  if (grade === 3) return 'grade-moderate'
  if (grade === 4) return 'grade-orange'
  return 'grade-heavy'
}

async function loadTaskResult() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await getNepgTask(props.afId)
    task.value = res.data.data
  } catch (error) {
    errorMessage.value =
      (error as AxiosError<{ message?: string }>).response?.data?.message ||
      '获取任务结果失败，请返回任务列表'
  } finally {
    loading.value = false
  }
}

function goToTaskList() {
  router.push('/nepg/tasks')
}

function goToTaskResult() {
  router.push(`/nepg/tasks/${props.afId}`)
}

onMounted(() => {
  if (history.state && history.state.taskData && typeof history.state.taskData === 'object') {
    task.value = history.state.taskData as NepgTaskDetail
    loading.value = false
  } else {
    loadTaskResult()
  }
})
</script>

<template>
  <div class="nepg-page">
    <NepgHeader />

    <!-- 二级导航 -->
    <div class="sub-nav">
      <button type="button" class="back-btn" aria-label="返回" @click="goToTaskList">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>
      <h2 class="sub-title">提交成功</h2>
      <div class="nav-placeholder"></div>
    </div>

    <main class="content">
      <div v-if="loading" class="state-feedback" role="status">
        <div class="spinner"></div>
        <span>正在加载提交结果…</span>
      </div>

      <div v-else-if="errorMessage" class="state-feedback error" role="alert">
        <p>{{ errorMessage }}</p>
        <button type="button" class="btn-primary" @click="goToTaskList">返回任务列表</button>
      </div>

      <template v-else-if="task">
        <!-- 成功状态图标与文案 -->
        <section class="success-hero">
          <div class="success-icon-halo">
            <div class="success-icon-circle">
              <svg viewBox="0 0 24 24" width="34" height="34" fill="none" stroke="#ffffff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
          </div>
          <h1 class="hero-title">提交成功</h1>
          <p class="hero-sub">检测数据已提交，系统已完成任务处理。</p>
        </section>

        <!-- 详细信息卡片 -->
        <section class="result-card" aria-label="处理结果摘要">
          <div class="info-row">
            <span class="info-label">地址</span>
            <span class="info-val address-text">{{ task.address }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">最终AQI</span>
            <span class="info-val aqi-val">{{ task.finalGrade ?? '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">污染级别</span>
            <div class="info-val">
              <span class="pollution-badge" :class="gradeBadgeClass(task.finalGrade)">
                {{ task.finalGradeName || gradeName(task.finalGrade) }}
              </span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">预警状态</span>
            <span class="info-val">
              {{ task.alertGenerated ? '已生成预警 (AQI≥4)' : '未生成预警' }}
            </span>
          </div>
          <div class="info-row">
            <span class="info-label">检测时间</span>
            <span class="info-val">{{ formatTime(task.detectedAt || task.completedAt) }}</span>
          </div>
        </section>

        <!-- 操作按钮组 -->
        <section class="action-buttons">
          <button type="button" class="btn-primary" @click="goToTaskList">
            返回任务列表
          </button>
          <button type="button" class="btn-secondary" @click="goToTaskResult">
            查看任务结果
          </button>
        </section>
      </template>
    </main>
  </div>
</template>

<style scoped>
.nepg-page {
  min-height: 100vh;
  background-color: var(--nepg-page);
  color: var(--nepg-text);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 二级导航 */
.sub-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #f0f3f7;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  color: #1e293b;
  background: transparent;
  border: 0;
  cursor: pointer;
  border-radius: 6px;
  margin-left: -6px;
}

.sub-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
  text-align: center;
}

.nav-placeholder {
  width: 28px;
}

.content {
  width: min(520px, calc(100% - 32px));
  margin: 0 auto;
  padding: 14px 0 32px;
}

/* 成功状态 */
.success-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0 18px;
}

.success-icon-halo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92px;
  height: 92px;
  background: #e6f9f0;
  border-radius: 50%;
  animation: scaleIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.success-icon-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.28);
}

@keyframes scaleIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.hero-title {
  margin: 14px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.hero-sub {
  margin: 6px 0 0;
  font-size: 13.5px;
  color: #64748b;
  text-align: center;
}

/* 结果卡片 */
.result-card {
  background: #ffffff;
  border-radius: var(--nepg-radius);
  box-shadow: var(--nepg-shadow);
  padding: 2px 18px;
  margin-bottom: 22px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 0;
  font-size: 14px;
  border-bottom: 1px solid #f1f5f9;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #64748b;
  flex-shrink: 0;
  width: 80px;
}

.info-val {
  color: #1e293b;
  font-weight: 500;
  text-align: right;
  flex: 1;
  word-break: break-word;
}

.address-text {
  font-weight: 600;
}

.aqi-val {
  font-weight: 600;
}

.pollution-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
  line-height: 1.3;
}

.grade-good {
  color: #16a34a;
  background: #dcfce7;
}

.grade-moderate {
  color: #ca8a04;
  background: #fef9c3;
}

.grade-orange {
  color: #ea580c;
  background: #ffedd5;
}

.grade-heavy {
  color: #dc2626;
  background: #fee2e2;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-primary {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  background: #1677ff;
  border: 0;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-primary:hover {
  background: #0958d9;
}

.btn-secondary {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  color: #1677ff;
  background: #ffffff;
  border: 1px solid #1677ff;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-secondary:hover {
  background: #eff6ff;
}

.state-feedback {
  background: #ffffff;
  border-radius: var(--nepg-radius);
  box-shadow: var(--nepg-shadow);
  padding: 48px 24px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.state-feedback.error {
  color: #dc2626;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}
</style>
