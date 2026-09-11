<script setup lang="ts">
import type { AqiFeedbackRow } from '@/api/aqiFeedback'
defineProps<{ rows: AqiFeedbackRow[]; loading: boolean }>()
const emit = defineEmits<{ detail: [row: AqiFeedbackRow] }>()
function state(row: AqiFeedbackRow) { return row.timeoutFlag ? '已超时' : ['待指派', '已指派', '已完成'][row.state ?? 0] ?? '未知' }
function grade(value: number | null) { return ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染'][(value ?? 1) - 1] ?? '—' }
</script>
<template><el-table :data="rows" v-loading="loading" stripe><el-table-column label="反馈编号" width="130"><template #default="{ row }">FB{{ row.afId }}</template></el-table-column><el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip /><el-table-column label="提交时间" width="165"><template #default="{ row }">{{ row.submittedAt?.replace('T', ' ').slice(0, 16) || '—' }}</template></el-table-column><el-table-column label="当前状态" width="110"><template #default="{ row }"><el-tag :type="row.timeoutFlag ? 'danger' : row.state === 2 ? 'success' : 'primary'">{{ state(row) }}</el-tag></template></el-table-column><el-table-column label="预估 AQI" width="120"><template #default="{ row }">{{ grade(row.estimatedGrade) }}</template></el-table-column><el-table-column label="操作" width="120" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="emit('detail', row)">查看 / 处理</el-button></template></el-table-column></el-table></template>
