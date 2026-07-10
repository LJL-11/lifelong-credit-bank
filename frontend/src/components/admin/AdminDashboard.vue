<script setup>
defineProps({
  statCards: { type: Array, default: () => [] },
  overview: { type: Array, default: () => [] },
  quickActions: { type: Array, default: () => [] },
  icons: { type: Object, default: () => ({}) },
  apiOnline: { type: Boolean, default: false },
  activeModule: { type: String, default: "" },
});

const emit = defineEmits(["switch-module"]);
</script>

<template>
  <section class="dashboard">
    <div class="stats-grid">
      <article v-for="card in statCards" :key="card.key" class="stat-card">
        <div class="stat-card-header">
          <span>{{ card.label }}</span>
          <span :class="['trend-badge', card.trendUp ? 'up' : 'down']">
            {{ card.trend }}
          </span>
        </div>
        <strong>{{ card.value.toLocaleString() }}</strong>
        <small>{{ card.note }}</small>
        <div class="stat-card-progress">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: Math.min(100, (card.value / 2000) * 100) + '%' }"/>
          </div>
        </div>
      </article>
    </div>

    <div class="split-grid">
      <section class="panel chart-panel">
        <div class="section-head">
          <h2>平台概览</h2>
          <span :class="['status-pill', apiOnline ? 'ok' : 'warn']">{{ apiOnline ? "服务正常" : "服务异常" }}</span>
        </div>
        <div class="overview-list">
          <div v-for="item in overview" :key="item.label">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
        <div class="chart-placeholder">
          <div class="chart-bars">
            <div v-for="i in 7" :key="i" class="chart-bar" :style="{ height: (20 + Math.random() * 60) + '%' }"/>
          </div>
          <p class="chart-label">近7日活跃趋势</p>
        </div>
      </section>

      <section class="panel">
        <div class="section-head"><h2>快捷入口</h2></div>
        <div class="quick-actions">
          <button v-for="item in quickActions" :key="item.key" type="button" @click="emit('switch-module', item.key)">
            <component :is="icons[item.icon]" :size="20"/>
            <span>{{ item.label }}</span>
          </button>
        </div>

        <div class="section-head" style="margin-top: 24px;">
          <h2>模块分布</h2>
        </div>
        <div class="distribution-chart">
          <div class="dist-item">
            <span class="dist-dot" style="background: #6366f1;"/>
            <span>学员档案</span>
            <strong>35%</strong>
          </div>
          <div class="dist-item">
            <span class="dist-dot" style="background: #8b5cf6;"/>
            <span>课程资源</span>
            <strong>25%</strong>
          </div>
          <div class="dist-item">
            <span class="dist-dot" style="background: #ec4899;"/>
            <span>积分交易</span>
            <strong>20%</strong>
          </div>
          <div class="dist-item">
            <span class="dist-dot" style="background: #22d3ee;"/>
            <span>其他模块</span>
            <strong>20%</strong>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-panel {
  display: flex;
  flex-direction: column;
}
</style>
