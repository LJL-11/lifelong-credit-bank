<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from "vue";
import * as echarts from "echarts";
import * as adminApi from "@/api/admin.js";

const props = defineProps({
  statCards:   { type: Array,  default: () => [] },
  overview:    { type: Array,  default: () => [] },
  quickActions:{ type: Array,  default: () => [] },
  icons:       { type: Object, default: () => ({}) },
  apiOnline:   { type: Boolean, default: false },
  activeModule:{ type: String,  default: "" },
});

const emit = defineEmits(["switch-module"]);

// ---------- chart refs ----------
const chartRefs  = { revenue: null, status: null, integrity: null, courseRank: null };
const revenueChart    = ref(null);
const statusChart     = ref(null);
const integrityChart  = ref(null);
const courseRankChart = ref(null);
const chartInstances = {};

const chartLoading  = ref(true);
const chartError    = ref("");

// ---------- dark mode ----------
const isDark = ref(document.documentElement.getAttribute("data-theme") === "dark");
let darkObserver = null;

const textColor  = () => isDark.value ? "#e2e8f0" : "#334155";
const axisColor  = () => isDark.value ? "#475569" : "#c8d2e0";
const bgColor    = () => "transparent";

// ---------- init / dispose ----------
function initChart(refKey, domRef) {
  if (chartInstances[refKey]) chartInstances[refKey].dispose();
  if (!domRef) return;
  const inst = echarts.init(domRef);
  chartInstances[refKey] = inst;
  return inst;
}

function resizeAll() {
  Object.values(chartInstances).forEach(c => { try { c.resize(); } catch (_) {} });
}

function disposeAll() {
  Object.values(chartInstances).forEach(c => c.dispose());
}

// ---------- build charts ----------
function renderRevenue(daily) {
  const inst = initChart("revenue", revenueChart.value);
  if (!inst) return;
  inst.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", top: 30, bottom: 24, containLabel: true },
    xAxis: {
      type: "category", data: daily.map(d => d.date),
      axisLine: { lineStyle: { color: axisColor() } },
      axisLabel: { color: textColor(), fontSize: 11 },
    },
    yAxis: {
      type: "value", name: "积分",
      nameTextStyle: { color: textColor(), fontSize: 11 },
      axisLabel: { color: textColor() },
      splitLine: { lineStyle: { color: axisColor(), type: "dashed" } },
    },
    series: [{
      type: "line", data: daily.map(d => d.amount),
      smooth: true, symbol: "circle", symbolSize: 8,
      lineStyle: { color: isDark.value ? "#818cf8" : "#4338ca", width: 3 },
      itemStyle: { color: isDark.value ? "#818cf8" : "#4338ca" },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: isDark.value ? "rgba(99,102,241,0.3)" : "rgba(99,102,241,0.15)" },
          { offset: 1, color: "rgba(99,102,241,0.01)" },
        ]),
      },
    }],
  }, true);
}

function renderStatus(statusDist) {
  const inst = initChart("status", statusChart.value);
  if (!inst) return;
  const colors = ["#6366f1", "#f59e0b", "#10b981", "#ef4444", "#06b6d4"];
  inst.setOption({
    tooltip: { trigger: "item" },
    series: [{
      type: "pie", radius: ["50%", "78%"], center: ["50%", "55%"],
      data: statusDist.map((d, i) => ({ value: d.count, name: d.status, itemStyle: { color: colors[i] } })),
      label: { color: textColor(), fontSize: 11 },
      emphasis: { label: { fontSize: 16, fontWeight: "bold" } },
    }],
  }, true);
}

function renderIntegrityLevel(levelDist) {
  const inst = initChart("integrity", integrityChart.value);
  if (!inst) return;
  const colors = { A: "#10b981", B: "#6366f1", C: "#f59e0b", D: "#ef4444" };
  inst.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: "3%", right: "4%", top: 30, bottom: 24, containLabel: true },
    xAxis: {
      type: "category", data: levelDist.map(d => d.level + "级"),
      axisLabel: { color: textColor(), fontWeight: "bold", fontSize: 14 },
      axisLine: { lineStyle: { color: axisColor() } },
    },
    yAxis: {
      type: "value", name: "人数",
      nameTextStyle: { color: textColor() },
      axisLabel: { color: textColor() },
      splitLine: { lineStyle: { color: axisColor(), type: "dashed" } },
    },
    series: [{
      type: "bar", data: levelDist.map(d => ({
        value: d.count,
        itemStyle: { color: colors[d.level] || "#6366f1", borderRadius: [6, 6, 0, 0] },
      })),
      barWidth: "50%",
    }],
  }, true);
}

function renderCourseRank(topCourses) {
  const inst = initChart("courseRank", courseRankChart.value);
  if (!inst) return;
  // 横向柱状图：课程名放 y 轴，天然防遮挡
  const data = topCourses.map(d => ({
    name: d.courseName,
    value: d.learnerCount,
    inst: d.institutionName || "",
  })).reverse(); // ECharts y 轴从下往上排列，reverse 让第一名在上方
  inst.setOption({
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
      formatter: (params) => {
        const p = params[0];
        const item = data[p.dataIndex];
        return `${item.name}<br/>完成人数: ${item.value}<br/>机构: ${item.inst || "—"}`;
      },
    },
    grid: { left: 120, right: 30, top: 10, bottom: 20 },
    xAxis: {
      type: "value", name: "完成人数", minInterval: 1,
      nameTextStyle: { color: textColor(), fontSize: 11 },
      axisLabel: { color: textColor() },
      splitLine: { lineStyle: { color: axisColor(), type: "dashed" } },
    },
    yAxis: {
      type: "category", data: data.map(d => d.name),
      axisLabel: { color: textColor(), fontSize: 12, width: 110, overflow: "truncate" },
      axisLine: { lineStyle: { color: axisColor() } },
      inverse: true,
    },
    series: [{
      type: "bar", data: data.map((d, i) => ({
        value: d.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: isDark.value ? "#a78bfa" : "#8b5cf6" },
            { offset: 1, color: isDark.value ? "#7c3aed" : "#6d28d9" },
          ]),
          borderRadius: [0, 6, 6, 0],
        },
      })),
      barWidth: "55%",
    }],
  }, true);
}

// ---------- load data ----------
async function loadCharts() {
  chartLoading.value = true;
  chartError.value = "";
  try {
    const [revenueData, integrityData, courseRankData] = await Promise.all([
      adminApi.getRevenueTrend(),
      adminApi.getIntegrityDist(),
      adminApi.getCourseRanking(),
    ]);
    await nextTick();
    renderRevenue(revenueData.daily || []);
    renderStatus(revenueData.statusDistribution || []);
    renderIntegrityLevel(integrityData.levelDistribution || []);
    renderCourseRank(courseRankData.topCourses || []);
  } catch (err) {
    chartError.value = err.message || "报表加载失败";
  } finally {
    chartLoading.value = false;
  }
}

function refreshTheme() {
  isDark.value = document.documentElement.getAttribute("data-theme") === "dark";
  // Re-render with appropriate colors
  Object.values(chartInstances).forEach(c => {
    const opt = c.getOption();
    if (!opt) return;
    // Update text/axis colors
    if (opt.xAxis && opt.xAxis[0]) {
      if (opt.xAxis[0].axisLabel) opt.xAxis[0].axisLabel.color = textColor();
      if (opt.xAxis[0].axisLine) opt.xAxis[0].axisLine.lineStyle = { color: axisColor() };
    }
    if (opt.yAxis && opt.yAxis[0]) {
      if (opt.yAxis[0].axisLabel) opt.yAxis[0].axisLabel.color = textColor();
      if (opt.yAxis[0].nameTextStyle) opt.yAxis[0].nameTextStyle.color = textColor();
      if (opt.yAxis[0].splitLine) opt.yAxis[0].splitLine.lineStyle = { color: axisColor(), type: "dashed" };
    }
    c.setOption(opt);
  });
}

// ---------- lifecycle ----------
onMounted(() => {
  window.addEventListener("resize", resizeAll);
  darkObserver = new MutationObserver(refreshTheme);
  darkObserver.observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });
  // 等父组件确认 token 有效后才加载图表
  if (props.apiOnline) loadCharts();
});

watch(() => props.apiOnline, (online) => {
  if (online && chartInstances.revenue == null) loadCharts();
});

onUnmounted(() => {
  window.removeEventListener("resize", resizeAll);
  if (darkObserver) darkObserver.disconnect();
  disposeAll();
});
</script>

<template>
  <section class="dashboard">
    <!-- ====== 统计卡片（不变） ====== -->
    <div class="stats-grid">
      <article v-for="card in statCards" :key="card.key" class="stat-card">
        <div class="stat-card-header">
          <span>{{ card.label }}</span>
          <span :class="['trend-badge', card.trendUp ? 'up' : 'down']">{{ card.trend }}</span>
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

    <!-- ====== ECharts 图表区 ====== -->
    <div class="charts-row">
      <!-- 近7日营收趋势 -->
      <section class="panel chart-box">
        <div class="section-head">
          <h2>近7日营收趋势</h2>
          <span v-if="chartLoading" class="status-pill warn">加载中</span>
          <span v-else-if="chartError" class="status-pill warn">异常</span>
        </div>
        <div v-if="chartError" class="state-block error">{{ chartError }}</div>
        <div ref="revenueChart" class="echart-canvas"></div>
      </section>

      <!-- 订单状态分布 -->
      <section class="panel chart-box">
        <div class="section-head"><h2>订单状态分布</h2></div>
        <div ref="statusChart" class="echart-canvas"></div>
      </section>
    </div>

    <div class="charts-row">
      <!-- 诚信等级分布 -->
      <section class="panel chart-box">
        <div class="section-head"><h2>诚信等级分布</h2></div>
        <div ref="integrityChart" class="echart-canvas"></div>
      </section>

      <!-- 课程学习排行榜 -->
      <section class="panel chart-box">
        <div class="section-head"><h2>课程学习排行榜</h2></div>
        <div ref="courseRankChart" class="echart-canvas"></div>
      </section>
    </div>

    <!-- ====== 下部：概览 + 快捷入口（不变） ====== -->
    <div class="split-grid">
      <section class="panel">
        <div class="section-head">
          <h2>平台概览</h2>
          <span :class="['status-pill', props.apiOnline ? 'ok' : 'warn']">{{ props.apiOnline ? "服务正常" : "服务异常" }}</span>
        </div>
        <div class="overview-list">
          <div v-for="item in overview" :key="item.label">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
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

/* ====== 图表行 ====== */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-box {
  display: flex;
  flex-direction: column;
}

.echart-canvas {
  width: 100%;
  height: 300px;
  min-height: 280px;
}

/* ====== 深色模式 ====== */
[data-theme="dark"] .stat-card,
[data-theme="dark"] .panel {
  background-color: #1e293b;
  border: 1px solid #334155;
}
[data-theme="dark"] h2,
[data-theme="dark"] .stat-card strong,
[data-theme="dark"] .overview-list strong {
  color: #ffffff;
}
[data-theme="dark"] .stat-card-header > span:first-child,
[data-theme="dark"] .overview-list span,
[data-theme="dark"] .chart-label {
  color: #e2e8f0;
}
[data-theme="dark"] .stat-card > small {
  color: #b8c2d3;
}
[data-theme="dark"] .quick-actions button {
  background-color: #334155;
  border-color: #475569;
  color: #e2e8f0;
}
[data-theme="dark"] .quick-actions button:hover {
  background-color: #475569;
  color: #ffffff;
}
[data-theme="dark"] .trend-badge.up {
  background-color: rgba(34,197,94,0.2);
  color: #4ade80;
}
[data-theme="dark"] .trend-badge.down {
  background-color: rgba(239,68,68,0.2);
  color: #f87171;
}
[data-theme="dark"] .status-pill.ok {
  background: rgba(34,197,94,0.2);
  color: #4ade80;
}
[data-theme="dark"] .status-pill.warn {
  background: rgba(234,179,8,0.2);
  color: #facc15;
}
[data-theme="dark"] .progress-track {
  background-color: #334155;
}
[data-theme="dark"] .progress-fill {
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}
</style>
