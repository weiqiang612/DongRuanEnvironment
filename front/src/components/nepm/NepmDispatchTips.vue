<script setup lang="ts">
import { Document, InfoFilled } from '@element-plus/icons-vue'

const dispatchTipsList = [
  { num: 1, title: '及时分派', desc: '收到公众反馈后尽快完成任务分派。', color: 'blue' },
  { num: 2, title: '超时任务优先', desc: '优先关注已超时反馈并及时处理。', color: 'amber' },
  { num: 3, title: '合理分配', desc: '候选网格员按同城优先、同省兜底规则提供。', color: 'green' },
  { num: 4, title: '跟踪处理', desc: '通过任务列表持续关注指派后的处理进度。', color: 'navy' },
]
</script>

<template>
  <div class="dispatch-tips-card">
    <div class="card-header">
      <el-icon class="header-icon"><Document /></el-icon>
      <h3 class="header-title">分派说明</h3>
    </div>

    <div class="tips-list">
      <div v-for="tip in dispatchTipsList" :key="tip.num" class="tip-item">
        <span :class="['num-circle', `num-circle--${tip.color}`]">{{ tip.num }}</span>
        <div class="tip-content">
          <strong class="tip-title">{{ tip.title }}</strong>
          <p class="tip-desc">{{ tip.desc }}</p>
        </div>
      </div>
    </div>

    <!-- 底部提示容器 -->
    <div class="notice-box">
      <el-icon class="notice-icon"><InfoFilled /></el-icon>
      <div class="notice-text">
        <span class="notice-title">提示</span>
        <p class="notice-desc">
          您可以在任务列表中查看任务的实时状态、预估AQI及当前负责人信息，并进行指派或重派操作。
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dispatch-tips-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 16px;
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

.num-circle--blue {
  background: #1677ff;
}

.num-circle--amber {
  background: #f59e0b;
}

.num-circle--green {
  background: #10b981;
}

.num-circle--navy {
  background: #3b82f6;
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

.notice-box {
  margin-top: 20px;
  background: #f0f7ff;
  border: 1px solid #bae0ff;
  border-radius: 8px;
  padding: 12px 14px;
  display: flex;
  gap: 10px;
}

.notice-icon {
  font-size: 18px;
  color: #1677ff;
  flex-shrink: 0;
  margin-top: 2px;
}

.notice-title {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #102f5e;
  margin-bottom: 2px;
}

.notice-desc {
  margin: 0;
  font-size: 12px;
  color: #4b5563;
  line-height: 1.5;
}
</style>
