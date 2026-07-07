<script setup>
import { ref } from "vue";
import { LoaderCircle, GraduationCap, LogIn } from "@lucide/vue";

const emit = defineEmits(["login-success"]);

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
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: username.value, password: password.value }),
    });
    const result = await response.json();
    if (!response.ok || result.code !== 200) {
      error.value = result.message || "登录失败";
      return;
    }
    const { token, learnerId, realName, role } = result.data;
    const userInfo = { learnerId, username: result.data.username, realName, role };
    localStorage.setItem("token", token);
    localStorage.setItem("userInfo", JSON.stringify(userInfo));
    emit("login-success", { token, userInfo });
  } catch (err) {
    error.value = "网络错误，请检查后端服务";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-head">
        <GraduationCap :size="40" />
        <h1>学分银行</h1>
        <p>Lifelong Credit Bank</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div v-if="error" class="login-error">{{ error }}</div>
        <label>
          <span>用户名</span>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            :disabled="loading"
          />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            :disabled="loading"
          />
        </label>
        <button type="submit" class="primary-button login-btn" :disabled="loading">
          <LoaderCircle v-if="loading" class="spin" :size="18" />
          <LogIn v-else :size="18" />
          {{ loading ? "登录中..." : "登 录" }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #123c39 0%, #1a5c57 50%, #0f4d45 100%);
  padding: 24px;
}

.login-card {
  width: min(420px, 100%);
  padding: 36px 32px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.30);
}

.login-head {
  text-align: center;
  margin-bottom: 28px;
  color: #123c39;
}

.login-head h1 {
  margin: 10px 0 4px;
  font-size: 26px;
}

.login-head p {
  margin: 0;
  font-size: 12px;
  color: #66758a;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.login-error {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: #fde8e6;
  color: #b42318;
  font-size: 14px;
}

.login-form {
  display: grid;
  gap: 14px;
}

.login-form label span {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #66758a;
}

.login-form input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #d8e1ec;
  border-radius: 8px;
  font-size: 15px;
  background: #fff;
  color: #182235;
}

.login-form input:focus {
  outline: 2px solid rgba(18, 60, 57, 0.16);
  border-color: #166a5f;
}

.login-btn {
  height: 44px;
  font-size: 15px;
  justify-content: center;
  width: 100%;
}
</style>
