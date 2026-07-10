<script setup>
const props = defineProps({
  loading: Boolean,
  token: String,
  enrollments: Array,
  signIns: Array,
  todaySigned: Boolean,
  todaySignRecord: Object,
});

const emit = defineEmits(["sign-today", "show-toast"]);

function badgeClass(v) {
  const t = String(v || "").toLowerCase();
  if (/active|open|published|approved|passed|increase|paid|delivered/.test(t)) return "success";
  if (/pending|learning|upcoming/.test(t)) return "pending";
  if (/reject|disable|closed|ended|failed|consume|refunded|cancelled/.test(t)) return "danger";
  return "neutral";
}

function formatCell(v) {
  if (v === null || v === undefined || v === "") return "-";
  if (typeof v === "string" && v.includes("T")) return v.replace("T", " ").slice(0, 16);
  return v;
}
</script>

<template>
  <main class="workspace">
    <header class="topbar">
      <div>
        <p class="eyebrow">Student Portal</p>
        <h1>报名与签到</h1>
        <p class="subtitle">课程报名申请与每日签到奖励</p>
      </div>
      <button :class="todaySigned ? 'ghost-button' : 'primary-button'" :disabled="todaySigned || loading"
              @click="emit('sign-today')">
        {{ todaySigned ? '今日已签到' : '今日签到' }}
      </button>
    </header>
    <section class="split-grid">
      <div class="panel">
        <div class="section-head"><h2>我的报名</h2></div>
        <div class="simple-list">
          <div v-for="e in enrollments" :key="e.id">
            <strong>课程 #{{ e.courseId }}</strong>
            <span :class="['data-badge', badgeClass(e.enrollStatus)]">{{ e.enrollStatus }}</span>
            <small>{{ formatCell(e.createdAt) }} {{ e.remark || '' }}</small>
          </div>
          <p v-if="enrollments.length===0" class="state-block">暂无报名记录，可在我的课程中报名</p>
        </div>
      </div>
      <div class="panel">
        <div class="section-head">
          <h2>签到记录</h2>
          <p v-if="todaySigned">今天已获得 +{{ todaySignRecord?.rewardCredits || 2 }} 积分</p>
        </div>
        <div class="simple-list">
          <div v-for="s in signIns" :key="s.id">
            <strong>{{ s.signDate }}</strong>
            <span>+{{ s.rewardCredits }} 积分</span>
            <small>{{ s.signType }}</small>
          </div>
          <p v-if="signIns.length===0" class="state-block">暂无签到记录</p>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* All simple-list, split-grid, etc. styles are in base.css */
</style>
