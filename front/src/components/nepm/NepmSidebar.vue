<script setup lang="ts">
import {
  Bell,
  ChatDotRound,
  DataAnalysis,
  DocumentChecked,
  House,
  Tickets,
} from '@element-plus/icons-vue'
import type { Component } from 'vue'

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
    icon: ChatDotRound,
    children: [
      { key: 'feedbacks', label: '反馈列表' },
    ],
  },
  {
    key: 'dispatchGroup',
    label: '任务调度管理',
    icon: Tickets,
    children: [
      { key: 'dispatch', label: '任务分派' },
      { key: 'handle', label: '任务处理' },
    ],
  },
  {
    key: 'resultGroup',
    label: 'AQI检测管理',
    icon: DocumentChecked,
    children: [
      { key: 'results', label: '检测结果' },
    ],
  },
  {
    key: 'alertGroup',
    label: '预警管理',
    icon: Bell,
    children: [
      { key: 'timeout', label: '超时预警' },
    ],
  },
  {
    key: 'analysisGroup',
    label: '统计分析',
    icon: DataAnalysis,
    children: [
      { key: 'analytics', label: '数据统计' },
      { key: 'regional', label: '区域分析' },
    ],
  },
]
</script>

<template>
  <aside class="nepm-sidebar">
    <nav class="sidebar-nav" aria-label="管理端主导航">
      <el-menu
        :default-active="active"
        :default-openeds="['feedback', 'dispatchGroup', 'resultGroup', 'alertGroup', 'analysisGroup']"
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

  </aside>
</template>

<style scoped>
.nepm-sidebar {
  width: 220px;
  flex: 0 0 220px;
  min-height: calc(100vh - 64px);
  background: #ffffff;
  border-right: 1px solid #eef2f6;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  user-select: none;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding-top: 12px;
}

.menu-container {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px;
  line-height: 46px;
  margin: 2px 10px;
  border-radius: 6px;
  color: #374151;
  font-size: 14px;
  transition: all 0.2s;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #f8fafc;
  color: #1677ff;
}

:deep(.el-sub-menu .el-menu-item) {
  height: 42px;
  line-height: 42px;
  padding-left: 48px !important;
  margin: 2px 10px;
  font-size: 13.5px;
  color: #4b5563;
}

/* 激活状态 */
:deep(.el-menu-item.is-active) {
  background-color: #e8f3ff !important;
  color: #1677ff !important;
  font-weight: 600;
  position: relative;
}

:deep(.el-menu-item.is-active)::before {
  content: "";
  position: absolute;
  left: -10px;
  top: 8px;
  bottom: 8px;
  width: 3.5px;
  background-color: #1677ff;
  border-radius: 0 4px 4px 0;
}

.menu-icon {
  font-size: 17px;
  margin-right: 10px;
  color: #64748b;
}

:deep(.el-menu-item.is-active) .menu-icon {
  color: #1677ff;
}

.prototype-badge {
  margin-left: auto;
  font-size: 11px;
  padding: 1px 6px;
  background: #f1f5f9;
  color: #94a3b8;
  border-radius: 4px;
  font-weight: normal;
}

</style>
