<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth.js";
import { LoaderCircle, LogIn, ShieldCheck } from "@lucide/vue";

const emit = defineEmits(["login-success"]);
const router = useRouter();
const authStore = useAuthStore();

const username = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");

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
    authStore.setAuth(token, userInfo);
    emit("login-success", {token, userInfo});
    router.push("/");
  } catch (err) {
    error.value = "网络错误，请检查后端服务";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-content">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="seal emblem">印</div>
        <h1>
          终身学习学分银行
          <span>Lifelong Learning Credit Bank</span>
        </h1>
        <p class="brand-desc">集学分认证、学习档案、成果认定于一体的终身教育平台</p>
        <div class="features">
          <div><span class="dot"></span>区块链存证</div>
          <div><span class="dot"></span>AI 学习助手</div>
          <div><span class="dot"></span>终身学习档案</div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-right">
        <form class="login-card" @submit.prevent="handleLogin">
          <h2>账户登录</h2>
          <p>请输入您的账号凭证</p>

          <!-- 错误提示 -->
          <Transition name="fade">
            <div v-if="error" class="login-error">{{ error }}</div>
          </Transition>

          <div class="field">
            <label>用户名</label>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
              :disabled="loading"
            />
          </div>

          <div class="field">
            <label>密码</label>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
          </div>

          <button type="submit" :disabled="loading">
            <LoaderCircle v-if="loading" class="spin" :size="18"/>
            <LogIn v-else :size="18"/>
            {{ loading ? "登录中..." : "登 录" }}
          </button>

          <div class="footnote">
            <ShieldCheck :size="14"/>
            <span>SSL 加密传输 · 数据安全有保障</span>
          </div>
        </form>
      </div>
    </div>

    <footer class="copyright">
      © 2026 终身学习学分银行平台 · 教育部继续教育示范基地
    </footer>
  </div>
</template>

<style scoped>
/* ====== 印章风格登录页 ====== */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-app);
}

.login-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 400px;
  align-items: center;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 48px;
  gap: 72px;
}

/* ---- 左侧品牌区 ---- */
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-left: 32px;
}

.emblem {
  --seal-size: 56px;
  font-size: 22px;
}

.brand-section h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
  letter-spacing: 0.04em;
}

.brand-section h1 span {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-muted);
  margin-top: 4px;
  letter-spacing: 0.02em;
}

.brand-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
  line-height: 1.6;
  max-width: 360px;
}

.features {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.features div {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
  padding: 6px 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-panel);
}

.features .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent-primary);
  flex-shrink: 0;
}

/* ---- 右侧登录卡片 ---- */
.login-right {
  display: flex;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 360px;
  padding: 40px 36px;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-panel);
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.login-card h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.login-card h2 + p {
  font-size: 13px;
  color: var(--text-muted);
  margin: -16px 0 0 0;
}

/* ---- 错误提示 ---- */
.login-error {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: var(--badge-danger-bg);
  border: 1px solid #fecaca;
  color: var(--accent-primary);
  font-size: 13px;
  font-weight: 500;
}

/* ---- 表单字段 ---- */
.field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.field label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.field input {
  height: 44px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-input);
  outline: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.field input:focus {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 3px rgba(185, 28, 28, 0.06);
  background: var(--bg-panel);
}

.field input::placeholder {
  color: #a09b95;
}

/* ---- 登录按钮 ---- */
.login-card button[type="submit"] {
  height: 44px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--accent-primary);
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  letter-spacing: 0.05em;
  transition: background var(--transition-fast);
}

.login-card button[type="submit"]:hover:not(:disabled) {
  background: var(--accent-secondary);
}

.login-card button[type="submit"]:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ---- 底部 ---- */
.footnote {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #a09b95;
}

.footnote svg {
  color: var(--text-success);
}

/* ---- 版权 ---- */
.copyright {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: #a09b95;
}

/* ---- 动画 ---- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---- 响应式 ---- */
@media (max-width: 900px) {
  .login-content {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 32px 24px;
  }
  .brand-section {
    align-items: center;
    text-align: center;
    padding-left: 0;
  }
  .brand-desc {
    max-width: 100%;
  }
  .features {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
