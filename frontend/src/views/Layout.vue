<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {
  Award, BookOpen, Bot, BriefcaseBusiness, ClipboardList, FileText,
  LayoutDashboard, MessagesSquare, ReceiptText, RefreshCw,
  Users, WalletCards, UserCircle, ShoppingBag, Zap, Sparkles,
} from "@lucide/vue";
import {menu, modules} from "@/config/modules.js";
import LoginView from "@/views/Login.vue";
import CreditMall from "@/views/mall/CreditMall.vue";
import ForumView from "@/views/Forum.vue";
import Sidebar from "@/components/Sidebar.vue";
import ToastMessage from "@/components/ToastMessage.vue";
import AdminDashboard from "@/components/admin/AdminDashboard.vue";
import AdminDataTable from "@/components/admin/AdminDataTable.vue";
import AdminDialogs from "@/components/admin/AdminDialogs.vue";
import StudentCourses from "@/components/student/StudentCourses.vue";
import StudentSign from "@/components/student/StudentSign.vue";
import StudentCreditSources from "@/components/student/StudentCreditSources.vue";
import StudentAchievements from "@/components/student/StudentAchievements.vue";
import StudentJobs from "@/components/student/StudentJobs.vue";
import StudentIntegrity from "@/components/student/StudentIntegrity.vue";
import StudentProfile from "@/components/student/StudentProfile.vue";
import AiChat from "@/components/student/AiChat.vue";
import LearningModal from "@/components/student/LearningModal.vue";
import JobApplyDialog from "@/components/student/JobApplyDialog.vue";
import { useAuthStore } from "@/store/auth.js";

// ==================== 主题切换 ====================
const isDark = ref(false);

function initTheme() {
  const saved = localStorage.getItem("theme");
  if (saved === "dark") {
    isDark.value = true;
  } else if (saved === "light") {
    isDark.value = false;
  } else {
    isDark.value = window.matchMedia("(prefers-color-scheme: dark)").matches;
  }
  applyTheme();
}

function applyTheme() {
  const root = document.documentElement;
  if (isDark.value) {
    root.setAttribute("data-theme", "dark");
  } else {
    root.removeAttribute("data-theme");
  }
}

function toggleTheme() {
  isDark.value = !isDark.value;
  localStorage.setItem("theme", isDark.value ? "dark" : "light");
  applyTheme();
}

watch(isDark, applyTheme);

// ==================== 认证状态 ====================
const authStore = useAuthStore();
const token = computed(() => authStore.token);
const userInfo = computed(() => authStore.userInfo);
const loggedIn = computed(() => authStore.loggedIn);

function handleLoginSuccess({token: t, userInfo: u}) {
  authStore.setAuth(t, u);
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
  authStore.logout();
  currentView.value = "admin";
  activeModule.value = "dashboard";
}

// ==================== 角色判断 ====================
const isAdmin = computed(() => userInfo.value?.role === "ADMIN");
const currentLearnerId = computed(() => userInfo.value?.learnerId || 0);

const adminMenu = computed(() => menu);
const studentMenu = computed(() => [
  {key: "profile", label: "我的信息", icon: "UserCircle"},
  {key: "courses", label: "我的课程", icon: "BookOpen"},
  {key: "sign", label: "报名签到", icon: "ClipboardList"},
  {key: "credit-sources", label: "积分来源", icon: "ReceiptText"},
  {key: "achievements", label: "成果认定", icon: "Award"},
  {key: "jobs", label: "招聘求职", icon: "BriefcaseBusiness"},
  {key: "integrity", label: "诚信评定", icon: "Award"},
  {key: "forum", label: "学习论坛", icon: "MessagesSquare"},
  {key: "mall", label: "积分商城", icon: "ShoppingBag"},
  {key: "ai", label: "AI 助手", icon: "Sparkles"},
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
const page = ref({current: 1, size: 10, total: 0});
const editor = ref({open: false, row: null, form: {}});
const creditDialog = ref({open: false, type: "increase", form: {}});
const accountDialog = ref({open: false, learnerId: ""});
const freezeDialog = ref({open: false, type: "freeze", form: {learnerId: "", amount: ""}});
const toast = ref({text: "", type: "ok"});
let toastTimer = 0;

const currentModule = computed(() => modules[activeModule.value]);
const totalPages = computed(() => Math.max(1, Math.ceil(page.value.total / page.value.size)));
const statCards = computed(() => [
  {key: "learnerCount", label: "学员总数", value: stats.value.learnerCount ?? 0, note: "已建档学员", trend: "+12%", trendUp: true},
  {key: "courseCount", label: "课程资源", value: stats.value.courseCount ?? 0, note: "可认定课程", trend: "+5%", trendUp: true},
  {key: "achievementCount", label: "成果认定", value: stats.value.achievementCount ?? 0, note: "审核记录", trend: "+23%", trendUp: true},
  {key: "creditTransactionCount", label: "积分流水", value: stats.value.creditTransactionCount ?? 0, note: "积分变动", trend: "+8%", trendUp: true},
  {key: "forumPostCount", label: "论坛帖子", value: stats.value.forumPostCount ?? 0, note: "社区内容", trend: "+15%", trendUp: true},
  {key: "jobPostingCount", label: "招聘岗位", value: stats.value.jobPostingCount ?? 0, note: "就业机会", trend: "+3%", trendUp: true},
]);
const overview = computed(() => [
  {label: "当前模块", value: currentModule.value.title},
  {label: "分页大小", value: `${page.value.size} 条/页`},
  {label: "接口状态", value: apiOnline.value ? "正常" : "待检查"},
]);
const quickActions = computed(() => menu.filter((item) => item.key !== "dashboard").slice(0, 6));
const hasCustomRowActions = computed(() => ["enrollments", "job-applications", "integrity-ratings"].includes(activeModule.value));
const studentSectionModules = ["sign", "credit-sources", "achievements", "jobs", "integrity"];

// ==================== API ====================
async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(token.value ? {Authorization: `Bearer ${token.value}`} : {}),
    ...(options.headers || {}),
  };
  const response = await fetch(url, {...options, headers});
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

// ==================== 学员状态 ====================
const profile = ref(null);
const courses = ref([]);
const coursePage = ref({current: 1, size: 10, total: 0});
const learnedCourseIds = ref(new Set());
const activeLearning = ref({open: false, course: null, completedStages: new Set()});

async function loadProfile() {
  loading.value = true;
  try {
    profile.value = await request("/api/student/profile");
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    loading.value = false;
  }
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
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    loading.value = false;
  }
}

function parseLearningStages(course) {
  if (!course) return [];
  try {
    const parsed = JSON.parse(course.learningStages || "[]");
    if (Array.isArray(parsed) && parsed.length) return parsed;
  } catch (err) { /* ignore */ }
  return [
    {title: "阅读课程资源", description: "打开外部学习资源，完成核心内容阅读或观看。"},
    {title: "整理学习笔记", description: "记录关键概念、示例和仍不清楚的问题。"},
    {title: "完成自测回顾", description: "按课程目标回顾知识点，确认可以独立复述或实践。"},
  ];
}

const activeCourseStages = computed(() => parseLearningStages(activeLearning.value.course));
const allStagesDone = computed(() => activeCourseStages.value.length > 0 && activeLearning.value.completedStages.size === activeCourseStages.value.length);

function openLearningCourse(course) {
  activeLearning.value = {open: true, course, completedStages: new Set()};
}

function closeLearningCourse() {
  activeLearning.value = {open: false, course: null, completedStages: new Set()};
}

function toggleLearningStage(index) {
  const completedStages = new Set(activeLearning.value.completedStages);
  if (completedStages.has(index)) completedStages.delete(index);
  else completedStages.add(index);
  activeLearning.value = {...activeLearning.value, completedStages};
}

async function completeCourse() {
  const course = activeLearning.value.course;
  if (!course || !allStagesDone.value) return;
  loading.value = true;
  try {
    const result = await request(`/api/student/courses/${course.id}/learn`, {method: "POST"});
    learnedCourseIds.value = new Set([...learnedCourseIds.value, course.id]);
    closeLearningCourse();
    showToast(`完成学习，获得 ${result.creditPoint} 积分`);
    if (currentView.value === "profile") loadProfile();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    loading.value = false;
  }
}

const enrollments = ref([]);
const signIns = ref([]);
const myAchievements = ref([]);
const jobs = ref([]);
const jobApplications = ref([]);
const integrity = ref(null);
const creditSources = ref([]);
const creditSourceAccount = ref(null);
const creditSourcePage = ref({current: 1, size: 50, total: 0});
const achievementForm = ref({achievementName: "", achievementType: "证书", suggestedCredits: 10, proofUrl: ""});
const proofUploading = ref(false);
const proofFileName = ref("");
const todaySigned = computed(() => signIns.value.some(s => s.signDate === todayString()));
const todaySignRecord = computed(() => signIns.value.find(s => s.signDate === todayString()));
const applyDialog = ref({open: false, job: null, resumeSummary: ""});

function todayString() {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

async function enrollCourse(course) {
  try {
    await request(`/api/student/courses/${course.id}/enroll`, {method: "POST"});
    showToast("报名已提交，等待管理员审核");
    await loadEnrollments();
  } catch (err) {
    showToast(err.message, "error");
  }
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
    const record = await request("/api/student/sign-ins/today", {method: "POST"});
    signIns.value = [record, ...signIns.value.filter(s => s.signDate !== record.signDate)];
    showToast("签到成功，获得 2 积分");
    await loadCreditSources();
  } catch (err) {
    showToast(err.message, "error");
    await loadSignIns();
  }
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
  creditSourcePage.value = {current: data.current || 1, size: data.size || creditSourcePage.value.size, total: data.total || 0};
}

const creditSourceStats = computed(() => {
  const rows = creditSources.value;
  const income = rows.filter(r => r.transactionType === "INCREASE").reduce((sum, r) => sum + Number(r.amount || 0), 0);
  const expense = rows.filter(r => r.transactionType === "CONSUME").reduce((sum, r) => sum + Number(r.amount || 0), 0);
  const bySource = rows.reduce((acc, r) => {
    const key = r.sourceType || "OTHER";
    if (!acc[key]) acc[key] = {sourceType: key, income: 0, expense: 0, count: 0};
    acc[key].count += 1;
    if (r.transactionType === "INCREASE") acc[key].income += Number(r.amount || 0);
    else acc[key].expense += Number(r.amount || 0);
    return acc;
  }, {});
  return {income, expense, net: income - expense, bySource: Object.values(bySource)};
});

async function loadMyAchievements() {
  const data = await request("/api/student/achievements?current=1&size=20");
  myAchievements.value = data.records || [];
}

async function uploadProofFile(event) {
  const file = event.target?.files?.[0];
  if (!file) return;
  const formData = new FormData();
  formData.append("file", file);
  proofUploading.value = true;
  try {
    const response = await fetch("/api/files/proofs", {
      method: "POST",
      headers: token.value ? {Authorization: `Bearer ${token.value}`} : {},
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
  if (!achievementForm.value.achievementName.trim()) {
    showToast("请填写成果名称", "error");
    return;
  }
  try {
    await request("/api/student/achievements", {method: "POST", body: JSON.stringify(achievementForm.value)});
    achievementForm.value = {achievementName: "", achievementType: "证书", suggestedCredits: 10, proofUrl: ""};
    proofFileName.value = "";
    showToast("成果已提交审核");
    await loadMyAchievements();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function loadJobs() {
  const data = await request("/api/student/jobs?current=1&size=20");
  jobs.value = data.records || [];
  const apps = await request("/api/student/job-applications?current=1&size=20");
  jobApplications.value = apps.records || [];
}

function openApplyDialog(job) {
  applyDialog.value = {open: true, job, resumeSummary: ""};
}

async function submitJobApplication() {
  const job = applyDialog.value.job;
  if (!job) return;
  try {
    await request(`/api/student/jobs/${job.id}/apply`, {
      method: "POST",
      body: JSON.stringify({resumeSummary: applyDialog.value.resumeSummary})
    });
    applyDialog.value = {open: false, job: null, resumeSummary: ""};
    showToast("投递成功");
    await loadJobs();
  } catch (err) {
    showToast(err.message, "error");
  }
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
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    loading.value = false;
  }
}

// ==================== 管理员操作 ====================
async function reviewEnrollment(row, status) {
  const remark = status === "REJECTED" ? prompt("请输入驳回原因", row.remark || "") : (row.remark || "");
  if (remark === null) return;
  try {
    await request(`/api/admin/core/enrollments/${row.id}/review`, {method: "PUT", body: JSON.stringify({status, remark})});
    showToast("报名审核已更新");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function reviewAchievement(row, status) {
  const rejectReason = status === "REJECTED" ? prompt("请输入驳回原因", row.rejectReason || "") : "";
  if (rejectReason === null) return;
  try {
    await request(`/api/admin/core/achievements/${row.id}/review`, {method: "PUT", body: JSON.stringify({status, rejectReason})});
    showToast(status === "APPROVED" ? "成果已通过，积分和存证已生成" : "成果已驳回");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function reviewJobApplication(row, status) {
  const remark = prompt("处理备注", row.remark || "");
  if (remark === null) return;
  try {
    const updated = await request(`/api/admin/core/job-applications/${row.id}/review`, {method: "PUT", body: JSON.stringify({status, remark})});
    Object.assign(row, updated);
    showToast(status === "ACCEPTED" ? "投递已接受" : "投递已拒绝");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function recomputeIntegrity(row) {
  try {
    await request(`/api/admin/core/integrity-ratings/${row.learnerId}/recompute`, {method: "POST"});
    showToast("诚信分已重新计算");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
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
  } else loadTable();
}

function changePage(n) {
  page.value.current = Math.min(Math.max(n, 1), totalPages.value);
  loadTable();
}

function openEditor(row = null) {
  const form = {};
  currentModule.value.fields.forEach(f => {
    form[f.key] = row?.[f.key] ?? (f.type === "radio" ? (f.options?.[0] || "") : "");
  });
  editor.value = {open: true, row, form};
}

function closeEditor() {
  editor.value = {open: false, row: null, form: {}};
}

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
  try {
    await request(url, {method, body: JSON.stringify(payload)});
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
  if (!confirm(`确认删除 ID ${row.id}？`)) return;
  try {
    await request(`${currentModule.value.api}/${row.id}`, {method: "DELETE"});
    showToast("删除成功");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function openCreditDialog(type) {
  creditDialog.value = {open: true, type, form: {learnerId: "", amount: "", sourceType: "MANUAL", sourceNo: "", remark: ""}};
}

function closeCreditDialog() { creditDialog.value.open = false; }

async function submitCreditDialog() {
  const payload = normalizePayload(creditDialog.value.form, [{key: "learnerId", type: "number"}, {key: "amount", type: "number"}]);
  saving.value = true;
  try {
    await request(`/api/admin/credit-accounts/${creditDialog.value.type}`, {method: "POST", body: JSON.stringify(payload)});
    showToast("操作成功");
    closeCreditDialog();
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saving.value = false;
  }
}

function openAccountDialog() { accountDialog.value = {open: true, learnerId: ""}; }
function closeAccountDialog() { accountDialog.value.open = false; }

async function submitOpenAccount() {
  saving.value = true;
  try {
    await request(`/api/admin/credit-accounts/open/${accountDialog.value.learnerId}`, {method: "POST"});
    showToast("账户已开通");
    closeAccountDialog();
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saving.value = false;
  }
}

function openFreezeDialog(type) { freezeDialog.value = {open: true, type, form: {learnerId: "", amount: ""}}; }
function closeFreezeDialog() { freezeDialog.value.open = false; }

async function submitFreezeDialog() {
  saving.value = true;
  try {
    const body = {learnerId: Number(freezeDialog.value.form.learnerId), amount: Number(freezeDialog.value.form.amount)};
    await request(`/api/admin/credit-accounts/${freezeDialog.value.type}`, {method: "POST", body: JSON.stringify(body)});
    showToast(freezeDialog.value.type === "freeze" ? "冻结成功" : "解冻成功");
    closeFreezeDialog();
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saving.value = false;
  }
}

async function toggleLearnerStatus(row) {
  const newStatus = row.status === "ACTIVE" ? "DISABLED" : "ACTIVE";
  if (!confirm(`确认将 ${row.realName} 的状态改为 ${newStatus}？`)) return;
  try {
    await request(`/api/admin/learners/${row.id}/status`, {method: "PUT", body: JSON.stringify({status: newStatus})});
    showToast("状态已更新");
    await loadTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

// ==================== 共享工具函数 ====================
function showToast(text, type = "ok") {
  toast.value = {text, type};
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.value.text = ""; }, 2600);
}

onMounted(() => {
  initTheme();
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
  <LoginView v-if="!loggedIn" @login-success="handleLoginSuccess"/>

  <div v-else class="app-layout">
    <Sidebar
        :is-admin="isAdmin" :is-dark="isDark" :user-info="userInfo"
        :active-module="activeModule" :current-menu="currentMenu" :icons="icons"
        @switch-module="switchModule" @toggle-theme="toggleTheme" @logout="logout"
    />

    <CreditMall
        v-if="currentView === 'mall'"
        :learner-id="currentLearnerId" :token="token" :show-id-input="isAdmin"
    />

    <ForumView
        v-else-if="currentView === 'forum'"
        :token="token" :is-admin="isAdmin"
        :current-user-id="currentLearnerId" :current-user-name="userInfo?.realName || ''"
    />

    <StudentCourses
        v-else-if="currentView === 'courses'"
        :loading="loading" :token="token" :courses="courses"
        :learned-course-ids="learnedCourseIds" :course-page="coursePage"
        @open-learning="openLearningCourse" @enroll-course="enrollCourse" @load-courses="loadCourses"
    />

    <StudentSign
        v-else-if="currentView === 'sign'"
        :loading="loading" :token="token" :enrollments="enrollments" :sign-ins="signIns"
        :today-signed="todaySigned" :today-sign-record="todaySignRecord"
        @sign-today="signToday"
    />

    <StudentCreditSources
        v-else-if="currentView === 'credit-sources'"
        :loading="loading" :token="token" :credit-source-account="creditSourceAccount"
        :credit-sources="creditSources" :credit-source-stats="creditSourceStats"
        :credit-source-page="creditSourcePage"
        @load-credit-sources="loadCreditSources"
    />

    <StudentAchievements
        v-else-if="currentView === 'achievements'"
        :loading="loading" :token="token" :my-achievements="myAchievements"
        :achievement-form="achievementForm" :proof-uploading="proofUploading" :proof-file-name="proofFileName"
        @submit-achievement="submitAchievement" @upload-proof-file="uploadProofFile"
        @update:achievement-form="(k, v) => achievementForm[k] = v"
    />

    <StudentJobs
        v-else-if="currentView === 'jobs'"
        :loading="loading" :token="token" :jobs="jobs" :job-applications="jobApplications"
        @open-apply-dialog="openApplyDialog"
    />

    <StudentIntegrity
        v-else-if="currentView === 'integrity'"
        :loading="loading" :token="token" :integrity="integrity"
        @load-integrity="loadIntegrity"
    />

    <AiChat
        v-else-if="currentView === 'ai'"
        :loading="loading" :token="token"
    />

    <StudentProfile
        v-else-if="currentView === 'profile'"
        :loading="loading" :token="token" :profile="profile"
    />

    <!-- 管理员后台 -->
    <main v-else class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Lifelong Credit Bank</p>
          <h1>{{ currentModule.title }}</h1>
          <p class="subtitle">{{ currentModule.subtitle }}</p>
        </div>
        <div class="top-actions">
          <a class="ghost-button" href="/swagger-ui.html" target="_blank">
            <FileText :size="17"/> 接口文档
          </a>
          <button class="primary-button" type="button" @click="reloadActive">
            <RefreshCw :size="17"/> 刷新
          </button>
        </div>
      </header>

      <AdminDashboard
          v-if="activeModule === 'dashboard'"
          :stat-cards="statCards" :overview="overview" :quick-actions="quickActions"
          :icons="icons" :api-online="apiOnline" :active-module="activeModule"
          @switch-module="switchModule"
      />

      <AdminDataTable
          v-else
          :current-module="currentModule" :rows="rows" :loading="loading" :error="error"
          :page="page" :total-pages="totalPages" :saving="saving"
          :active-module="activeModule" :has-custom-row-actions="hasCustomRowActions"
          :token="token" :user-info="userInfo"
          @open-editor="openEditor"
          @remove-row="removeRow"
          @change-page="changePage"
          @review-enrollment="reviewEnrollment"
          @review-achievement="reviewAchievement"
          @review-job-application="reviewJobApplication"
          @recompute-integrity="recomputeIntegrity"
          @toggle-learner-status="toggleLearnerStatus"
          @open-credit-dialog="openCreditDialog"
          @open-account-dialog="openAccountDialog"
          @open-freeze-dialog="openFreezeDialog"
          @reload="reloadActive"
      />

      <AdminDialogs
          :editor="editor" :credit-dialog="creditDialog" :account-dialog="accountDialog"
          :freeze-dialog="freezeDialog" :current-module="currentModule" :saving="saving"
          @close-editor="closeEditor"
          @submit-editor="submitEditor"
          @close-credit-dialog="closeCreditDialog"
          @submit-credit-dialog="submitCreditDialog"
          @close-account-dialog="closeAccountDialog"
          @submit-account-dialog="submitOpenAccount"
          @close-freeze-dialog="closeFreezeDialog"
          @submit-freeze-dialog="submitFreezeDialog"
      />
    </main>

    <LearningModal
        :active-learning="activeLearning" :loading="loading" :token="token"
        :active-course-stages="activeCourseStages" :all-stages-done="allStagesDone"
        @close="closeLearningCourse" @toggle-stage="toggleLearningStage" @complete-course="completeCourse"
    />

    <JobApplyDialog
        :loading="loading" :token="token" :apply-dialog="applyDialog"
        @close="applyDialog.open = false" @submit="submitJobApplication"
    />

    <ToastMessage :toast="toast"/>
  </div>
</template>
