<script setup lang="ts">
defineProps<{ title: string; badge?: string }>()
</script>

<template>
  <section class="chart-card"><div class="chart-title"><h2>{{ title }}</h2><el-tag v-if="badge" size="small" type="info" effect="plain">{{ badge }}</el-tag></div><slot /></section>
</template>

<style scoped>
.chart-card { min-width: 0; padding: var(--space-4) var(--space-5); background: var(--nepm-surface); border: 1px solid var(--nepm-border); border-radius: var(--nepm-radius); box-shadow: var(--nepm-shadow); }.chart-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--space-3); }.chart-title h2 { margin: 0; color: var(--nepm-text); font-size: 18px; }.chart-title h2::before { content: '▮'; margin-right: 8px; color: var(--nepm-primary); }
</style>
