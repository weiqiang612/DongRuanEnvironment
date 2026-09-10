<script setup lang="ts">
import { computed, ref } from 'vue'
import type { AxiosError } from 'axios'
import { useRouter } from 'vue-router'
import { loginEmployee, type EmployeePortal } from '@/api/employeeAuth'
import { loginNeps } from '@/api/nepsAuth'
import type { ResultVO } from '@/api/aqiFeedback'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'
import background from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_10 PM (3).png'

type LoginPortal = EmployeePortal | 'neps'

const props = defineProps<{ portal: LoginPortal }>()

const router = useRouter()
const accountCode = ref('')
const password = ref('')
const showPassword = ref(false)
const message = ref('')
const submitting = ref(false)

const portalConfig = {
  neps: {
    code: 'NEPS',
    title: '公众监督员端',
    accountLabel: '手机号',
    accountPlaceholder: '请输入手机号',
    accountMaxLength: 32,
    portalDescription: '数据赋能　守护美丽中国',
    mission: ['让环境更美好', '让公众参与更有力量'],
    ecoSlogan: '生态优先 · 绿色发展 · 共建共享',
    accountNote: '还没有账号？立即注册',
    portalPath: '/aqiFeedback',
  },
  nepg: {
    code: 'NEPG',
    title: '网格员端',
    accountLabel: '登录编码',
    accountPlaceholder: '请输入登录编码',
    accountMaxLength: 20,
    portalDescription: '面向网格员日常工作使用',
    mission: ['让环境更美好', '让巡查更有力量'],
    ecoSlogan: '生态优先 · 绿色发展 · 共建共享',
    accountNote: '网格员账号由系统统一分配',
    portalPath: '/nepg/portal',
  },
  nepm: {
    code: 'NEPM',
    title: '系统管理端',
    accountLabel: '登录编码',
    accountPlaceholder: '请输入登录编码',
    accountMaxLength: 20,
    portalDescription: '面向系统管理员使用',
    mission: ['让环境更美好', '让管理更高效'],
    ecoSlogan: '生态优先 · 绿色发展 · 共建共享',
    accountNote: '管理员账号由系统统一维护',
    portalPath: '/nepm/portal',
  },
  nepv: {
    code: 'NEPV',
    title: '决策者可视化大屏',
    accountLabel: '账号',
    accountPlaceholder: '请输入账号',
    accountMaxLength: 20,
    portalDescription: '面向生态环境决策支撑',
    mission: ['数据洞察环境', '共建美丽中国'],
    ecoSlogan: '生态优先 · 绿色发展 · 科学决策',
    accountNote: '仅限授权用户访问',
    portalPath: '/nepv/portal',
  },
} as const

const config = computed(() => portalConfig[props.portal])
const isNeps = computed(() => props.portal === 'neps')

function returnToEntry() {
  router.push('/')
}

async function submitLogin() {
  const trimmedAccountCode = accountCode.value.trim()
  if (!trimmedAccountCode || !password.value) {
    message.value = `请输入${config.value.accountLabel}和密码`
    return
  }

  submitting.value = true
  message.value = ''
  try {
    const response =
      props.portal === 'neps'
        ? await loginNeps({ telId: trimmedAccountCode, password: password.value })
        : await loginEmployee(props.portal, {
            accountCode: trimmedAccountCode,
            password: password.value,
          })
    if (response.data.code !== 200) {
      message.value = response.data.message || '登录失败，请稍后重试'
      return
    }
    password.value = ''
    await router.push(config.value.portalPath)
  } catch (error) {
    const axiosError = error as AxiosError<ResultVO<null>>
    message.value = axiosError.response?.data.message || '登录失败，请检查网络后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <div class="login-bg-wrap" aria-hidden="true">
      <img class="login-background" :src="background" alt="" />
      <div class="login-bg-mask"></div>
    </div>

    <header class="site-header">
      <div class="brand-wrap">
        <img class="brand-icon" :src="logo" alt="" aria-hidden="true" />
        <div class="brand-text">
          <span class="brand-name">东软环保公众监督系统</span>
          <span class="brand-sub">{{ config.code }} {{ config.title }}</span>
        </div>
      </div>
      <div class="header-slogan">
        <div class="slogan-row">
          <span class="slogan-dash">——</span>
          <span class="slogan-text">绿 水 青 山　共 同 守 护</span>
          <span class="slogan-dash">——</span>
        </div>
        <small class="slogan-en">Environmental Protection for a Better Tomorrow</small>
      </div>
    </header>

    <section class="login-content" :aria-labelledby="`${portal}-login-title`">
      <aside class="mission-copy" aria-label="系统理念">
        <p class="script-slogan">{{ config.mission[0] }}<br />{{ config.mission[1] }}</p>
        <div class="green-arc" aria-hidden="true"></div>
        <p class="eco-slogan">{{ config.ecoSlogan }}</p>
      </aside>

      <form class="login-card" @submit.prevent="submitLogin">
        <button class="return-entry-link" type="button" @click="returnToEntry">← 返回</button>
        <img class="card-logo" :src="logo" alt="" aria-hidden="true" />
        <h1 :id="`${portal}-login-title`">东软环保公众监督系统</h1>
        <h2>{{ config.code }} {{ config.title }}</h2>

        <div class="card-subtitle-wrap">
          <span class="sub-line"></span>
          <span class="card-subtitle">{{ config.portalDescription }}</span>
          <span class="sub-line"></span>
        </div>

        <div class="field-group">
          <label for="account-code">{{ config.accountLabel }}</label>
          <div class="input-wrap">
            <svg class="field-svg" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path
                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
              />
            </svg>
            <input
              id="account-code"
              v-model="accountCode"
              :name="isNeps ? 'telId' : 'accountCode'"
              :type="isNeps ? 'tel' : 'text'"
              :maxlength="config.accountMaxLength"
              autocomplete="username"
              :placeholder="config.accountPlaceholder"
              @input="message = ''"
            />
          </div>
        </div>

        <div class="field-group">
          <label for="password">密码</label>
          <div class="input-wrap">
            <svg class="field-svg" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path
                d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
              />
            </svg>
            <input
              id="password"
              v-model="password"
              name="password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入密码"
              @input="message = ''"
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
        </div>

        <p v-if="message" class="form-message" role="alert">{{ message }}</p>

        <button class="submit-button" type="submit" :disabled="submitting" :aria-busy="submitting">
          {{ submitting ? '登录中…' : '登 录' }}
        </button>

        <button
          v-if="isNeps"
          class="card-footer-tip register-link"
          type="button"
          @click="message = '注册功能暂未开放'"
        >
          {{ config.accountNote }}
        </button>
        <p v-else class="card-footer-tip">{{ config.accountNote }}</p>
      </form>
    </section>

    <footer class="site-footer">
      <div>
        © 2024 东软环保公众监督系统　版权所有　|　Neusoft Environmental Public Supervision System
      </div>
      <div class="footer-right"><span>——</span><span>科技赋能　绿色未来</span><span>——</span></div>
    </footer>
  </main>
</template>

<style scoped>
:global(#app) {
  min-height: 100vh;
  margin: 0;
  padding: 0;
}

.login-page {
  --primary-blue: #2375c9;
  --hover-blue: #185ea6;
  position: relative;
  min-width: 1000px;
  min-height: 580px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #122b46;
  background: #eaf3fb;
  box-sizing: border-box;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.login-bg-wrap {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.login-background {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 30%;
}

.login-bg-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgb(255 255 255 / 0.28),
    rgb(255 255 255 / 0.04) 48%,
    rgb(255 255 255 / 0.15)
  );
}

.site-header,
.site-footer {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4.5%;
  box-sizing: border-box;
  background: rgb(255 255 255 / 0.95);
}

.site-header {
  height: clamp(74px, 10vh, 98px);
  flex-shrink: 0;
  box-shadow: 0 1px 8px rgb(18 48 88 / 0.06);
}

.brand-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-icon {
  width: clamp(48px, 6.8vh, 64px);
  height: clamp(48px, 6.8vh, 64px);
  object-fit: contain;
}

.brand-text {
  display: grid;
  gap: 3px;
}

.brand-name {
  color: #082d5a;
  font-size: clamp(22px, 2.1vw, 29px);
  font-weight: 800;
  letter-spacing: 0.04em;
  line-height: 1.15;
}

.brand-sub {
  color: #586e90;
  font-size: clamp(12px, 1.1vw, 17px);
  font-weight: 600;
}

.header-slogan {
  color: #2a5585;
  text-align: center;
}

.return-entry-link {
  position: absolute;
  top: clamp(14px, 1.8vh, 20px);
  left: clamp(18px, 2.2vw, 28px);
  padding: 0;
  color: #285789;
  background: transparent;
  border: 0;
  cursor: pointer;
  font: inherit;
  font-size: clamp(14px, 1.05vw, 16px);
  line-height: 1.25;
  transition: color 180ms ease;
}

.return-entry-link:hover {
  color: var(--primary-blue);
}

.return-entry-link:focus-visible {
  outline: 2px solid var(--primary-blue);
  outline-offset: 4px;
  box-shadow: 0 0 0 3px rgb(35 117 201 / 0.14);
}

.slogan-row,
.footer-right {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.slogan-row {
  font-size: clamp(13px, 1.15vw, 17px);
  font-weight: 500;
  letter-spacing: 0.22em;
}

.slogan-dash,
.footer-right > span:first-child,
.footer-right > span:last-child {
  color: #9cb1c7;
  letter-spacing: -0.1em;
}

.slogan-en {
  display: block;
  margin-top: 3px;
  color: #6a89ab;
  font-size: clamp(10px, 0.8vw, 12px);
}

.login-content {
  position: relative;
  z-index: 1;
  flex: 1;
  width: min(1240px, 88%);
  display: grid;
  grid-template-columns: 1.15fr min(460px, 42%);
  gap: clamp(24px, 6vw, 80px);
  align-items: center;
  margin: 0 auto;
  padding: clamp(6px, 1.5vh, 20px) 0;
  box-sizing: border-box;
}

.mission-copy,
.login-card {
  text-align: center;
}

.mission-copy {
  margin-bottom: clamp(8px, 1.5vh, 20px);
}

.script-slogan {
  margin: 0;
  color: #174a7f;
  font-family: 'KaiTi', 'STKaiti', serif;
  font-size: clamp(26px, 2.6vw, 38px);
  line-height: 1.45;
  transform: rotate(-5deg);
}

.green-arc {
  width: clamp(140px, 16vw, 210px);
  height: 12px;
  margin: clamp(8px, 1.2vh, 14px) auto clamp(12px, 1.8vh, 18px);
  border-top: 5px solid #30b663;
  border-radius: 50%;
  transform: rotate(-7deg);
}

.eco-slogan {
  margin: 0;
  color: #386f8a;
  font-size: clamp(13px, 1.1vw, 16.5px);
  font-weight: 500;
  letter-spacing: 0.18em;
}

.login-card {
  position: relative;
  padding: clamp(20px, 2.8vh, 32px) clamp(26px, 3.2vw, 44px);
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 16px 36px rgb(18 52 92 / 0.16);
  box-sizing: border-box;
}

.card-logo {
  width: clamp(52px, 6.2vh, 70px);
  height: clamp(52px, 6.2vh, 70px);
  margin-bottom: clamp(2px, 0.4vh, 6px);
  object-fit: contain;
}

.login-card h1 {
  margin: 0;
  color: #072b5c;
  font-size: clamp(21px, 2.2vh, 26px);
  line-height: 1.25;
}

.login-card h2 {
  margin: 3px 0 0;
  color: #435b78;
  font-size: clamp(15px, 1.6vh, 18px);
}

.card-subtitle-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: clamp(8px, 1.2vh, 16px) 0 clamp(12px, 1.8vh, 20px);
}

.sub-line {
  flex: 1;
  height: 1px;
  background: #d4e0ee;
}

.card-subtitle {
  color: #6a82a0;
  font-size: clamp(12px, 1.15vh, 13.5px);
  letter-spacing: 0.06em;
  white-space: nowrap;
}

.field-group {
  margin-bottom: clamp(10px, 1.5vh, 16px);
  text-align: left;
}

.field-group label {
  display: block;
  margin-bottom: 5px;
  color: #1e3c60;
  font-size: clamp(12.5px, 1.2vh, 14px);
  font-weight: 600;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.field-svg {
  position: absolute;
  left: 14px;
  width: clamp(17px, 1.9vh, 20px);
  height: clamp(17px, 1.9vh, 20px);
  color: #8da4be;
  pointer-events: none;
}

.input-wrap input {
  width: 100%;
  height: clamp(42px, 5vh, 48px);
  padding: 0 54px 0 42px;
  color: #1a395c;
  background: #fbfdff;
  border: 1px solid #c7d7ea;
  border-radius: 7px;
  box-sizing: border-box;
  font: inherit;
  font-size: clamp(13px, 1.3vh, 15px);
}

.input-wrap input::placeholder {
  color: #9cb2cb;
}

.input-wrap input:focus,
.password-toggle:focus-visible,
.submit-button:focus-visible {
  outline: none;
  border-color: var(--primary-blue);
  box-shadow: 0 0 0 3px rgb(35 117 201 / 0.14);
}

.password-toggle {
  position: absolute;
  right: 8px;
  padding: 4px;
  color: #617b9e;
  background: transparent;
  border: 0;
  border-radius: 4px;
  cursor: pointer;
  font: inherit;
  font-size: 12px;
}

.form-message {
  min-height: 18px;
  margin: -4px 0 8px;
  color: #dc2626;
  text-align: left;
  font-size: clamp(12px, 1.1vh, 13px);
}

.submit-button {
  width: 100%;
  height: clamp(42px, 5.2vh, 50px);
  margin-top: 4px;
  color: #fff;
  background: var(--primary-blue);
  border: 0;
  border-radius: 7px;
  box-shadow: 0 4px 10px rgb(35 117 201 / 0.25);
  cursor: pointer;
  font: inherit;
  font-size: clamp(15px, 1.65vh, 18px);
  font-weight: 600;
  letter-spacing: 0.28em;
  transition:
    background 180ms ease,
    transform 180ms ease;
}

.submit-button:hover:not(:disabled) {
  background: var(--hover-blue);
  transform: translateY(-1px);
}

.submit-button:disabled {
  cursor: wait;
  background: #8fb1d7;
}

.card-footer-tip {
  margin: clamp(10px, 1.6vh, 18px) 0 0;
  color: #6a82a0;
  font-size: clamp(12px, 1.2vh, 13.5px);
}

.register-link {
  border: 0;
  background: transparent;
  cursor: pointer;
  font: inherit;
}

.register-link:hover {
  color: var(--primary-blue);
  text-decoration: underline;
}

.site-footer {
  min-height: clamp(34px, 5vh, 48px);
  color: #7189a6;
  font-size: clamp(11px, 0.78vw, 13px);
}

.footer-right {
  color: #627e9e;
  letter-spacing: 0.18em;
}

@media (max-height: 620px) {
  .login-page {
    height: auto;
    min-height: 100vh;
  }
}

@media (prefers-reduced-motion: reduce) {
  .submit-button {
    transition: none;
  }
}
</style>
