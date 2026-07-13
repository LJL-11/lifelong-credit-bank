<script setup>
import { GraduationCap, UserCircle, LogOut } from "@lucide/vue";

defineProps({
  isAdmin: { type: Boolean, default: false },
  userInfo: { type: Object, default: null },
  activeModule: { type: String, default: "" },
  currentMenu: { type: Array, default: () => [] },
  icons: { type: Object, default: () => ({}) },
});

const emit = defineEmits(["switch-module", "logout"]);
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="seal brand-mark">
        <GraduationCap :size="20"/>
      </div>
      <div class="brand-text">
        <strong>学分银行</strong>
        <small>{{ isAdmin ? "管理后台" : "学员中心" }}</small>
      </div>
    </div>

    <div class="user-card">
      <div class="user-avatar">
        <UserCircle :size="28"/>
      </div>
      <div class="user-info">
        <strong>{{ userInfo?.realName }}</strong>
        <small>{{ userInfo?.username }}</small>
        <span v-if="userInfo?.institutionName" class="inst-label">{{ userInfo.institutionName }}</span>
      </div>
    </div>

    <nav class="nav">
      <button
          v-for="item in currentMenu"
          :key="item.key"
          :class="{ active: activeModule === item.key }"
          type="button"
          @click="emit('switch-module', item.key)"
      >
        <component :is="icons[item.icon]" :size="17"/>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <div class="nav-footer">
      <button class="logout-btn" type="button" @click="emit('logout')">
        <LogOut :size="15"/>
        <span>退出登录</span>
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 18px 12px;
  background: var(--bg-sidebar);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ---- 品牌区 ---- */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px 16px;
  margin-bottom: 6px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.brand-mark {
  --seal-size: 36px;
}

.brand-text strong {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.02em;
}

.brand-text small {
  display: block;
  font-size: 10px;
  color: #a09b95;
  margin-top: 1px;
  font-weight: 500;
}

/* ---- 用户卡片 ---- */
.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 0 4px 14px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--accent-primary);
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.user-avatar svg {
  color: #ffffff;
}

.user-info strong {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #e8e4de;
}

.user-info small {
  display: block;
  font-size: 10px;
  color: #a09b95;
  margin-top: 1px;
}

.inst-label {
  display: block;
  margin-top: 2px;
  font-size: 10px;
  color: var(--text-accent);
  font-weight: 600;
}

/* ---- 导航 ---- */
.nav {
  display: flex;
  flex-direction: column;
  gap: 1px;
  flex: 1;
  overflow-y: auto;
}

.nav button {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 38px;
  padding: 0 12px;
  border: none;
  border-radius: var(--radius-sm);
  color: #a09b95;
  background: transparent;
  text-align: left;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.nav button:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.04);
}

.nav button.active {
  color: #ffffff;
  background: rgba(185, 28, 28, 0.18);
}

.nav button svg {
  flex-shrink: 0;
}

/* ---- 底部 ---- */
.nav-footer {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 34px;
  padding: 0 12px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: #a09b95;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all var(--transition-fast);
  font-family: inherit;
}

.logout-btn:hover {
  color: var(--accent-primary);
  background: rgba(185, 28, 28, 0.08);
}

/* ---- 响应式 ---- */
@media (max-width: 1100px) {
  .sidebar {
    position: static;
    height: auto;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  }
  .nav {
    flex-direction: row;
    flex-wrap: wrap;
    overflow-x: auto;
  }
}
</style>
