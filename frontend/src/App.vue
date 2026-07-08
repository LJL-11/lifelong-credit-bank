<script setup>
import { computed, onMounted, ref } from "vue";
import {
  Award, BookOpen, BriefcaseBusiness, ClipboardList, FileText,
  LayoutDashboard, LoaderCircle, LogOut, MessagesSquare, Plus,
  ReceiptText, RefreshCw, Save, ShoppingBag, Trash2, UserCircle,
  Users, WalletCards, X, Zap,
} from "@lucide/vue";
import { menu, modules } from "./modules";
import LoginView from "./LoginView.vue";
import CreditMall from "./CreditMall.vue";
import ForumView from "./ForumView.vue";

// ==================== 认证状态 ====================
const token = ref(localStorage.getItem("token") || "");
const userInfo = ref(JSON.parse(localStorage.getItem("userInfo") || "null"));
const loggedIn = ref(!!token.value);

function handleLoginSuccess({ token: t, userInfo: u }) {
  token.value = t;
  userInfo.value = u;
  loggedIn.value = true;
  if (u.role === "ADMIN") {
    currentView.value = "admin";
    activeModule.value = "dashboard";
    loadDashboard();
  } else {
    currentView.value = "mall";
    activeModule.value = "mall";
  }
}

function logout() {
  fetch("/api/auth/logout", {
    method: "POST",
    headers: { Authorization: `Bearer ${token.value}` },
  }).catch(() => {});
  token.value = "";
  userInfo.value = null;
  loggedIn.value = false;
  localStorage.removeItem("token");
  localStorage.removeItem("userInfo");
  currentView.value = "admin";
  activeModule.value = "dashboard";
}

// ==================== 角色判断 ====================
const isAdmin = computed(() => userInfo.value?.role === "ADMIN");
const currentLearnerId = computed(() => userInfo.value?.learnerId || 0);

const adminMenu = computed(() => menu);
const studentMenu = computed(() => [
  { key: "profile", label: "我的信息", icon: "UserCircle" },
  { key: "courses", label: "我的课程", icon: "BookOpen" },
  { key: "forum", label: "学习论坛", icon: "MessagesSquare" },
  { key: "mall", label: "积分商城", icon: "ShoppingBag" },
]);
const currentMenu = computed(() => isAdmin.value ? adminMenu.value : studentMenu.value);

// ==================== 图标 ====================
const icons = {
  Award, BookOpen, BriefcaseBusiness, ClipboardList,
  LayoutDashboard, MessagesSquare, ReceiptText, Users, WalletCards,
  UserCircle, ShoppingBag, Zap,
};

const activeModule = ref("dashboard");
const currentView = ref("admin");
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
const freezeDialog = ref({ open: false, type: "freeze", form: { learnerId: "", amount: "" } });
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

// ==================== API ====================
async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(token.value ? { Authorization: `Bearer ${token.value}` } : {}),
    ...(options.headers || {}),
  };
  const response = await fetch(url, { ...options, headers });
  const result = await response.json();
  if (response.status === 401) {
    showToast(result.message || "登录已过期", "error");
    logout();
    throw new Error(result.message || "未登录");
  }
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
  } finally { loading.value = false; }
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
  } finally { loading.value = false; }
}

const profile = ref(null);
const courses = ref([]);
const coursePage = ref({ current: 1, size: 10, total: 0 });
const learnedCourseIds = ref(new Set());
async function loadProfile() {
  loading.value = true;
  try { profile.value = await request("/api/student/profile"); }
  catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

async function loadCourses() {
  loading.value = true;
  try {
    const data = await request(`/api/student/courses?current=${coursePage.value.current}&size=${coursePage.value.size}`);
    courses.value = data.records || [];
    coursePage.value.total = data.total || 0;
  } catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

async function learnCourse(course) {
  if (!confirm(`确认学习「${course.courseName}」并获得 ${course.creditPoint} 积分？`)) return;
  loading.value = true;
  try {
    const result = await request(`/api/student/courses/${course.id}/learn`, { method: "POST" });
    learnedCourseIds.value = new Set([...learnedCourseIds.value, course.id]);
    showToast(`完成学习！获得 ${result.creditPoint} 积分`);
    // 刷新用户信息中的积分余额
    if (currentView.value === "profile") loadProfile();
  } catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

function switchModule(key) {
  activeModule.value = key;
  page.value.current = 1;
  rows.value = [];
  error.value = "";
  if (key === "dashboard") {
    currentView.value = "admin";
    loadDashboard();
  } else if (key === "mall") {
    currentView.value = "mall";
  } else if (key === "forum") {
    currentView.value = "forum";
  } else if (key === "profile") {
    currentView.value = "profile";
    loadProfile();
  } else if (key === "courses") {
    if (isAdmin.value) {
      currentView.value = "admin";
      loadTable();
    } else {
      currentView.value = "courses";
      loadCourses();
    }
  } else {
    currentView.value = "admin";
    loadTable();
  }
}

function reloadActive() {
  if (activeModule.value === "dashboard") loadDashboard();
  else if (activeModule.value === "mall" || activeModule.value === "forum") { /* own refresh */ }
  else if (activeModule.value === "profile") loadProfile();
  else if (activeModule.value === "courses") {
    if (isAdmin.value) loadTable();
    else loadCourses();
  }
  else loadTable();
}

function changePage(n) { page.value.current = Math.min(Math.max(n, 1), totalPages.value); loadTable(); }

function openEditor(row = null) {
  const form = {};
  currentModule.value.fields.forEach(f => { form[f.key] = row?.[f.key] ?? ""; });
  editor.value = { open: true, row, form };
}
function closeEditor() { editor.value = { open: false, row: null, form: {} }; }

function normalizePayload(form, fields = []) {
  const numKeys = new Set(fields.filter(f => f.type === "number").map(f => f.key));
  return Object.entries(form).reduce((p, [k, v]) => {
    if (v === "") return p;
    p[k] = numKeys.has(k) ? Number(v) : v;
    return p;
  }, {});
}

async function submitEditor() {
  const payload = normalizePayload(editor.value.form, currentModule.value.fields);
  const id = editor.value.row?.id;
  const method = id ? "PUT" : "POST";
  const url = id ? `${currentModule.value.api}/${id}` : currentModule.value.api;
  saving.value = true;
  try { await request(url, { method, body: JSON.stringify(payload) }); showToast("保存成功"); closeEditor(); await loadTable(); }
  catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function removeRow(row) {
  if (!confirm(`确认删除 ID ${row.id}？`)) return;
  try { await request(`${currentModule.value.api}/${row.id}`, { method: "DELETE" }); showToast("删除成功"); await loadTable(); }
  catch (err) { showToast(err.message, "error"); }
}

function openCreditDialog(type) {
  creditDialog.value = { open: true, type, form: { learnerId: "", amount: "", sourceType: "MANUAL", sourceNo: "", remark: "" } };
}
function closeCreditDialog() { creditDialog.value.open = false; }

async function submitCreditDialog() {
  const payload = normalizePayload(creditDialog.value.form, [{ key: "learnerId", type: "number" }, { key: "amount", type: "number" }]);
  saving.value = true;
  try { await request(`/api/admin/credit-accounts/${creditDialog.value.type}`, { method: "POST", body: JSON.stringify(payload) }); showToast("操作成功"); closeCreditDialog(); await loadTable(); }
  catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

function openAccountDialog() { accountDialog.value = { open: true, learnerId: "" }; }
function closeAccountDialog() { accountDialog.value.open = false; }

async function submitOpenAccount() {
  saving.value = true;
  try { await request(`/api/admin/credit-accounts/open/${accountDialog.value.learnerId}`, { method: "POST" }); showToast("账户已开通"); closeAccountDialog(); await loadTable(); }
  catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

function openFreezeDialog(type) { freezeDialog.value = { open: true, type, form: { learnerId: "", amount: "" } }; }
function closeFreezeDialog() { freezeDialog.value.open = false; }

async function submitFreezeDialog() {
  saving.value = true;
  try {
    const body = { learnerId: Number(freezeDialog.value.form.learnerId), amount: Number(freezeDialog.value.form.amount) };
    await request(`/api/admin/credit-accounts/${freezeDialog.value.type}`, { method: "POST", body: JSON.stringify(body) });
    showToast(freezeDialog.value.type === "freeze" ? "冻结成功" : "解冻成功");
    closeFreezeDialog(); await loadTable();
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function toggleLearnerStatus(row) {
  const newStatus = row.status === "ACTIVE" ? "DISABLED" : "ACTIVE";
  if (!confirm(`确认将 ${row.realName} 的状态改为 ${newStatus}？`)) return;
  try { await request(`/api/admin/learners/${row.id}/status`, { method: "PUT", body: JSON.stringify({ status: newStatus }) }); showToast("状态已更新"); await loadTable(); }
  catch (err) { showToast(err.message, "error"); }
}

function formatCell(v) {
  if (v === null || v === undefined || v === "") return "-";
  if (typeof v === "string" && v.includes("T")) return v.replace("T", " ").slice(0, 16);
  return v;
}
function badgeClass(v) {
  const t = String(v || "").toLowerCase();
  if (/active|open|published|approved|passed|increase|paid|delivered/.test(t)) return "success";
  if (/pending|learning/.test(t)) return "pending";
  if (/upcoming/.test(t)) return "pending";
  if (/reject|disable|closed|ended|failed|consume|refunded|cancelled/.test(t)) return "danger";
  return "neutral";
}
function showToast(text, type = "ok") {
  toast.value = { text, type };
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.value.text = ""; }, 2600);
}

onMounted(() => {
  if (!loggedIn.value) return;
  if (isAdmin.value) {
    currentView.value = "admin";
    activeModule.value = "dashboard";
    loadDashboard();
  } else {
    currentView.value = "mall";
    activeModule.value = "mall";
  }
});
</script>

<template>
  <LoginView v-if="!loggedIn" @login-success="handleLoginSuccess" />

  <div v-else class="app-layout">
    <!-- ====== 侧边栏 ====== -->
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">LB</span>
        <div>
          <strong>学分银行</strong>
          <small>{{ isAdmin ? "Admin" : "Student" }}</small>
        </div>
      </div>

      <div v-if="!isAdmin" class="user-card">
        <UserCircle :size="36" />
        <div>
          <strong>{{ userInfo?.realName }}</strong>
          <small>{{ userInfo?.username }}</small>
          <small v-if="userInfo?.institutionName" class="inst-label">{{ userInfo.institutionName }}</small>
        </div>
      </div>
      <div v-if="isAdmin && userInfo?.institutionName" class="inst-badge">
        {{ userInfo.institutionName }}
      </div>

      <nav class="nav">
        <button v-for="item in currentMenu" :key="item.key"
          :class="{ active: activeModule === item.key }"
          type="button" @click="switchModule(item.key)">
          <component :is="icons[item.icon]" :size="18" />
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="nav-footer">
        <button class="logout-btn" type="button" @click="logout">
          <LogOut :size="16" /> 退出登录
        </button>
      </div>
    </aside>

    <!-- ====== 积分商城 ====== -->
    <CreditMall
      v-if="currentView === 'mall'"
      :learner-id="currentLearnerId"
      :token="token"
      :show-id-input="isAdmin"
    />

    <!-- ====== 学习论坛 ====== -->
    <ForumView v-else-if="currentView === 'forum'"
      :token="token" :is-admin="isAdmin"
      :current-user-id="currentLearnerId"
      :current-user-name="userInfo?.realName || ''"
    />


    <!-- 学员我的课程 -->
    <main v-else-if="currentView === 'courses'" class="workspace">
      <header class="topbar">
        <div><p class="eyebrow">Student Portal</p><h1>我的课程</h1><p class="subtitle">本机构发布的课程资源</p></div>
      </header>
      <section class="panel">
        <div v-if="loading" class="state-block"><LoaderCircle class="spin" :size="22" /> 加载中...</div>
        <div v-else-if="courses.length === 0" class="state-block">暂无课程</div>
        <div v-else class="table-wrap">
          <table><thead><tr><th>ID</th><th>课程编码</th><th>课程名称</th><th>提供方</th><th>分类</th><th>学分</th><th>积分</th><th>操作</th></tr></thead>
            <tbody><tr v-for="c in courses" :key="c.id">
              <td>{{ c.id }}</td><td>{{ c.courseCode }}</td><td>{{ c.courseName }}</td>
              <td>{{ c.provider }}</td><td>{{ c.category }}</td><td>{{ c.creditValue }}</td><td>{{ c.creditPoint }}</td>
              <td>
                <button v-if="!learnedCourseIds.has(c.id)"
                  class="primary-button small" type="button"
                  :disabled="loading"
                  @click="learnCourse(c)">开始学习</button>
                <span v-else class="data-badge success">已学完</span>
              </td>
            </tr></tbody>
          </table>
        </div>
        <footer v-if="coursePage.total > coursePage.size" class="pager">
          <button :disabled="coursePage.current <= 1" @click="coursePage.current--; loadCourses()">上一页</button>
          <span>第 {{ coursePage.current }} / {{ Math.ceil(coursePage.total / coursePage.size) }} 页</span>
          <button :disabled="coursePage.current >= Math.ceil(coursePage.total / coursePage.size)" @click="coursePage.current++; loadCourses()">下一页</button>
        </footer>
      </section>
    </main>


    <!-- ====== 学员个人信息 ====== -->
    <main v-else-if="currentView === 'profile'" class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Student Portal</p>
          <h1>我的信息</h1>
          <p class="subtitle">查看和更新个人资料</p>
        </div>
      </header>
      <section class="panel">
        <div v-if="loading" class="state-block"><LoaderCircle class="spin" :size="22" /> 加载中...</div>
        <div v-else-if="profile" class="profile-grid">
          <div class="profile-field"><span>用户名</span><strong>{{ profile.username }}</strong></div>
          <div class="profile-field"><span>姓名</span><strong>{{ profile.realName }}</strong></div>
          <div class="profile-field"><span>手机号</span><strong>{{ profile.phone || '-' }}</strong></div>
          <div class="profile-field"><span>邮箱</span><strong>{{ profile.email || '-' }}</strong></div>
          <div class="profile-field"><span>学历</span><strong>{{ profile.educationLevel || '-' }}</strong></div>
          <div class="profile-field"><span>角色</span><span :class="['data-badge', profile.role === 'ADMIN' ? 'success' : 'neutral']">{{ profile.role }}</span></div>
        </div>
      </section>
    </main>

    <!-- ====== 管理员后台 ====== -->
    <main v-else class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Lifelong Credit Bank</p>
          <h1>{{ currentModule.title }}</h1>
          <p class="subtitle">{{ currentModule.subtitle }}</p>
        </div>
        <div class="top-actions">
          <a class="ghost-button" href="/swagger-ui.html" target="_blank"><FileText :size="17" /> 接口文档</a>
          <button class="primary-button" type="button" @click="reloadActive"><RefreshCw :size="17" /> 刷新</button>
        </div>
      </header>

      <!-- Dashboard -->
      <section v-if="activeModule === 'dashboard'" class="dashboard">
        <div class="stats-grid">
          <article v-for="card in statCards" :key="card.key" class="stat-card">
            <span>{{ card.label }}</span><strong>{{ card.value }}</strong><small>{{ card.note }}</small>
          </article>
        </div>
        <div class="split-grid">
          <section class="panel">
            <div class="section-head">
              <h2>平台概览</h2>
              <span :class="['status-pill', apiOnline ? 'ok' : 'warn']">{{ apiOnline ? "服务正常" : "服务异常" }}</span>
            </div>
            <div class="overview-list">
              <div v-for="item in overview" :key="item.label"><span>{{ item.label }}</span><strong>{{ item.value }}</strong></div>
            </div>
          </section>
          <section class="panel">
            <div class="section-head"><h2>快捷入口</h2></div>
            <div class="quick-actions">
              <button v-for="item in quickActions" :key="item.key" type="button" @click="switchModule(item.key)">{{ item.label }}</button>
            </div>
          </section>
        </div>
      </section>

      <!-- 数据表格 -->
      <section v-else class="panel">
        <div class="section-head table-headline">
          <div><h2>{{ currentModule.tableTitle }}</h2><p>{{ page.total }} 条记录</p></div>
          <div class="table-actions">
            <button v-if="currentModule.operations?.credit" class="ghost-button" type="button" @click="openCreditDialog('increase')"><Plus :size="17" /> 增加积分</button>
            <button v-if="currentModule.operations?.credit" class="ghost-button" type="button" @click="openCreditDialog('consume')"><ReceiptText :size="17" /> 消费积分</button>
            <button v-if="currentModule.operations?.openAccount" class="ghost-button" type="button" @click="openAccountDialog"><WalletCards :size="17" /> 开通账户</button>
            <button v-if="currentModule.operations?.freeze" class="ghost-button" type="button" @click="openFreezeDialog('freeze')">冻结积分</button>
            <button v-if="currentModule.operations?.freeze" class="ghost-button" type="button" @click="openFreezeDialog('unfreeze')">解冻积分</button>
            <button v-if="currentModule.canCreate" class="primary-button" type="button" @click="openEditor()"><Plus :size="17" /> 新增</button>
          </div>
        </div>

        <div v-if="loading" class="state-block"><LoaderCircle class="spin" :size="22" /> 正在加载数据...</div>
        <div v-else-if="error" class="state-block error">{{ error }}</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th v-for="col in currentModule.columns" :key="col.key">{{ col.label }}</th><th v-if="currentModule.canEdit || currentModule.canDelete">操作</th></tr></thead>
            <tbody>
              <tr v-if="rows.length === 0"><td :colspan="currentModule.columns.length + 1" class="empty-cell">暂无数据</td></tr>
              <tr v-for="row in rows" :key="row.id">
                <td v-for="col in currentModule.columns" :key="col.key">
                  <span v-if="col.badge" :class="['data-badge', badgeClass(row[col.key])]">{{ formatCell(row[col.key]) }}</span>
                  <span v-else>{{ formatCell(row[col.key]) }}</span>
                </td>
                <td v-if="currentModule.canEdit || currentModule.canDelete" class="row-actions">
                  <button v-if="currentModule.canEdit" type="button" @click="openEditor(row)"><Save :size="15" /> 编辑</button>
                  <button v-if="currentModule.canDelete" type="button" @click="removeRow(row)"><Trash2 :size="15" /> 删除</button>
                  <button v-if="activeModule === 'learners'" type="button" @click="toggleLearnerStatus(row)" :class="row.status === 'ACTIVE' ? 'ghost-button' : 'primary-button'">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</button>
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

      <!-- Modals -->
      <div v-if="editor.open" class="modal-backdrop" @click.self="closeEditor">
        <form class="modal" @submit.prevent="submitEditor">
          <div class="modal-head">
            <h2>{{ editor.row?.id ? "编辑" : "新增" }}{{ currentModule.title }}</h2>
            <button type="button" class="icon-button" @click="closeEditor"><X :size="18" /></button>
          </div>
          <div class="form-grid">
            <label v-for="f in currentModule.fields" :key="f.key">
              <span>{{ f.label }}</span>
              <textarea v-if="f.type === 'textarea'" v-model="editor.form[f.key]" :placeholder="f.placeholder || f.label"></textarea>
              <input v-else v-model="editor.form[f.key]" :type="f.type || 'text'" :placeholder="f.placeholder || f.label" />
            </label>
          </div>
          <div class="modal-actions">
            <button class="ghost-button" type="button" @click="closeEditor">取消</button>
            <button class="primary-button" type="submit" :disabled="saving"><Save :size="17" />{{ saving ? "保存中..." : "保存" }}</button>
          </div>
        </form>
      </div>

      <div v-if="creditDialog.open" class="modal-backdrop" @click.self="closeCreditDialog">
        <form class="modal compact" @submit.prevent="submitCreditDialog">
          <div class="modal-head">
            <h2>{{ creditDialog.type === "increase" ? "增加积分" : "消费积分" }}</h2>
            <button type="button" class="icon-button" @click="closeCreditDialog"><X :size="18" /></button>
          </div>
          <div class="form-grid one">
            <label><span>学员 ID</span><input v-model="creditDialog.form.learnerId" type="number" min="1" required /></label>
            <label><span>积分数量</span><input v-model="creditDialog.form.amount" type="number" min="1" required /></label>
            <label><span>来源类型</span><input v-model="creditDialog.form.sourceType" placeholder="COURSE / MANUAL" /></label>
            <label><span>来源编号</span><input v-model="creditDialog.form.sourceNo" placeholder="业务编号" /></label>
            <label><span>备注</span><textarea v-model="creditDialog.form.remark" placeholder="操作说明"></textarea></label>
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
            <button type="button" class="icon-button" @click="closeAccountDialog"><X :size="18" /></button>
          </div>
          <div class="form-grid one">
            <label><span>学员 ID</span><input v-model="accountDialog.learnerId" type="number" min="1" required /></label>
          </div>
          <div class="modal-actions">
            <button class="ghost-button" type="button" @click="closeAccountDialog">取消</button>
            <button class="primary-button" type="submit" :disabled="saving">开通</button>
          </div>
        </form>
      </div>

      <div v-if="freezeDialog.open" class="modal-backdrop" @click.self="closeFreezeDialog">
        <form class="modal compact" @submit.prevent="submitFreezeDialog">
          <div class="modal-head">
            <h2>{{ freezeDialog.type === 'freeze' ? '冻结积分' : '解冻积分' }}</h2>
            <button type="button" class="icon-button" @click="closeFreezeDialog"><X :size="18" /></button>
          </div>
          <div class="form-grid one">
            <label><span>学员 ID</span><input v-model="freezeDialog.form.learnerId" type="number" min="1" required /></label>
            <label><span>积分数量</span><input v-model="freezeDialog.form.amount" type="number" min="1" required /></label>
          </div>
          <div class="modal-actions">
            <button class="ghost-button" type="button" @click="closeFreezeDialog">取消</button>
            <button class="primary-button" type="submit" :disabled="saving">{{ freezeDialog.type === 'freeze' ? '确认冻结' : '确认解冻' }}</button>
          </div>
        </form>
      </div>
    </main>

    <div v-if="toast.text" :class="['toast', toast.type]">{{ toast.text }}</div>
  </div>
</template>

<style scoped>
.user-card {
  display: flex; align-items: center; gap: 10px; padding: 12px; margin-bottom: 8px;
  border-radius: 8px; background: rgba(255,255,255,0.08); color: #ecf7f5;
}
.user-card strong { display: block; font-size: 14px; }
.user-card small { color: #a8c8c3; font-size: 12px; }

.nav-footer { margin-top: auto; padding-top: 12px; border-top: 1px solid rgba(255,255,255,0.10); }
.logout-btn {
  display: flex; align-items: center; gap: 8px; width: 100%; min-height: 38px; padding: 0 12px;
  border: 1px solid rgba(255,255,255,0.15); border-radius: 8px; color: #cfe4e1;
  background: transparent; cursor: pointer; font-size: 14px;
}
.logout-btn:hover { color: #fff; background: rgba(180,35,24,0.25); border-color: rgba(180,35,24,0.4); }

.profile-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
.profile-field { padding: 14px; border: 1px solid var(--line); border-radius: 8px; background: #f9fbfd; }
.profile-field span { display: block; font-size: 12px; color: var(--muted); margin-bottom: 4px; }
.profile-field strong { font-size: 18px; }

.sidebar { display: flex; flex-direction: column; }

/* 机构标签 */
.inst-label { display: block; margin-top: 2px; font-size: 11px; color: #f0c46a; }
.inst-badge {
  padding: 6px 12px; margin-bottom: 8px; border-radius: 6px;
  background: rgba(240,196,106,0.12); color: #f0c46a; font-size: 12px;
  font-weight: 600; text-align: center;
}
</style>
