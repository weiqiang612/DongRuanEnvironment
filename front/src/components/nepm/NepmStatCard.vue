<script setup lang="ts">
import {
  CaretBottom,
  CaretTop,
  ChatDotRound,
  CircleCheckFilled,
  Document,
  DocumentChecked,
  Flag,
  Histogram,
  WarningFilled,
} from '@element-plus/icons-vue'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title: string
    value: string | number
    unit?: string
    change?: string
    tone?: 'blue' | 'amber' | 'red' | 'green' | 'red-light'
    isIncrease?: boolean
    subtext?: string
    iconType?: 'chat' | 'doc' | 'warn' | 'check' | 'flag' | 'bar' | 'bell'
  }>(),
  {
    unit: '条',
    tone: 'blue',
    isIncrease: true,
    subtext: '较昨日',
  }
)

const iconComponent = computed(() => {
  if (props.iconType === 'doc') return Document
  if (props.iconType === 'warn') return WarningFilled
  if (props.iconType === 'check') return CircleCheckFilled
  if (props.iconType === 'flag') return Flag
  if (props.iconType === 'bar') return Histogram
  if (props.tone === 'green') return CircleCheckFilled
  if (props.tone === 'red' || props.tone === 'red-light') return WarningFilled
  if (props.tone === 'amber') return DocumentChecked
  return ChatDotRound
})
</script>

<template>
  <div :class="['stat-card', `stat-card--${tone}`]">
    <div class="stat-icon-wrapper">
      <el-icon class="stat-icon"><component :is="iconComponent" /></el-icon>
    </div>
    <div class="stat-info">
      <span class="stat-title">{{ title }}</span>
      <div class="stat-num-row">
        <span class="stat-value">{{ value }}</span>
        <span v-if="unit" class="stat-unit">{{ unit }}</span>
      </div>
      <div v-if="change" class="stat-trend-row">
        <span class="stat-subtext">{{ subtext }}</span>
        <span :class="['stat-change', isIncrease ? 'stat-change--up' : 'stat-change--down']">
          {{ change }}
          <el-icon class="trend-caret">
            <component :is="isIncrease ? CaretTop : CaretBottom" />
          </el-icon>
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  transition: all 0.25s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(16, 47, 94, 0.06);
  border-color: #cbd5e1;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon {
  font-size: 24px;
}

/* 风格色系 */
.stat-card--blue .stat-icon-wrapper {
  background: #eff6ff;
  color: #1677ff;
}

.stat-card--amber .stat-icon-wrapper {
  background: #fef3c7;
  color: #d97706;
}

.stat-card--red .stat-icon-wrapper,
.stat-card--red-light .stat-icon-wrapper {
  background: #fee2e2;
  color: #ef4444;
}

.stat-card--green .stat-icon-wrapper {
  background: #ecfdf5;
  color: #10b981;
}

.stat-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-title {
  font-size: 13.5px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-num-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.15;
}

.stat-unit {
  font-size: 13px;
  color: #64748b;
}

.stat-trend-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
}

.stat-subtext {
  color: #94a3b8;
}

.stat-change {
  display: inline-flex;
  align-items: center;
  font-weight: 600;
  gap: 1px;
}

.stat-change--up {
  color: #ef4444;
}

.stat-change--down {
  color: #10b981;
}

.trend-caret {
  font-size: 13px;
}
</style>
