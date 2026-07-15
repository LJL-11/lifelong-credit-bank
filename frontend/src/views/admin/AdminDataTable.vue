<script setup>
import { LoaderCircle, Plus, ReceiptText, WalletCards, Save, Trash2 } from "@lucide/vue";
import { displayLabel } from "@/utils/displayLabels.js";

const props = defineProps({
  currentModule: { type: Object, default: () => ({}) },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: "" },
  page: { type: Object, default: () => ({ current: 1, size: 10, total: 0 }) },
  totalPages: { type: Number, default: 1 },
  saving: { type: Boolean, default: false },
  activeModule: { type: String, default: "" },
  hasCustomRowActions: { type: Boolean, default: false },
  token: { type: String, default: "" },
  userInfo: { type: Object, default: null },
});

const emit = defineEmits([
  "open-editor",
  "remove-row",
  "change-page",
  "review-enrollment",
  "review-achievement",
  "review-job-application",
  "recompute-integrity",
  "toggle-learner-status",
  "open-credit-dialog",
  "open-account-dialog",
  "open-freeze-dialog",
  "reload",
]);

function formatCell(v) {
  if (v === null || v === undefined || v === "") return "-";
  if (typeof v === "string" && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(v)) {
    return v.replace("T", " ").slice(0, 16);
  }
  return displayLabel(v);
}

function badgeClass(v) {
  const t = String(v || "").toLowerCase();
  if (/active|open|published|approved|passed|increase|paid|delivered/.test(t)) return "success";
  if (/pending|learning|submitted|viewed/.test(t)) return "pending";
  if (/upcoming/.test(t)) return "pending";
  if (/reject|disable|closed|ended|failed|consume|refunded|cancelled/.test(t)) return "danger";
  return "neutral";
}

function proofMaterialHref(value) {
  if (!value) return "";
  const val = String(value).trim();
  if (!val) return "";
  const urlReg = /^https?:\/\//i;
  if (urlReg.test(val) || val.startsWith("/uploads/")) return val;
  return "";
}
</script>

<template>
  <section class="panel">
    <div class="section-head table-headline">
      <div>
        <h2>{{ currentModule.tableTitle }}</h2>
        <p>{{ page.total }} 条记录</p>
      </div>
      <div class="table-actions">
        <button v-if="currentModule.operations?.credit" class="ghost-button" type="button"
                @click="emit('open-credit-dialog', 'increase')">
          <Plus :size="17"/>
          增加积分
        </button>
        <button v-if="currentModule.operations?.credit" class="ghost-button" type="button"
                @click="emit('open-credit-dialog', 'consume')">
          <ReceiptText :size="17"/>
          消费积分
        </button>
        <button v-if="currentModule.operations?.openAccount" class="ghost-button" type="button"
                @click="emit('open-account-dialog')">
          <WalletCards :size="17"/>
          开通账户
        </button>
        <button v-if="currentModule.operations?.freeze" class="ghost-button" type="button"
                @click="emit('open-freeze-dialog', 'freeze')">冻结积分
        </button>
        <button v-if="currentModule.operations?.freeze" class="ghost-button" type="button"
                @click="emit('open-freeze-dialog', 'unfreeze')">解冻积分
        </button>
        <button v-if="currentModule.canCreate" class="primary-button" type="button" @click="emit('open-editor')">
          <Plus :size="17"/>
          {{ currentModule.createLabel || "新增" }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="state-block">
      <LoaderCircle class="spin" :size="22"/>
      正在加载数据...
    </div>
    <div v-else-if="error" class="state-block error">{{ error }}</div>
    <div v-else class="table-wrap">
      <table>
        <thead>
        <tr>
          <th v-for="col in currentModule.columns" :key="col.key">{{ col.label }}</th>
          <th v-if="currentModule.canEdit || currentModule.canDelete || hasCustomRowActions">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="rows.length === 0">
          <td :colspan="currentModule.columns.length + 1" class="empty-cell">暂无数据</td>
        </tr>
        <tr v-for="(row, rowIdx) in rows" :key="row.id" :class="{ 'row-even': rowIdx % 2 === 1 }">
          <td v-for="col in currentModule.columns" :key="col.key">
            <div v-if="col.key === 'auditAction'" class="row-actions inline-actions">
              <button v-if="row.auditStatus === 'PENDING'" type="button" class="primary-button"
                      @click="emit('review-achievement', row, 'APPROVED')">通过
              </button>
              <button v-if="row.auditStatus === 'PENDING'" type="button"
                      @click="emit('review-achievement', row, 'REJECTED')">驳回
              </button>
              <span v-if="row.auditStatus !== 'PENDING'" class="data-badge neutral">已处理</span>
            </div>
            <a v-else-if="['proofUrl', 'resumeUrl'].includes(col.key) && proofMaterialHref(row[col.key])" class="resource-link"
               :href="proofMaterialHref(row[col.key])" target="_blank" rel="noopener">{{ col.key === 'resumeUrl' ? (row.resumeFileName || '查看简历') : '查看材料' }}</a>
            <span v-else-if="['proofUrl', 'resumeUrl'].includes(col.key)">{{ row[col.key] ? '文件地址无效' : '-' }}</span>
            <span v-else-if="col.badge"
                  :class="['data-badge', badgeClass(row[col.key])]">{{ formatCell(row[col.key]) }}</span>
            <span v-else>{{ formatCell(row[col.key]) }}</span>
          </td>
          <td v-if="currentModule.canEdit || currentModule.canDelete || hasCustomRowActions" class="row-actions">
            <button v-if="currentModule.canEdit" type="button" @click="emit('open-editor', row)">
              <Save :size="15"/>
              编辑
            </button>
            <button v-if="currentModule.canDelete" type="button" @click="emit('remove-row', row)">
              <Trash2 :size="15"/>
              删除
            </button>
            <button v-if="activeModule === 'learners'" type="button" @click="emit('toggle-learner-status', row)"
                    :class="row.status === 'ACTIVE' ? 'ghost-button' : 'primary-button'">
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </button>
            <button v-if="activeModule === 'enrollments' && row.enrollStatus === 'PENDING'" type="button"
                    class="primary-button" @click="emit('review-enrollment', row, 'APPROVED')">通过
            </button>
            <button v-if="activeModule === 'enrollments' && row.enrollStatus === 'PENDING'" type="button"
                    @click="emit('review-enrollment', row, 'REJECTED')">驳回
            </button>
            <button v-if="activeModule === 'job-applications' && ['SUBMITTED', 'VIEWED', 'PENDING'].includes(row.applyStatus)" type="button"
                    class="ghost-button" @click="emit('review-job-application', row, 'VIEWED')">已查看
            </button>
            <button v-if="activeModule === 'job-applications' && ['SUBMITTED', 'VIEWED', 'PENDING'].includes(row.applyStatus)" type="button"
                    class="primary-button" @click="emit('review-job-application', row, 'ACCEPTED')">接受
            </button>
            <button v-if="activeModule === 'job-applications' && ['SUBMITTED', 'VIEWED', 'PENDING'].includes(row.applyStatus)" type="button"
                    @click="emit('review-job-application', row, 'REJECTED')">拒绝
            </button>
            <span v-if="activeModule === 'job-applications' && !['SUBMITTED', 'VIEWED', 'PENDING'].includes(row.applyStatus)"
                  :class="['data-badge', badgeClass(row.applyStatus)]">
              {{ displayLabel(row.applyStatus) }}
            </span>
            <button v-if="activeModule === 'integrity-ratings'" type="button" class="ghost-button"
                    @click="emit('recompute-integrity', row)">重算
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <footer class="pager">
      <button type="button" :disabled="page.current <= 1" @click="emit('change-page', page.current - 1)">上一页</button>
      <span>第 {{ page.current }} / {{ totalPages }} 页</span>
      <button type="button" :disabled="page.current >= totalPages" @click="emit('change-page', page.current + 1)">下一页
      </button>
    </footer>
  </section>
</template>
