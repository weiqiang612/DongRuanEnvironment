<script setup lang="ts">
import { Opportunity } from '@element-plus/icons-vue'

const alertAdviceList = [
  { num: 1, title: '优先处理超时任务', desc: '优先调度超时反馈，避免持续积压。', color: 'red' },
  { num: 2, title: '及时联系责任人', desc: '确认当前网格员处理进展，必要时执行重派。', color: 'orange' },
  { num: 3, title: '保留调度记录', desc: '通过系统完成指派或继续处理，确保过程可追溯。', color: 'blue' },
  { num: 4, title: '关注高发区域', desc: '结合真实反馈分布，识别需要优先协调的区域。', color: 'green' },
]
</script>

<template>
  <div class="alert-tips-card">
    <div class="card-header">
      <div class="header-left">
        <el-icon class="header-icon"><Opportunity /></el-icon>
        <h3 class="header-title">处理建议</h3>
      </div>
      <a href="javascript:void(0)" class="header-more">更多 &gt;</a>
    </div>

    <div class="tips-list">
      <div v-for="tip in alertAdviceList" :key="tip.num" class="tip-item">
        <span :class="['num-circle', `num-circle--${tip.color}`]">{{ tip.num }}</span>
        <div class="tip-content">
          <strong class="tip-title">{{ tip.title }}</strong>
          <p class="tip-desc">{{ tip.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.alert-tips-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: #1677ff;
}

.header-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.header-more {
  font-size: 12px;
  color: #1677ff;
  text-decoration: none;
}

.header-more:hover {
  text-decoration: underline;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.num-circle {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 1px;
}

.num-circle--red {
  background: #ef4444;
}

.num-circle--orange {
  background: #f97316;
}

.num-circle--blue {
  background: #1677ff;
}

.num-circle--green {
  background: #10b981;
}

.tip-content {
  flex: 1;
}

.tip-title {
  display: block;
  font-size: 14px;
  color: #1f2937;
  font-weight: 600;
  margin-bottom: 3px;
}

.tip-desc {
  margin: 0;
  font-size: 12.5px;
  color: #64748b;
  line-height: 1.55;
}
</style>
