<script setup>
import { displayLabel } from "@/utils/displayLabels.js";
const props = defineProps({
  loading: Boolean,
  token: String,
  jobs: Array,
  jobApplications: Array,
});

const emit = defineEmits(["open-apply-dialog", "show-toast"]);

function badgeClass(v) {
  const t = String(v || "").toLowerCase();
  if (/active|open|published|approved|passed|increase|paid|delivered/.test(t)) return "success";
  if (/pending|learning|upcoming/.test(t)) return "pending";
  if (/reject|disable|closed|ended|failed|consume|refunded|cancelled/.test(t)) return "danger";
  return "neutral";
}
</script>

<template>
  <main class="workspace student-dark-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">学生端</p>
        <h1>招聘求职</h1>
        <p class="subtitle">查看岗位并投递简历摘要</p>
      </div>
    </header>
    <section class="panel">
      <div class="course-grid">
        <article v-for="j in jobs" :key="j.id" class="course-card">
          <div class="course-card-head">
            <div>
              <span class="course-code">{{ j.companyName }}</span>
              <h2>{{ j.positionName }}</h2>
            </div>
            <span class="data-badge success">{{ displayLabel(j.status) }}</span>
          </div>
          <p class="course-summary">{{ j.requirement }}</p>
          <div class="course-meta">
            <span>{{ j.city || '-' }}</span>
            <span>{{ j.contact || '-' }}</span>
          </div>
          <button class="primary-button small" @click="emit('open-apply-dialog', j)">投递</button>
        </article>
      </div>
      <p v-if="jobs.length===0" class="state-block">暂无开放岗位</p>
    </section>
    <section class="panel">
      <div class="section-head"><h2>我的投递</h2></div>
      <div class="simple-list">
        <div v-for="a in jobApplications" :key="a.id">
          <strong>岗位 #{{ a.jobId }}</strong>
          <span :class="['data-badge', badgeClass(a.applyStatus)]">{{ displayLabel(a.applyStatus) }}</span>
          <small>{{ a.remark || a.resumeSummary || '' }}</small>
          <a v-if="a.resumeUrl" class="resource-link" :href="a.resumeUrl" target="_blank" rel="noopener">{{ a.resumeFileName || '查看简历' }}</a>
        </div>
        <p v-if="jobApplications.length===0" class="state-block">暂无投递记录</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* All course-grid, course-card, simple-list, etc. styles are in base.css */
</style>
