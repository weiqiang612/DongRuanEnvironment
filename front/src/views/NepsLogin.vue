<script setup lang="ts">
import { ref } from 'vue'
import type { AxiosError } from 'axios'
import { useRouter } from 'vue-router'
import { loginNeps } from '@/api/nepsAuth'
import type { ResultVO } from '@/api/aqiFeedback'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'
import brandLogo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (2).png'
import background from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_10 PM (3).png'

const router = useRouter()
const telId = ref('')
const password = ref('')
const showPassword = ref(false)
const submitting = ref(false)
const message = ref('')
const messageType = ref<'error' | 'info'>('error')

function showMessage(text: string, type: 'error' | 'info' = 'error') {
  message.value = text
  messageType.value = type
}

async function submitLogin() {
  const trimmedTelId = telId.value.trim()
  if (!trimmedTelId || !password.value) {
    showMessage('请输入手机号和密码')
    return
  }

  submitting.value = true
  message.value = ''
  try {
    const response = await loginNeps({ telId: trimmedTelId, password: password.value })
    if (response.data.code !== 200) {
      showMessage(response.data.message || '登录失败，请稍后重试')
      return
    }
    await router.push('/aqiFeedback')
  } catch (error) {
    const axiosError = error as AxiosError<ResultVO<null>>
    showMessage(axiosError.response?.data.message || '登录失败，请检查网络后重试')
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
          <span class="brand-sub">NEPS 公众监督员端</span>
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

    <section class="login-content" aria-labelledby="login-title">
      <aside class="mission-copy" aria-label="系统理念">
        <p class="script-slogan">让环境更美好<br />让公众参与更有力量</p>
        <div class="green-arc" aria-hidden="true"></div>
        <div class="eco-slogan">
          <span>生态优先 · 绿色发展 · 共建共享</span>
        </div>
      </aside>

      <form class="login-card" @submit.prevent="submitLogin">
        <img class="card-logo" :src="logo" alt="" aria-hidden="true" />
        <h1 id="login-title">东软环保公众监督系统</h1>
        <h2>NEPS 公众监督员端</h2>

        <div class="card-subtitle-wrap">
          <span class="sub-line"></span>
          <span class="card-subtitle">数据赋能　守护美丽中国</span>
          <span class="sub-line"></span>
        </div>

        <div class="field-group">
          <label for="tel-id">手机号</label>
          <div class="input-wrap">
            <svg class="field-svg" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
            <input
              id="tel-id"
              v-model="telId"
              type="tel"
              autocomplete="username"
              placeholder="请输入手机号"
              maxlength="32"
              @input="message = ''"
            />
          </div>
        </div>

        <div class="field-group">
          <label for="password">密码</label>
          <div class="input-wrap">
            <svg class="field-svg" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
            </svg>
            <input
              id="password"
              v-model="password"
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
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.44-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z" />
              </svg>
            </button>
          </div>
        </div>

        <p v-if="message" class="form-message" :class="`form-message--${messageType}`" role="alert">
          {{ message }}
        </p>

        <button class="submit-button" type="submit" :disabled="submitting">
          {{ submitting ? '登录中…' : '登 录' }}
        </button>

        <div class="card-footer-tip">
          <button class="register-link" type="button" @click="showMessage('注册功能暂未开放', 'info')">
            还没有账号？<strong>立即注册</strong>
          </button>
        </div>
      </form>
    </section>

    <footer class="site-footer">
      <div class="footer-left">
        © 2024 东软环保公众监督系统　版权所有　|　Neusoft Environmental Public Supervision System
      </div>
      <div class="footer-right">
        <span class="footer-dash">—</span>
        <span>科技赋能　绿色未来</span>
        <span class="footer-dash">—</span>
      </div>
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
  height: 100vh;
  min-height: 580px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow-x: hidden;
  overflow-y: auto;
  color: #122b46;
  background: #eaf3fb;
  box-sizing: border-box;
}

/* 背景风景照 */
.login-bg-wrap {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.login-background {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 30%;
  opacity: 0.95;
}

.login-bg-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.25) 0%,
    rgba(255, 255, 255, 0.05) 45%,
    rgba(255, 255, 255, 0.15) 100%
  );
}

/* 顶部导航：放大 Logo 与标题文字 */
.site-header {
  position: relative;
  z-index: 2;
  height: clamp(74px, 10vh, 98px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4.5%;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 1px 8px rgba(18, 48, 88, 0.06);
  box-sizing: border-box;
  flex-shrink: 0;
}

.brand-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
  user-select: none;
}

.brand-icon {
  width: clamp(48px, 6.8vh, 64px);
  height: clamp(48px, 6.8vh, 64px);
  object-fit: contain;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-name {
  color: #082d5a;
  font-size: clamp(22px, 2.1vw, 29px);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: 0.04em;
}

.brand-sub {
  color: #3b5f88;
  font-size: clamp(12px, 0.95vw, 15px);
  font-weight: 600;
  letter-spacing: 0.04em;
  margin-top: 3px;
}

.header-slogan {
  text-align: center;
  color: #2a5585;
}

.slogan-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  font-size: clamp(13.5px, 1.15vw, 17px);
  font-weight: 500;
  letter-spacing: 0.22em;
}

.slogan-dash {
  color: #8da4be;
  font-weight: 300;
  letter-spacing: -0.1em;
}

.slogan-en {
  display: block;
  margin-top: 3px;
  font-size: clamp(10px, 0.8vw, 12px);
  color: #6a89ab;
  letter-spacing: 0.05em;
}

/* 内容区 */
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
  min-height: 0;
}

/* 左侧理念 */
.mission-copy {
  align-self: center;
  margin-bottom: clamp(8px, 1.5vh, 20px);
  text-align: center;
}

.script-slogan {
  margin: 0;
  font-family: 'KaiTi', 'STKaiti', serif;
  color: #174a7f;
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
  color: #386f8a;
  font-size: clamp(13px, 1.1vw, 16.5px);
  letter-spacing: 0.18em;
  font-weight: 500;
}

/* 右侧登录卡片 */
.login-card {
  padding: clamp(20px, 2.8vh, 32px) clamp(26px, 3.2vw, 44px);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 16px 36px rgba(18, 52, 92, 0.16);
  text-align: center;
  box-sizing: border-box;
}

.card-logo {
  width: clamp(52px, 6.2vh, 70px);
  height: clamp(52px, 6.2vh, 70px);
  object-fit: contain;
  margin-bottom: clamp(2px, 0.4vh, 6px);
}

.login-card h1 {
  margin: 0;
  color: #072b5c;
  font-size: clamp(21px, 2.2vh, 26px);
  font-weight: 700;
  line-height: 1.25;
}

.login-card h2 {
  margin: 3px 0 0;
  color: #435b78;
  font-size: clamp(15px, 1.6vh, 18px);
  font-weight: 600;
}

.card-subtitle-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
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

/* 输入表单项 */
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
  border: 1px solid #c7d7ea;
  border-radius: 7px;
  outline: none;
  padding: 0 42px;
  color: #1a395c;
  background: #fbfdff;
  font: inherit;
  font-size: clamp(13px, 1.3vh, 15px);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  box-sizing: border-box;
}

.input-wrap input::placeholder {
  color: #9cb2cb;
}

.input-wrap input:focus {
  border-color: var(--primary-blue);
  box-shadow: 0 0 0 3px rgba(35, 117, 201, 0.14);
}

.password-toggle {
  position: absolute;
  right: 8px;
  width: 32px;
  height: 32px;
  border: 0;
  color: #8da4be;
  background: transparent;
  display: grid;
  place-items: center;
  cursor: pointer;
  border-radius: 4px;
}

.password-toggle:hover {
  color: #3b5f88;
}

.form-message {
  min-height: 18px;
  margin: -4px 0 8px;
  text-align: left;
  font-size: clamp(12px, 1.1vh, 13px);
}

.form-message--error {
  color: #dc2626;
}

.form-message--info {
  color: #2563eb;
}

.submit-button {
  width: 100%;
  height: clamp(42px, 5.2vh, 50px);
  min-height: 0;
  margin-top: 4px;
  border: 0;
  border-radius: 7px;
  color: #ffffff;
  background: var(--primary-blue);
  box-shadow: 0 4px 10px rgba(35, 117, 201, 0.25);
  font: inherit;
  font-size: clamp(15px, 1.65vh, 18px);
  font-weight: 600;
  letter-spacing: 0.28em;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
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
  margin-top: clamp(10px, 1.6vh, 18px);
  font-size: clamp(12px, 1.2vh, 13.5px);
  color: #6a82a0;
}

.register-link {
  border: 0;
  background: transparent;
  color: #6a82a0;
  font: inherit;
  cursor: pointer;
  padding: 2px 6px;
}

.register-link strong {
  margin-left: 5px;
  color: #1e70d4;
  font-weight: 600;
}

.register-link:hover strong {
  text-decoration: underline;
}

/* 页脚 */
.site-footer {
  position: relative;
  z-index: 2;
  height: clamp(34px, 5vh, 48px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4.5%;
  color: #7189a6;
  font-size: clamp(11px, 0.78vw, 13px);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 -1px 4px rgba(18, 48, 88, 0.03);
  box-sizing: border-box;
  flex-shrink: 0;
}

.footer-left {
  letter-spacing: 0.02em;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 8px;
  letter-spacing: 0.18em;
  color: #627e9e;
}

.footer-dash {
  color: #9cb1c7;
}

@media (max-height: 620px) {
  .login-page {
    height: auto;
    min-height: 100vh;
  }
}

@media (prefers-reduced-motion: reduce) {
  .input-wrap input,
  .submit-button {
    transition: none;
  }
}
</style>
