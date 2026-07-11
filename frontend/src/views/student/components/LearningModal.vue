<script setup>
import { X, ExternalLink, CheckCircle2 } from "@lucide/vue";

const props = defineProps({
  activeLearning: Object,
  loading: Boolean,
  token: String,
  activeCourseStages: Array,
  allStagesDone: Boolean,
});

const emit = defineEmits(["close", "toggle-stage", "complete-course", "show-toast"]);
</script>

<template>
  <div v-if="activeLearning.open" class="modal-backdrop" @click.self="emit('close')">
    <section class="modal course-learning-modal">
      <div class="modal-head">
        <div>
          <h2>{{ activeLearning.course?.courseName }}</h2>
          <p>{{ activeLearning.course?.resourceSummary }}</p>
        </div>
        <button type="button" class="icon-button" @click="emit('close')">
          <X :size="18"/>
        </button>
      </div>
      <div class="learning-resource-box">
        <div>
          <span>学习资源</span>
          <strong>{{ activeLearning.course?.resourceTitle || '课程外部资源' }}</strong>
        </div>
        <a v-if="activeLearning.course?.resourceUrl" :href="activeLearning.course.resourceUrl" target="_blank"
           rel="noopener" class="primary-button small">
          <ExternalLink :size="15"/>
          打开资源
        </a>
      </div>
      <div class="learning-stage-list">
        <label v-for="(stage, index) in activeCourseStages" :key="stage.title" class="learning-stage">
          <input type="checkbox" :checked="activeLearning.completedStages.has(index)"
                 @change="emit('toggle-stage', index)"/>
          <span class="stage-index">{{ index + 1 }}</span>
          <span class="stage-body">
            <strong>{{ stage.title }}</strong>
            <small>{{ stage.description }}</small>
          </span>
        </label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close')">稍后继续</button>
        <button class="primary-button" type="button" :disabled="loading || !allStagesDone" @click="emit('complete-course')">
          <CheckCircle2 :size="17"/>
          完成课程并领取 {{ activeLearning.course?.creditPoint || 0 }} 积分
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.course-learning-modal {
  max-width: 760px;
  width: min(760px, calc(100vw - 32px));
}

.course-learning-modal .modal-head p {
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.5;
  transition: color 0.3s ease;
}

.learning-resource-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-app);
  margin-bottom: 16px;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.learning-resource-box span {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.learning-resource-box strong {
  color: var(--text-primary);
  font-size: 15px;
  transition: color 0.3s ease;
}

.learning-stage-list {
  display: grid;
  gap: 10px;
}

.learning-stage {
  display: grid;
  grid-template-columns: 18px 32px minmax(0, 1fr);
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-chart);
  cursor: pointer;
  transition: all 0.2s ease;
}

.learning-stage:hover {
  border-color: var(--border-color-hover);
  background: rgba(99, 102, 241, 0.06);
}

.learning-stage input {
  margin-top: 8px;
  width: 16px;
  height: 16px;
  accent-color: #6366f1;
}

.stage-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.06);
  color: var(--text-accent);
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 13px;
  transition: color 0.3s ease;
}

.stage-body strong {
  display: block;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-size: 14px;
  transition: color 0.3s ease;
}

.stage-body small {
  display: block;
  color: var(--text-muted);
  line-height: 1.5;
  font-size: 12px;
  transition: color 0.3s ease;
}
</style>
