<template>
  <section class="unavailable-card">
    <el-empty description="检测结果模块后续开放">
      <template #description>
        <p>当前尚未接入网格员实测数据、最终 AQI 和检测结果查询接口，因此不展示模拟检测数据。</p>
      </template>
    </el-empty>
  </section>
</template>

<style scoped>
.unavailable-card { padding: 56px 24px; background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; }.unavailable-card p { color: #64748b; }
</style>
