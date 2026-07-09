<script setup>
import { computed, onMounted, ref } from "vue";
import {
  Award, BookOpen, Bot, BriefcaseBusiness, ClipboardList, FileText,
  LayoutDashboard, LoaderCircle, LogOut, MessagesSquare, Plus,
  ReceiptText, RefreshCw, Save, ShoppingBag, Sparkles, Trash2, UserCircle,
  CheckCircle2, ExternalLink,
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
  { key: "sign", label: "报名签到", icon: "ClipboardList" },
  { key: "credit-sources", label: "积分来源", icon: "ReceiptText" },
  { key: "achievements", label: "成果认定", icon: "Award" },
  { key: "jobs", label: "招聘求职", icon: "BriefcaseBusiness" },
  { key: "integrity", label: "诚信评定", icon: "Award" },
  { key: "forum", label: "学习论坛", icon: "MessagesSquare" },
  { key: "mall", label: "积分商城", icon: "ShoppingBag" },
  { key: "ai", label: "AI 助手", icon: "Sparkles" },
]);
const currentMenu = computed(() => isAdmin.value ? adminMenu.value : studentMenu.value);

// ==================== 图标 ====================
const icons = {
  Award, BookOpen, Bot, BriefcaseBusiness, ClipboardList,
  LayoutDashboard, MessagesSquare, ReceiptText, Users, WalletCards,
  UserCircle, ShoppingBag, Zap, Sparkles,
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
const hasCustomRowActions = computed(() => ["enrollments", "job-applications", "integrity-ratings"].includes(activeModule.value));
const studentSectionModules = ["sign", "credit-sources", "achievements", "jobs", "integrity"];

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
const activeLearning = ref({ open: false, course: null, completedStages: new Set() });
async function loadProfile() {
  loading.value = true;
  try { profile.value = await request("/api/student/profile"); }
  catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

async function loadCourses() {
  loading.value = true;
  try {
    const [courseData, recordData] = await Promise.all([
      request(`/api/student/courses?current=${coursePage.value.current}&size=${coursePage.value.size}`),
      request("/api/student/learning-records?current=1&size=200"),
    ]);
    courses.value = courseData.records || [];
    coursePage.value.total = courseData.total || 0;
    const completedIds = (recordData.records || [])
      .filter(r => r.result === "PASSED")
      .map(r => r.courseId);
    learnedCourseIds.value = new Set(completedIds);
  } catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

function parseLearningStages(course) {
  if (!course) return [];
  try {
    const parsed = JSON.parse(course.learningStages || "[]");
    if (Array.isArray(parsed) && parsed.length) return parsed;
  } catch (err) {
    console.warn("Invalid learning stages", err);
  }
  return [
    { title: "阅读课程资源", description: "打开外部学习资源，完成核心内容阅读或观看。" },
    { title: "整理学习笔记", description: "记录关键概念、示例和仍不清楚的问题。" },
    { title: "完成自测回顾", description: "按课程目标回顾知识点，确认可以独立复述或实践。" },
  ];
}

const activeCourseStages = computed(() => parseLearningStages(activeLearning.value.course));
const allStagesDone = computed(() => activeCourseStages.value.length > 0 && activeLearning.value.completedStages.size === activeCourseStages.value.length);

function openLearningCourse(course) {
  activeLearning.value = { open: true, course, completedStages: new Set() };
}

function closeLearningCourse() {
  activeLearning.value = { open: false, course: null, completedStages: new Set() };
}

function toggleLearningStage(index) {
  const completedStages = new Set(activeLearning.value.completedStages);
  if (completedStages.has(index)) completedStages.delete(index);
  else completedStages.add(index);
  activeLearning.value = { ...activeLearning.value, completedStages };
}

async function completeCourse() {
  const course = activeLearning.value.course;
  if (!course || !allStagesDone.value) return;
  loading.value = true;
  try {
    const result = await request(`/api/student/courses/${course.id}/learn`, { method: "POST" });
    learnedCourseIds.value = new Set([...learnedCourseIds.value, course.id]);
    closeLearningCourse();
    showToast(`完成学习，获得 ${result.creditPoint} 积分`);
    if (currentView.value === "profile") loadProfile();
  } catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}


const enrollments = ref([]);
const signIns = ref([]);
const myAchievements = ref([]);
const jobs = ref([]);
const jobApplications = ref([]);
const integrity = ref(null);
const creditSources = ref([]);
const creditSourceAccount = ref(null);
const creditSourcePage = ref({ current: 1, size: 50, total: 0 });
const achievementForm = ref({ achievementName: "", achievementType: "证书", suggestedCredits: 10, proofUrl: "" });
const proofUploading = ref(false);
const proofFileName = ref("");
const proofFileInput = ref(null);
const todaySigned = computed(() => signIns.value.some(s => s.signDate === todayString()));
const todaySignRecord = computed(() => signIns.value.find(s => s.signDate === todayString()));
const applyDialog = ref({ open: false, job: null, resumeSummary: "" });


function todayString() {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

async function enrollCourse(course) {
  try {
    await request(`/api/student/courses/${course.id}/enroll`, { method: "POST" });
    showToast("报名已提交，等待管理员审核");
    await loadEnrollments();
  } catch (err) { showToast(err.message, "error"); }
}

async function loadEnrollments() {
  const data = await request("/api/student/enrollments?current=1&size=20");
  enrollments.value = data.records || [];
}

async function loadSignIns() {
  const data = await request("/api/student/sign-ins?current=1&size=20");
  signIns.value = data.records || [];
}

async function signToday() {
  if (todaySigned.value) return;
  try {
    const record = await request("/api/student/sign-ins/today", { method: "POST" });
    signIns.value = [record, ...signIns.value.filter(s => s.signDate !== record.signDate)];
    showToast("签到成功，获得 2 积分");
    await loadCreditSources();
  } catch (err) { showToast(err.message, "error"); await loadSignIns(); }
}


async function loadCreditSources() {
  const [accountResult, transactionResult] = await Promise.allSettled([
    request("/api/student/account"),
    request(`/api/student/credit-transactions?current=${creditSourcePage.value.current}&size=${creditSourcePage.value.size}`),
  ]);
  creditSourceAccount.value = accountResult.status === "fulfilled" ? accountResult.value : null;
  if (transactionResult.status === "rejected") throw transactionResult.reason;
  const data = transactionResult.value;
  creditSources.value = data.records || [];
  creditSourcePage.value = { current: data.current || 1, size: data.size || creditSourcePage.value.size, total: data.total || 0 };
}

const creditSourceStats = computed(() => {
  const rows = creditSources.value;
  const income = rows.filter(r => r.transactionType === "INCREASE").reduce((sum, r) => sum + Number(r.amount || 0), 0);
  const expense = rows.filter(r => r.transactionType === "CONSUME").reduce((sum, r) => sum + Number(r.amount || 0), 0);
  const bySource = rows.reduce((acc, r) => {
    const key = r.sourceType || "OTHER";
    if (!acc[key]) acc[key] = { sourceType: key, income: 0, expense: 0, count: 0 };
    acc[key].count += 1;
    if (r.transactionType === "INCREASE") acc[key].income += Number(r.amount || 0);
    else acc[key].expense += Number(r.amount || 0);
    return acc;
  }, {});
  return { income, expense, net: income - expense, bySource: Object.values(bySource) };
});

async function loadMyAchievements() {
  const data = await request("/api/student/achievements?current=1&size=20");
  myAchievements.value = data.records || [];
}


async function uploadProofFile(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  const formData = new FormData();
  formData.append("file", file);
  proofUploading.value = true;
  try {
    const response = await fetch("/api/files/proofs", {
      method: "POST",
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      body: formData,
    });
    const result = await response.json();
    if (!response.ok || result.code !== 200) {
      throw new Error(result.message || "上传失败");
    }
    achievementForm.value.proofUrl = result.data.url;
    proofFileName.value = result.data.fileName || file.name;
    showToast("证明文件已上传");
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    proofUploading.value = false;
  }
}

async function submitAchievement() {
  if (!achievementForm.value.achievementName.trim()) { showToast("请填写成果名称", "error"); return; }
  try {
    await request("/api/student/achievements", { method: "POST", body: JSON.stringify(achievementForm.value) });
    achievementForm.value = { achievementName: "", achievementType: "证书", suggestedCredits: 10, proofUrl: "" };
    proofFileName.value = "";
    showToast("成果已提交审核");
    await loadMyAchievements();
  } catch (err) { showToast(err.message, "error"); }
}

async function loadJobs() {
  const data = await request("/api/student/jobs?current=1&size=20");
  jobs.value = data.records || [];
  const apps = await request("/api/student/job-applications?current=1&size=20");
  jobApplications.value = apps.records || [];
}

function openApplyDialog(job) {
  applyDialog.value = { open: true, job, resumeSummary: "" };
}

async function submitJobApplication() {
  const job = applyDialog.value.job;
  if (!job) return;
  try {
    await request(`/api/student/jobs/${job.id}/apply`, { method: "POST", body: JSON.stringify({ resumeSummary: applyDialog.value.resumeSummary }) });
    applyDialog.value = { open: false, job: null, resumeSummary: "" };
    showToast("投递成功");
    await loadJobs();
  } catch (err) { showToast(err.message, "error"); }
}

async function loadIntegrity() {
  integrity.value = await request("/api/student/integrity");
}

async function loadStudentSection(key) {
  loading.value = true;
  try {
    if (key === "sign") await Promise.all([loadEnrollments(), loadSignIns()]);
    if (key === "credit-sources") await loadCreditSources();
    if (key === "achievements") await loadMyAchievements();
    if (key === "jobs") await loadJobs();
    if (key === "integrity") await loadIntegrity();
  } catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

// ==================== AI 助手 ====================
const aiMessages = ref([]);
const aiInput = ref("");
const aiSending = ref(false);
const aiMsgListRef = ref(null);

function newAiChat() {
  aiMessages.value = [];
}

function scrollAiToBottom() {
  requestAnimationFrame(() => {
    if (aiMsgListRef.value) {
      aiMsgListRef.value.scrollTop = aiMsgListRef.value.scrollHeight;
    }
  });
}

async function sendAiMessage() {
  const msg = aiInput.value.trim();
  if (!msg || aiSending.value) return;
  aiInput.value = "";
  aiSending.value = true;

  // 添加用户消息
  aiMessages.value.push({ isUser: true, content: msg });

  // 添加机器人占位消息
  const botIdx = aiMessages.value.length;
  aiMessages.value.push({ isUser: false, content: "", isTyping: true });
  scrollAiToBottom();

  try {
    const response = await fetch("/api/student/ai/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token.value}`,
      },
      body: JSON.stringify({ message: msg }),
    });

    if (!response.ok) {
      aiMessages.value[botIdx].content = "抱歉，AI 助手暂时无法响应，请稍后再试。";
      aiMessages.value[botIdx].isTyping = false;
      aiSending.value = false;
      return;
    }

    // langchain4j reactor 输出纯文本流，逐块读取追加
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      aiMessages.value[botIdx].content += decoder.decode(value, { stream: true });
      scrollAiToBottom();
    }
    aiMessages.value[botIdx].isTyping = false;
  } catch (err) {
    aiMessages.value[botIdx].content = "网络错误，请稍后重试。";
    aiMessages.value[botIdx].isTyping = false;
  } finally {
    aiSending.value = false;
  }
}


async function reviewEnrollment(row, status) {
  const remark = status === "REJECTED" ? prompt("请输入驳回原因", row.remark || "") : (row.remark || "");
  if (remark === null) return;
  try {
    await request(`/api/admin/core/enrollments/${row.id}/review`, { method: "PUT", body: JSON.stringify({ status, remark }) });
    showToast("报名审核已更新");
    await loadTable();
  } catch (err) { showToast(err.message, "error"); }
}

async function reviewAchievement(row, status) {
  const rejectReason = status === "REJECTED" ? prompt("请输入驳回原因", row.rejectReason || "") : "";
  if (rejectReason === null) return;
  try {
    await request(`/api/admin/core/achievements/${row.id}/review`, { method: "PUT", body: JSON.stringify({ status, rejectReason }) });
    showToast(status === "APPROVED" ? "成果已通过，积分和存证已生成" : "成果已驳回");
    await loadTable();
  } catch (err) { showToast(err.message, "error"); }
}

async function reviewJobApplication(row, status) {
  const remark = prompt("处理备注", row.remark || "");
  if (remark === null) return;
  try {
    const updated = await request(`/api/admin/core/job-applications/${row.id}/review`, { method: "PUT", body: JSON.stringify({ status, remark }) });
    Object.assign(row, updated);
    showToast(status === "ACCEPTED" ? "投递已接受" : "投递已拒绝");
    await loadTable();
  } catch (err) { showToast(err.message, "error"); }
}

async function recomputeIntegrity(row) {
  try {
    await request(`/api/admin/core/integrity-ratings/${row.learnerId}/recompute`, { method: "POST" });
    showToast("诚信分已重新计算");
    await loadTable();
  } catch (err) { showToast(err.message, "error"); }
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
  } else if (!isAdmin.value && studentSectionModules.includes(key)) {
    currentView.value = key;
    loadStudentSection(key);
  } else if (key === "forum") {
    currentView.value = "forum";
  } else if (key === "ai") {
    currentView.value = "ai";
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
  else if (activeModule.value === "mall" || activeModule.value === "forum" || activeModule.value === "ai") { /* own refresh */ }
  else if (activeModule.value === "profile") loadProfile();
  else if (activeModule.value === "courses") {
    if (isAdmin.value) loadTable();
    else loadCourses();
  } else if (!isAdmin.value && studentSectionModules.includes(activeModule.value)) {
    loadStudentSection(activeModule.value);
  }
  else loadTable();
}

function changePage(n) { page.value.current = Math.min(Math.max(n, 1), totalPages.value); loadTable(); }

function openEditor(row = null) {
  const form = {};
  currentModule.value.fields.forEach(f => { form[f.key] = row?.[f.key] ?? (f.type === "radio" ? (f.options?.[0] || "") : ""); });
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


function sourceTypeText(v) {
  const map = {
    COURSE_COMPLETE: "课程学习",
    ACHIEVEMENT_APPROVED: "成果认定",
    DAILY_SIGNIN: "每日签到",
    ORDER_PAY: "商城兑换",
    ADMIN_GRANT: "管理员发放",
    MANUAL: "人工调整",
  };
  return map[v] || v || "其他来源";
}

function transactionTypeText(v) {
  if (v === "INCREASE") return "收入";
  if (v === "CONSUME") return "支出";
  return v || "变动";
}

function proofMaterialHref(value) {
  if (!value) return "";
  if (/^https?:\/\//i.test(value) || value.startsWith("/uploads/")) return value;
  return "";
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

      <div class="user-card">
        <UserCircle :size="36" />
        <div>
          <strong>{{ userInfo?.realName }}</strong>
          <small>{{ userInfo?.username }}</small>
          <small v-if="userInfo?.institutionName" class="inst-label">{{ userInfo.institutionName }}</small>
        </div>
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
        <div v-else class="course-grid">
          <article v-for="c in courses" :key="c.id" class="course-card">
            <div class="course-card-head">
              <div>
                <span class="course-code">{{ c.courseCode }}</span>
                <h2>{{ c.courseName }}</h2>
              </div>
              <span :class="['data-badge', learnedCourseIds.has(c.id) ? 'success' : 'neutral']">
                {{ learnedCourseIds.has(c.id) ? '已学完' : '未完成' }}
              </span>
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
              <a v-if="c.resourceUrl" :href="c.resourceUrl" target="_blank" rel="noopener" class="resource-link">
                <ExternalLink :size="15" />{{ c.resourceTitle || '学习资源' }}
              </a>
              <span v-else class="resource-missing">暂无外部资源</span>
            </div>
            <div v-if="!learnedCourseIds.has(c.id)" class="course-actions">
              <button class="primary-button small" type="button" :disabled="loading || learnedCourseIds.has(c.id)" @click="openLearningCourse(c)">进入学习</button>
              <button class="ghost-button small" type="button" :disabled="loading" @click="enrollCourse(c)">报名课程</button>
            </div>
            <span v-else class="complete-note"><CheckCircle2 :size="16" /> 已完成并获得积分</span>
          </article>
        </div>
        <footer v-if="coursePage.total > coursePage.size" class="pager">
          <button :disabled="coursePage.current <= 1" @click="coursePage.current--; loadCourses()">上一页</button>
          <span>第 {{ coursePage.current }} / {{ Math.ceil(coursePage.total / coursePage.size) }} 页</span>
          <button :disabled="coursePage.current >= Math.ceil(coursePage.total / coursePage.size)" @click="coursePage.current++; loadCourses()">下一页</button>
        </footer>
      </section>
    </main>



    <!-- 学员报名签到 -->
    <main v-else-if="currentView === 'sign'" class="workspace">
      <header class="topbar"><div><p class="eyebrow">Student Portal</p><h1>报名与签到</h1><p class="subtitle">课程报名申请与每日签到奖励</p></div><button :class="todaySigned ? 'ghost-button' : 'primary-button'" :disabled="todaySigned || loading" @click="signToday">{{ todaySigned ? '今日已签到' : '今日签到' }}</button></header>
      <section class="split-grid">
        <div class="panel"><div class="section-head"><h2>我的报名</h2></div><div class="simple-list"><div v-for="e in enrollments" :key="e.id"><strong>课程 #{{ e.courseId }}</strong><span :class="['data-badge', badgeClass(e.enrollStatus)]">{{ e.enrollStatus }}</span><small>{{ formatCell(e.createdAt) }} {{ e.remark || '' }}</small></div><p v-if="enrollments.length===0" class="state-block">暂无报名记录，可在我的课程中报名</p></div></div>
        <div class="panel"><div class="section-head"><h2>签到记录</h2><p v-if="todaySigned">今天已获得 +{{ todaySignRecord?.rewardCredits || 2 }} 积分</p></div><div class="simple-list"><div v-for="s in signIns" :key="s.id"><strong>{{ s.signDate }}</strong><span>+{{ s.rewardCredits }} 积分</span><small>{{ s.signType }}</small></div><p v-if="signIns.length===0" class="state-block">暂无签到记录</p></div></div>
      </section>
    </main>


    <!-- 学员积分来源 -->
    <main v-else-if="currentView === 'credit-sources'" class="workspace">
      <header class="topbar">
        <div><p class="eyebrow">Student Portal</p><h1>积分来源</h1><p class="subtitle">查看积分收入、消费来源和余额变化明细</p></div>
        <button class="ghost-button" type="button" @click="loadCreditSources"><RefreshCw :size="17" /> 刷新</button>
      </header>
      <section class="stats-grid credit-source-stats">
        <article class="stat-card"><span>可用积分</span><strong>{{ creditSourceAccount?.availableCredits ?? 0 }}</strong><small>数据库 credit_account 当前余额</small></article>
        <article class="stat-card"><span>累计积分</span><strong>{{ creditSourceAccount?.totalCredits ?? 0 }}</strong><small>历史累计获得</small></article>
        <article class="stat-card"><span>冻结积分</span><strong>{{ creditSourceAccount?.frozenCredits ?? 0 }}</strong><small>{{ creditSourceAccount?.accountStatus || '账户状态' }}</small></article>
        <article class="stat-card"><span>当前页收入</span><strong>{{ creditSourceStats.income }}</strong><small>学习、成果、签到等获得</small></article>
        <article class="stat-card"><span>当前页支出</span><strong>{{ creditSourceStats.expense }}</strong><small>商城兑换和扣减</small></article>
        <article class="stat-card"><span>当前页净变化</span><strong>{{ creditSourceStats.net }}</strong><small>{{ creditSourcePage.total }} 条流水</small></article>
      </section>
      <section class="split-grid">
        <div class="panel">
          <div class="section-head"><h2>来源分类</h2><p>{{ creditSourceStats.bySource.length }} 类</p></div>
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
          <div class="section-head"><h2>明细记录</h2><p>{{ creditSourcePage.total }} 条</p></div>
          <div class="transaction-list">
            <article v-for="tx in creditSources" :key="tx.id" class="transaction-item">
              <div>
                <strong>{{ tx.remark || sourceTypeText(tx.sourceType) }}</strong>
                <small>{{ sourceTypeText(tx.sourceType) }} / {{ tx.sourceNo || '-' }} / {{ formatCell(tx.createdAt) }}</small>
              </div>
              <div class="transaction-amount">
                <span :class="['data-badge', badgeClass(tx.transactionType)]">{{ transactionTypeText(tx.transactionType) }}</span>
                <strong :class="tx.transactionType === 'INCREASE' ? 'amount-plus' : 'amount-minus'">{{ tx.transactionType === 'INCREASE' ? '+' : '-' }}{{ tx.amount || 0 }}</strong>
                <small>{{ tx.balanceBefore ?? '-' }} -> {{ tx.balanceAfter ?? '-' }}</small>
              </div>
            </article>
            <p v-if="creditSources.length===0" class="state-block">暂无积分来源记录</p>
          </div>
        </div>
      </section>
    </main>

    <!-- 学员成果认定 -->
    <main v-else-if="currentView === 'achievements'" class="workspace">
      <header class="topbar"><div><p class="eyebrow">Student Portal</p><h1>成果认定</h1><p class="subtitle">提交证书、竞赛、实践等学习成果，审核通过后获得积分和存证</p></div></header>
      <section class="split-grid">
        <form class="panel form-grid one" @submit.prevent="submitAchievement">
          <label><span>成果名称</span><input v-model="achievementForm.achievementName" required /></label>
          <label><span>成果类型</span><input v-model="achievementForm.achievementType" placeholder="证书 / 竞赛 / 实践" /></label>
          <label><span>建议积分</span><input v-model="achievementForm.suggestedCredits" type="number" min="1" /></label>
          <label><span>证明链接</span><input v-model="achievementForm.proofUrl" placeholder="可手填链接，也可上传本地文件" /></label>
          <div class="file-upload-field">
            <span>本地证明文件</span>
            <input ref="proofFileInput" class="visually-hidden-file" type="file" accept="image/*,.pdf,.doc,.docx" :disabled="proofUploading" @change="uploadProofFile" />
            <button class="ghost-button" type="button" :disabled="proofUploading" @click="proofFileInput?.click()">{{ proofUploading ? '上传中...' : (proofFileName ? '重新选择文件' : '选择文件') }}</button>
            <small :class="proofFileName ? 'upload-success' : ''">{{ proofFileName ? `已上传：${proofFileName}` : '支持图片、PDF、Word，最大20MB' }}</small>
          </div>
          <a v-if="achievementForm.proofUrl" class="resource-link" :href="achievementForm.proofUrl" target="_blank" rel="noopener">查看已上传证明</a>
          <button class="primary-button" type="submit">提交审核</button>
        </form>
        <div class="panel"><div class="section-head"><h2>我的成果</h2></div><div class="simple-list"><div v-for="a in myAchievements" :key="a.id"><strong>{{ a.achievementName }}</strong><span :class="['data-badge', badgeClass(a.auditStatus)]">{{ a.auditStatus }}</span><small>{{ a.achievementType }} / {{ a.suggestedCredits }} 积分 {{ a.rejectReason || '' }}</small></div><p v-if="myAchievements.length===0" class="state-block">暂无成果</p></div></div>
      </section>
    </main>

    <!-- 学员招聘求职 -->
    <main v-else-if="currentView === 'jobs'" class="workspace">
      <header class="topbar"><div><p class="eyebrow">Student Portal</p><h1>招聘求职</h1><p class="subtitle">查看岗位并投递简历摘要</p></div></header>
      <section class="panel"><div class="course-grid"><article v-for="j in jobs" :key="j.id" class="course-card"><div class="course-card-head"><div><span class="course-code">{{ j.companyName }}</span><h2>{{ j.positionName }}</h2></div><span class="data-badge success">{{ j.status }}</span></div><p class="course-summary">{{ j.requirement }}</p><div class="course-meta"><span>{{ j.city || '-' }}</span><span>{{ j.contact || '-' }}</span></div><button class="primary-button small" @click="openApplyDialog(j)">投递</button></article></div><p v-if="jobs.length===0" class="state-block">暂无开放岗位</p></section>
      <section class="panel"><div class="section-head"><h2>我的投递</h2></div><div class="simple-list"><div v-for="a in jobApplications" :key="a.id"><strong>岗位 #{{ a.jobId }}</strong><span :class="['data-badge', badgeClass(a.applyStatus)]">{{ a.applyStatus }}</span><small>{{ a.remark || a.resumeSummary || '' }}</small></div><p v-if="jobApplications.length===0" class="state-block">暂无投递记录</p></div></section>
    </main>

    <!-- 学员诚信评定 -->
    <main v-else-if="currentView === 'integrity'" class="workspace">
      <header class="topbar"><div><p class="eyebrow">Student Portal</p><h1>诚信评定</h1><p class="subtitle">基于学习、签到、成果、论坛和求职行为自动计算</p></div><button class="ghost-button" @click="loadIntegrity">重新计算</button></header>
      <section class="panel integrity-panel" v-if="integrity"><div class="integrity-score"><strong>{{ integrity.score }}</strong><span>等级 {{ integrity.levelName }}</span></div><div class="overview-list"><div><span>学习</span><strong>{{ integrity.learningScore }}</strong></div><div><span>签到</span><strong>{{ integrity.signinScore }}</strong></div><div><span>成果</span><strong>{{ integrity.achievementScore }}</strong></div><div><span>论坛</span><strong>{{ integrity.forumScore }}</strong></div><div><span>求职</span><strong>{{ integrity.jobScore }}</strong></div></div><p>{{ integrity.remark }}</p></section>
    </main>

    <!-- ====== AI 学习助手 ====== -->
    <main v-else-if="currentView === 'ai'" class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Student Portal</p>
          <h1><Sparkles :size="22" class="ai-title-icon" /> AI 学习助手</h1>
          <p class="subtitle">问课程、查积分、推荐学习路径 — 你的专属学习伙伴</p>
        </div>
        <div class="top-actions">
          <button class="ghost-button" type="button" @click="newAiChat">新对话</button>
        </div>
      </header>
      <section class="panel ai-chat-panel">
        <div class="ai-msg-list" ref="aiMsgListRef">
          <div v-if="aiMessages.length === 0" class="ai-welcome">
            <Bot :size="48" class="ai-bot-icon" />
            <h3>你好！我是学分助手 🎓</h3>
            <p>试试问我：</p>
            <div class="ai-hints">
              <button v-for="h in ['有哪些Java课程？','我的积分是多少？','推荐适合我的课程','我学了哪些课程？']" :key="h"
                class="ghost-button small" @click="aiInput = h; sendAiMessage()">{{ h }}</button>
            </div>
          </div>
          <div v-for="(msg, idx) in aiMessages" :key="idx"
            :class="['ai-msg', msg.isUser ? 'ai-user' : 'ai-bot']">
            <div class="ai-msg-avatar">
              <UserCircle v-if="msg.isUser" :size="28" />
              <Bot v-else :size="28" />
            </div>
            <div class="ai-msg-body">
              <div class="ai-msg-content" v-html="msg.content || (msg.isTyping ? '<span class=ai-typing>思考中<span class=dotting>...</span></span>' : '')"></div>
            </div>
          </div>
        </div>
        <div class="ai-input-bar">
          <input v-model="aiInput" type="text" placeholder="输入你的问题..."
            :disabled="aiSending"
            @keyup.enter="sendAiMessage" />
          <button class="primary-button" :disabled="aiSending || !aiInput.trim()" @click="sendAiMessage">
            {{ aiSending ? '发送中...' : '发送' }}
          </button>
        </div>
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
            <button v-if="currentModule.canCreate" class="primary-button" type="button" @click="openEditor()"><Plus :size="17" /> {{ currentModule.createLabel || "新增" }}</button>
          </div>
        </div>

        <div v-if="loading" class="state-block"><LoaderCircle class="spin" :size="22" /> 正在加载数据...</div>
        <div v-else-if="error" class="state-block error">{{ error }}</div>
        <div v-else class="table-wrap">
          <table>
            <thead><tr><th v-for="col in currentModule.columns" :key="col.key">{{ col.label }}</th><th v-if="currentModule.canEdit || currentModule.canDelete || hasCustomRowActions">操作</th></tr></thead>
            <tbody>
              <tr v-if="rows.length === 0"><td :colspan="currentModule.columns.length + 1" class="empty-cell">暂无数据</td></tr>
              <tr v-for="row in rows" :key="row.id">
                <td v-for="col in currentModule.columns" :key="col.key">
                  <div v-if="col.key === 'auditAction'" class="row-actions inline-actions">
                    <button v-if="row.auditStatus === 'PENDING'" type="button" class="primary-button" @click="reviewAchievement(row, 'APPROVED')">通过</button>
                    <button v-if="row.auditStatus === 'PENDING'" type="button" @click="reviewAchievement(row, 'REJECTED')">驳回</button>
                    <span v-if="row.auditStatus !== 'PENDING'" class="data-badge neutral">已处理</span>
                  </div>
                  <a v-else-if="col.key === 'proofUrl' && proofMaterialHref(row[col.key])" class="resource-link" :href="proofMaterialHref(row[col.key])" target="_blank" rel="noopener">查看材料</a>
                  <span v-else-if="col.key === 'proofUrl'">{{ row[col.key] ? '材料地址无效' : '-' }}</span>
                  <span v-else-if="col.badge" :class="['data-badge', badgeClass(row[col.key])]">{{ formatCell(row[col.key]) }}</span>
                  <span v-else>{{ formatCell(row[col.key]) }}</span>
                </td>
                <td v-if="currentModule.canEdit || currentModule.canDelete || hasCustomRowActions" class="row-actions">
                  <button v-if="currentModule.canEdit" type="button" @click="openEditor(row)"><Save :size="15" /> 编辑</button>
                  <button v-if="currentModule.canDelete" type="button" @click="removeRow(row)"><Trash2 :size="15" /> 删除</button>
                  <button v-if="activeModule === 'learners'" type="button" @click="toggleLearnerStatus(row)" :class="row.status === 'ACTIVE' ? 'ghost-button' : 'primary-button'">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</button>
                  <button v-if="activeModule === 'enrollments' && row.enrollStatus === 'PENDING'" type="button" class="primary-button" @click="reviewEnrollment(row, 'APPROVED')">通过</button>
                  <button v-if="activeModule === 'enrollments' && row.enrollStatus === 'PENDING'" type="button" @click="reviewEnrollment(row, 'REJECTED')">驳回</button>
                  <button v-if="activeModule === 'job-applications' && row.applyStatus === 'PENDING'" type="button" class="primary-button" @click="reviewJobApplication(row, 'ACCEPTED')">接受</button>
                  <button v-if="activeModule === 'job-applications' && row.applyStatus === 'PENDING'" type="button" @click="reviewJobApplication(row, 'REJECTED')">拒绝</button>
                  <span v-if="activeModule === 'job-applications' && row.applyStatus !== 'PENDING'" :class="['data-badge', badgeClass(row.applyStatus)]">{{ row.applyStatus === 'ACCEPTED' ? '已接受' : '已拒绝' }}</span>
                  <button v-if="activeModule === 'integrity-ratings'" type="button" class="ghost-button" @click="recomputeIntegrity(row)">重算</button>
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
              <div v-else-if="f.type === 'radio'" class="radio-group">
                <label v-for="option in f.options" :key="option" class="radio-option">
                  <input v-model="editor.form[f.key]" type="radio" :value="option" />
                  <span>{{ option }}</span>
                </label>
              </div>
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


    <div v-if="activeLearning.open" class="modal-backdrop" @click.self="closeLearningCourse">
      <section class="modal course-learning-modal">
        <div class="modal-head">
          <div>
            <h2>{{ activeLearning.course?.courseName }}</h2>
            <p>{{ activeLearning.course?.resourceSummary }}</p>
          </div>
          <button type="button" class="icon-button" @click="closeLearningCourse"><X :size="18" /></button>
        </div>
        <div class="learning-resource-box">
          <div>
            <span>学习资源</span>
            <strong>{{ activeLearning.course?.resourceTitle || '课程外部资源' }}</strong>
          </div>
          <a v-if="activeLearning.course?.resourceUrl" :href="activeLearning.course.resourceUrl" target="_blank" rel="noopener" class="primary-button small">
            <ExternalLink :size="15" /> 打开资源
          </a>
        </div>
        <div class="learning-stage-list">
          <label v-for="(stage, index) in activeCourseStages" :key="stage.title" class="learning-stage">
            <input type="checkbox" :checked="activeLearning.completedStages.has(index)" @change="toggleLearningStage(index)" />
            <span class="stage-index">{{ index + 1 }}</span>
            <span class="stage-body">
              <strong>{{ stage.title }}</strong>
              <small>{{ stage.description }}</small>
            </span>
          </label>
        </div>
        <div class="modal-actions">
          <button class="ghost-button" type="button" @click="closeLearningCourse">稍后继续</button>
          <button class="primary-button" type="button" :disabled="loading || !allStagesDone" @click="completeCourse">
            <CheckCircle2 :size="17" /> 完成课程并领取 {{ activeLearning.course?.creditPoint || 0 }} 积分
          </button>
        </div>
      </section>
    </div>


    <div v-if="applyDialog.open" class="modal-backdrop" @click.self="applyDialog.open = false">
      <form class="modal compact" @submit.prevent="submitJobApplication">
        <div class="modal-head"><h2>投递 {{ applyDialog.job?.positionName }}</h2><button type="button" class="icon-button" @click="applyDialog.open = false"><X :size="18" /></button></div>
        <label class="single-field"><span>简历摘要</span><textarea v-model="applyDialog.resumeSummary" placeholder="简要说明你的学习经历、技能和求职意向"></textarea></label>
        <div class="modal-actions"><button class="ghost-button" type="button" @click="applyDialog.open = false">取消</button><button class="primary-button" type="submit">提交投递</button></div>
      </form>
    </div>

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




.file-upload-field { display: grid; gap: 6px; }
.file-upload-field input[type="file"] { border: 1px dashed var(--line); border-radius: 8px; padding: 10px; background: #f9fbfd; }
.file-upload-field small { color: #66758a; font-size: 12px; }
.visually-hidden-file { position: absolute; width: 1px; height: 1px; opacity: 0; pointer-events: none; }
.upload-success { color: #166a5f !important; font-weight: 700; }


.simple-list { display: grid; gap: 10px; }
.simple-list > div { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 6px 12px; padding: 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; }
.simple-list strong { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.simple-list small { grid-column: 1 / -1; color: #66758a; }
.integrity-panel { display: grid; gap: 18px; }
.integrity-score { display: flex; align-items: baseline; gap: 14px; }
.integrity-score strong { font-size: 56px; color: #166a5f; line-height: 1; }
.integrity-score span { font-weight: 800; color: #182235; }
.single-field { display: grid; gap: 8px; }
.single-field span { font-size: 13px; color: #536274; font-weight: 700; }
.single-field textarea { min-height: 120px; resize: vertical; border: 1px solid var(--line); border-radius: 8px; padding: 10px; }

.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px; }
.course-card { border: 1px solid var(--line); border-radius: 8px; padding: 16px; background: #fff; display: flex; flex-direction: column; gap: 12px; min-height: 260px; }
.course-card-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.course-card h2 { margin: 4px 0 0; font-size: 18px; color: #182235; line-height: 1.35; }
.course-code { font-size: 12px; font-weight: 700; color: #166a5f; }
.course-summary { margin: 0; color: #536274; font-size: 13px; line-height: 1.6; min-height: 42px; }
.course-meta { display: flex; flex-wrap: wrap; gap: 6px; }
.course-meta span { padding: 4px 8px; border-radius: 6px; background: #f4f6f9; color: #536274; font-size: 12px; }
.course-resource-row { min-height: 28px; display: flex; align-items: center; }
.resource-link { display: inline-flex; align-items: center; gap: 6px; color: #166a5f; font-size: 13px; font-weight: 700; text-decoration: none; }
.resource-link:hover { text-decoration: underline; }
.resource-missing { color: #8a95a6; font-size: 13px; }
.complete-note { display: inline-flex; align-items: center; gap: 6px; color: #166a5f; font-weight: 700; font-size: 13px; margin-top: auto; }
.course-card > .primary-button { margin-top: auto; align-self: flex-start; }
.course-actions { display: flex; gap: 8px; margin-top: auto; flex-wrap: wrap; }
.course-learning-modal { max-width: 760px; width: min(760px, calc(100vw - 32px)); }
.course-learning-modal .modal-head p { margin: 6px 0 0; color: #66758a; font-size: 13px; line-height: 1.5; }
.learning-resource-box { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px; border: 1px solid var(--line); border-radius: 8px; background: #f8fbfa; margin-bottom: 14px; }
.learning-resource-box span { display: block; font-size: 12px; color: #66758a; margin-bottom: 4px; }
.learning-resource-box strong { color: #182235; font-size: 15px; }
.learning-stage-list { display: grid; gap: 10px; }
.learning-stage { display: grid; grid-template-columns: 18px 32px minmax(0, 1fr); align-items: flex-start; gap: 10px; padding: 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; cursor: pointer; }
.learning-stage input { margin-top: 8px; width: 16px; height: 16px; }
.stage-index { width: 28px; height: 28px; border-radius: 50%; background: #e8f4f2; color: #166a5f; display: grid; place-items: center; font-weight: 800; font-size: 13px; }
.stage-body strong { display: block; color: #182235; margin-bottom: 4px; }
.stage-body small { display: block; color: #66758a; line-height: 1.5; }


.radio-group { display: flex; gap: 10px; flex-wrap: wrap; }
.radio-option { display: inline-flex; align-items: center; gap: 6px; padding: 8px 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; font-weight: 700; color: #334155; cursor: pointer; }
.radio-option input { width: auto; }
.credit-source-stats { margin-bottom: 16px; }
.source-breakdown { display: grid; gap: 10px; }
.source-breakdown > div { display: grid; grid-template-columns: minmax(0, 1fr) auto auto; gap: 6px 12px; align-items: center; padding: 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; }
.source-breakdown small { grid-column: 1 / -1; color: #66758a; }
.transaction-list { display: grid; gap: 10px; max-height: 620px; overflow: auto; }
.transaction-item { display: flex; justify-content: space-between; gap: 14px; padding: 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; }
.transaction-item strong { color: #182235; }
.transaction-item small { display: block; margin-top: 5px; color: #66758a; line-height: 1.4; }
.transaction-amount { text-align: right; display: grid; justify-items: end; gap: 4px; min-width: 120px; }
.amount-plus { color: #166a5f; }
.amount-minus { color: #b42318; }

/* AI 助手 */
.ai-title-icon { vertical-align: -4px; margin-right: 4px; color: #f0c46a; }
.ai-chat-panel { display: flex; flex-direction: column; height: calc(100vh - 180px); min-height: 400px; }
.ai-msg-list { flex: 1; overflow-y: auto; padding: 12px 0; display: flex; flex-direction: column; gap: 12px; }
.ai-welcome { text-align: center; padding: 48px 20px 32px; }
.ai-bot-icon { color: #166a5f; margin-bottom: 12px; }
.ai-welcome h3 { margin: 0 0 8px; font-size: 20px; color: #123c39; }
.ai-welcome p { color: #66758a; margin: 0 0 16px; }
.ai-hints { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.ai-hints .ghost-button.small { font-size: 12px; padding: 6px 14px; }

.ai-msg { display: flex; gap: 10px; max-width: 90%; }
.ai-user { align-self: flex-end; flex-direction: row-reverse; }
.ai-bot { align-self: flex-start; }
.ai-msg-avatar { flex-shrink: 0; color: #166a5f; }
.ai-user .ai-msg-avatar { color: #66758a; }
.ai-msg-body { padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; }
.ai-user .ai-msg-body { background: #e8f4f2; color: #182235; border-bottom-right-radius: 4px; }
.ai-bot .ai-msg-body { background: #f4f6f9; color: #182235; border-bottom-left-radius: 4px; white-space: pre-wrap; word-break: break-word; }

.ai-typing { color: #66758a; font-style: italic; }
.dotting::after { content: ''; animation: aiDots 1.4s steps(3, end) infinite; }
@keyframes aiDots {
  0% { content: ''; } 33% { content: '.'; } 66% { content: '..'; } 100% { content: '...'; }
}

.ai-input-bar { display: flex; gap: 10px; padding: 12px 0 0; border-top: 1px solid var(--line); margin-top: auto; }
.ai-input-bar input { flex: 1; height: 42px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; font-size: 14px; }
.ai-input-bar input:focus { outline: 2px solid rgba(18,60,57,0.16); border-color: #166a5f; }
</style>
