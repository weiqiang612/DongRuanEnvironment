<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ArrowDown, Calendar, SwitchButton, UserFilled } from '@element-plus/icons-vue'

defineProps<{ displayName?: string }>()
const emit = defineEmits<{ logout: [] }>()

const nowTime = ref(new Date())
let timer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  timer = setInterval(() => {
    nowTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const currentDateText = computed(() => {
  const now = nowTime.value
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[now.getDay()]
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${year}年${month}月${date}日 ${weekDay} ${hours}:${minutes}`
})
</script>

<template>
  <header class="nepm-header">
    <div class="header-left">
      <!-- 预留左侧区域 -->
    </div>

    <div class="header-right">
      <!-- 实时日期时间 -->
      <div class="date-display">
        <el-icon class="date-icon"><Calendar /></el-icon>
        <span>{{ currentDateText }}</span>
      </div>

      <div class="header-divider"></div>

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click" @command="(command: string) => command === 'logout' && emit('logout')">
        <div class="user-profile" role="button" tabindex="0" aria-label="管理员账号菜单">
          <span class="user-avatar"><el-icon><UserFilled /></el-icon></span>
          <span class="user-name">{{ displayName || '管理员' }}</span>
          <el-icon class="user-caret"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.nepm-header {
  height: 52px;
  background: #ffffff;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 15;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
}

.date-display {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.date-icon {
  font-size: 14px;
  color: #94a3b8;
}

.header-divider {
  width: 1px;
  height: 16px;
  background: #e2e8f0;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-profile:hover {
  background-color: #f8fafc;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1890ff;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.25);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.user-caret {
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 1000px) {
  .header-slogan {
    display: none;
  }
}

@media (max-width: 768px) {
  .date-display, .header-divider {
    display: none;
  }
}
</style>

