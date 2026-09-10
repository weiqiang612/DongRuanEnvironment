<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'
import landscape from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_10 PM (3).png'

const router = useRouter()
const notice = ref('')

interface SystemCard {
  code: string
  title: string
  description: string
  icon: 'public' | 'grid' | 'admin' | 'screen'
  loginPath: string
}

const systems: SystemCard[] = [
  {
    code: 'NEPS',
    title: '公众监督员端',
    description: '面向社会公众，提供环境问题反馈上报和办理进度查询等功能。',
    icon: 'public',
    loginPath: '/neps/login',
  },
  {
    code: 'NEPG',
    title: '网格员端',
    description: '面向网格员/巡查人员，提供任务查看、现场 AQI 数据上报等功能。',
    icon: 'grid',
    loginPath: '/nepg/login',
  },
  {
    code: 'NEPM',
    title: '系统管理端',
    description: '面向系统管理员，提供任务分配、数据确认、统计分析、预警处理和超时任务管理等功能。',
    icon: 'admin',
    loginPath: '/nepm/login',
  },
  {
    code: 'NEPV',
    title: '决策者可视化大屏',
    description: '面向决策者，提供环境数据统计分析和预警信息展示。',
    icon: 'screen',
    loginPath: '/nepv/login',
  },
]

function enterSystem(system: SystemCard) {
  notice.value = ''
  void router.push(system.loginPath)
}
</script>

<template>
  <main class="entry-page">
    <div class="landscape-banner" aria-hidden="true">
      <img class="banner-img" :src="landscape" alt="" />
      <div class="banner-mask"></div>
    </div>

    <header class="site-header">
      <div class="brand-wrap">
        <img class="brand-icon" :src="logo" alt="" aria-hidden="true" />
        <div class="brand-text">
          <span class="brand-name">东软环保公众监督系统</span>
          <span class="brand-en">Neusoft Environmental Public Supervision System</span>
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

    <section class="entry-content" aria-labelledby="entry-title">
      <div class="intro-bar">
        <div class="intro-left">
          <p class="script-slogan">让环境更美好<br />让公众参与更有力量</p>
          <div class="green-arc" aria-hidden="true"></div>
        </div>

        <div class="intro-center">
          <h1 id="entry-title">系统统一入口</h1>
          <p>请选择您所属的系统端，进入相应平台</p>
        </div>

        <div class="intro-right">
          <span class="eco-dash">——</span>
          <span class="eco-text">生态优先 · 绿色发展 · 共建共享</span>
        </div>
      </div>

      <div class="system-grid" aria-label="系统端入口">
        <article
          v-for="system in systems"
          :key="system.code"
          class="system-card"
          :class="`system-card--${system.icon}`"
        >
          <div class="system-icon-wrap">
            <div class="system-icon" :class="`system-icon--${system.icon}`" aria-hidden="true">
              <!-- NEPS: 人民/公众群体 -->
              <svg v-if="system.icon === 'public'" viewBox="0 0 48 48">
                <circle cx="24" cy="15" r="6" />
                <circle cx="11" cy="20" r="4.5" />
                <circle cx="37" cy="20" r="4.5" />
                <path d="M12 37c0-6 5.4-11 12-11s12 5 12 11" />
                <path d="M3 37c0-4.8 3.8-9 8.5-9.8" />
                <path d="M45 37c0-4.8-3.8-9-8.5-9.8" />
              </svg>
              <!-- NEPG: 经典佩戴安全帽的巡查网格员 -->
              <svg v-else-if="system.icon === 'grid'" viewBox="0 0 48 48">
                <path d="M14 17.5c0-5.8 4.5-10.5 10-10.5s10 4.7 10 10.5v2H14v-2z" />
                <path d="M9 19.5h30a2 2 0 0 1 2 2v1.5H7v-1.5a2 2 0 0 1 2-2z" />
                <circle cx="24" cy="28" r="4.8" />
                <path d="M11 41c0-5.8 5.6-10 13-10s13 4.2 13 10" />
              </svg>
              <!-- NEPM: 经典8齿机械齿轮 -->
              <svg v-else-if="system.icon === 'admin'" viewBox="0 0 48 48">
                <path
                  d="M21 5h6v5.2a14.8 14.8 0 0 1 4.2 1.7l3.7-3.7 4.2 4.2-3.7 3.7c.7 1.3 1.3 2.7 1.7 4.2H42v6h-5.2a14.8 14.8 0 0 1-1.7 4.2l3.7 3.7-4.2 4.2-3.7-3.7a14.8 14.8 0 0 1-4.2 1.7V43h-6v-5.2a14.8 14.8 0 0 1-4.2-1.7l-3.7 3.7-4.2-4.2 3.7-3.7a14.8 14.8 0 0 1-1.7-4.2H6v-6h5.2a14.8 14.8 0 0 1 1.7-4.2l-3.7-3.7 4.2-4.2 3.7 3.7a14.8 14.8 0 0 1 4.2-1.7V5zm3 12a7 7 0 1 0 0 14 7 7 0 0 0 0-14z"
                />
              </svg>
              <!-- NEPV: 决策者大屏与折线走势 -->
              <svg v-else viewBox="0 0 48 48">
                <rect
                  x="6"
                  y="8"
                  width="36"
                  height="25"
                  rx="3"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="3"
                />
                <polyline
                  points="13,23 20,16 26,21 35,12"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="3"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle cx="20" cy="16" r="2.2" />
                <circle cx="26" cy="21" r="2.2" />
                <circle cx="35" cy="12" r="2.2" />
                <path
                  d="M17 39h14M24 33v6"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="3"
                  stroke-linecap="round"
                />
              </svg>
            </div>
          </div>

          <h2>
            <strong>{{ system.code }}</strong> {{ system.title }}
          </h2>
          <p>{{ system.description }}</p>

          <button type="button" @click="enterSystem(system)">
            进入登录页 <span class="arrow" aria-hidden="true">→</span>
          </button>
        </article>
      </div>

      <div class="tip-bar">
        <div class="tip-line"></div>
        <div class="tip-pill" role="status" aria-live="polite">
          <svg class="tip-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"
            />
          </svg>
          <span>{{ notice || '请根据账号所属角色进入对应系统端' }}</span>
        </div>
        <div class="tip-line"></div>
      </div>
    </section>

    <footer class="site-footer">
      <div class="footer-left">
        © 2024 东软环保公众监督系统　版权所有　|　Neusoft Environmental Public Supervision System
      </div>
      <div class="footer-right">
        <span class="footer-dash">——</span>
        <span>科技赋能　绿色未来</span>
        <span class="footer-dash">——</span>
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

.entry-page {
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
  background: #f4f8fc;
  box-sizing: border-box;
}

/* 山水横幅背景：向下伸展覆盖至中下部，通透清澈展示湖泊水面 */
.landscape-banner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 70%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 25%;
  opacity: 0.98;
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.22) 0%,
    rgba(255, 255, 255, 0) 25%,
    rgba(244, 248, 252, 0.25) 60%,
    rgba(244, 248, 252, 0.82) 85%,
    rgba(244, 248, 252, 1) 100%
  );
}

/* 顶部导航：显著放大 Logo 尺寸与存在感 */
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

.brand-en {
  color: #3b5f88;
  font-size: clamp(10px, 0.8vw, 12px);
  font-weight: 500;
  letter-spacing: 0.03em;
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

/* 核心内容区 */
.entry-content {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: min(1360px, 92%);
  margin: 0 auto;
  padding: clamp(6px, 1vh, 16px) 0;
  box-sizing: border-box;
  min-height: 0;
}

/* 标语区：与卡片保持开阔间距，多显露湖光山色 */
.intro-bar {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 2%;
  margin-bottom: clamp(16px, 3.2vh, 36px);
  flex-shrink: 0;
}

.intro-left {
  text-align: center;
  padding-left: 2%;
}

.script-slogan {
  margin: 0;
  font-family: 'KaiTi', 'STKaiti', serif;
  color: #174b82;
  font-size: clamp(17px, 1.5vw, 24px);
  line-height: 1.45;
  transform: rotate(-5deg);
}

.green-arc {
  width: clamp(130px, 15vw, 200px);
  height: 10px;
  margin: 6px auto 0;
  border-top: 4px solid #30b663;
  border-radius: 50%;
  transform: rotate(-7deg);
}

.intro-center {
  text-align: center;
}

h1 {
  margin: 0;
  color: #082d5a;
  font-size: clamp(30px, 2.9vw, 46px);
  font-weight: 800;
  letter-spacing: 0.12em;
  line-height: 1.15;
}

.intro-center p {
  margin: clamp(5px, 0.8vh, 10px) 0 0;
  color: #365375;
  font-size: clamp(13.5px, 1.1vw, 17.5px);
  letter-spacing: 0.04em;
}

.intro-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  color: #2f7a6c;
  font-size: clamp(12.5px, 0.95vw, 15.5px);
  letter-spacing: 0.14em;
  padding-right: 2%;
}

.eco-dash {
  color: #79a89c;
  letter-spacing: -0.1em;
}

/* 四端卡片网格：适度收敛高度与宽度，让背景更加开阔生动 */
.system-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: clamp(16px, 1.9vw, 30px);
  width: min(1260px, 100%);
  margin: 0 auto;
  flex-shrink: 0;
}

.system-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: clamp(20px, 2.8vh, 28px) clamp(16px, 1.6vw, 24px) clamp(18px, 2.4vh, 24px);
  height: clamp(305px, 37vh, 355px);
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid rgba(175, 198, 224, 0.38);
  box-shadow: 0 10px 28px rgba(22, 54, 92, 0.08);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
  box-sizing: border-box;
}

.system-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 36px rgba(22, 54, 92, 0.15);
}

/* 各卡片底部微彩色强调边框线 */
.system-card--public {
  border-bottom: 5px solid #82d8a7;
}

.system-card--grid {
  border-bottom: 5px solid #8ec0f5;
}

.system-card--admin {
  border-bottom: 5px solid #b2c1d6;
}

.system-card--screen {
  border-bottom: 5px solid #82d8a7;
}

.system-icon-wrap {
  margin-bottom: clamp(6px, 1vh, 12px);
}

/* 大号圆形图标容器与图形饱满度 */
.system-icon {
  width: clamp(72px, 8.4vh, 88px);
  height: clamp(72px, 8.4vh, 88px);
  display: grid;
  place-items: center;
  border-radius: 50%;
  flex-shrink: 0;
}

.system-icon--public {
  background: #eaf8f1;
  color: #16a34a;
}

.system-icon--grid {
  background: #eff6ff;
  color: #2563eb;
}

.system-icon--admin {
  background: #f1f5f9;
  color: #64748b;
}

.system-icon--screen {
  background: #eaf8f1;
  color: #16a34a;
}

.system-icon svg {
  width: 55%;
  height: 55%;
  fill: currentColor;
}

/* 标题与代码字号比例对齐 */
h2 {
  margin: 0;
  color: #072b5c;
  text-align: center;
  font-size: clamp(17.5px, 1.4vw, 21.5px);
  font-weight: 700;
  white-space: nowrap;
}

h2 strong {
  letter-spacing: 0.04em;
  margin-right: 4px;
}

.system-card p {
  flex: 1;
  margin: clamp(8px, 1.2vh, 14px) 0 clamp(10px, 1.6vh, 16px);
  color: #4b627d;
  font-size: clamp(12.5px, 0.92vw, 14px);
  line-height: 1.55;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

button {
  width: 86%;
  height: clamp(38px, 4.6vh, 44px);
  border: 0;
  border-radius: 7px;
  padding: 0 16px;
  color: #ffffff;
  background: var(--primary-blue);
  box-shadow: 0 4px 10px rgba(35, 117, 201, 0.25);
  font: inherit;
  font-weight: 600;
  font-size: clamp(14px, 1.02vw, 16px);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition:
    background 0.18s ease,
    transform 0.18s ease;
  flex-shrink: 0;
}

button:hover {
  background: var(--hover-blue);
  transform: translateY(-1px);
}

button .arrow {
  font-size: clamp(16px, 1.2vw, 20px);
  line-height: 1;
}

/* 底部提示胶囊与分割线 */
.tip-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: clamp(14px, 2.4vh, 28px);
  flex-shrink: 0;
}

.tip-line {
  flex: 1;
  max-width: 260px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(160, 185, 214, 0.6), transparent);
}

.tip-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #53759e;
  font-size: clamp(12px, 0.88vw, 13.5px);
  letter-spacing: 0.02em;
}

.tip-icon {
  width: 17px;
  height: 17px;
  color: #2b79cc;
  flex-shrink: 0;
}

/* 页脚 */
.site-footer {
  position: relative;
  z-index: 2;
  height: clamp(36px, 5.2vh, 48px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4.5%;
  color: #6d86a4;
  font-size: clamp(11.5px, 0.8vw, 13px);
  background: rgba(255, 255, 255, 0.9);
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
  color: #5e7b9b;
}

.footer-dash {
  color: #9cb1c7;
  letter-spacing: -0.1em;
}

@media (max-height: 620px) {
  .entry-page {
    height: auto;
    min-height: 100vh;
  }
}

@media (prefers-reduced-motion: reduce) {
  .system-card,
  button {
    transition: none;
  }
}
</style>
