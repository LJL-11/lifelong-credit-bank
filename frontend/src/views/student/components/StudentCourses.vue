<script setup>
import { LoaderCircle, ExternalLink, CheckCircle2 } from "@lucide/vue";

const props = defineProps({
  loading: Boolean,
  token: String,
  courses: Array,
  learnedCourseIds: Set,
  coursePage: Object,
});

const emit = defineEmits(["open-learning", "enroll-course", "load-courses", "show-toast"]);

function enrollmentLabel(status) {
  if (status === "APPROVED") return "已购买";
  if (status === "PENDING") return "待审核";
  if (status === "REJECTED") return "已驳回";
  return "未报名";
}

function enrollmentClass(status) {
  if (status === "APPROVED") return "success";
  if (status === "PENDING") return "pending";
  if (status === "REJECTED") return "danger";
  return "neutral";
}
</script>

<template>
  <main class="workspace student-dark-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">学生端</p>
        <h1>我的课程</h1>
        <p class="subtitle">已报名通过或商城购买开通的课程</p>
      </div>
    </header>
    <section class="panel">
      <div v-if="loading" class="state-block">
        <LoaderCircle class="spin" :size="22"/>
        加载中...
      </div>
      <div v-else-if="courses.length === 0" class="state-block">暂无课程</div>
      <div v-else class="course-grid">
        <article v-for="c in courses" :key="c.id" class="course-card">
          <div class="course-card-head">
            <div>
              <span class="course-code">{{ c.courseCode }}</span>
              <h2>{{ c.courseName }}</h2>
            </div>
            <div class="course-status-stack">
              <span :class="['data-badge', learnedCourseIds.has(c.id) ? 'success' : 'neutral']">
                {{ learnedCourseIds.has(c.id) ? '已学完' : '未完成' }}
              </span>
              <span :class="['data-badge', enrollmentClass(c.enrollStatus)]">{{ enrollmentLabel(c.enrollStatus) }}</span>
            </div>
          </div>
          <p class="course-summary">{{ c.resourceSummary || `${c.provider} / ${c.category}` }}</p>
          <div class="course-meta">
            <span>{{ c.provider }}</span>
            <span>{{ c.category }}</span>
            <span>{{ c.creditValue }} 学分</span>
            <span>{{ c.creditPoint }} 积分</span>
            <span>{{ c.estimatedMinutes || 90 }} 分钟</span>
          </div>
          <div class="course-resource-row">
            <a v-if="c.enrollStatus === 'APPROVED' && c.resourceUrl" :href="c.resourceUrl" target="_blank" rel="noopener" class="resource-link">
              <ExternalLink :size="15"/>
              {{ c.resourceTitle || '学习资源' }}
            </a>
            <span v-else class="resource-missing">{{ c.enrollStatus === 'APPROVED' ? '暂无外部资源' : '报名审核通过后开放学习资源' }}</span>
          </div>
          <div v-if="!learnedCourseIds.has(c.id)" class="course-actions">
            <button class="primary-button small" type="button" :disabled="loading || learnedCourseIds.has(c.id) || c.enrollStatus !== 'APPROVED'"
                    @click="emit('open-learning', c)">{{ c.enrollStatus === 'APPROVED' ? '进入学习' : '审核通过后学习' }}
            </button>
            <button class="ghost-button small" type="button" :disabled="loading || c.enrollStatus === 'APPROVED' || c.enrollStatus === 'PENDING'" @click="emit('enroll-course', c)">
              {{ c.enrollStatus === 'APPROVED' ? '已开通' : c.enrollStatus === 'PENDING' ? '审核中' : '报名课程' }}
            </button>
          </div>
          <span v-else class="complete-note"><CheckCircle2 :size="16"/> 已完成并获得积分</span>
        </article>
      </div>
      <footer v-if="coursePage.total > coursePage.size" class="pager">
        <button :disabled="coursePage.current <= 1" @click="coursePage.current--; emit('load-courses')">上一页</button>
        <span>第 {{ coursePage.current }} / {{ Math.ceil(coursePage.total / coursePage.size) }} 页</span>
        <button :disabled="coursePage.current >= Math.ceil(coursePage.total / coursePage.size)"
                @click="coursePage.current++; emit('load-courses')">下一页
        </button>
      </footer>
    </section>
  </main>
</template>

<style scoped>
/* All course-card, course-grid, course-actions, etc. styles are in base.css */
</style>
