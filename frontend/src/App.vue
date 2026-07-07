<script setup>
import { computed, onMounted, ref } from "vue";
import {
  Award,
  BookOpen,
  BriefcaseBusiness,
  ClipboardList,
  FileText,
  LayoutDashboard,
  LoaderCircle,
  MessagesSquare,
  Plus,
  ReceiptText,
  RefreshCw,
  Save,
  Trash2,
  Users,
  WalletCards,
  X,
} from "@lucide/vue";
import { menu, modules } from "./modules";

const icons = {
  Award,
  BookOpen,
  BriefcaseBusiness,
  ClipboardList,
  LayoutDashboard,
  MessagesSquare,
  ReceiptText,
  Users,
  WalletCards,
};

const activeModule = ref("dashboard");
const stats = ref({});
const rows = ref([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const apiOnline = ref(false);
const page = ref({ current: 1, size: 10, total: 0 });
const editor = ref({ open: false, row: null, form: {} });
const creditDialog = ref({ open: false, type: "increase", form: {} });
const accountDialog = ref({ open: false, learnerId: "" });
const toast = ref({ text: "", type: "ok" });
let toastTimer = 0;

const currentModule = computed(() => modules[activeModule.value]);
const totalPages = computed(() => Math.max(1, Math.ceil(page.value.total / page.value.size)));
const statCards = computed(() => [
  { key: "learnerCount", label: "学员总数", value: stats.value.learnerCount ?? 0, note: "已建档学员" },
  { key: "courseCount", label: "课程资源", value: stats.value.courseCount ?? 0, note: "可认定课程" },
  { key: "achievementCount", label: "成果认定", value: stats.value.achievementCount ?? 0, note: "审核记录" },
  { key: "creditTransactionCount", label: "积分流水", value: stats.value.creditTransactionCount ?? 0, note: "积分变动" },
  { key: "forumPostCount", label: "论坛帖子", value: stats.value.forumPostCount ?? 0, note: "社区内容" },
  { key: "jobPostingCount", label: "招聘岗位", value: stats.value.jobPostingCount ?? 0, note: "就业机会" },
]);
const overview = computed(() => [
  { label: "当前模块", value: currentModule.value.title },
  { label: "分页大小", value: `${page.value.size} 条/页` },
  { label: "接口状态", value: apiOnline.value ? "正常" : "待检查" },
]);
const quickActions = computed(() => menu.filter((item) => item.key !== "dashboard").slice(0, 6));

async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const result = await response.json();
  if (!response.ok || result.code !== 200) {
    throw new Error(result.message || "请求失败");
  }
  return result.data;
}

async function loadDashboard() {
  loading.value = true;
  error.value = "";
  try {
    stats.value = await request("/api/admin/dashboard/stats");
    apiOnline.value = true;
  } catch (err) {
    apiOnline.value = false;
    error.value = err.message;
    showToast(err.message, "error");
  } finally {
    loading.value = false;
  }
}

async function loadTable() {
  loading.value = true;
  error.value = "";
  try {
    const data = await request(`${currentModule.value.api}?current=${page.value.current}&size=${page.value.size}`);
    rows.value = data.records || [];
    page.value.total = data.total || 0;
    page.value.current = data.current || page.value.current;
    apiOnline.value = true;
  } catch (err) {
    rows.value = [];
    apiOnline.value = false;
    error.value = err.message;
  } finally {
    loading.value = false;
  }
}

function switchModule(key) {
  activeModule.value = key;
  page.value.current = 1;
  rows.value = [];
  error.value = "";
  if (key === "dashboard") {
    loadDashboard();
  } else {
    loadTable();
  }
}

function reloadActive() {
  if (activeModule.value === "dashboard") {
    loadDashboard();
  } else {
    loadTable();
  }
}

function changePage(nextPage) {
  page.value.current = Math.min(Math.max(nextPage, 1), totalPages.value);
  loadTable();
}

function openEditor(row = null) {
  const form = {};
  currentModule.value.fields.forEach((field) => {
    form[field.key] = row?.[field.key] ?? "";
  });
  editor.value = { open: true, row, form };
}

function closeEditor() {
  editor.value = { open: false, row: null, form: {} };
}

function normalizePayload(form, fields = []) {
  const numericKeys = new Set(fields.filter((field) => field.type === "number").map((field) => field.key));
  return Object.entries(form).reduce((payload, [key, value]) => {
    if (value === "") {
      return payload;
    }
    payload[key] = numericKeys.has(key) ? Number(value) : value;
    return payload;
  }, {});
}

async function submitEditor() {
  const payload = normalizePayload(editor.value.form, currentModule.value.fields);
  const id = editor.value.row?.id;
  const method = id ? "PUT" : "POST";
  const url = id ? `${currentModule.value.api}/${id}` : currentModule.value.api;
  saving.value = true;
  try {
    await request(url, { method, body: JSON.stringify(payload) });
    showToast("保存成功");
    closeEditor();
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saving.value = false;
  }
}

async function removeRow(row) {
  if (!window.confirm(`确认删除 ID ${row.id}？`)) {
    return;
  }
  try {
    await request(`${currentModule.value.api}/${row.id}`, { method: "DELETE" });
    showToast("删除成功");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function openCreditDialog(type) {
  creditDialog.value = {
    open: true,
    type,
    form: { learnerId: "", amount: "", sourceType: "MANUAL", sourceNo: "", remark: "" },
  };
}

function closeCreditDialog() {
  creditDialog.value.open = false;
}

async function submitCreditDialog() {
  const payload = normalizePayload(creditDialog.value.form, [
    { key: "learnerId", type: "number" },
    { key: "amount", type: "number" },
  ]);
  const url = `/api/admin/credit-accounts/${creditDialog.value.type}`;
  saving.value = true;
  try {
    await request(url, { method: "POST", body: JSON.stringify(payload) });
    showToast("操作成功");
    closeCreditDialog();
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saving.value = false;
  }
}

function openAccountDialog() {
  accountDialog.value = { open: true, learnerId: "" };
}

function closeAccountDialog() {
  accountDialog.value.open = false;
}

async function submitOpenAccount() {
  saving.value = true;
  try {
    await request(`/api/admin/credit-accounts/open/${accountDialog.value.learnerId}`, { method: "POST" });
    showToast("账户已开通");
    closeAccountDialog();
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saving.value = false;
  }
}

function formatCell(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  if (typeof value === "string" && value.includes("T")) {
    return value.replace("T", " ").slice(0, 16);
  }
  return value;
}

function badgeClass(value) {
  const text = String(value || "").toLowerCase();
  if (text.includes("active") || text.includes("open") || text.includes("published") || text.includes("approved") || text.includes("passed") || text.includes("increase")) {
    return "success";
  }
  if (text.includes("pending") || text.includes("learning")) {
    return "pending";
  }
  if (text.includes("reject") || text.includes("disable") || text.includes("closed") || text.includes("failed") || text.includes("consume")) {
    return "danger";
  }
  return "neutral";
}

function showToast(text, type = "ok") {
  toast.value = { text, type };
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => {
    toast.value.text = "";
  }, 2600);
}

onMounted(loadDashboard);
</script>

<template>
  <aside class="sidebar">
    <div class="brand">
      <span class="brand-mark">LB</span>
      <div>
        <strong>学分银行</strong>
        <small>Credit Bank Admin</small>
      </div>
    </div>

    <nav class="nav">
      <button
        v-for="item in menu"
        :key="item.key"
        :class="{ active: activeModule === item.key }"
        type="button"
        @click="switchModule(item.key)"
      >
        <component :is="icons[item.icon]" :size="18" />
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </aside>

  <main class="workspace">
    <header class="topbar">
      <div>
        <p class="eyebrow">Lifelong Credit Bank</p>
        <h1>{{ currentModule.title }}</h1>
        <p class="subtitle">{{ currentModule.subtitle }}</p>
      </div>
      <div class="top-actions">
        <a class="ghost-button" href="/swagger-ui.html" target="_blank">
          <FileText :size="17" />
          接口文档
        </a>
        <button class="primary-button" type="button" @click="reloadActive">
          <RefreshCw :size="17" />
          刷新
        </button>
      </div>
    </header>

    <section v-if="activeModule === 'dashboard'" class="dashboard">
      <div class="stats-grid">
        <article class="stat-card" v-for="card in statCards" :key="card.key">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <small>{{ card.note }}</small>
        </article>
      </div>

      <div class="split-grid">
        <section class="panel">
          <div class="section-head">
            <h2>平台概览</h2>
            <span :class="['status-pill', apiOnline ? 'ok' : 'warn']">
              {{ apiOnline ? "服务正常" : "服务异常" }}
            </span>
          </div>
          <div class="overview-list">
            <div v-for="item in overview" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="section-head">
            <h2>快捷入口</h2>
          </div>
          <div class="quick-actions">
            <button v-for="item in quickActions" :key="item.key" type="button" @click="switchModule(item.key)">
              {{ item.label }}
            </button>
          </div>
        </section>
      </div>
    </section>

    <section v-else class="panel">
      <div class="section-head table-headline">
        <div>
          <h2>{{ currentModule.tableTitle }}</h2>
          <p>{{ page.total }} 条记录</p>
        </div>
        <div class="table-actions">
          <button v-if="currentModule.operations?.credit" class="ghost-button" type="button" @click="openCreditDialog('increase')">
            <Plus :size="17" />
            增加积分
          </button>
          <button v-if="currentModule.operations?.credit" class="ghost-button" type="button" @click="openCreditDialog('consume')">
            <ReceiptText :size="17" />
            消费积分
          </button>
          <button v-if="currentModule.operations?.openAccount" class="ghost-button" type="button" @click="openAccountDialog">
            <WalletCards :size="17" />
            开通账户
          </button>
          <button v-if="currentModule.canCreate" class="primary-button" type="button" @click="openEditor()">
            <Plus :size="17" />
            新增
          </button>
        </div>
      </div>

      <div v-if="loading" class="state-block">
        <LoaderCircle class="spin" :size="22" />
        正在加载数据...
      </div>
      <div v-else-if="error" class="state-block error">{{ error }}</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th v-for="column in currentModule.columns" :key="column.key">{{ column.label }}</th>
              <th v-if="currentModule.canEdit || currentModule.canDelete">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="rows.length === 0">
              <td :colspan="currentModule.columns.length + 1" class="empty-cell">暂无数据</td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td v-for="column in currentModule.columns" :key="column.key">
                <span v-if="column.badge" :class="['data-badge', badgeClass(row[column.key])]">
                  {{ formatCell(row[column.key]) }}
                </span>
                <span v-else>{{ formatCell(row[column.key]) }}</span>
              </td>
              <td v-if="currentModule.canEdit || currentModule.canDelete" class="row-actions">
                <button v-if="currentModule.canEdit" type="button" @click="openEditor(row)">
                  <Save :size="15" />
                  编辑
                </button>
                <button v-if="currentModule.canDelete" type="button" @click="removeRow(row)">
                  <Trash2 :size="15" />
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pager">
        <button type="button" :disabled="page.current <= 1" @click="changePage(page.current - 1)">上一页</button>
        <span>第 {{ page.current }} / {{ totalPages }} 页</span>
        <button type="button" :disabled="page.current >= totalPages" @click="changePage(page.current + 1)">下一页</button>
      </footer>
    </section>
  </main>

  <div v-if="editor.open" class="modal-backdrop" @click.self="closeEditor">
    <form class="modal" @submit.prevent="submitEditor">
      <div class="modal-head">
        <h2>{{ editor.row?.id ? "编辑" : "新增" }}{{ currentModule.title }}</h2>
        <button type="button" class="icon-button" @click="closeEditor">
          <X :size="18" />
        </button>
      </div>
      <div class="form-grid">
        <label v-for="field in currentModule.fields" :key="field.key">
          <span>{{ field.label }}</span>
          <textarea v-if="field.type === 'textarea'" v-model="editor.form[field.key]" :placeholder="field.placeholder || field.label"></textarea>
          <input v-else v-model="editor.form[field.key]" :type="field.type || 'text'" :placeholder="field.placeholder || field.label" />
        </label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="closeEditor">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">
          <Save :size="17" />
          {{ saving ? "保存中..." : "保存" }}
        </button>
      </div>
    </form>
  </div>

  <div v-if="creditDialog.open" class="modal-backdrop" @click.self="closeCreditDialog">
    <form class="modal compact" @submit.prevent="submitCreditDialog">
      <div class="modal-head">
        <h2>{{ creditDialog.type === "increase" ? "增加积分" : "消费积分" }}</h2>
        <button type="button" class="icon-button" @click="closeCreditDialog">
          <X :size="18" />
        </button>
      </div>
      <div class="form-grid one">
        <label>
          <span>学员 ID</span>
          <input v-model="creditDialog.form.learnerId" type="number" min="1" required />
        </label>
        <label>
          <span>积分数量</span>
          <input v-model="creditDialog.form.amount" type="number" min="1" required />
        </label>
        <label>
          <span>来源类型</span>
          <input v-model="creditDialog.form.sourceType" placeholder="COURSE / MANUAL" />
        </label>
        <label>
          <span>来源编号</span>
          <input v-model="creditDialog.form.sourceNo" placeholder="业务编号" />
        </label>
        <label>
          <span>备注</span>
          <textarea v-model="creditDialog.form.remark" placeholder="操作说明"></textarea>
        </label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="closeCreditDialog">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">提交</button>
      </div>
    </form>
  </div>

  <div v-if="accountDialog.open" class="modal-backdrop" @click.self="closeAccountDialog">
    <form class="modal compact" @submit.prevent="submitOpenAccount">
      <div class="modal-head">
        <h2>开通积分账户</h2>
        <button type="button" class="icon-button" @click="closeAccountDialog">
          <X :size="18" />
        </button>
      </div>
      <div class="form-grid one">
        <label>
          <span>学员 ID</span>
          <input v-model="accountDialog.learnerId" type="number" min="1" required />
        </label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="closeAccountDialog">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">开通</button>
      </div>
    </form>
  </div>

  <div v-if="toast.text" :class="['toast', toast.type]">{{ toast.text }}</div>
</template>
