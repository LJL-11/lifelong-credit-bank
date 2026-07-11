<script setup>
defineProps({
  statCards: {type: Array, default: () => []},
  overview: {type: Array, default: () => []},
  quickActions: {type: Array, default: () => []},
  icons: {type: Object, default: () => ({})},
  apiOnline: {type: Boolean, default: false},
  activeModule: {type: String, default: ""},
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

/* ========== 深色模式全局优化：高对比度文字 ========== */
/* 卡片背景、面板深色底色 */
[data-theme="dark"] .stat-card,
[data-theme="dark"] .panel {
  background-color: #1e293b;
  border: 1px solid #334155;
}

/* 一级标题、大数字、强调文字：纯白，最高清晰 */
[data-theme="dark"] h2,
[data-theme="dark"] .stat-card strong,
[data-theme="dark"] .overview-list strong,
[data-theme="dark"] .dist-item strong {
  color: #ffffff;
}

/* 二级说明文字：亮浅灰，区别于纯白但清晰可见 */
[data-theme="dark"] .stat-card-header > span:first-child,
[data-theme="dark"] .overview-list span,
[data-theme="dark"] .chart-label,
[data-theme="dark"] .quick-actions button span,
[data-theme="dark"] .dist-item span:nth-child(2) {
  color: #e2e8f0;
}

/* 辅助小字、备注：中度灰色，不刺眼但看得清 */
[data-theme="dark"] .stat-card > small {
  color: #b8c2d3;
}

/* 快捷按钮样式深色适配 */
[data-theme="dark"] .quick-actions button {
  background-color: #334155;
  border: 1px solid #475569;
  color: #e2e8f0;
}
[data-theme="dark"] .quick-actions button:hover {
  background-color: #475569;
  color: #ffffff;
}

/* 趋势徽章深色配色 */
[data-theme="dark"] .trend-badge.up {
  background-color: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}
[data-theme="dark"] .trend-badge.down {
  background-color: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

/* 服务状态徽章深色 */
[data-theme="dark"] .status-pill.ok {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}
[data-theme="dark"] .status-pill.warn {
  background: rgba(234, 179, 8, 0.2);
  color: #facc15;
}

/* 进度条深色 */
[data-theme="dark"] .progress-track {
  background-color: #334155;
}
[data-theme="dark"] .progress-fill {
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

/* 图表柱状条深色 */
[data-theme="dark"] .chart-bar {
  background: linear-gradient(to top, #6366f1, #818cf8);
  opacity: 0.85;
}
</style>