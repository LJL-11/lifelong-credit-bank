<script setup>
import { GraduationCap, UserCircle, Moon, Sun, LogOut } from "@lucide/vue";

defineProps({
  isAdmin: { type: Boolean, default: false },
  isDark: { type: Boolean, default: false },
  userInfo: { type: Object, default: null },
  activeModule: { type: String, default: "" },
  currentMenu: { type: Array, default: () => [] },
  icons: { type: Object, default: () => ({}) },
});

const emit = defineEmits(["switch-module", "toggle-theme", "logout"]);
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-glow"/>

    <div class="brand">
      <div class="brand-mark">
        <GraduationCap :size="28"/>
      </div>
      <div class="brand-text">
        <strong>学分银行</strong>
        <small>{{ isAdmin ? "管理后台" : "学员中心" }}</small>
      </div>
    </div>

    <div class="user-card">
      <div class="user-avatar">
        <UserCircle :size="32"/>
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
        <div class="nav-indicator"/>
        <component :is="icons[item.icon]" :size="18"/>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <div class="nav-footer">
      <button class="theme-toggle-btn" type="button" @click="emit('toggle-theme')">
        <Moon v-if="!isDark" :size="16"/>
        <Sun v-else :size="16"/>
        <span>{{ isDark ? '浅色模式' : '深色模式' }}</span>
      </button>
      <button class="logout-btn" type="button" @click="emit('logout')">
        <LogOut :size="16"/>
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
  padding: 20px 14px;
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.sidebar-glow {
  position: absolute;
  top: -100px;
  left: -100px;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: grid;
  place-items: center;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08);
}

.brand-mark svg {
  color: white;
}

.brand-text strong {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  transition: color 0.3s ease;
}

.brand-text small {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: var(--bg-user-card);
  border: 1px solid var(--border-color-light);
  position: relative;
  z-index: 1;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.user-avatar svg {
  color: white;
}

.user-info strong {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.user-info small {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 1px;
}

.inst-label {
  display: block;
  margin-top: 3px;
  font-size: 10px;
  color: var(--text-accent);
  font-weight: 600;
  transition: color 0.3s ease;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

.nav button {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 12px;
  border: none;
  border-radius: 10px;
  color: var(--text-muted);
  background: transparent;
  text-align: left;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  position: relative;
  transition: all 0.2s ease;
}

.nav button:hover {
  color: var(--text-secondary);
  background: var(--bg-hover);
}

.nav button.active {
  color: var(--text-accent);
  background: rgba(99, 102, 241, 0.06);
}

.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #6366f1, #818cf8);
  transition: height 0.3s ease;
}

.nav button.active .nav-indicator {
  height: 20px;
}

.nav button svg {
  flex-shrink: 0;
}

.nav-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border-color 0.3s ease;
}

.theme-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 38px;
  padding: 0 12px;
  border: 1px solid var(--border-color-light);
  border-radius: 10px;
  color: var(--text-muted);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.theme-toggle-btn:hover {
  color: var(--text-accent);
  border-color: rgba(99, 102, 241, 0.2);
  background: rgba(99, 102, 241, 0.06);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 38px;
  padding: 0 12px;
  border: 1px solid var(--border-color-light);
  border-radius: 10px;
  color: var(--text-muted);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.2);
  background: rgba(239, 68, 68, 0.04);
}

@media (max-width: 1100px) {
  .sidebar {
    position: static;
    height: auto;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }

  .nav {
    flex-direction: row;
    flex-wrap: wrap;
    overflow-x: auto;
  }
}
</style>
