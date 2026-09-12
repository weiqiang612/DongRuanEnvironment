<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSessionStatus, logoutSession } from '@/api/session'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'

const router = useRouter()
const userName = ref(localStorage.getItem('nepg_user_name') || '网格员')
const menuVisible = ref(false)
const confirmLogoutVisible = ref(false)
const loggingOut = ref(false)

onMounted(async () => {
  try {
    const res = await getSessionStatus()
    if (res.data.data?.displayName) {
      userName.value = res.data.data.displayName
      localStorage.setItem('nepg_user_name', res.data.data.displayName)
    }
  } catch {
    // 保留本地已有缓存
  }
})

function toggleMenu() {
  menuVisible.value = !menuVisible.value
}

function openLogoutConfirm() {
  menuVisible.value = false
  confirmLogoutVisible.value = true
}

function closeLogoutConfirm() {
  confirmLogoutVisible.value = false
}

async function handleConfirmLogout() {
  loggingOut.value = true
  try {
    await logoutSession()
  } catch {
    // 忽略登出网络失败
  } finally {
    localStorage.removeItem('nepg_user_name')
    confirmLogoutVisible.value = false
    loggingOut.value = false
    router.push('/nepg/login')
  }
}
</script>

<template>
  <header class="nepg-header">
    <div class="brand" @click="router.push('/nepg/tasks')">
      <img :src="logo" alt="东软环保公众监督系统" class="brand-logo" />
      <div class="brand-text">
        <span class="brand-title">东软环保公众监督系统</span>
        <span class="brand-sub">NEPG 网格员端</span>
      </div>
    </div>
    <div class="user-menu-wrap">
      <button
        type="button"
        class="user-btn"
        aria-label="用户个人中心"
        :aria-expanded="menuVisible"
        @click="toggleMenu"
      >
        <span class="avatar-circle">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
        </span>
        <span class="user-name">{{ userName }}</span>
        <svg class="arrow-icon" :class="{ rotated: menuVisible }" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </button>

      <div v-if="menuVisible" class="dropdown-menu">
        <button type="button" class="menu-item danger" @click="openLogoutConfirm">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          退出登录
        </button>
      </div>
    </div>

    <!-- 退出登录二次确认弹窗 -->
    <Teleport to="body">
      <div v-if="confirmLogoutVisible" class="modal-backdrop" @click="closeLogoutConfirm">
        <div class="modal-box" role="dialog" aria-modal="true" @click.stop>
          <div class="modal-icon-wrap">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#dc2626" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
          </div>
          <h3 class="modal-title">退出系统确认</h3>
          <p class="modal-desc">确定要退出网格员工作端吗？退出后需重新登录。</p>
          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeLogoutConfirm">取消</button>
            <button
              type="button"
              class="btn-confirm"
              :disabled="loggingOut"
              @click="handleConfirmLogout"
            >
              {{ loggingOut ? '退出中…' : '确认退出' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </header>
</template>

<style scoped>
.nepg-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 16px;
  color: #fff;
  background: linear-gradient(90deg, #135da7 0%, #1984c8 100%);
  box-shadow: 0 2px 8px rgba(10, 48, 88, 0.15);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.brand-logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.12));
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.3px;
  line-height: 1.25;
}

.brand-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 0.4px;
  line-height: 1.3;
}

.user-menu-wrap {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 5px 8px;
  color: #fff;
  background: transparent;
  border: 0;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.avatar-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  color: #fff;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.arrow-icon {
  transition: transform 0.2s;
}

.arrow-icon.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 140px;
  padding: 6px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.12);
  border: 1px solid #edf2f7;
  display: flex;
  flex-direction: column;
  gap: 2px;
  z-index: 200;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 9px 12px;
  font-size: 13px;
  color: #334155;
  background: transparent;
  border: 0;
  border-radius: 6px;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.15s;
}

.menu-item:hover {
  background: #f1f5f9;
}

.menu-item.danger {
  color: #ef4444;
}

.menu-item.danger:hover {
  background: #fef2f2;
}

/* 二次确认弹窗样式 */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  padding: 16px;
  animation: fadeIn 0.15s ease-out;
}

.modal-box {
  width: min(380px, calc(100% - 32px));
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  animation: scaleUp 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #fee2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.modal-desc {
  margin: 8px 0 22px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.btn-cancel {
  flex: 1;
  height: 42px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-confirm {
  flex: 1;
  height: 42px;
  background: #dc2626;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-confirm:hover:not(:disabled) {
  background: #b91c1c;
}

.btn-confirm:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@media (min-width: 768px) {
  .nepg-header {
    padding: 0 clamp(20px, 4vw, 48px);
  }
}
</style>
