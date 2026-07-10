<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { LoaderCircle, GraduationCap, LogIn, Sparkles, ShieldCheck } from "@lucide/vue";

const emit = defineEmits(["login-success"]);

const username = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");
const focusedField = ref("");

// 粒子背景
const particles = ref([]);
let particleTimer = null;

function initParticles() {
  particles.value = Array.from({ length: 30 }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    y: Math.random() * 100,
    size: Math.random() * 3 + 1,
    speed: Math.random() * 0.3 + 0.1,
    opacity: Math.random() * 0.5 + 0.2,
    delay: Math.random() * 5,
  }));
}

onMounted(() => {
  initParticles();
  particleTimer = setInterval(() => {
    particles.value = particles.value.map(p => ({
      ...p,
      y: p.y - p.speed < -10 ? 110 : p.y - p.speed,
    }));
  }, 50);
});

onUnmounted(() => {
  if (particleTimer) clearInterval(particleTimer);
});

async function handleLogin() {
  if (!username.value || !password.value) {
    error.value = "请输入用户名和密码";
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify({username: username.value, password: password.value}),
    });
    const result = await response.json();
    if (!response.ok || result.code !== 200) {
      error.value = result.message || "登录失败";
      return;
    }
    const {token, learnerId, realName, role, institutionName} = result.data;
    const userInfo = {learnerId, username: result.data.username, realName, role, institutionName};
    localStorage.setItem("token", token);
    localStorage.setItem("userInfo", JSON.stringify(userInfo));
    emit("login-success", {token, userInfo});
  } catch (err) {
    error.value = "网络错误，请检查后端服务";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 动态粒子背景 -->
    <div class="particles-container">
      <div
          v-for="p in particles"
          :key="p.id"
          class="particle"
          :style="{
          left: p.x + '%',
          top: p.y + '%',
          width: p.size + 'px',
          height: p.size + 'px',
          opacity: p.opacity,
          animationDelay: p.delay + 's',
        }"
      />
    </div>

    <!-- 背景渐变层 -->
    <div class="gradient-bg"/>
    <div class="gradient-bg-2"/>

    <!-- 装饰性光晕 -->
    <div class="glow glow-1"/>
    <div class="glow glow-2"/>

    <!-- 主内容区 -->
    <div class="login-content">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-logo">
          <div class="logo-ring">
            <GraduationCap :size="48" class="logo-icon"/>
          </div>
          <div class="logo-glow"/>
        </div>
        <h1 class="brand-title">
          终身学习学分银行
          <span class="brand-badge">V2.0</span>
        </h1>
        <p class="brand-subtitle">Lifelong Learning Credit Bank</p>
        <div class="brand-features">
          <div class="feature-item">
            <ShieldCheck :size="18"/>
            <span>区块链存证</span>
          </div>
          <div class="feature-item">
            <Sparkles :size="18"/>
            <span>AI 学习助手</span>
          </div>
          <div class="feature-item">
            <LogIn :size="18"/>
            <span>终身学习档案</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-card-wrapper">
        <div class="login-card">
          <div class="card-shine"/>

          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账户以继续</p>
          </div>

          <form class="login-form" @submit.prevent="handleLogin">
            <!-- 错误提示 -->
            <Transition name="slide-down">
              <div v-if="error" class="login-error">
                <span class="error-dot"/>
                {{ error }}
              </div>
            </Transition>

            <!-- 用户名 -->
            <div
                class="input-group"
                :class="{ focused: focusedField === 'username', filled: username }"
            >
              <label class="input-label">
                <span class="label-text">用户名</span>
                <span class="label-required">*</span>
              </label>
              <div class="input-wrapper">
                <input
                    v-model="username"
                    type="text"
                    placeholder="请输入用户名"
                    autocomplete="username"
                    :disabled="loading"
                    @focus="focusedField = 'username'"
                    @blur="focusedField = ''"
                />
                <div class="input-line"/>
                <div class="input-line-active"/>
              </div>
            </div>

            <!-- 密码 -->
            <div
                class="input-group"
                :class="{ focused: focusedField === 'password', filled: password }"
            >
              <label class="input-label">
                <span class="label-text">密码</span>
                <span class="label-required">*</span>
              </label>
              <div class="input-wrapper">
                <input
                    v-model="password"
                    type="password"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                    :disabled="loading"
                    @focus="focusedField = 'password'"
                    @blur="focusedField = ''"
                    @keyup.enter="handleLogin"
                />
                <div class="input-line"/>
                <div class="input-line-active"/>
              </div>
            </div>

            <!-- 登录按钮 -->
            <button
                type="submit"
                class="login-btn"
                :disabled="loading"
                :class="{ loading }"
            >
              <div class="btn-bg"/>
              <span class="btn-content">
                <LoaderCircle v-if="loading" class="spin" :size="20"/>
                <LogIn v-else :size="20"/>
                {{ loading ? "登录中..." : "登 录" }}
              </span>
              <div class="btn-shine"/>
            </button>
          </form>

          <!-- 底部信息 -->
          <div class="login-footer">
            <div class="divider">
              <span>安全登录</span>
            </div>
            <p class="security-note">
              <ShieldCheck :size="14"/>
              您的数据已通过 SSL 加密传输
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="copyright">
      <span>© 2026 终身学习学分银行平台</span>
      <span class="dot"/>
      <span>教育部继续教育示范基地</span>
    </div>
  </div>
</template>

<style scoped>
/* ====== 基础布局 ====== */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: #0a0e1a;
  font-family: "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ====== 粒子背景 ====== */
.particles-container {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.4);
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(1.2);
  }
}

/* ====== 渐变背景 ====== */
.gradient-bg {
  position: fixed;
  inset: 0;
  background: radial-gradient(ellipse 80% 50% at 20% 40%, rgba(99, 102, 241, 0.15) 0%, transparent 50%),
  radial-gradient(ellipse 60% 40% at 80% 60%, rgba(139, 92, 246, 0.12) 0%, transparent 50%),
  radial-gradient(ellipse 50% 30% at 50% 100%, rgba(59, 130, 246, 0.08) 0%, transparent 50%);
  z-index: 0;
}

.gradient-bg-2 {
  position: fixed;
  inset: 0;
  background: radial-gradient(ellipse 40% 40% at 10% 80%, rgba(236, 72, 153, 0.06) 0%, transparent 50%),
  radial-gradient(ellipse 40% 40% at 90% 20%, rgba(34, 211, 238, 0.06) 0%, transparent 50%);
  z-index: 0;
}

/* ====== 装饰光晕 ====== */
.glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
}

.glow-1 {
  width: 500px;
  height: 500px;
  top: -10%;
  left: -5%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.2) 0%, transparent 70%);
  animation: pulse-glow 6s ease-in-out infinite;
}

.glow-2 {
  width: 400px;
  height: 400px;
  bottom: -10%;
  right: -5%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.15) 0%, transparent 70%);
  animation: pulse-glow 8s ease-in-out infinite reverse;
}

@keyframes pulse-glow {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

/* ====== 主内容区 ====== */
.login-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 480px;
  gap: 80px;
  align-items: center;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 60px;
  position: relative;
  z-index: 2;
}

/* ====== 左侧品牌区 ====== */
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding-left: 40px;
}

.brand-logo {
  position: relative;
  width: 88px;
  height: 88px;
}

.logo-ring {
  width: 100%;
  height: 100%;
  border-radius: 24px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  display: grid;
  place-items: center;
  position: relative;
  z-index: 2;
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.4),
  inset 0 1px 1px rgba(255, 255, 255, 0.2);
}

.logo-icon {
  color: white;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.logo-glow {
  position: absolute;
  inset: -8px;
  border-radius: 32px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  opacity: 0.3;
  filter: blur(20px);
  z-index: 1;
  animation: logo-breathe 3s ease-in-out infinite;
}

@keyframes logo-breathe {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.05);
  }
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  color: #f8fafc;
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.02em;
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-badge {
  font-size: 13px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  letter-spacing: 0.05em;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.brand-subtitle {
  font-size: 18px;
  color: #94a3b8;
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-features {
  display: flex;
  gap: 24px;
  margin-top: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: #cbd5e1;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-2px);
}

.feature-item svg {
  color: #818cf8;
}

/* ====== 右侧登录卡片 ====== */
.login-card-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.4),
  inset 0 1px 1px rgba(255, 255, 255, 0.05);
}

.card-shine {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
      circle at 50% 50%,
      rgba(99, 102, 241, 0.08) 0%,
      transparent 50%
  );
  pointer-events: none;
}

.login-header {
  margin-bottom: 32px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 8px;
}

.login-header p {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

/* ====== 表单 ====== */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 错误提示 */
.login-error {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  font-size: 14px;
  font-weight: 500;
}

.error-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
  animation: error-pulse 2s ease-in-out infinite;
}

@keyframes error-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

/* 输入框组 */
.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #94a3b8;
  transition: color 0.3s ease;
}

.input-group.focused .input-label {
  color: #818cf8;
}

.label-required {
  color: #ef4444;
  font-size: 12px;
}

.input-wrapper {
  position: relative;
}

.input-wrapper input {
  width: 100%;
  height: 52px;
  padding: 0 16px;
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  color: #f1f5f9;
  font-size: 15px;
  font-weight: 500;
  outline: none;
  transition: all 0.3s ease;
}

.input-wrapper input::placeholder {
  color: #475569;
  font-weight: 400;
}

.input-wrapper input:hover {
  background: rgba(255, 255, 255, 0.07);
}

.input-wrapper input:focus {
  background: rgba(255, 255, 255, 0.08);
}

.input-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.input-line-active {
  position: absolute;
  bottom: 0;
  left: 50%;
  right: 50%;
  height: 2px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-group.focused .input-line-active {
  left: 0;
  right: 0;
}

/* ====== 登录按钮 ====== */
.login-btn {
  position: relative;
  height: 52px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  margin-top: 8px;
  transition: all 0.3s ease;
}

.login-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  background-size: 200% 200%;
  animation: gradient-shift 3s ease infinite;
  transition: all 0.3s ease;
}

.login-btn:hover:not(:disabled) .btn-bg {
  filter: brightness(1.1);
  transform: scale(1.02);
}

.login-btn:active:not(:disabled) .btn-bg {
  transform: scale(0.98);
}

@keyframes gradient-shift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.btn-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
  );
  transition: left 0.5s ease;
  z-index: 1;
}

.login-btn:hover:not(:disabled) .btn-shine {
  left: 100%;
}

/* ====== 底部信息 ====== */
.login-footer {
  margin-top: 32px;
  text-align: center;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.divider::before,
.divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
}

.divider span {
  font-size: 12px;
  color: #475569;
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.security-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #475569;
  margin: 0;
}

.security-note svg {
  color: #10b981;
}

/* ====== 底部版权 ====== */
.copyright {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  font-size: 13px;
  color: #475569;
}

.copyright .dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #475569;
}

/* ====== 动画 ====== */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.spin {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ====== 响应式 ====== */
@media (max-width: 1100px) {
  .login-content {
    grid-template-columns: 1fr;
    gap: 48px;
    padding: 40px 24px;
  }

  .brand-section {
    align-items: center;
    text-align: center;
    padding-left: 0;
  }

  .brand-features {
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px;
    border-radius: 20px;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-subtitle {
    font-size: 14px;
  }
}
</style>