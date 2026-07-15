<script setup>
import { LoaderCircle } from "@lucide/vue";

const props = defineProps({
  loading: Boolean,
  token: String,
  profile: Object,
});

const emit = defineEmits(["show-toast"]);
</script>

<template>
  <main class="workspace student-dark-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">学生端</p>
        <h1>我的信息</h1>
        <p class="subtitle">查看和更新个人资料</p>
      </div>
    </header>
    <section class="panel">
      <div v-if="loading" class="state-block">
        <LoaderCircle class="spin" :size="22"/>
        加载中...
      </div>
      <div v-else-if="profile" class="profile-grid">
        <div class="profile-field"><span>用户名</span><strong>{{ profile.username }}</strong></div>
        <div class="profile-field"><span>姓名</span><strong>{{ profile.realName }}</strong></div>
        <div class="profile-field"><span>手机号</span><strong>{{ profile.phone || '-' }}</strong></div>
        <div class="profile-field"><span>邮箱</span><strong>{{ profile.email || '-' }}</strong></div>
        <div class="profile-field"><span>学历</span><strong>{{ profile.educationLevel || '-' }}</strong></div>
        <div class="profile-field"><span>角色</span><span
            :class="['data-badge', profile.role === 'ADMIN' ? 'success' : 'neutral']">{{ profile.role }}</span></div>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* All profile-grid, profile-field, etc. styles are in base.css */
</style>
