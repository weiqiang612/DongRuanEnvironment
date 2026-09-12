<template>
  <div class="neps-page" @click="handleGlobalClick">
    <a class="skip-link" href="#main-content">跳至主要内容</a>

    <!-- 1. 顶部 Header (简洁、白底、轻分割线) -->
    <header class="site-header">
      <div class="header-inner">
        <!-- 左侧 Logo 与系统名称 -->
        <button class="brand" type="button" aria-label="返回首页" @click="switchPage('home')">
          <img :src="logo" alt="系统Logo" class="brand-logo" />
          <div class="brand-text">
            <strong class="system-title">东软环保公众监督系统</strong>
            <small class="portal-subtitle">NEPS 公众监督员端</small>
          </div>
        </button>

        <!-- 中间导航 -->
        <nav class="header-nav" aria-label="监督员主导航">
          <button
            :class="{ active: activePage === 'home' }"
            type="button"
            @click="switchPage('home')"
          >
            首页
          </button>
          <button
            :class="{ active: activePage === 'submit' }"
            type="button"
            @click="switchPage('submit')"
          >
            我要反馈
          </button>
          <button
            :class="{ active: activePage === 'mine' }"
            type="button"
            @click="switchPage('mine')"
          >
            我的反馈
          </button>
        </nav>

        <!-- 右侧标语与用户区 -->
        <div class="header-tools">
          <span class="header-slogan">— 公众参与 · 共建美丽中国 —</span>
          <span class="header-divider" aria-hidden="true"></span>
          <div class="account-menu" ref="accountMenuRef">
            <button
              class="account-trigger"
              type="button"
              :aria-expanded="accountMenuOpen"
              aria-haspopup="menu"
              @click.stop="accountMenuOpen = !accountMenuOpen"
            >
              <span class="user-avatar-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M12 12a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Zm-8 9a8 8 0 0 1 16 0" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </span>
              <span class="user-greeting">欢迎您</span>
              <svg class="dropdown-caret" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="m6 9 6 6 6-6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
            <transition name="dropdown-fade">
              <div v-if="accountMenuOpen" class="account-dropdown" role="menu" @click.stop>
                <div class="account-profile">
                  <div class="profile-name">公众监督员</div>
                  <div class="profile-role">NEPS 环保监督员账号</div>
                </div>
                <div class="dropdown-divider"></div>
                <button class="logout-item" type="button" role="menuitem" @click="openLogoutModal">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  <span>退出登录</span>
                </button>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </header>

    <!-- 2. Hero 环保主题区域 (高适中、轻蒙层、文字山水两不误) -->
    <section class="hero-section">
      <div class="hero-inner">
        <div class="hero-content">
          <p class="hero-eyebrow">NEPS · 环境空气质量公众监督</p>
          <h1 class="hero-title" tabindex="-1">{{ heroContent.title }}</h1>
          <p class="hero-subtitle">{{ heroContent.subtitle }}</p>
        </div>
        <div class="hero-slogan">
          <span>绿水青山</span>
          <span>就是金山银山</span>
        </div>
      </div>
    </section>

    <!-- 3. 首页主内容区 (自适应百分比栅格、负边距浮于水面) -->
    <main id="main-content" class="workspace">
      <!-- 提示条信息 -->
      <transition name="msg-fade">
        <div v-if="message" class="message-banner" :class="messageType" role="status">
          <span class="msg-icon">{{ messageType === 'success' ? '✓' : '!' }}</span>
          <span>{{ message }}</span>
        </div>
      </transition>

      <!-- ================= 首页视图：100% 对齐参考截图 ================= -->
      <div v-if="activePage === 'home'" class="home-view" aria-label="监督员首页">
        <!-- 左右双栏：左侧我要反馈，右侧概况+说明流程 -->
        <div class="home-grid">
          <!-- 左侧：“我要反馈”卡片 (水平标签紧凑表单) -->
          <section class="panel form-panel">
            <div class="panel-header">
              <div class="header-title-group">
                <span class="header-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 0 0 0-1.41l-2.34-2.34a.996.996 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                  </svg>
                </span>
                <h2 class="panel-title">我要反馈</h2>
              </div>
              <span class="header-extra">发现环境问题？请及时反馈</span>
            </div>

            <form class="feedback-form" @submit.prevent="submitForm">
              <!-- 第1行：省份、城市 (双列水平标签排布) -->
              <div class="form-row-dual-inline">
                <div class="inline-field">
                  <label class="field-label">省份 <span class="required">*</span></label>
                  <div class="custom-select">
                    <select v-model.number="form.provinceId" required @change="handleProvinceChange">
                      <option :value="null" disabled>请选择省份</option>
                      <option v-for="option in provinceOptions" :key="option.id" :value="option.id">
                        {{ option.name }}
                      </option>
                    </select>
                    <span class="select-arrow"></span>
                  </div>
                </div>

                <div class="inline-field">
                  <label class="field-label">城市 <span class="required">*</span></label>
                  <div class="custom-select">
                    <select v-model.number="form.cityId" required :disabled="form.provinceId === null">
                      <option :value="null" disabled>请选择城市</option>
                      <option v-for="option in cityOptions" :key="option.id" :value="option.id">
                        {{ option.name }}
                      </option>
                    </select>
                    <span class="select-arrow"></span>
                  </div>
                </div>
              </div>

              <!-- 第2行：详细地址 (水平标签) -->
              <div class="form-row-single">
                <label class="field-label">详细地址 <span class="required">*</span></label>
                <input
                  v-model.trim="form.address"
                  type="text"
                  required
                  maxlength="255"
                  class="custom-input"
                  placeholder="请输入详细地址（如街道、路口、建筑物等）"
                />
              </div>

              <!-- 第3行：预估AQI等级 (水平标签 + 6个并排浅色业务色块) -->
              <div class="form-row-single">
                <label class="field-label">预估AQI等级 <span class="required">*</span></label>
                <div class="aqi-grade-grid">
                  <label
                    v-for="grade in gradeOptions"
                    :key="grade.value"
                    :class="['aqi-grade-item', `grade-${grade.value}`, { active: form.estimatedGrade === grade.value }]"
                  >
                    <input
                      v-model="form.estimatedGrade"
                      type="radio"
                      :value="grade.value"
                      required
                      class="sr-only"
                    />
                    <span>{{ grade.label }}</span>
                  </label>
                </div>
              </div>

              <!-- 第4行：现场描述 (水平标签 + 多行输入) -->
              <div class="form-row-single align-start">
                <label class="field-label">现场描述 <span class="required">*</span></label>
                <div class="textarea-wrapper">
                  <textarea
                    v-model.trim="form.information"
                    required
                    maxlength="500"
                    class="custom-textarea"
                    placeholder="请描述现场情况，如异味、烟尘、扬尘等（不少于10个字）"
                  ></textarea>
                  <span class="textarea-counter">{{ form.information.length }}/500</span>
                </div>
              </div>

              <!-- 第5行：提交反馈按钮 (全宽主操作按钮) -->
              <div class="form-submit-row">
                <button class="primary-submit-btn" type="submit" :disabled="submitting">
                  {{ submitting ? '提交中…' : '提交反馈' }}
                </button>
              </div>
            </form>
          </section>

          <!-- 右侧：概况卡片 + 流程说明卡片 -->
          <div class="home-sidebar">
            <!-- 右上：“我的反馈概况” (3 个并排浅底指标) -->
            <section class="panel overview-panel">
              <div class="panel-header">
                <div class="header-title-group">
                  <span class="header-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M3 13h4v8H3zm7-8h4v16h-4zm7 4h4v12h-4z" />
                    </svg>
                  </span>
                  <h2 class="panel-title">我的反馈概况</h2>
                </div>
                <span class="header-extra">截至目前</span>
              </div>
              <div class="overview-metric-grid">
                <article class="metric-card card-total">
                  <span class="metric-label">我的反馈总数</span>
                  <div class="metric-value">
                    <strong class="num-total">{{ summary.total }}</strong>
                    <span class="unit">条</span>
                  </div>
                </article>
                <article class="metric-card card-pending">
                  <span class="metric-label">待指派</span>
                  <div class="metric-value">
                    <strong class="num-pending">{{ summary.pending }}</strong>
                    <span class="unit">条</span>
                  </div>
                </article>
                <article class="metric-card card-complete">
                  <span class="metric-label">已完成</span>
                  <div class="metric-value">
                    <strong class="num-complete">{{ summary.completed }}</strong>
                    <span class="unit">条</span>
                  </div>
                </article>
              </div>
            </section>

            <!-- 右下：“反馈处理说明”流程卡片 (严谨专业政务风格) -->
            <section class="panel process-panel">
              <div class="panel-header">
                <div class="header-title-group">
                  <span class="header-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z" />
                    </svg>
                  </span>
                  <h2 class="panel-title">反馈处理说明</h2>
                </div>
                <span class="header-extra">您的反馈将按以下流程进行处理</span>
              </div>

              <div class="flow-container">
                <!-- 节点 1 -->
                <div class="flow-node">
                  <div class="flow-circle circle-pending">1</div>
                  <strong class="flow-name name-pending">待指派</strong>
                  <p class="flow-desc">反馈已提交<br />等待相关部门指派</p>
                </div>

                <div class="flow-connector">&gt;</div>

                <!-- 节点 2 -->
                <div class="flow-node">
                  <div class="flow-circle circle-assigned">2</div>
                  <strong class="flow-name name-assigned">已指派</strong>
                  <p class="flow-desc">相关部门已受理<br />正在处理</p>
                </div>

                <div class="flow-connector">&gt;</div>

                <!-- 节点 3 -->
                <div class="flow-node">
                  <div class="flow-circle circle-complete">3</div>
                  <strong class="flow-name name-complete">已完成</strong>
                  <p class="flow-desc">处理完成<br />并生成最终AQI结果</p>
                </div>

                <div class="flow-connector">&gt;</div>

                <!-- 节点 4 -->
                <div class="flow-node">
                  <div class="flow-circle circle-timeout">!</div>
                  <strong class="flow-name name-timeout">已超时</strong>
                  <p class="flow-desc">超过规定时间<br />仍未完成处理</p>
                </div>
              </div>
            </section>
          </div>
        </div>

        <!-- 4. 底部通栏“我的反馈”记录表格卡片 -->
        <section class="panel table-panel">
          <div class="panel-header">
            <div class="header-title-group">
              <span class="header-icon">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H5v-4h9v4zm0-6H5V7h9v4zm5 6h-4v-4h4v4zm0-6h-4V7h4v4z" />
                </svg>
              </span>
              <h2 class="panel-title">我的反馈</h2>
            </div>
            <span class="header-extra">共 {{ recentFeedbackList.length }} 条</span>
          </div>

          <div class="table-responsive">
            <table class="gov-table">
              <thead>
                <tr>
                  <th style="width: 64px;" class="text-center">序号</th>
                  <th style="width: 32%;">地址</th>
                  <th style="width: 17%;" class="text-center">提交时间</th>
                  <th style="width: 12%;" class="text-center">当前状态</th>
                  <th style="width: 13%;" class="text-center">我的预估</th>
                  <th style="width: 11%;" class="text-center">最终AQI</th>
                  <th style="width: 13%;" class="text-center">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="feedbackLoading || feedbackError || recentFeedbackList.length === 0">
                  <td colspan="7" class="text-center empty-cell">
                    {{ feedbackLoading ? '正在加载真实反馈记录…' : feedbackError || '暂无反馈记录' }}
                  </td>
                </tr>
                <tr v-for="(item, index) in recentFeedbackList" :key="item.id">
                  <td class="text-center">{{ index + 1 }}</td>
                  <td class="address-col">{{ item.address }}</td>
                  <td class="text-center time-col">{{ item.time }}</td>
                  <td class="text-center">
                    <span class="status-pill" :class="item.statusClass">{{ item.statusText }}</span>
                  </td>
                  <td class="text-center">
                    <span class="grade-pill" :class="item.estimateGradeClass">{{ item.estimateGradeText }}</span>
                  </td>
                  <td class="text-center">
                    <span v-if="item.finalAqiText !== '-'" class="grade-pill" :class="item.finalAqiClass">
                      {{ item.finalAqiText }}
                    </span>
                    <span v-else class="text-dash">-</span>
                  </td>
                  <td class="text-center">
                    <button class="view-detail-btn" type="button" @click="viewDetail(item)">
                      <span>查看详情</span>
                      <span class="btn-arrow">&gt;</span>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <!-- ================= 我要反馈视图 ================= -->
      <div v-else-if="activePage === 'submit'" class="submit-page-view">
        <div class="home-grid">
          <section class="panel form-panel">
            <div class="panel-header header-with-desc">
              <div class="header-main-title">
                <span class="header-icon large-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 0 0 0-1.41l-2.34-2.34a.996.996 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                  </svg>
                </span>
                <div class="header-titles">
                  <h2 class="panel-title main-title-text">我要反馈</h2>
                  <p class="panel-desc">发现身边的环境问题？请填写以下信息，您的反馈将有助于我们更好地守护美丽家园。</p>
                </div>
              </div>
            </div>

            <form class="feedback-form" @submit.prevent="submitForm">
              <div class="form-row-dual-inline">
                <div class="inline-field">
                  <label class="field-label">省份 <span class="required">*</span></label>
                  <div class="custom-select">
                    <select v-model.number="form.provinceId" required @change="handleProvinceChange">
                      <option :value="null" disabled>请选择省份</option>
                      <option v-for="option in provinceOptions" :key="option.id" :value="option.id">
                        {{ option.name }}
                      </option>
                    </select>
                    <span class="select-arrow"></span>
                  </div>
                </div>

                <div class="inline-field">
                  <label class="field-label">城市 <span class="required">*</span></label>
                  <div class="custom-select">
                    <select v-model.number="form.cityId" required :disabled="form.provinceId === null">
                      <option :value="null" disabled>请选择城市</option>
                      <option v-for="option in cityOptions" :key="option.id" :value="option.id">
                        {{ option.name }}
                      </option>
                    </select>
                    <span class="select-arrow"></span>
                  </div>
                </div>
              </div>

              <div class="form-row-single">
                <label class="field-label">详细地址 <span class="required">*</span></label>
                <input
                  v-model.trim="form.address"
                  type="text"
                  required
                  maxlength="255"
                  class="custom-input"
                  placeholder="请输入详细地址（如街道、路口、建筑物等）"
                />
              </div>

              <div class="form-row-single aqi-choice-row">
                <label class="field-label">预估AQI等级 <span class="required">*</span></label>
                <div class="aqi-field-wrapper">
                  <div class="aqi-grade-grid">
                    <label
                      v-for="grade in gradeOptions"
                      :key="grade.value"
                      :class="['aqi-grade-item', `grade-${grade.value}`, { active: form.estimatedGrade === grade.value }]"
                    >
                      <input
                        v-model="form.estimatedGrade"
                        type="radio"
                        :value="grade.value"
                        required
                        class="sr-only"
                      />
                      <span>{{ grade.label }}</span>
                    </label>
                  </div>
                  <div class="aqi-field-hint">
                    <svg class="hint-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z" />
                    </svg>
                    <span>请根据现场实际情况，选择您认为的空气质量等级。</span>
                  </div>
                </div>
              </div>

              <div class="form-row-single align-start">
                <label class="field-label">现场描述 <span class="required">*</span></label>
                <div class="textarea-wrapper">
                  <textarea
                    v-model.trim="form.information"
                    required
                    maxlength="500"
                    class="custom-textarea"
                    placeholder="请描述现场情况，如异味、烟尘、扬尘等（不少于10个字）"
                  ></textarea>
                  <span class="textarea-counter">{{ form.information.length }}/500</span>
                </div>
              </div>

              <div class="form-submit-row">
                <button class="primary-submit-btn" type="submit" :disabled="submitting">
                  {{ submitting ? '提交中…' : '提交反馈' }}
                </button>
              </div>
            </form>
          </section>

          <!-- 右侧竖向说明与温馨提示 -->
          <section class="panel process-panel vertical-guide">
            <div class="panel-header">
              <div class="header-title-group">
                <span class="header-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z" />
                  </svg>
                </span>
                <h2 class="panel-title">反馈处理说明</h2>
              </div>
            </div>
            <div class="vertical-steps">
              <div class="vertical-step">
                <div class="step-badge">1</div>
                <div class="step-detail">
                  <strong class="step-name">提交反馈</strong>
                  <p class="step-text">请如实填写现场信息并提交，我们将第一时间接收您的反馈。</p>
                </div>
              </div>
              <div class="vertical-step">
                <div class="step-badge">2</div>
                <div class="step-detail">
                  <strong class="step-name">核实处理</strong>
                  <p class="step-text">相关部门将对您反映的问题进行核实，并按规定开展调查处理。</p>
                </div>
              </div>
              <div class="vertical-step">
                <div class="step-badge">3</div>
                <div class="step-detail">
                  <strong class="step-name">结果反馈</strong>
                  <p class="step-text">处理完成后，您可在“我的反馈”中查看办理进度和结果。</p>
                </div>
              </div>
            </div>

            <!-- 下方：温馨提示 -->
            <div class="side-tips-box">
              <div class="side-tips-header">
                <span class="tip-lamp-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7zm2.85 11.1l-.85.6V16h-4v-2.3l-.85-.6C7.8 12.16 7 10.63 7 9c0-2.76 2.24-5 5-5s5 2.24 5 5c0 1.63-.8 3.16-2.15 4.1z" />
                  </svg>
                </span>
                <h3 class="side-tips-title">温馨提示</h3>
              </div>
              <ul class="side-tips-list">
                <li>请尽量提供准确的地点和详细的现场描述。</li>
                <li>如有照片等更多信息，可在描述中注明。</li>
                <li>我们会严格保护您的个人信息。</li>
                <li>感谢您的积极参与，让我们共同守护蓝天碧水！</li>
              </ul>
            </div>
          </section>
        </div>
      </div>

      <!-- ================= 我的反馈视图 ================= -->
      <div v-else class="mine-page-view">
        <section class="panel table-panel">
          <div class="panel-header">
            <div class="header-title-group">
              <span class="header-icon">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H5v-4h9v4zm0-6H5V7h9v4zm5 6h-4v-4h4v4zm0-6h-4V7h4v4z" />
                </svg>
              </span>
              <h2 class="panel-title">我的反馈全部记录</h2>
            </div>
            <span class="header-extra">共 {{ recentFeedbackList.length }} 条</span>
          </div>

          <div class="table-responsive">
            <table class="gov-table">
              <thead>
                <tr>
                  <th style="width: 64px;" class="text-center">序号</th>
                  <th style="width: 33%;">地址</th>
                  <th style="width: 17%;" class="text-center">提交时间</th>
                  <th style="width: 12%;" class="text-center">当前状态</th>
                  <th style="width: 13%;" class="text-center">我的预估</th>
                  <th style="width: 11%;" class="text-center">最终AQI</th>
                  <th style="width: 12%;" class="text-center">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="feedbackLoading || feedbackError || recentFeedbackList.length === 0">
                  <td colspan="7" class="text-center empty-cell">
                    {{ feedbackLoading ? '正在加载真实反馈记录…' : feedbackError || '暂无反馈记录' }}
                  </td>
                </tr>
                <tr v-for="(item, index) in recentFeedbackList" :key="item.id">
                  <td class="text-center">{{ index + 1 }}</td>
                  <td class="address-col">{{ item.address }}</td>
                  <td class="text-center time-col">{{ item.time }}</td>
                  <td class="text-center">
                    <span class="status-pill" :class="item.statusClass">{{ item.statusText }}</span>
                  </td>
                  <td class="text-center">
                    <span class="grade-pill" :class="item.estimateGradeClass">{{ item.estimateGradeText }}</span>
                  </td>
                  <td class="text-center">
                    <span v-if="item.finalAqiText !== '-'" class="grade-pill" :class="item.finalAqiClass">
                      {{ item.finalAqiText }}
                    </span>
                    <span v-else class="text-dash">-</span>
                  </td>
                  <td class="text-center">
                    <button class="view-detail-btn" type="button" @click="viewDetail(item)">
                      <span>查看详情</span>
                      <span class="btn-arrow">&gt;</span>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </main>

    <!-- 查看详情弹窗 -->
    <transition name="dialog-fade">
      <div
        v-if="selectedDetail"
        class="modal-backdrop"
        role="dialog"
        aria-modal="true"
        @click.self="selectedDetail = null"
      >
        <div class="modal-card">
          <button class="modal-close" type="button" aria-label="关闭详情" @click="selectedDetail = null">×</button>
          <div class="modal-header">
            <span class="header-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H5v-4h9v4zm0-6H5V7h9v4zm5 6h-4v-4h4v4zm0-6h-4V7h4v4z" />
              </svg>
            </span>
            <h3>反馈详情</h3>
          </div>
          <div class="modal-body">
            <div class="detail-item">
              <span class="detail-label">处理状态</span>
              <span class="detail-value">
                <span class="status-pill" :class="selectedDetail.statusClass">{{ selectedDetail.statusText }}</span>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">提交时间</span>
              <span class="detail-value">{{ selectedDetail.time }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">预估等级</span>
              <span class="detail-value">
                <span class="grade-pill" :class="selectedDetail.estimateGradeClass">{{ selectedDetail.estimateGradeText }}</span>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">详细地址</span>
              <span class="detail-value">{{ selectedDetail.address }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">现场描述</span>
              <div class="detail-desc-box">{{ selectedDetail.desc }}</div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn" type="button" @click="selectedDetail = null">知道了</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 底部 Footer -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-left">
          <span>东软集团股份有限公司</span>
          <span class="sep">|</span>
          <span>版权所有</span>
          <span class="sep">|</span>
          <span>京ICP备xxxxxxxx号</span>
        </div>
        <div class="footer-right">
          <span>绿色科技 创造美好未来</span>
        </div>
      </div>
    </footer>

    <!-- 退出登录二次确认弹窗 -->
    <Teleport to="body">
      <div v-if="confirmLogoutVisible" class="neps-modal-backdrop" @click="closeLogoutModal">
        <div class="neps-modal-box" role="dialog" aria-modal="true" @click.stop>
          <div class="neps-modal-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#dc2626" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
          </div>
          <h3 class="neps-modal-title">退出登录确认</h3>
          <p class="neps-modal-desc">确定要退出公众监督员端吗？退出后需重新登录。</p>
          <div class="neps-modal-actions">
            <button type="button" class="btn-modal-cancel" @click="closeLogoutModal">取消</button>
            <button
              type="button"
              class="btn-modal-confirm"
              :disabled="loggingOut"
              @click="handleConfirmLogout"
            >
              {{ loggingOut ? '退出中…' : '确认退出' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import type { AxiosError } from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getAqiFeedbackList, saveAqiFeedback, type AqiFeedbackRow } from '@/api/aqiFeedback'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'
import { logoutSession } from '@/api/session'
import logo from '@/assets/ChatGPT Image Sep 8, 2026, 04_32_09 PM (1).png'

type PageName = 'home' | 'submit' | 'mine'
const router = useRouter()
const activePage = ref<PageName>('home')

// 顶部 Hero 内容
const heroContent = computed(() => {
  if (activePage.value === 'mine') {
    return { title: '我的反馈', subtitle: '关注身边的环境问题，共同守护蓝天碧水' }
  }
  return { title: '共同守护蓝天碧水', subtitle: '您的每一次反馈，都是更美好环境的力量' }
})

// 表单状态定义
interface FeedbackFormState {
  provinceId: number | null
  cityId: number | null
  address: string
  estimatedGrade: number | null
  information: string
}

const form = ref<FeedbackFormState>({
  provinceId: null,
  cityId: null,
  address: '',
  estimatedGrade: null,
  information: '',
})

const submitting = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')
const accountMenuOpen = ref(false)
const accountMenuRef = ref<HTMLElement | null>(null)

const provinceOptions = ref<RegionOption[]>([])
const cityOptions = ref<RegionOption[]>([])

async function handleProvinceChange() {
  form.value.cityId = null
  cityOptions.value = []
  if (!form.value.provinceId) return
  try {
    cityOptions.value = (await getCityOptions(form.value.provinceId)).data.data
  } catch {
    showToast('城市选项加载失败，请稍后重试', 'error')
  }
}

// 预估 AQI 6级标准选项
const gradeOptions = [
  { value: 1, label: '优' },
  { value: 2, label: '良' },
  { value: 3, label: '轻度污染' },
  { value: 4, label: '中度污染' },
  { value: 5, label: '重度污染' },
  { value: 6, label: '严重污染' },
]

const summary = ref({
  total: 0,
  pending: 0,
  completed: 0,
})

interface FeedbackRecordItem {
  id: number
  address: string
  time: string
  statusText: string
  statusClass: string
  estimateGradeText: string
  estimateGradeClass: string
  finalAqiText: string
  finalAqiClass: string
  desc: string
}

const recentFeedbackList = ref<FeedbackRecordItem[]>([])
const feedbackLoading = ref(false)
const feedbackError = ref('')

const gradeNames = ['', '优', '良', '轻度污染', '中度污染', '重度污染', '严重污染']
function formatTime(row: AqiFeedbackRow) {
  const value = row.submittedAt || (row.afDate && row.afTime ? `${row.afDate} ${row.afTime}` : '')
  return value ? value.replace('T', ' ').slice(0, 16) : '—'
}
function toRecord(row: AqiFeedbackRow): FeedbackRecordItem {
  const timeout = Boolean(row.timeoutFlag)
  const state = row.state ?? 0
  return {
    id: row.afId,
    address: row.address,
    time: formatTime(row),
    statusText: timeout ? '已超时' : ['待指派', '已指派', '已完成'][state] || '—',
    statusClass: timeout ? 'status-timeout' : ['status-pending', 'status-assigned', 'status-complete'][state] || '',
    estimateGradeText: gradeNames[row.estimatedGrade ?? 0] || '—',
    estimateGradeClass: `grade-${row.estimatedGrade ?? 0}`,
    finalAqiText: gradeNames[row.finalAqiId ?? 0] || '-',
    finalAqiClass: row.finalAqiId ? `grade-${row.finalAqiId}` : '',
    desc: row.information,
  }
}
async function loadFeedbacks() {
  feedbackLoading.value = true
  feedbackError.value = ''
  try {
    const rows = (await getAqiFeedbackList()).data.data
    recentFeedbackList.value = rows.map(toRecord)
    summary.value = {
      total: rows.length,
      pending: rows.filter((item) => item.state === 0).length,
      completed: rows.filter((item) => item.state === 2).length,
    }
  } catch (error) {
    feedbackError.value = (error as AxiosError<{ message?: string }>).response?.data?.message || '反馈记录加载失败，请稍后重试'
  } finally {
    feedbackLoading.value = false
  }
}

// 详情弹窗
const selectedDetail = ref<FeedbackRecordItem | null>(null)
function viewDetail(item: FeedbackRecordItem) {
  selectedDetail.value = item
}

// 页面切换
function switchPage(page: PageName) {
  activePage.value = page
  accountMenuOpen.value = false
  if (page === 'mine') void loadFeedbacks()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 提示消息
function showToast(text: string, type: 'success' | 'error' = 'success') {
  message.value = text
  messageType.value = type
  window.setTimeout(() => {
    if (message.value === text) {
      message.value = ''
    }
  }, 3500)
}

async function submitForm() {
  if (!form.value.provinceId || !form.value.cityId) {
    showToast('请选择省份和城市', 'error')
    return
  }
  if (!form.value.address) {
    showToast('请输入详细地址', 'error')
    return
  }
  if (!form.value.estimatedGrade) {
    showToast('请选择预估AQI等级', 'error')
    return
  }
  if (!form.value.information || form.value.information.length < 10) {
    showToast('现场描述不少于10个字', 'error')
    return
  }

  submitting.value = true
  try {
    const response = await saveAqiFeedback(form.value)
    if (response.data.code !== 200) throw new Error(response.data.message)
    showToast('反馈已提交成功，感谢您的参与！', 'success')
    form.value.address = ''
    form.value.estimatedGrade = null
    form.value.information = ''
    await loadFeedbacks()
    activePage.value = 'mine'
  } catch (error) {
    showToast((error as AxiosError<{ message?: string }>).response?.data?.message || '提交失败，请稍后重试', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    provinceOptions.value = (await getProvinceOptions()).data.data
  } catch {
    showToast('省份选项加载失败，请刷新后重试', 'error')
  }
  await loadFeedbacks()
})

// 退出登录：二次确认模态弹窗
const confirmLogoutVisible = ref(false)
const loggingOut = ref(false)

function openLogoutModal() {
  accountMenuOpen.value = false
  confirmLogoutVisible.value = true
}

function closeLogoutModal() {
  confirmLogoutVisible.value = false
}

async function handleConfirmLogout() {
  loggingOut.value = true
  try {
    await logoutSession()
  } catch {
    // 忽略异常
  } finally {
    confirmLogoutVisible.value = false
    loggingOut.value = false
    void router.replace('/')
  }
}



function handleGlobalClick(event: MouseEvent) {
  if (accountMenuRef.value && !accountMenuRef.value.contains(event.target as Node)) {
    accountMenuOpen.value = false
  }
}
</script>

<style scoped>
/* ================= 全局基础变量与政务系统设计令牌 ================= */
.neps-page {
  --primary-blue: #1e75d8;
  --primary-hover: #1664bc;
  --gov-navy: #15325b;
  --text-primary: #1f2d3d;
  --text-secondary: #5a6e85;
  --text-placeholder: #8c9ba5;
  --border-light: #dde5ee;
  --border-focus: #1e75d8;
  --bg-page: #f4f7fa;
  --bg-card: #ffffff;
  --card-shadow: 0 2px 8px rgba(15, 35, 60, 0.04);

  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  background-color: var(--bg-page);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  box-sizing: border-box;
}

*, *::before, *::after {
  box-sizing: border-box;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

.skip-link {
  position: fixed;
  top: -50px;
  left: 20px;
  z-index: 1000;
  padding: 6px 12px;
  background: var(--primary-blue);
  color: #fff;
  font-size: 13px;
  border-radius: 4px;
  transition: top 0.2s;
}
.skip-link:focus {
  top: 10px;
}

/* ================= 1. 顶部 Header ================= */
.site-header {
  position: relative;
  z-index: 50;
  width: 100%;
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e5ecf4;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(94vw, 1460px);
  height: 100%;
  margin: 0 auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  text-align: left;
}

.brand-logo {
  width: 38px;
  height: 38px;
  object-fit: contain;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.system-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--gov-navy);
  letter-spacing: 0.3px;
  line-height: 1.2;
}

.portal-subtitle {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-top: 2px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 36px;
  height: 100%;
}

.header-nav button {
  position: relative;
  height: 100%;
  padding: 0 4px;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.15s ease;
}

.header-nav button:hover {
  color: var(--primary-blue);
}

.header-nav button.active {
  color: var(--primary-blue);
  font-weight: 700;
}

.header-nav button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--primary-blue);
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-slogan {
  font-size: 13px;
  color: #64809c;
  letter-spacing: 0.5px;
}

.header-divider {
  width: 1px;
  height: 16px;
  background: #d8e2ec;
}

.account-menu {
  position: relative;
}

.account-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  color: var(--gov-navy);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 4px;
  transition: background-color 0.15s;
}

.account-trigger:hover {
  background: #edf3fa;
}

.user-avatar-icon {
  display: flex;
  align-items: center;
  color: #3b5f88;
}

.user-avatar-icon svg {
  width: 18px;
  height: 18px;
}

.user-greeting {
  font-size: 13px;
}

.dropdown-caret {
  width: 13px;
  height: 13px;
  color: #7b94b2;
}

.account-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  width: 160px;
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 6px;
  box-shadow: 0 6px 18px rgba(15, 35, 60, 0.1);
  padding: 6px;
  z-index: 100;
}

.account-profile {
  padding: 6px 8px;
}

.profile-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--gov-navy);
}

.profile-role {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 1px;
}

.dropdown-divider {
  height: 1px;
  background: #edf2f7;
  margin: 4px 0;
}

.logout-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 8px;
  border: none;
  background: transparent;
  color: #c53030;
  font-size: 13px;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
  text-align: left;
}

.logout-item svg {
  width: 14px;
  height: 14px;
}

.logout-item:hover {
  background: #fdf2f2;
}

/* ================= 2. Hero 环保主题区域 ================= */
.hero-section {
  position: relative;
  width: 100%;
  height: clamp(260px, 22vw, 290px);
  background-image: url('@/assets/hero-bg.png');
  background-size: cover;
  background-position: center 23%;
  background-repeat: no-repeat;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.55) 0%,
    rgba(255, 255, 255, 0.25) 50%,
    rgba(255, 255, 255, 0.05) 100%
  );
}

.hero-inner {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: min(94vw, 1460px);
  margin: 0 auto;
  padding-top: 30px;
}

.hero-content {
  max-width: 680px;
}

.hero-eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #1b4272;
  letter-spacing: 0.6px;
}

.hero-title {
  margin: 8px 0 4px;
  font-size: clamp(28px, 3.2vw, 36px);
  font-weight: 800;
  color: #153965;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.hero-subtitle {
  margin: 0;
  font-size: clamp(14px, 1.3vw, 17px);
  font-weight: 500;
  color: #274c78;
}

.hero-slogan {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-top: 6px;
  font-family: 'STKaiti', 'KaiTi', 'SimSun', serif;
  font-size: 24px;
  font-weight: bold;
  color: #224775;
  line-height: 1.35;
  letter-spacing: 3px;
  transform: rotate(-3deg);
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.9);
}

/* ================= 3. 主内容区 (负边距上移悬浮、纯正政务卡片) ================= */
.workspace {
  position: relative;
  z-index: 10;
  width: min(94vw, 1460px);
  margin: -95px auto 36px;
}

.message-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  margin-bottom: 14px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.message-banner.success {
  background: #eaf8f0;
  border: 1px solid #bcead0;
  color: #1a6e3d;
}

.message-banner.error {
  background: #fdf2f2;
  border: 1px solid #f8cdcd;
  color: #b92529;
}

.msg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: currentColor;
  color: #fff;
  font-size: 11px;
}

/* 通用政务卡片 Panel (克制小圆角、浅边框) */
.panel {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 6px;
  box-shadow: var(--card-shadow);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid #edf2f8;
  background: #fafcfe;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 4px;
  background: var(--primary-blue);
  color: #ffffff;
  flex-shrink: 0;
}

.header-icon svg {
  width: 13px;
  height: 13px;
}

.panel-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-extra {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ================= 首页布局：左右双栏 + 底部通栏 ================= */
.home-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.home-grid {
  display: grid;
  grid-template-columns: minmax(0, 56%) minmax(0, 44%);
  gap: 16px;
}

.home-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 左侧：“我要反馈”表单 (水平标签紧凑排布) */
.feedback-form {
  padding: 16px 18px 18px;
}

.form-row-dual-inline {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 12px;
}

.inline-field {
  display: grid;
  grid-template-columns: 78px 1fr;
  align-items: center;
  gap: 8px;
}

.form-row-single {
  display: grid;
  grid-template-columns: 78px 1fr;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.form-row-single.align-start {
  align-items: start;
}

.form-row-single.align-start .field-label {
  padding-top: 8px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

.required {
  color: #d93025;
  margin-left: 2px;
}

.custom-select {
  position: relative;
  width: 100%;
}

.custom-select select,
.custom-input {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  background: #ffffff;
  border: 1px solid #c7d5e4;
  border-radius: 4px;
  color: var(--text-primary);
  font-size: 13px;
  outline: none;
  transition: border-color 0.15s;
}

.custom-select select {
  appearance: none;
  cursor: pointer;
  padding-right: 28px;
}

.select-arrow {
  position: absolute;
  top: 50%;
  right: 10px;
  width: 6px;
  height: 6px;
  border-right: 2px solid #7c93a8;
  border-bottom: 2px solid #7c93a8;
  transform: translateY(-65%) rotate(45deg);
  pointer-events: none;
}

.custom-select select:focus,
.custom-input:focus,
.custom-textarea:focus {
  border-color: var(--border-focus);
}

/* 预估 AQI 等级（浅色业务色块标签） */
.aqi-grade-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
}

.aqi-grade-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  border-radius: 4px;
  border: 2px solid transparent;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: border-color 0.15s;
  user-select: none;
}

/* AQI 浅色业务语义色 (100% 对照截图) */
.grade-1 { background-color: #d1f2d9; color: #1b6a3e; }
.grade-2 { background-color: #c4ebd0; color: #1b6a3e; }
.grade-3 { background-color: #fbe9b6; color: #7d5514; }
.grade-4 { background-color: #fcd9b8; color: #914216; }
.grade-5 { background-color: #fad0d0; color: #962828; }
.grade-6 { background-color: #e3d5f8; color: #59258a; }

.aqi-grade-item.active {
  border-color: var(--primary-blue);
  font-weight: 700;
}

/* 现场描述 Textarea */
.textarea-wrapper {
  position: relative;
  width: 100%;
}

.custom-textarea {
  width: 100%;
  height: 72px;
  padding: 8px 10px 20px;
  background: #ffffff;
  border: 1px solid #c7d5e4;
  border-radius: 4px;
  color: var(--text-primary);
  font-size: 13px;
  font-family: inherit;
  outline: none;
  resize: vertical;
}

.textarea-counter {
  position: absolute;
  right: 10px;
  bottom: 4px;
  font-size: 11px;
  color: var(--text-placeholder);
  pointer-events: none;
}

/* 主提交按钮 */
.form-submit-row {
  margin-top: 6px;
}

.primary-submit-btn {
  width: 100%;
  height: 38px;
  background: var(--primary-blue);
  border: none;
  border-radius: 4px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.15s;
}

.primary-submit-btn:hover:not(:disabled) {
  background: var(--primary-hover);
}

.primary-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ================= 右上：“我的反馈概况” ================= */
.overview-metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px 16px;
}

.metric-card {
  padding: 10px 14px;
  background: #fbfdff;
  border: 1px solid #e1ecf6;
  border-radius: 4px;
}

.metric-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.metric-value {
  display: flex;
  align-items: baseline;
  margin-top: 4px;
}

.metric-value strong {
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}

.num-total { color: #1e75d8; }
.num-pending { color: #d97706; }
.num-complete { color: #15803d; }

.unit {
  font-size: 12px;
  color: #7890a8;
  margin-left: 4px;
}

/* ================= 右下：“反馈处理说明”流程卡片 ================= */
.flow-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 14px;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
}

.flow-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 5px;
}

.circle-pending { background: #2a72b5; }
.circle-assigned { background: #3b82c4; }
.circle-complete { background: #2e8555; }
.circle-timeout { background: #c0392b; }

.flow-name {
  display: block;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 2px;
}

.name-pending { color: #2a72b5; }
.name-assigned { color: #2367a6; }
.name-complete { color: #2e8555; }
.name-timeout { color: #c0392b; }

.flow-desc {
  margin: 0;
  font-size: 11px;
  color: #6c8096;
  line-height: 1.35;
}

.flow-connector {
  color: #a3b6c8;
  font-size: 15px;
  font-weight: bold;
  padding: 0 4px;
  margin-top: -18px;
}

/* ================= 4. 底部“我的反馈”记录表格卡片 ================= */
.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.gov-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  text-align: left;
}

.gov-table th {
  padding: 10px 14px;
  background: #f5f8fb;
  color: #476282;
  font-weight: 700;
  border-bottom: 1px solid var(--border-light);
  white-space: nowrap;
}

.gov-table td {
  padding: 11px 14px;
  border-bottom: 1px solid #edf2f7;
  color: var(--text-primary);
  vertical-align: middle;
}

.gov-table tbody tr:hover {
  background: #f8fbfe;
}

.empty-cell {
  padding: 28px !important;
  color: var(--text-secondary) !important;
}

.text-center { text-align: center; }

.address-col {
  font-weight: 500;
}

.time-col {
  color: #5c728a;
  font-size: 12px;
}

/* 状态胶囊标签 (政务色系) */
.status-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-pending { background: #e3effa; color: #1e75d8; }
.status-assigned { background: #e6ebf5; color: #2b5282; }
.status-complete { background: #dbf1e3; color: #15803d; }
.status-timeout { background: #fce4e4; color: #c0392b; }

/* AQI 胶囊标签 */
.grade-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.text-dash {
  color: #8c9ba5;
}

/* 查看详情操作按钮 */
.view-detail-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  background: transparent;
  border: none;
  color: #1e75d8;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}

.view-detail-btn:hover {
  text-decoration: underline;
}

.btn-arrow {
  font-size: 11px;
}

/* 我要反馈独立页面专属排版 */
.header-with-desc {
  padding: 14px 18px 12px;
}

.header-main-title {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.header-icon.large-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: var(--primary-blue);
  flex-shrink: 0;
}

.header-icon.large-icon svg {
  width: 18px;
  height: 18px;
}

.header-titles {
  display: flex;
  flex-direction: column;
}

.main-title-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--gov-navy);
  line-height: 1.2;
}

.panel-desc {
  margin: 5px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.45;
}

.aqi-choice-row {
  align-items: start;
}

.aqi-choice-row .field-label {
  padding-top: 8px;
}

.aqi-field-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.aqi-field-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #5a738e;
  margin-top: 2px;
}

.hint-icon {
  width: 14px;
  height: 14px;
  color: #1976d2;
  flex-shrink: 0;
}

.vertical-steps {
  padding: 18px 20px 10px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.vertical-step {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.step-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #1976d2;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 1px;
}

.step-detail {
  flex: 1;
}

.step-name {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: var(--gov-navy);
}

.step-text {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 右下：温馨提示模块 */
.side-tips-box {
  margin: 12px 20px 20px;
  padding-top: 14px;
  border-top: 1px dashed #dbe5ee;
}

.side-tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.tip-lamp-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  color: #1976d2;
}

.tip-lamp-icon svg {
  width: 16px;
  height: 16px;
}

.side-tips-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--gov-navy);
}

.side-tips-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.85;
  list-style-type: disc;
}

.side-tips-list li::marker {
  color: #1976d2;
}

/* 详情弹窗 Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 35, 60, 0.45);
  backdrop-filter: blur(3px);
  padding: 20px;
}

.modal-card {
  position: relative;
  width: 100%;
  max-width: 500px;
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 10px 30px rgba(15, 35, 60, 0.16);
  overflow: hidden;
}

.modal-close {
  position: absolute;
  top: 12px;
  right: 14px;
  background: transparent;
  border: none;
  font-size: 22px;
  color: #8c9ba5;
  cursor: pointer;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  border-bottom: 1px solid #edf2f7;
  background: #fafcfe;
}

.modal-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.modal-body {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: grid;
  grid-template-columns: 74px 1fr;
  gap: 10px;
  font-size: 13px;
}

.detail-label {
  font-weight: 600;
  color: var(--text-secondary);
}

.detail-value {
  color: var(--text-primary);
  font-weight: 500;
}

.detail-desc-box {
  padding: 8px 10px;
  background: #f7fafc;
  border: 1px solid #e2edf6;
  border-radius: 4px;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 10px 18px;
  background: #fafcfe;
  border-top: 1px solid #edf2f7;
}

.modal-btn {
  padding: 6px 18px;
  background: var(--primary-blue);
  border: none;
  border-radius: 4px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

/* ================= 底部 Footer ================= */
.site-footer {
  width: 100%;
  background: #ffffff;
  border-top: 1px solid #e5ecf4;
  padding: 16px 0;
}

.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(94vw, 1460px);
  margin: 0 auto;
  font-size: 12px;
  color: #6a829c;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sep {
  color: #d8e2ec;
}

/* 过渡动画 */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active,
.msg-fade-enter-active,
.msg-fade-leave-active,
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.15s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to,
.msg-fade-enter-from,
.msg-fade-leave-to,
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

/* ================= 响应式规则 (1366px / 1440px / 1600px / 1920px) ================= */
@media (max-width: 1200px) {
  .home-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-slogan,
  .header-divider,
  .hero-slogan {
    display: none;
  }
  .header-nav {
    gap: 18px;
  }
  .form-row-dual-inline {
    grid-template-columns: 1fr;
  }
  .form-row-single {
    grid-template-columns: 1fr;
  }
  .inline-field {
    grid-template-columns: 1fr;
  }
  .aqi-grade-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .flow-container {
    flex-direction: column;
    gap: 10px;
  }
  .flow-connector {
    transform: rotate(90deg);
    margin: 0;
  }
  .footer-inner {
    flex-direction: column;
    gap: 6px;
    text-align: center;
  }
}

/* 退出登录二次确认弹窗样式 */
.neps-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background-color: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  animation: modalFadeIn 0.2s ease-out;
}

.neps-modal-box {
  width: 100%;
  max-width: 380px;
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  animation: modalScaleUp 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.neps-modal-icon {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #fee2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.neps-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 8px;
}

.neps-modal-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px;
  line-height: 1.5;
}

.neps-modal-actions {
  display: flex;
  gap: 12px;
}

.btn-modal-cancel,
.btn-modal-confirm {
  flex: 1;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-modal-cancel {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
}
.btn-modal-cancel:hover {
  background: #e2e8f0;
}

.btn-modal-confirm {
  background: #dc2626;
  border: 1px solid #dc2626;
  color: #ffffff;
}
.btn-modal-confirm:hover {
  background: #b91c1c;
}
.btn-modal-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes modalScaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
