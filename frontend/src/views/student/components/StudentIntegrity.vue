<script setup>
const props = defineProps({
  loading: Boolean,
  token: String,
  integrity: Object,
});

const emit = defineEmits(["load-integrity", "show-toast"]);
</script>

<template>
  <main class="workspace">
    <header class="topbar">
      <div>
        <p class="eyebrow">Student Portal</p>
        <h1>诚信评定</h1>
        <p class="subtitle">基于学习、签到、成果、论坛和求职行为自动计算</p>
      </div>
      <button class="ghost-button" @click="emit('load-integrity')">重新计算</button>
    </header>
    <section class="panel integrity-panel" v-if="integrity">
      <div class="integrity-score">
        <strong>{{ integrity.score }}</strong>
        <span>等级 {{ integrity.levelName }}</span>
      </div>
      <div class="overview-list">
        <div><span>学习</span><strong>{{ integrity.learningScore }}</strong></div>
        <div><span>签到</span><strong>{{ integrity.signinScore }}</strong></div>
        <div><span>成果</span><strong>{{ integrity.achievementScore }}</strong></div>
        <div><span>论坛</span><strong>{{ integrity.forumScore }}</strong></div>
        <div><span>求职</span><strong>{{ integrity.jobScore }}</strong></div>
      </div>
      <p>{{ integrity.remark }}</p>
    </section>
  </main>
</template>

<style scoped>
/* All integrity-panel, integrity-score, overview-list, etc. styles are in base.css */
</style>
