<script setup lang="ts">
import type { AxiosError } from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNepgTask, submitMeasurement, type NepgTaskDetail } from '@/api/nepg'
import NepgHeader from '@/components/nepg/NepgHeader.vue'

const props = defineProps<{ afId: string }>()
const route = useRoute()
const router = useRouter()

const task = ref<NepgTaskDetail | null>(null)
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref('')
const form = ref({ so2Value: '', coValue: '', spmValue: '' })

// 模式：当已指派时，支持 'detail'（任务详情）或 'measure'（现场实测）；已完成时显示 'result'
const isMeasuring = ref(false)

const assigned = computed(() => task.value?.state === 1)
const formatTime = (value: string | null) => (value ? value.replace('T', ' ').slice(0, 16) : '—')

const gradeName = (grade: number | null) =>
  ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'][(grade || 0) - 1] || '—'

const gradeBadgeClass = (grade: number | null) => {
  if (!grade) return 'aqi-grade-1'
  return `aqi-grade-${grade}`
}

async function loadTask() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await getNepgTask(props.afId)
    task.value = res.data.data
    // 如果 url 带了 query 参数或者从某些入口来可以开启
    if (route.query.measure === 'true' && task.value.state === 1) {
      isMeasuring.value = true
    }
  } catch (error) {
    errorMessage.value =
      (error as AxiosError<{ message?: string }>).response?.data?.message ||
      '任务详情加载失败，请返回任务列表后重试'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  if (isMeasuring.value) {
    isMeasuring.value = false
  } else {
    router.push('/nepg/tasks')
  }
}

async function submit() {
  const values = [form.value.so2Value, form.value.coValue, form.value.spmValue].map(Number)
  if (values.some((value) => !Number.isFinite(value) || value < 0)) {
    errorMessage.value = '请填写三项非负实测数据'
    return
  }

  submitting.value = true
  errorMessage.value = ''
  try {
    const res = await submitMeasurement(props.afId, {
      so2Value: values[0]!,
      coValue: values[1]!,
      spmValue: values[2]!,
    })
    // 提交成功，直接跳转到专门的提交成功页面，携带最新数据
    await router.push({
      name: 'nepgTaskSuccess',
      params: { afId: props.afId },
      state: { taskData: JSON.parse(JSON.stringify(res.data.data)) },
    })
  } catch (error) {
    errorMessage.value =
      (error as AxiosError<{ message?: string }>).response?.data?.message ||
      '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(loadTask)
</script>

<template>
  <div class="nepg-page">
    <NepgHeader />

    <!-- 二级导航栏 -->
    <div class="sub-nav">
      <button type="button" class="back-btn" aria-label="返回" @click="handleBack">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>
      <h2 class="sub-title">
        <template v-if="task">
          {{ assigned ? (isMeasuring ? '现场实测' : '任务详情') : '任务结果' }}
        </template>
        <template v-else>任务</template>
      </h2>
      <div class="nav-placeholder"></div>
    </div>

    <main class="content">
      <div v-if="loading" class="state-feedback" role="status">
        <div class="spinner"></div>
        <span>正在加载任务详情…</span>
      </div>

      <div v-else-if="errorMessage && !task" class="state-feedback error" role="alert">
        <p>{{ errorMessage }}</p>
        <button type="button" class="btn-primary" @click="loadTask">重新加载</button>
      </div>

      <template v-else-if="task">
        <!-- ==================== 视图 1：任务详情（已指派，未进入实测）参考图 2 ==================== -->
        <template v-if="assigned && !isMeasuring">
          <section class="card detail-card">
            <div class="card-top">
              <div class="address-title">
                <svg class="location-pin" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
                </svg>
                <h2>{{ task.address }}</h2>
              </div>
              <span v-if="task.timeoutFlag" class="timeout-badge">已超时 2小时</span>
            </div>

            <div class="detail-list">
              <div class="detail-row">
                <span class="label">反馈编号</span>
                <span class="val">{{ task.afId }}</span>
              </div>
              <div class="detail-row">
                <span class="label">所属地区</span>
                <span class="val">{{ task.provinceName }} / {{ task.cityName }}</span>
              </div>
              <div class="detail-row">
                <span class="label">详细地址</span>
                <span class="val">{{ task.address }}</span>
              </div>
              <div class="detail-row">
                <span class="label">问题描述</span>
                <span class="val desc-val">{{ task.information }}</span>
              </div>
              <div class="detail-row">
                <span class="label">公众预估等级</span>
                <div class="val grade-row">
                  <span :class="['aqi-pill', `aqi-grade-${task.estimatedGrade}`]">{{ gradeName(task.estimatedGrade) }}（{{ task.estimatedGrade }}级）</span>
                </div>
              </div>
              <div class="detail-row">
                <span class="label">派发时间</span>
                <span class="val">{{ formatTime(task.assignedAt) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">当前状态</span>
                <span class="val status-blue">已指派</span>
              </div>
            </div>
          </section>

          <!-- 提示横幅 -->
          <div class="info-banner blue-banner">
            <svg class="info-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
            </svg>
            <p>网格员仅提交 SO₂、CO、PM2.5 实测数据；最终等级、检测时间和预警结果由系统生成。</p>
          </div>

          <!-- 底部按钮 -->
          <div class="bottom-action">
            <button type="button" class="btn-primary" @click="isMeasuring = true">
              开始实测
            </button>
          </div>
        </template>

        <!-- ==================== 视图 2：现场实测（填写数据）参考图 3 ==================== -->
        <template v-else-if="assigned && isMeasuring">
          <!-- 任务摘要卡片 -->
          <section class="card mini-card">
            <div class="card-top">
              <div class="address-title">
                <svg class="location-pin" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
                </svg>
                <h2>{{ task.address }}</h2>
              </div>
            </div>

            <div class="mini-meta">
              <div class="mini-row">
                <span class="label">反馈编号</span>
                <span class="val">{{ task.afId }}</span>
              </div>
              <div class="mini-row">
                <span class="label">公众预估等级</span>
                <div class="val grade-row">
                  <span :class="['aqi-pill', `aqi-grade-${task.estimatedGrade}`]">{{ gradeName(task.estimatedGrade) }}（{{ task.estimatedGrade }}级）</span>
                </div>
              </div>
            </div>
          </section>

          <!-- 填写表单卡片 -->
          <section class="card form-card">
            <h3 class="form-title">填写现场实测数据</h3>

            <form @submit.prevent="submit">
              <div class="form-group">
                <label for="so2Input">SO₂（μg/m³）</label>
                <input
                  id="so2Input"
                  v-model="form.so2Value"
                  type="number"
                  min="0"
                  step="0.01"
                  inputmode="decimal"
                  placeholder="请输入 SO₂ 实测浓度"
                  required
                />
                <span class="field-hint">请输入非负数值，系统将按等级区间校验</span>
              </div>

              <div class="form-group">
                <label for="coInput">CO（mg/m³）</label>
                <input
                  id="coInput"
                  v-model="form.coValue"
                  type="number"
                  min="0"
                  step="0.01"
                  inputmode="decimal"
                  placeholder="请输入 CO 实测浓度"
                  required
                />
                <span class="field-hint">请输入非负数值，系统将按等级区间校验</span>
              </div>

              <div class="form-group">
                <label for="spmInput">PM2.5（μg/m³）</label>
                <input
                  id="spmInput"
                  v-model="form.spmValue"
                  type="number"
                  min="0"
                  step="0.01"
                  inputmode="decimal"
                  placeholder="请输入 PM2.5 实测浓度"
                  required
                />
                <span class="field-hint">请输入非负数值，系统将按等级区间校验</span>
              </div>

              <!-- 橙色提示框 -->
              <div class="form-alert-orange">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
                </svg>
                <span>提交后系统将计算最终等级。</span>
              </div>

              <p v-if="errorMessage" class="error-msg" role="alert">{{ errorMessage }}</p>
            </form>
          </section>

          <!-- 提示横幅 -->
          <div class="info-banner blue-banner">
            <svg class="info-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
            </svg>
            <p>提交后不可修改，系统将生成检测时间和最终等级</p>
          </div>

          <!-- 底部按钮 -->
          <div class="bottom-action">
            <button
              type="button"
              class="btn-primary"
              :disabled="submitting"
              @click="submit"
            >
              {{ submitting ? '提交中…' : '确认并提交' }}
            </button>
          </div>
        </template>

        <!-- ==================== 视图 3：任务结果（已完成）参考图 4 ==================== -->
        <template v-else>
          <!-- 任务基本信息 -->
          <section class="card result-top-card">
            <div class="card-top">
              <div class="address-title">
                <svg class="location-pin" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
                </svg>
                <h2>{{ task.address }}</h2>
              </div>
              <span class="status-badge-complete">已完成</span>
            </div>
            <p class="task-desc">{{ task.information }}</p>

            <div class="detail-row af-code-row">
              <span class="label">任务编号</span>
              <span class="val">{{ task.afId }}</span>
            </div>
          </section>

          <!-- 系统最终等级卡片 -->
          <section class="card grade-card">
            <h3 class="section-title">系统最终等级</h3>
            <div class="grade-hero-row">
              <div class="big-num-wrap">
                <span class="big-num">{{ task.finalGrade ?? '—' }}</span>
                <span class="big-unit">级</span>
              </div>
              <div class="v-line"></div>
              <div class="badge-wrap">
                <span class="big-grade-badge" :class="gradeBadgeClass(task.finalGrade)">
                  {{ task.finalGradeName || gradeName(task.finalGrade) }}
                </span>
              </div>
            </div>

            <div class="alert-status-box">
              <span class="dot" :class="task.alertGenerated ? 'dot-red' : 'dot-gray'"></span>
              <span>
                {{ task.alertGenerated ? '已生成预警（最终等级达到4级及以上）' : '未生成预警' }}
              </span>
            </div>
            <p class="calc-note">最终等级取三项污染物中最高等级，由系统自动计算。</p>
          </section>

          <!-- 检测数据卡片 -->
          <section class="card measurement-card">
            <h3 class="section-title">检测数据</h3>

            <div class="measure-list">
              <div class="measure-row">
                <span class="m-label">SO₂（μg/m³）</span>
                <span class="m-val">{{ task.so2Value ?? '—' }}</span>
                <span class="m-badge" :class="gradeBadgeClass(task.so2Level)">
                  {{ gradeName(task.so2Level) }}
                </span>
              </div>

              <div class="measure-row">
                <span class="m-label">CO（mg/m³）</span>
                <span class="m-val">{{ task.coValue ?? '—' }}</span>
                <span class="m-badge" :class="gradeBadgeClass(task.coLevel)">
                  {{ gradeName(task.coLevel) }}
                </span>
              </div>

              <div class="measure-row">
                <span class="m-label">PM2.5（μg/m³）</span>
                <span class="m-val">{{ task.spmValue ?? '—' }}</span>
                <span class="m-badge" :class="gradeBadgeClass(task.spmLevel)">
                  {{ gradeName(task.spmLevel) }}
                </span>
              </div>

              <div class="measure-time-row">
                <span class="time-label">检测时间（系统生成）</span>
                <span class="time-val">{{ formatTime(task.detectedAt || task.completedAt) }}</span>
              </div>
            </div>
          </section>

          <!-- 底部返回按钮 -->
          <div class="bottom-action">
            <button type="button" class="btn-primary" @click="router.push('/nepg/tasks')">
              返回任务列表
            </button>
          </div>
        </template>
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
  padding: 16px 0 48px;
}

/* 通用卡片 */
.card {
  background: #ffffff;
  border-radius: var(--nepg-radius);
  box-shadow: var(--nepg-shadow);
  padding: 18px 20px;
  margin-bottom: 14px;
}

.card-top {
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
  font-size: 18px;
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

.status-badge-complete {
  flex-shrink: 0;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #16a34a;
  background: #dcfce7;
  border-radius: 6px;
}

/* 详情列表 */
.detail-list {
  margin-top: 14px;
  border-top: 1px solid #f1f5f9;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 13px 0;
  font-size: 14px;
  border-bottom: 1px solid #f8fafc;
}

.detail-row:last-child {
  border-bottom: none;
}

.label {
  color: #64748b;
  flex-shrink: 0;
  width: 95px;
}

.val {
  color: #1e293b;
  font-weight: 500;
  text-align: right;
  flex: 1;
}

.desc-val {
  text-align: left;
  line-height: 1.5;
  color: #334155;
}

.status-blue {
  color: #1677ff;
  font-weight: 600;
}

.grade-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}


/* 提示横幅 */
.info-banner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  margin-bottom: 18px;
}

.blue-banner {
  background-color: #eaf4fe;
}

.blue-banner .info-icon {
  color: #1677ff;
  flex-shrink: 0;
  margin-top: 1px;
}

.blue-banner p {
  margin: 0;
  font-size: 13px;
  color: #1e40af;
  line-height: 1.5;
}

/* 紧凑卡片 (实测前) */
.mini-meta {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.mini-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13.5px;
}

/* 实测表单 */
.form-title {
  margin: 0 0 16px;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-group label {
  font-size: 14.5px;
  font-weight: 600;
  color: #1e293b;
}

.form-group input {
  height: 46px;
  padding: 0 14px;
  font-size: 16px;
  color: #111827;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.15);
}

.field-hint {
  font-size: 12px;
  color: #8c9ba8;
}

.form-alert-orange {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff7ed;
  border-radius: 8px;
  padding: 10px 12px;
  color: #ea580c;
  font-size: 13px;
  margin-top: 8px;
}

.error-msg {
  color: #dc2626;
  font-size: 13px;
  margin-top: 10px;
}

/* 任务结果部分 */
.task-desc {
  margin: 10px 0 14px 28px;
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.5;
}

.af-code-row {
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.grade-hero-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 8px 0 18px;
}

.big-num-wrap {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.big-num {
  font-size: 46px;
  font-weight: 800;
  color: #111827;
  line-height: 1;
}

.big-unit {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.v-line {
  width: 1px;
  height: 48px;
  background-color: #edf2f7;
}

.big-grade-badge {
  display: inline-block;
  padding: 8px 18px;
  font-size: 18px;
  font-weight: 700;
  border-radius: 8px;
}

.alert-status-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13.5px;
  color: #334155;
  margin-bottom: 12px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-red {
  background-color: #dc2626;
}

.dot-gray {
  background-color: #94a3b8;
}

.calc-note {
  margin: 0;
  font-size: 12.5px;
  color: #8c9ba8;
}

/* 检测数据列表 */
.measure-list {
  display: flex;
  flex-direction: column;
}

.measure-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f8fafc;
}

.m-label {
  font-size: 14px;
  color: #334155;
  width: 130px;
}

.m-val {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  flex: 1;
}

.m-badge {
  padding: 3px 12px;
  font-size: 12.5px;
  font-weight: 600;
  border-radius: 6px;
}

.measure-time-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  font-size: 13px;
}

.time-label {
  color: #64748b;
}

.time-val {
  color: #334155;
  font-weight: 500;
}

/* 底部操作 */
.bottom-action {
  margin-top: 20px;
}

.btn-primary {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  background: #1677ff;
  border: 0;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-primary:hover:not(:disabled) {
  background: #0958d9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

