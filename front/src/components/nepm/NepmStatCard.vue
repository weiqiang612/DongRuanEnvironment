<script setup lang="ts">
import {
  ChatDotRound,
  CircleCheckFilled,
  Document,
  Histogram,
  Setting,
  TopRight,
  WarningFilled,
} from '@element-plus/icons-vue'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title: string
    value: string | number
    unit?: string
    change?: string
    tone?: 'blue' | 'amber' | 'red' | 'green' | 'coral' | 'red-light'
    isIncrease?: boolean | null
    subtext?: string
    iconType?: 'chat' | 'doc' | 'warn' | 'check' | 'setting' | 'bar'
  }>(),
  {
    unit: '',
    tone: 'blue',
    isIncrease: true,
    subtext: '较昨日',
  }
)

const iconComponent = computed(() => {
  if (props.iconType === 'doc') return Document
  if (props.iconType === 'warn') return WarningFilled
  if (props.iconType === 'check') return CircleCheckFilled
  if (props.iconType === 'setting') return Setting
  if (props.iconType === 'bar') return Histogram
  if (props.tone === 'green') return CircleCheckFilled
  if (props.tone === 'red') return WarningFilled
  if (props.tone === 'coral') return Document
  if (props.tone === 'blue') return Setting
  return ChatDotRound
})
</script>

<template>
  <div :class="['stat-card', `stat-card--${tone}`]">
    <!-- 左侧圆角图标 -->
    <div class="stat-icon-wrapper">
      <el-icon class="stat-icon"><component :is="iconComponent" /></el-icon>
    </div>

    <!-- 中间指标主数值 -->
    <div class="stat-main">
      <span class="stat-title">{{ title }}</span>
      <div class="stat-num-row">
        <span class="stat-value">{{ value }}</span>
        <span v-if="unit" class="stat-unit">{{ unit }}</span>
      </div>
    </div>

    <!-- 右侧环比走势 -->
    <div v-if="change !== undefined" class="stat-trend">
      <span class="trend-subtext">{{ subtext }}</span>
      <div
        :class="[
          'trend-value',
          isIncrease === true ? 'trend-value--up' : isIncrease === false ? 'trend-value--down' : 'trend-value--neutral'
        ]"
      >
        <span>{{ change }}</span>
        <el-icon v-if="isIncrease === true" class="trend-arrow"><TopRight /></el-icon>
        <span v-else-if="isIncrease === null" class="trend-dash">—</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
}

.stat-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 14px;
}

.stat-icon {
  font-size: 22px;
}

.stat-main {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.stat-title {
  font-size: 13.5px;
  color: #475569;
  font-weight: 500;
  margin-bottom: 2px;
}

.stat-num-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.5px;
}

.stat-unit {
  font-size: 12px;
  color: #64748b;
}

.stat-trend {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
  margin-left: 12px;
}

.trend-subtext {
  font-size: 11.5px;
  color: #94a3b8;
}

.trend-value {
  font-size: 13.5px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
}

.trend-arrow {
  font-size: 13px;
  stroke-width: 2;
}

.trend-dash {
  font-size: 14px;
  color: #94a3b8;
  margin-left: 2px;
}

.trend-value--up {
  color: #ef4444;
}

.trend-value--down {
  color: #10b981;
}

.trend-value--neutral {
  color: #64748b;
}

/* 4大主题淡雅微渐变风格 */
/* 珊瑚粉红：待指派 */
.stat-card--coral {
  background: linear-gradient(135deg, #fff6f6 0%, #fffafa 100%);
  border-color: #fee2e2;
}
.stat-card--coral .stat-icon-wrapper {
  background: #fee2e2;
  color: #ef4444;
}
.stat-card--coral .stat-value {
  color: #e11d48;
}

/* 科技天蓝：处理中 */
.stat-card--blue {
  background: linear-gradient(135deg, #f0f7ff 0%, #f9fbff 100%);
  border-color: #dbeafe;
}
.stat-card--blue .stat-icon-wrapper {
  background: #dbeafe;
  color: #1890ff;
}
.stat-card--blue .stat-value {
  color: #0284c7;
}

/* 警戒浅红：超时任务 */
.stat-card--red,
.stat-card--red-light {
  background: linear-gradient(135deg, #fff3f3 0%, #fff8f8 100%);
  border-color: #fecaca;
}
.stat-card--red .stat-icon-wrapper,
.stat-card--red-light .stat-icon-wrapper {
  background: #fecaca;
  color: #dc2626;
}
.stat-card--red .stat-value,
.stat-card--red-light .stat-value {
  color: #dc2626;
}

/* 暖黄琥珀：处理中备用 */
.stat-card--amber {
  background: linear-gradient(135deg, #fffdf0 0%, #fffef8 100%);
  border-color: #fef08a;
}
.stat-card--amber .stat-icon-wrapper {
  background: #fef3c7;
  color: #d97706;
}
.stat-card--amber .stat-value {
  color: #d97706;
}

/* 生态翠绿：今日完成 */
.stat-card--green {
  background: linear-gradient(135deg, #f0fdf4 0%, #f8fef9 100%);
  border-color: #bbf7d0;
}
.stat-card--green .stat-icon-wrapper {
  background: #dcfce7;
  color: #16a34a;
}
.stat-card--green .stat-value {
  color: #16a34a;
}
.stat-card--green .trend-value--up {
  color: #16a34a;
}
</style>
