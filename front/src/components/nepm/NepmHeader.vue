<script setup lang="ts">
import { ArrowDown, SwitchButton, UserFilled } from "@element-plus/icons-vue"
import logo from "@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png"

defineProps<{ displayName?: string }>()
const emit = defineEmits<{ logout: [] }>()
</script>

<template>
  <header class="nepm-header">
    <div class="brand">
      <img :src="logo" alt="东软环保公众监督系统" class="brand-logo" />
      <div class="brand-info">
        <strong class="brand-title">东软环保公众监督系统</strong>
        <span class="brand-subtitle">NEPM 系统管理端</span>
      </div>
    </div>

    <div class="header-right">
      <el-dropdown trigger="click" @command="(command: string) => command === 'logout' && emit('logout')">
        <div class="user-profile" role="button" tabindex="0" aria-label="管理员账号菜单">
          <span class="user-avatar"><el-icon><UserFilled /></el-icon></span>
          <span class="user-name">{{ displayName || "管理员" }}</span>
          <el-icon class="user-caret"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu><el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item></el-dropdown-menu>
        </template>
      </el-dropdown>

      <div class="header-divider"></div>
      <div class="date-display">2024年11月20日 星期三</div>
    </div>
  </header>
</template>

<style scoped>
.nepm-header {
  height: 64px;
  background: #ffffff;
  border-bottom: 1px solid #eef2f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(16, 47, 94, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}
.brand { display: flex; align-items: center; gap: 12px; }
.brand-logo { width: 38px; height: 38px; object-fit: contain; }
.brand-info { display: flex; flex-direction: column; }
.brand-title { font-size: 19px; font-weight: 700; color: #102f5e; letter-spacing: 0.5px; line-height: 1.25; }
.brand-subtitle { font-size: 11px; color: #64748b; margin-top: 2px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-profile { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-avatar {
  width: 32px; height: 32px; border-radius: 50%; background: #e2e8f0; color: #64748b;
  display: flex; align-items: center; justify-content: center; font-size: 17px;
}
.user-name { font-size: 14px; font-weight: 500; color: #1f2937; }
.user-caret { font-size: 12px; color: #94a3b8; }
.header-divider { width: 1px; height: 18px; background: #e2e8f0; }
.date-display { font-size: 13px; color: #64748b; }
@media (max-width: 900px) {
  .date-display, .header-divider { display: none; }
}
</style>
