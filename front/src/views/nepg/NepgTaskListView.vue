<script setup lang="ts">
import type { AxiosError } from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNepgTasks, type NepgTask } from '@/api/nepg'
import NepgHeader from '@/components/nepg/NepgHeader.vue'

const router = useRouter()
const tasks = ref<NepgTask[]>([])
const loading = ref(true)
const errorMessage = ref('')
const activeTab = ref<'all' | 'assigned' | 'completed'>('all')

const assignedCount = computed(() => tasks.value.filter((item) => item.state === 1).length)
const completedCount = computed(() => tasks.value.filter((item) => item.state === 2).length)
const timeoutCount = computed(() => tasks.value.filter((item) => item.timeoutFlag).length)

const visibleTasks = computed(() =>
  tasks.value.filter((item) =>
    activeTab.value === 'all'
      ? true
      : activeTab.value === 'assigned'
        ? item.state === 1
        : item.state === 2,
  ),
)

const formatTime = (value: string | null) => (value ? value.replace('T', ' ').slice(0, 16) : '—')

async function loadTasks() {
  loading.value = true
  errorMessage.value = ''
  try {
    tasks.value = (await getNepgTasks()).data.data
  } catch (error) {
    errorMessage.value =
      (error as AxiosError<{ message?: string }>).response?.data?.message ||
      '任务列表加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function handleCardAction(task: NepgTask) {
  router.push(`/nepg/tasks/${task.afId}`)
}

onMounted(loadTasks)
</script>

<template>
  <div class="nepg-page">
    <NepgHeader />

    <main id="main-content" class="content">
      <div class="title-row">
        <h1 class="page-title">我的任务</h1>
        <span class="count-badge">当前列表共 {{ tasks.length }} 条</span>
      </div>

      <!-- 统计卡片 -->
      <section class="summary-card" aria-label="任务统计">
        <div class="summary-item">
          <span class="summary-num primary">{{ assignedCount }}</span>
          <span class="summary-label">待处理</span>
        </div>
        <div class="divider"></div>
        <div class="summary-item">
          <span class="summary-num success">{{ completedCount }}</span>
          <span class="summary-label">已完成</span>
        </div>
        <div class="divider"></div>
        <div class="summary-item">
          <span class="summary-num danger">{{ timeoutCount }}</span>
          <span class="summary-label">超时</span>
        </div>
      </section>

      <!-- 选项卡切换 -->
      <nav class="tabs-nav" aria-label="任务筛选">
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'all' }"
          @click="activeTab = 'all'"
        >
          <span>全部 ({{ tasks.length }})</span>
          <i v-if="activeTab === 'all'" class="active-indicator"></i>
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'assigned' }"
          @click="activeTab = 'assigned'"
        >
          <span>已指派 ({{ assignedCount }})</span>
          <i v-if="activeTab === 'assigned'" class="active-indicator"></i>
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === 'completed' }"
          @click="activeTab = 'completed'"
        >
          <span>已完成 ({{ completedCount }})</span>
          <i v-if="activeTab === 'completed'" class="active-indicator"></i>
        </button>
      </nav>

      <!-- 列表状态反馈 -->
      <div v-if="loading" class="state-feedback" role="status">
        <div class="spinner"></div>
        <span>正在加载任务…</span>
      </div>
      <div v-else-if="errorMessage" class="state-feedback error" role="alert">
        <span>{{ errorMessage }}</span>
        <button type="button" class="retry-btn" @click="loadTasks">重新加载</button>
      </div>
      <div v-else-if="visibleTasks.length === 0" class="state-feedback empty">
        <p>当前筛选下暂无任务</p>
      </div>

      <!-- 任务列表 -->
      <section v-else class="task-list" aria-label="任务列表">
        <article
          v-for="task in visibleTasks"
          :key="task.afId"
          class="task-card"
        >
          <div class="card-header">
            <div class="address-title">
              <svg class="location-pin" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
              </svg>
              <h2>{{ task.address }}</h2>
            </div>
            <span v-if="task.timeoutFlag" class="timeout-badge">已超时 2小时</span>
          </div>

          <p class="task-desc">{{ task.information }}</p>

          <div class="card-footer">
            <div class="meta-group">
              <div class="meta-col">
                <span class="meta-label">派发时间</span>
                <span class="meta-val">{{ formatTime(task.assignedAt) }}</span>
              </div>

              <div class="v-divider"></div>

              <div class="meta-col status-col">
                <span class="meta-label">状态</span>
                <span class="status-val" :class="task.state === 2 ? 'complete' : 'assigned'">
                  {{ task.state === 2 ? '已完成' : '已指派' }}
                </span>
              </div>
            </div>

            <div class="action-wrap">
              <button
                type="button"
                :class="task.state === 1 ? 'btn-primary' : 'btn-outline'"
                @click="handleCardAction(task)"
              >
                {{ task.state === 1 ? '查看并实测' : '查看详情' }}
              </button>
            </div>
          </div>
        </article>
      </section>
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

.content {
  width: min(520px, calc(100% - 32px));
  margin: 0 auto;
  padding: 20px 0 48px;
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.2px;
}

.count-badge {
  font-size: 13px;
  color: #8c9ba8;
}

/* 统计卡片 */
.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #ffffff;
  border-radius: var(--nepg-radius);
  box-shadow: var(--nepg-shadow);
  padding: 18px 12px;
  margin-bottom: 22px;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-num {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.summary-num.primary {
  color: #1677ff;
}

.summary-num.success {
  color: #16a34a;
}

.summary-num.danger {
  color: #dc2626;
}

.summary-label {
  font-size: 13px;
  color: #64748b;
}

.divider {
  width: 1px;
  height: 32px;
  background-color: #edf2f7;
}

/* Tab 切换 */
.tabs-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding: 0 4px;
  position: relative;
}

.tab-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  padding: 8px 0 10px;
  background: transparent;
  border: 0;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  color: #64748b;
  transition: color 0.15s;
}

.tab-btn.active {
  color: #1677ff;
  font-weight: 700;
}

.active-indicator {
  position: absolute;
  bottom: 0;
  width: 38px;
  height: 3px;
  background-color: #1677ff;
  border-radius: 2px;
}

/* 状态展示 */
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
}

.retry-btn {
  margin-left: 12px;
  padding: 4px 12px;
  color: #1677ff;
  background: transparent;
  border: 1px solid #1677ff;
  border-radius: 4px;
  cursor: pointer;
}

/* 任务列表与卡片 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.task-card {
  background: #ffffff;
  border-radius: var(--nepg-radius);
  box-shadow: var(--nepg-shadow);
  padding: 18px 20px;
  transition: transform 0.15s, box-shadow 0.15s;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.address-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.location-pin {
  color: #4b5563;
  flex-shrink: 0;
}

.address-title h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeout-badge {
  flex-shrink: 0;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 500;
  color: #dc2626;
  background: #fef2f2;
  border-radius: 6px;
  line-height: 1.2;
}

.task-desc {
  margin: 10px 0 16px 26px;
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid #f8fafc;
}

.meta-group {
  display: flex;
  align-items: center;
  gap: 18px;
}

.meta-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: #8c9ba8;
}

.meta-val {
  font-size: 13.5px;
  color: #334155;
  font-weight: 500;
}

.v-divider {
  width: 1px;
  height: 28px;
  background-color: #edf2f7;
}

.status-val {
  font-size: 14px;
  font-weight: 600;
}

.status-val.assigned {
  color: #1677ff;
}

.status-val.complete {
  color: #16a34a;
}

.action-wrap {
  margin-left: auto;
}

.btn-primary {
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  background: #1677ff;
  border: 0;
  border-radius: var(--nepg-radius-sm);
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-primary:hover {
  background: #0958d9;
}

.btn-outline {
  padding: 7px 18px;
  font-size: 14px;
  font-weight: 600;
  color: #1677ff;
  background: #ffffff;
  border: 1px solid #1677ff;
  border-radius: var(--nepg-radius-sm);
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-outline:hover {
  background: #eff6ff;
}
</style>

