<script setup lang="ts">
import {
  Bell,
  ChatDotSquare,
  CircleCheck,
  Histogram,
  House,
  Tickets,
} from '@element-plus/icons-vue'
import type { Component } from 'vue'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'

defineProps<{ active: string }>()
const emit = defineEmits<{ change: [value: string] }>()

interface NavigationChild {
  key: string
  label: string
  prototype?: boolean
}

interface NavigationGroup {
  key: string
  label: string
  icon: Component
  children?: NavigationChild[]
}

const groups: NavigationGroup[] = [
  { key: 'dashboard', label: '工作台', icon: House },
  {
    key: 'feedback',
    label: '公众反馈管理',
    icon: ChatDotSquare,
    children: [
      { key: 'feedbacks', label: '反馈列表' },
    ],
  },
  {
    key: 'dispatch',
    label: '任务管理',
    icon: Tickets,
  },
  {
    key: 'resultGroup',
    label: 'AQI检测管理',
    icon: CircleCheck,
    children: [
      { key: 'results', label: '检测结果' },
    ],
  },
  {
    key: 'alertGroup',
    label: '预警管理',
    icon: Bell,
    children: [
      { key: 'aqiAlert', label: 'AQI预警' },
      { key: 'timeout', label: '超时预警' },
    ],
  },
  {
    key: 'analytics',
    label: '统计分析',
    icon: Histogram,
  },
]
</script>

<template>
  <aside class="nepm-sidebar">
    <!-- 顶部系统品牌区 -->
    <div class="sidebar-brand">
      <img :src="logo" alt="系统Logo" class="brand-logo" />
      <div class="brand-text">
        <h2 class="brand-title">东软环保公众监督系统</h2>
        <span class="brand-subtitle">NEPM 系统管理端</span>
      </div>
    </div>

    <!-- 菜单导航区 -->
    <nav class="sidebar-nav" aria-label="管理端主导航">
      <el-menu
        :default-active="active"
        :default-openeds="['feedback', 'dispatchGroup', 'resultGroup', 'alertGroup']"
        class="menu-container"
        @select="(value: string) => emit('change', value)"
      >
        <template v-for="group in groups" :key="group.key">
          <!-- 一级菜单项 (如工作台) -->
          <el-menu-item v-if="!group.children" :index="group.key" class="menu-item-single">
            <el-icon class="menu-icon"><component :is="group.icon" /></el-icon>
            <span class="menu-label">{{ group.label }}</span>
          </el-menu-item>

          <!-- 一级带折叠子菜单 -->
          <el-sub-menu v-else :index="group.key" class="menu-submenu">
            <template #title>
              <el-icon class="menu-icon"><component :is="group.icon" /></el-icon>
              <span class="group-label">{{ group.label }}</span>
            </template>
            <el-menu-item
              v-for="child in group.children"
              :key="child.key"
              :index="child.key"
              class="sub-item"
            >
              <span class="sub-label">{{ child.label }}</span>
              <span v-if="child.prototype" class="prototype-badge">原型</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </nav>

    <!-- 底部水墨山水画卷与环保标语 -->
    <div class="sidebar-footer" aria-hidden="true">
      <div class="slogan-content">
        <div class="slogan-line">守护绿水青山</div>
        <div class="slogan-line bottom-line">
          <span>共建美丽中国</span>
          <span class="slogan-line-bar" />
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.nepm-sidebar {
  width: 236px;
  flex: 0 0 236px;
  min-height: 100vh;
  height: 100vh;
  background-color: #030d1d;
  /* 进一步调深暗夜深蓝蒙层，呈现庄重沉稳的深海墨蓝质感 */
  background-image:
    linear-gradient(180deg, rgba(2, 10, 24, 0.85) 0%, rgba(3, 12, 28, 0.65) 60%, rgba(2, 7, 18, 0.78) 100%),
    url('@/assets/sidebar-bg.png');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center bottom;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  position: relative;
  user-select: none;
  overflow: hidden;
  box-shadow: 1px 0 6px rgba(0, 0, 0, 0.35);
  z-index: 20;
}

/* 顶部品牌区 */
.sidebar-brand {
  height: 64px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.2);
}

.brand-logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.35));
}

.brand-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-title {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.3px;
  line-height: 1.25;
  white-space: nowrap;
}

.brand-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
  letter-spacing: 0.5px;
}

/* 导航主体 */
.sidebar-nav {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-top: 10px;
  padding-bottom: 12px;
  position: relative;
  z-index: 2;
}

.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}
.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 4px;
}

.menu-container {
  border-right: none;
  background: transparent !important;
  --el-menu-bg-color: transparent !important;
  --el-menu-text-color: rgba(255, 255, 255, 0.85);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
  --el-menu-active-color: #ffffff;
}

/* 消除展开子菜单的白色背景块 */
:deep(.el-menu),
:deep(.el-menu--inline),
:deep(.el-sub-menu .el-menu) {
  background-color: transparent !important;
  background: transparent !important;
  border: none !important;
}

/* 一级与展开标题 */
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
  margin: 3px 12px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.85) !important;
  font-size: 14px;
  transition: background-color 0.2s, color 0.2s;
  background-color: transparent !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.07) !important;
  color: #ffffff !important;
}

/* 二级子菜单项 */
:deep(.el-sub-menu .el-menu-item) {
  height: 38px;
  line-height: 38px;
  padding-left: 46px !important;
  margin: 2px 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72) !important;
  background-color: transparent !important;
  border-radius: 6px;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.07) !important;
  color: #ffffff !important;
}

/* 激活状态：沉稳科技蓝胶囊 */
:deep(.el-menu-item.is-active) {
  background-color: #1677ff !important;
  color: #ffffff !important;
  font-weight: 500;
  box-shadow: none !important;
}

/* 图标颜色：统一柔和白色/浅灰白 */
.menu-icon {
  font-size: 16px;
  margin-right: 10px;
  color: rgba(255, 255, 255, 0.85);
  transition: color 0.2s;
}

:deep(.el-menu-item:hover) .menu-icon,
:deep(.el-sub-menu__title:hover) .menu-icon,
:deep(.el-menu-item.is-active) .menu-icon {
  color: #ffffff;
}

:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.45);
}

.prototype-badge {
  margin-left: auto;
  font-size: 11px;
  padding: 1px 6px;
  background: rgba(255, 255, 255, 0.1);
  color: #cbd5e1;
  border-radius: 4px;
  font-weight: normal;
}

/* 底部环保标语：精致纤细无衬线排版 + 纯正矢量水平线 */
.sidebar-footer {
  flex-shrink: 0;
  height: 96px;
  width: 100%;
  position: relative;
  pointer-events: none;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  padding-left: 24px;
  padding-bottom: 22px;
  box-sizing: border-box;
}

.slogan-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.slogan-line {
  font-size: 11px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 1.8px;
  line-height: 1.35;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.85);
  white-space: nowrap;
}

.slogan-line.bottom-line {
  display: flex;
  align-items: center;
  gap: 7px;
}

.slogan-line-bar {
  display: inline-block;
  width: 48px;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.6) 0%, rgba(255, 255, 255, 0.15) 100%);
  border-radius: 1px;
}
</style>
