<script setup>
import { RefreshCw } from "@lucide/vue";
import { displayLabel } from "@/utils/displayLabels.js";

const props = defineProps({
  loading: Boolean,
  token: String,
  creditSourceAccount: Object,
  creditSources: Array,
  creditSourceStats: Object,
  creditSourcePage: Object,
});

const emit = defineEmits(["load-credit-sources", "show-toast"]);

function sourceTypeText(v) {
  const map = {
    COURSE_COMPLETE: "课程学习",
    ACHIEVEMENT_APPROVED: "成果认定",
    DAILY_SIGNIN: "每日签到",
    ORDER_PAY: "商城兑换",
    ADMIN_GRANT: "管理员发放",
    MANUAL: "人工调整",
  };
  return map[v] || displayLabel(v) || "其他来源";
}

function transactionTypeText(v) {
  return v === "INCREASE" ? "收入" : v === "CONSUME" ? "支出" : displayLabel(v) || "变动";
}

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
  <main class="workspace student-dark-page credit-sources-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">学生端</p>
        <h1>积分来源</h1>
        <p class="subtitle">查看积分收入、消费来源和余额变化明细</p>
      </div>
      <button class="ghost-button" type="button" @click="emit('load-credit-sources')">
        <RefreshCw :size="17"/>
        刷新
      </button>
    </header>
    <section class="stats-grid credit-source-stats">
      <article class="stat-card">
        <span>可用积分</span>
        <strong>{{ creditSourceAccount?.availableCredits ?? 0 }}</strong>
        <small>数据库 credit_account 当前余额</small>
      </article>
      <article class="stat-card">
        <span>累计积分</span>
        <strong>{{ creditSourceAccount?.totalCredits ?? 0 }}</strong>
        <small>历史累计获得</small>
      </article>
      <article class="stat-card">
        <span>冻结积分</span>
        <strong>{{ creditSourceAccount?.frozenCredits ?? 0 }}</strong>
        <small>{{ displayLabel(creditSourceAccount?.accountStatus) }}</small>
      </article>
      <article class="stat-card">
        <span>当前页收入</span>
        <strong>{{ creditSourceStats.income }}</strong>
        <small>学习、成果、签到等获得</small>
      </article>
      <article class="stat-card">
        <span>当前页支出</span>
        <strong>{{ creditSourceStats.expense }}</strong>
        <small>商城兑换和扣减</small>
      </article>
      <article class="stat-card">
        <span>当前页净变化</span>
        <strong>{{ creditSourceStats.net }}</strong>
        <small>{{ creditSourcePage.total }} 条流水</small>
      </article>
    </section>
    <section class="split-grid">
      <div class="panel">
        <div class="section-head"><h2>来源分类</h2>
          <p>{{ creditSourceStats.bySource.length }} 类</p></div>
        <div class="source-breakdown">
          <div v-for="item in creditSourceStats.bySource" :key="item.sourceType">
            <strong>{{ sourceTypeText(item.sourceType) }}</strong>
            <span>收入 +{{ item.income }}</span>
            <span>消费 -{{ item.expense }}</span>
            <small>{{ item.count }} 笔记录</small>
          </div>
          <p v-if="creditSourceStats.bySource.length===0" class="state-block">暂无积分流水</p>
        </div>
      </div>
      <div class="panel">
        <div class="section-head"><h2>明细记录</h2>
          <p>{{ creditSourcePage.total }} 条</p></div>
        <div class="transaction-list">
          <article v-for="tx in creditSources" :key="tx.id" class="transaction-item">
            <div>
              <strong>{{ tx.remark || sourceTypeText(tx.sourceType) }}</strong>
              <small>{{ sourceTypeText(tx.sourceType) }} / {{ tx.sourceNo || '-' }} / {{
                  formatCell(tx.createdAt)
                }}</small>
            </div>
            <div class="transaction-amount">
              <span :class="['data-badge', badgeClass(tx.transactionType)]">{{
                  transactionTypeText(tx.transactionType)
                }}</span>
              <strong :class="tx.transactionType === 'INCREASE' ? 'amount-plus' : 'amount-minus'">{{
                  tx.transactionType === 'INCREASE' ? '+' : '-'
                }}{{ tx.amount || 0 }}</strong>
              <small>{{ tx.balanceBefore ?? '-' }} -> {{ tx.balanceAfter ?? '-' }}</small>
            </div>
          </article>
          <p v-if="creditSources.length===0" class="state-block">暂无积分来源记录</p>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* Styles are inherited from base.css via student-dark-page. */
</style>
