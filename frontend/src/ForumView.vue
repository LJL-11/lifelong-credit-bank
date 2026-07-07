<script setup>
import { computed, onMounted, ref } from "vue";
import {
  MessageCircle, MessagesSquare, Plus, Send, X, Eye, User, Tag,
  BookOpen, ShoppingBag, GraduationCap, HelpCircle, PartyPopper, Clock, RefreshCw,
} from "@lucide/vue";

const props = defineProps({
  token: { type: String, default: "" },
  isAdmin: { type: Boolean, default: false },
  currentUserId: { type: Number, default: 0 },
  currentUserName: { type: String, default: "" },
});

// ==================== 状态 ====================
const sections = ["学习交流", "积分商城", "课程讨论", "技术问答", "活动分享"];
const sectionIcons = { "学习交流": MessageCircle, "积分商城": ShoppingBag, "课程讨论": BookOpen, "技术问答": HelpCircle, "活动分享": PartyPopper };
const activeSection = ref("");

const loading = ref(false);
const posts = ref([]);
const page = ref({ current: 1, size: 10, total: 0 });
const showMyPosts = ref(false);

// 详情弹窗
const detailDialog = ref({ open: false, post: null });

// 发帖弹窗
const editor = ref({ open: false, title: "", content: "", section: "学习交流" });

const toast = ref({ text: "", type: "ok" });
let toastTimer = 0;

const currentPage = computed(() => Math.max(1, Math.ceil(page.value.total / page.value.size)));

// ==================== API ====================
async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(props.token ? { Authorization: `Bearer ${props.token}` } : {}),
    ...(options.headers || {}),
  };
  const response = await fetch(url, { ...options, headers });
  const result = await response.json();
  if (!response.ok || result.code !== 200) {
    throw new Error(result.message || "请求失败");
  }
  return result.data;
}

// ==================== 数据加载 ====================
async function loadPosts() {
  loading.value = true;
  try {
    let url;
    if (showMyPosts.value) {
      url = `/api/student/forum/mine?current=${page.value.current}&size=${page.value.size}`;
    } else {
      url = `/api/student/forum?current=${page.value.current}&size=${page.value.size}`;
      if (activeSection.value) url += `&section=${encodeURIComponent(activeSection.value)}`;
    }
    const data = await request(url);
    posts.value = data.records || [];
    page.value.total = data.total || 0;
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    loading.value = false;
  }
}

function switchTab(section) {
  activeSection.value = section;
  showMyPosts.value = false;
  page.value.current = 1;
  loadPosts();
}

function switchMyPosts() {
  showMyPosts.value = !showMyPosts.value;
  activeSection.value = "";
  page.value.current = 1;
  loadPosts();
}

function goPage(n) {
  page.value.current = Math.min(Math.max(n, 1), currentPage.value);
  loadPosts();
}

// ==================== 发帖 ====================
function openEditor() {
  editor.value = { open: true, title: "", content: "", section: "学习交流" };
}
function closeEditor() { editor.value.open = false; }

async function submitPost() {
  if (!editor.value.title.trim() || !editor.value.content.trim()) {
    showToast("标题和内容不能为空", "error");
    return;
  }
  try {
    await request("/api/student/forum", {
      method: "POST",
      body: JSON.stringify({
        title: editor.value.title,
        content: editor.value.content,
        section: editor.value.section,
      }),
    });
    showToast("发帖成功！");
    closeEditor();
    await loadPosts();
  } catch (err) {
    showToast(err.message, "error");
  }
}

// ==================== 详情 ====================
async function openDetail(post) {
  try {
    const data = await request(`/api/student/forum/${post.id}`);
    detailDialog.value = { open: true, post: data };
  } catch {
    // 如果详情接口不可用，直接用列表数据
    detailDialog.value = { open: true, post };
  }
}
function closeDetail() { detailDialog.value.open = false; }

// ==================== 管理操作 ====================
async function togglePostStatus(post) {
  const newStatus = post.status === "VISIBLE" ? "HIDDEN" : "VISIBLE";
  if (!confirm(`确认${newStatus === "HIDDEN" ? "隐藏" : "显示"}该帖子？`)) return;
  try {
    await request(`/api/admin/forum-posts/${post.id}/status`, {
      method: "PUT",
      body: JSON.stringify({ status: newStatus }),
    });
    post.status = newStatus;
    showToast("状态已更新");
  } catch (err) {
    showToast(err.message, "error");
  }
}

// ==================== 工具 ====================
function formatTime(v) {
  if (!v) return "-";
  return String(v).replace("T", " ").slice(0, 16);
}

function timeAgo(v) {
  if (!v) return "";
  const d = new Date(String(v).replace(" ", "T"));
  const now = new Date();
  const diff = now - d;
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "刚刚";
  if (mins < 60) return `${mins}分钟前`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}天前`;
  return formatTime(v);
}

function showToast(text, type = "ok") {
  toast.value = { text, type };
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.value.text = ""; }, 2600);
}

onMounted(loadPosts);
</script>

<template>
  <div class="forum-page">
    <!-- 顶栏 -->
    <header class="forum-top">
      <div class="forum-title">
        <MessagesSquare :size="24" />
        <div>
          <h2>{{ showMyPosts ? "我的帖子" : "学习论坛" }}</h2>
          <p>交流学习心得，分享学习资源</p>
        </div>
      </div>
      <div class="forum-actions">
        <button class="ghost-button" @click="loadPosts"><RefreshCw :size="16" /> 刷新</button>
        <button class="ghost-button" :class="{ active: showMyPosts }" @click="switchMyPosts">
          <User :size="16" /> {{ showMyPosts ? "全部帖子" : "我的帖子" }}
        </button>
        <button class="primary-button" @click="openEditor"><Plus :size="17" /> 发帖</button>
      </div>
    </header>

    <!-- 板块标签 -->
    <nav v-if="!showMyPosts" class="section-tabs">
      <button :class="{ active: !activeSection }" @click="switchTab('')">全部板块</button>
      <button
        v-for="sec in sections"
        :key="sec"
        :class="{ active: activeSection === sec }"
        @click="switchTab(sec)"
      >
        <component :is="sectionIcons[sec]" :size="15" />
        {{ sec }}
      </button>
    </nav>

    <!-- 帖子列表 -->
    <section class="post-list">
      <div v-if="loading" class="state-block"><Clock class="spin" :size="22" /> 加载中...</div>
      <div v-else-if="posts.length === 0" class="state-block">
        <MessageCircle :size="28" />
        {{ showMyPosts ? "你还没发过帖子" : "暂无帖子，快来发第一帖吧！" }}
      </div>

      <article v-for="post in posts" :key="post.id" class="post-card" @click="openDetail(post)">
        <div class="post-main">
          <div class="post-head">
            <div class="post-meta">
              <span class="section-tag">{{ post.section }}</span>
              <span :class="['status-dot', post.status === 'VISIBLE' ? 'ok' : 'warn']"></span>
            </div>
            <span class="post-time">{{ timeAgo(post.createdAt) }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-preview">{{ (post.content || "").slice(0, 120) }}{{ (post.content || "").length > 120 ? "..." : "" }}</p>
        </div>
        <div class="post-foot">
          <span class="post-author">
            <User :size="14" />
            {{ post.learnerName || `学员${post.learnerId}` }}
          </span>
          <span class="post-meta-item">
            <MessageCircle :size="14" />
            {{ post.replyCount || 0 }}
          </span>
          <button v-if="isAdmin" class="ghost-button small" @click.stop="togglePostStatus(post)">
            {{ post.status === "VISIBLE" ? "隐藏" : "显示" }}
          </button>
        </div>
      </article>

      <footer v-if="page.total > page.size" class="pager">
        <button :disabled="page.current <= 1" @click="goPage(page.current - 1)">上一页</button>
        <span>第 {{ page.current }} / {{ currentPage }} 页</span>
        <button :disabled="page.current >= currentPage" @click="goPage(page.current + 1)">下一页</button>
      </footer>
    </section>

    <!-- 发帖弹窗 -->
    <div v-if="editor.open" class="modal-backdrop" @click.self="closeEditor">
      <div class="modal">
        <div class="modal-head">
          <h2>发表新帖</h2>
          <button type="button" class="icon-button" @click="closeEditor"><X :size="18" /></button>
        </div>
        <div class="form-grid two-col">
          <label>
            <span>板块</span>
            <select v-model="editor.section">
              <option v-for="sec in sections" :key="sec" :value="sec">{{ sec }}</option>
            </select>
          </label>
          <label>
            <span>标题</span>
            <input v-model="editor.title" placeholder="请输入帖子标题" />
          </label>
        </div>
        <label style="margin-top: 12px;">
          <span>内容</span>
          <textarea v-model="editor.content" rows="8" placeholder="分享你的想法..."></textarea>
        </label>
        <div class="modal-actions">
          <button class="ghost-button" type="button" @click="closeEditor">取消</button>
          <button class="primary-button" type="button" @click="submitPost"><Send :size="16" /> 发布</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="detailDialog.open" class="modal-backdrop" @click.self="closeDetail">
      <div class="modal">
        <div class="modal-head">
          <div>
            <span class="section-tag">{{ detailDialog.post?.section }}</span>
            <h2 style="margin-top: 8px;">{{ detailDialog.post?.title }}</h2>
          </div>
          <button type="button" class="icon-button" @click="closeDetail"><X :size="18" /></button>
        </div>
        <div class="detail-meta">
          <span><User :size="15" /> {{ detailDialog.post?.learnerName || `学员${detailDialog.post?.learnerId}` }}</span>
          <span><Clock :size="15" /> {{ formatTime(detailDialog.post?.createdAt) }}</span>
          <span><MessageCircle :size="15" /> {{ detailDialog.post?.replyCount || 0 }} 回复</span>
        </div>
        <div class="detail-content">
          {{ detailDialog.post?.content }}
        </div>
        <div class="modal-actions">
          <button class="ghost-button" type="button" @click="closeDetail">关闭</button>
          <button v-if="isAdmin" class="ghost-button" type="button" @click="togglePostStatus(detailDialog.post); closeDetail();">
            {{ detailDialog.post?.status === "VISIBLE" ? "隐藏帖子" : "显示帖子" }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="toast.text" :class="['toast', toast.type]">{{ toast.text }}</div>
  </div>
</template>

<style scoped>
.forum-page { background: var(--bg); min-height: 100vh; color: var(--ink); font-family: "Microsoft YaHei","PingFang SC","Segoe UI",sans-serif; }

.forum-top { display: flex; align-items: center; justify-content: space-between; padding: 18px 28px; background: #fff; border-bottom: 1px solid var(--line); flex-wrap: wrap; gap: 12px; }
.forum-title { display: flex; align-items: center; gap: 12px; }
.forum-title h2 { margin: 0; font-size: 20px; }
.forum-title p { margin: 3px 0 0; font-size: 13px; color: var(--muted); }
.forum-actions { display: flex; gap: 8px; }
.forum-actions .ghost-button.active { border-color: var(--primary); color: var(--primary); background: var(--success-bg); }

.section-tabs { display: flex; gap: 6px; padding: 14px 28px; flex-wrap: wrap; background: #fff; border-bottom: 1px solid var(--line); }
.section-tabs button { display: flex; align-items: center; gap: 5px; height: 34px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--muted); font-size: 13px; cursor: pointer; transition: all 0.15s; }
.section-tabs button:hover, .section-tabs button.active { border-color: var(--primary); color: var(--primary); background: var(--success-bg); }

.post-list { padding: 18px 28px; display: grid; gap: 10px; }
.post-card { padding: 16px 18px; border: 1px solid var(--line); border-radius: 10px; background: var(--panel); cursor: pointer; transition: box-shadow 0.15s; }
.post-card:hover { box-shadow: 0 4px 16px rgba(24,34,53,0.07); }
.post-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.post-meta { display: flex; align-items: center; gap: 8px; }
.post-time { font-size: 12px; color: var(--muted); }

.section-tag { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; color: var(--primary-dark); background: var(--success-bg); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.ok { background: #166a5f; }
.status-dot.warn { background: #a45f00; }

.post-title { margin: 0 0 6px; font-size: 17px; line-height: 1.35; }
.post-preview { margin: 0; font-size: 14px; color: var(--muted); line-height: 1.5; }
.post-foot { display: flex; align-items: center; gap: 14px; margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--line); font-size: 13px; color: var(--muted); }
.post-author { display: flex; align-items: center; gap: 5px; }
.post-meta-item { display: flex; align-items: center; gap: 4px; }
button.small { height: 28px; padding: 0 10px; font-size: 12px; margin-left: auto; }

/* 弹窗 */
.modal-backdrop { position: fixed; inset: 0; z-index: 20; display: grid; place-items: center; padding: 22px; background: rgba(10,18,30,0.48); }
.modal { width: min(680px, 100%); max-height: calc(100vh - 44px); overflow: auto; padding: 22px; border-radius: 10px; background: var(--panel); box-shadow: 0 24px 70px rgba(10,18,30,0.25); }
.modal-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.icon-button { display: grid; place-items: center; width: 34px; height: 34px; border: 1px solid var(--line); border-radius: 8px; background: var(--panel); cursor: pointer; }

.form-grid.two-col { display: grid; grid-template-columns: 140px 1fr; gap: 12px; align-items: end; }
label span { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 700; color: var(--muted); }
input, select, textarea { width: 100%; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font-size: 14px; color: var(--ink); background: #fff; }
input, select { height: 40px; }
textarea { min-height: 120px; resize: vertical; padding: 10px 12px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }

.detail-meta { display: flex; gap: 16px; flex-wrap: wrap; padding-bottom: 12px; margin-bottom: 14px; border-bottom: 1px solid var(--line); font-size: 13px; color: var(--muted); }
.detail-meta span { display: flex; align-items: center; gap: 5px; }
.detail-content { font-size: 15px; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }

/* 复用全局样式 */
.state-block { min-height: 160px; display: grid; place-items: center; gap: 8px; border: 1px dashed var(--line); border-radius: 10px; color: var(--muted); }
.pager { display: flex; align-items: center; justify-content: flex-end; gap: 10px; margin-top: 10px; }
.pager button, .pager span { display: inline-flex; align-items: center; height: 36px; padding: 0 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; cursor: pointer; font-size: 13px; }
.pager button:disabled { opacity: 0.5; cursor: not-allowed; }
.pager span { border: 0; color: var(--muted); }
.primary-button, .ghost-button { display: inline-flex; align-items: center; gap: 6px; height: 38px; padding: 0 14px; border-radius: 8px; border: 1px solid var(--line); background: var(--panel); cursor: pointer; font-size: 14px; }
.primary-button { border-color: var(--primary); color: #fff; background: var(--primary); }
.ghost-button:hover { border-color: var(--primary); color: var(--primary); }
.toast { position: fixed; right: 24px; bottom: 24px; z-index: 30; padding: 12px 16px; border-radius: 8px; color: #fff; background: var(--primary); box-shadow: 0 8px 28px rgba(0,0,0,0.2); }
.toast.error { background: #b42318; }
.spin { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
