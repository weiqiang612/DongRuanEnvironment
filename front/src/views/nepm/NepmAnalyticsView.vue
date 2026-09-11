<script setup lang="ts">
import { onMounted, ref } from 'vue'
import NepmStatCard from '@/components/nepm/NepmStatCard.vue'
import { getOverview } from '@/api/nepm'

const overview = ref({ total: 0, pending: 0, assigned: 0, completed: 0, timeout: 0 })
async function loadOverview() { overview.value = (await getOverview({})).data.data }
onMounted(() => { void loadOverview() })
</script>

<template>
  <section>
    <div class="stat-grid"><NepmStatCard title="反馈总数" :value="overview.total" tone="blue" /><NepmStatCard title="待指派" :value="overview.pending" tone="amber" /><NepmStatCard title="处理中" :value="overview.assigned" tone="blue" /><NepmStatCard title="已完成" :value="overview.completed" tone="green" /><NepmStatCard title="超时" :value="overview.timeout" tone="red" /></div>
    <div class="note">以上统计仅基于真实反馈与调度状态；趋势、检测结果和 AQI 分布将在后续模块开放。</div>
  </section>
</template>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 16px; }.note { margin-top: 16px; padding: 14px 18px; color: #64748b; background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; }@media (max-width: 1200px) { .stat-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
