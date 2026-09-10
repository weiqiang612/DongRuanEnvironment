<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { EmployeePortal } from '@/api/employeeAuth'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'

const props = defineProps<{ portal: EmployeePortal }>()

const router = useRouter()
const portalConfig = {
  nepg: {
    code: 'NEPG',
    title: '网格员端',
    description: '登录状态已建立，检测任务功能将在后续任务中开放。',
  },
  nepm: {
    code: 'NEPM',
    title: '系统管理端',
    description: '登录状态已建立，任务管理功能将在后续任务中开放。',
  },
  nepv: {
    code: 'NEPV',
    title: '决策者可视化端',
    description: '登录状态已建立，统计与预警大屏将在后续任务中开放。',
  },
} as const

const config = computed(() => portalConfig[props.portal])

function returnToEntry() {
  void router.push('/')
}
</script>

<template>
  <main class="portal-page">
    <section class="portal-card" :aria-labelledby="`${portal}-portal-title`">
      <img :src="logo" alt="东软环保公众监督系统标志" />
      <p class="portal-code">{{ config.code }}</p>
      <h1 :id="`${portal}-portal-title`">{{ config.title }}</h1>
      <p>{{ config.description }}</p>
      <button type="button" @click="returnToEntry">返回系统统一入口</button>
    </section>
  </main>
</template>

<style scoped>
.portal-page {
  min-width: 1000px;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 40px;
  box-sizing: border-box;
  background: linear-gradient(145deg, #eaf1f8, #f8fbfd);
  color: #1f2d3d;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.portal-card {
  width: min(560px, 90%);
  padding: 56px;
  text-align: center;
  background: #fff;
  border: 1px solid #d9e1ea;
  border-radius: 12px;
  box-shadow: 0 16px 42px rgb(31 90 148 / 0.14);
}

img {
  width: 68px;
  height: 68px;
  object-fit: contain;
}

.portal-code {
  margin: 20px 0 8px;
  color: #1f5a94;
  font-weight: 700;
  letter-spacing: 0.12em;
}

h1 {
  margin: 0;
  color: #082d5a;
  font-size: 30px;
}

.portal-card > p:not(.portal-code) {
  margin: 18px 0 30px;
  color: #606266;
  line-height: 1.7;
}

button {
  min-height: 44px;
  padding: 0 24px;
  color: #fff;
  background: #1f5a94;
  border: 0;
  border-radius: 5px;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

button:hover {
  background: #174875;
}

button:focus-visible {
  outline: 3px solid rgb(31 90 148 / 0.35);
  outline-offset: 3px;
}
</style>
