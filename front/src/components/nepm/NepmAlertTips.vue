<script setup lang="ts">
import { Opportunity } from '@element-plus/icons-vue'

const alertAdviceList = [
  {
    num: 1,
    title: '优先处理超时任务',
    desc: '请优先处理已超时的任务，避免问题持续积压，影响公众满意度。',
    color: 'red',
  },
  {
    num: 2,
    title: '及时联系责任人',
    desc: '对于待指派或处理中的超时任务，请及时与相关责任人沟通，督促尽快完成。',
    color: 'orange',
  },
  {
    num: 3,
    title: '必要时进行催办或重派',
    desc: '若任务长时间未处理，可进行催办或重新指派，确保问题闭环。',
    color: 'amber',
  },
]
</script>

<template>
  <div class="alert-tips-card">
    <div class="card-header">
      <div class="header-left">
        <el-icon class="header-icon"><Opportunity /></el-icon>
        <h3 class="header-title">处理建议</h3>
      </div>
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
  height: fit-content;
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
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.num-circle {
  width: 22px;
  height: 22px;
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

.num-circle--amber {
  background: #f59e0b;
}

.tip-content {
  flex: 1;
}

.tip-title {
  display: block;
  font-size: 13.5px;
  color: #1f2937;
  font-weight: 600;
  margin-bottom: 4px;
}

.tip-desc {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.55;
}
</style>
