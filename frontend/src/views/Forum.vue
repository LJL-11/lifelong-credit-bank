<script setup>
import { computed, onMounted, ref } from "vue";
import {
  MessageCircle, Plus, Send, X, Eye, User, Tag,
  BookOpen, ShoppingBag, GraduationCap, HelpCircle, PartyPopper, Clock, RefreshCw, ThumbsUp, Trash2,
} from "@lucide/vue";
import { request } from "@/utils/request.js";

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
const detailDialog = ref({ open: false, post: null, replies: [], replyContent: "", repliesLoading: false });

// 发帖弹窗
const editor = ref({ open: false, title: "", content: "", section: "学习交流" });

const toast = ref({ text: "", type: "ok" });
let toastTimer = 0;

const currentPage = computed(() => Math.max(1, Math.ceil(page.value.total / page.value.size)));

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

// ==================== 点赞 ====================
async function toggleLike(post) {
  try {
    if (post.liked) {
      await request(`/api/student/forum/${post.id}/like`, { method: "DELETE" });
      post.liked = false;
      post.likeCount = Math.max(0, (post.likeCount || 0) - 1);
    } else {
      await request(`/api/student/forum/${post.id}/like`, { method: "POST" });
      post.liked = true;
      post.likeCount = (post.likeCount || 0) + 1;
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

// ==================== 详情 ====================
async function openDetail(post) {
  let loadedPost = post;
  try {
    loadedPost = await request(`/api/student/forum/${post.id}`);
  } catch {
    loadedPost = post;
  }
  detailDialog.value = { open: true, post: loadedPost, replies: [], replyContent: "", repliesLoading: true };
  await loadReplies(loadedPost.id);
}
function closeDetail() { detailDialog.value.open = false; }

async function loadReplies(postId) {
  if (!postId) return;
  detailDialog.value.repliesLoading = true;
  try {
    const data = await request(`/api/student/forum/${postId}/replies?current=1&size=100`);
    detailDialog.value.replies = data.records || [];
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    detailDialog.value.repliesLoading = false;
  }
}

async function submitReply() {
  const post = detailDialog.value.post;
  const content = detailDialog.value.replyContent.trim();
  if (!post || !content) {
    showToast("评论内容不能为空", "error");
    return;
  }
  try {
    const reply = await request(`/api/student/forum/${post.id}/replies`, {
      method: "POST",
      body: JSON.stringify({ content }),
    });
    detailDialog.value.replies.push({ ...reply, learnerName: props.currentUserName || `学员${props.currentUserId}` });
    detailDialog.value.replyContent = "";
    post.replyCount = (post.replyCount || 0) + 1;
    const listPost = posts.value.find(p => p.id === post.id);
    if (listPost) listPost.replyCount = post.replyCount;
    showToast("评论已发布");
  } catch (err) {
    showToast(err.message, "error");
  }
}

// ==================== 管理操作 ====================
async function deletePost(post) {
  if (!post || !confirm("确认删除该帖子？删除后不可恢复。")) return;
  try {
    await request(`/api/admin/forum-posts/${post.id}`, { method: "DELETE" });
    posts.value = posts.value.filter(p => p.id !== post.id);
    if (detailDialog.value.post?.id === post.id) closeDetail();
    showToast("帖子已删除");
  } catch (err) {
    showToast(err.message, "error");
  }
}

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
  <main class="workspace student-dark-page forum-page">
    <!-- 顶栏 -->
    <header class="topbar forum-top">
      <div>
        <p class="eyebrow">Learning Forum</p>
        <h1>{{ showMyPosts ? "我的帖子" : "学习论坛" }}</h1>
        <p class="subtitle">交流学习心得，分享学习资源</p>
      </div>
      <div class="top-actions forum-actions">
        <button class="ghost-button" @click="loadPosts"><RefreshCw :size="16" /> 刷新</button>
        <button class="ghost-button" :class="{ active: showMyPosts }" @click="switchMyPosts">
          <User :size="16" /> {{ showMyPosts ? "全部帖子" : "我的帖子" }}
        </button>
        <button class="primary-button" @click="openEditor"><Plus :size="17" /> 发帖</button>
      </div>
    </header>

    <!-- 板块标签 -->
    <nav v-if="!showMyPosts" class="panel section-tabs">
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
    <section class="panel post-list">
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
          <button class="like-button" :class="{ liked: post.liked }" @click.stop="toggleLike(post)">
            <ThumbsUp :size="14" />
            {{ post.likeCount || 0 }}
          </button>
          <button v-if="isAdmin" class="ghost-button small" @click.stop="togglePostStatus(post)">
            {{ post.status === "VISIBLE" ? "隐藏" : "显示" }}
          </button>
          <button v-if="isAdmin" class="ghost-button small danger" @click.stop="deletePost(post)">
            <Trash2 :size="13" /> 删除
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
          <button class="like-button detail-like" :class="{ liked: detailDialog.post?.liked }" @click="toggleLike(detailDialog.post)">
            <ThumbsUp :size="15" />
            {{ detailDialog.post?.likeCount || 0 }}
          </button>
        </div>
        <div class="detail-content">
          {{ detailDialog.post?.content }}
        </div>
        <section class="reply-section">
          <div class="reply-head">
            <h3>评论</h3>
            <span>{{ detailDialog.replies.length }} 条</span>
          </div>
          <div v-if="detailDialog.repliesLoading" class="reply-empty">评论加载中...</div>
          <div v-else-if="detailDialog.replies.length === 0" class="reply-empty">暂无评论</div>
          <div v-else class="reply-list">
            <article v-for="reply in detailDialog.replies" :key="reply.id" class="reply-item">
              <div class="reply-meta"><strong>{{ reply.learnerName || `学员${reply.learnerId}` }}</strong><span>{{ formatTime(reply.createdAt) }}</span></div>
              <p>{{ reply.content }}</p>
            </article>
          </div>
          <form class="reply-form" @submit.prevent="submitReply">
            <textarea v-model="detailDialog.replyContent" rows="3" placeholder="写下你的评论"></textarea>
            <button class="primary-button" type="submit"><Send :size="15" /> 评论</button>
          </form>
        </section>
        <div class="modal-actions">
          <button class="ghost-button" type="button" @click="closeDetail">关闭</button>
          <button v-if="isAdmin" class="ghost-button" type="button" @click="togglePostStatus(detailDialog.post); closeDetail();">
            {{ detailDialog.post?.status === "VISIBLE" ? "隐藏帖子" : "显示帖子" }}
          </button>
          <button v-if="isAdmin" class="ghost-button danger" type="button" @click="deletePost(detailDialog.post)">
            <Trash2 :size="15" /> 删除帖子
          </button>
        </div>
      </div>
    </div>

    <div v-if="toast.text" :class="['toast', toast.type]">{{ toast.text }}</div>
  </main>
</template>

<style scoped>
.forum-page { background: #f1f5f9; min-height: 100vh; color: var(--text-primary); font-family: "Microsoft YaHei","PingFang SC","Segoe UI",sans-serif; }

.forum-top { display: flex; align-items: center; justify-content: space-between; padding: 18px 28px; background: var(--bg-panel); border-bottom: 1px solid var(--border-color-light); flex-wrap: wrap; gap: 12px; }
.forum-title { display: flex; align-items: center; gap: 12px; }
.forum-title h2 { margin: 0; font-size: 20px; }
.forum-title p { margin: 3px 0 0; font-size: 13px; color: var(--text-secondary); }
.forum-actions { display: flex; gap: 8px; }
.forum-actions .ghost-button.active { border-color: #6366f1; color: #6366f1; background: rgba(99, 102, 241, 0.08); }

.section-tabs { display: flex; gap: 6px; padding: 14px 28px; flex-wrap: wrap; background: var(--bg-panel); border-bottom: 1px solid var(--border-color-light); }
.section-tabs button { display: flex; align-items: center; gap: 5px; height: 34px; padding: 0 14px; border: 1px solid var(--border-color-light); border-radius: 8px; background: var(--bg-panel); color: var(--text-secondary); font-size: 13px; cursor: pointer; transition: all 0.15s; }
.section-tabs button:hover, .section-tabs button.active { border-color: #6366f1; color: #6366f1; background: rgba(99, 102, 241, 0.08); }

.post-list { padding: 18px 28px; display: grid; gap: 10px; }
.post-card { padding: 16px 18px; border: 1px solid var(--border-color-light); border-radius: 10px; background: var(--bg-panel); cursor: pointer; transition: box-shadow 0.15s; }
.post-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.post-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.post-meta { display: flex; align-items: center; gap: 8px; }
.post-time { font-size: 12px; color: var(--text-secondary); }

.section-tag { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; color: #4f46e5; background: rgba(99, 102, 241, 0.08); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.ok { background: #059669; }
.status-dot.warn { background: #d97706; }

.post-title { margin: 0 0 6px; font-size: 17px; line-height: 1.35; }
.post-preview { margin: 0; font-size: 14px; color: var(--text-secondary); line-height: 1.5; }
.post-foot { display: flex; align-items: center; gap: 14px; margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--border-color-light); font-size: 13px; color: var(--text-secondary); }
.post-author { display: flex; align-items: center; gap: 5px; }
.post-meta-item { display: flex; align-items: center; gap: 4px; }
.like-button { display: flex; align-items: center; gap: 4px; height: 28px; padding: 0 10px; border: 1px solid var(--border-color-light); border-radius: 8px; background: var(--bg-panel); color: var(--text-secondary); font-size: 13px; cursor: pointer; transition: all 0.15s; }
.like-button:hover { border-color: #6366f1; color: #6366f1; }
.like-button.liked { border-color: #6366f1; color: #fff; background: #6366f1; }
.detail-like { height: 30px; padding: 0 12px; }
button.small { height: 28px; padding: 0 10px; font-size: 12px; }
button.danger { color: #dc2626; border-color: #fecaca; }

/* 弹窗 */
.modal-backdrop { position: fixed; inset: 0; z-index: 20; display: grid; place-items: center; padding: 22px; background: rgba(0,0,0,0.3); }
.modal { width: min(680px, 100%); max-height: calc(100vh - 44px); overflow: auto; padding: 22px; border-radius: 10px; background: var(--bg-panel); box-shadow: 0 12px 40px rgba(0,0,0,0.08); }
.modal-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.icon-button { display: grid; place-items: center; width: 34px; height: 34px; border: 1px solid var(--border-color-light); border-radius: 8px; background: var(--bg-panel); cursor: pointer; }

.form-grid.two-col { display: grid; grid-template-columns: 140px 1fr; gap: 12px; align-items: end; }
label span { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 700; color: var(--text-secondary); }
input, select, textarea { width: 100%; border: 1px solid var(--border-color-light); border-radius: 8px; padding: 0 12px; font-size: 14px; color: var(--text-primary); background: var(--bg-panel); }
input, select { height: 40px; }
textarea { min-height: 120px; resize: vertical; padding: 10px 12px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }

.detail-meta { display: flex; gap: 16px; flex-wrap: wrap; padding-bottom: 12px; margin-bottom: 14px; border-bottom: 1px solid var(--border-color-light); font-size: 13px; color: var(--text-secondary); }
.detail-meta span { display: flex; align-items: center; gap: 5px; }
.detail-content { font-size: 15px; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }
.reply-section { margin-top: 18px; padding-top: 16px; border-top: 1px solid var(--border-color-light); }
.reply-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.reply-head h3 { margin: 0; font-size: 16px; }
.reply-head span, .reply-empty { color: var(--text-secondary); font-size: 13px; }
.reply-list { display: grid; gap: 10px; margin-bottom: 12px; }
.reply-item { padding: 12px; border: 1px solid var(--border-color-light); border-radius: 8px; background: var(--bg-card); }
.reply-meta { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 6px; font-size: 13px; }
.reply-meta span { color: var(--text-secondary); }
.reply-item p { margin: 0; white-space: pre-wrap; line-height: 1.6; }
.reply-form { display: grid; gap: 10px; }
.reply-form button { justify-self: end; }

/* 复用全局样式 */
.state-block { min-height: 160px; display: grid; place-items: center; gap: 8px; border: 1px dashed #e2e8f0; border-radius: 10px; color: var(--text-secondary); }
.pager { display: flex; align-items: center; justify-content: flex-end; gap: 10px; margin-top: 10px; }
.pager button, .pager span { display: inline-flex; align-items: center; height: 36px; padding: 0 12px; border: 1px solid var(--border-color-light); border-radius: 8px; background: var(--bg-panel); cursor: pointer; font-size: 13px; }
.pager button:disabled { opacity: 0.5; cursor: not-allowed; }
.pager span { border: 0; color: var(--text-secondary); }
.primary-button, .ghost-button { display: inline-flex; align-items: center; gap: 6px; height: 38px; padding: 0 14px; border-radius: 8px; border: 1px solid var(--border-color-light); background: var(--bg-panel); cursor: pointer; font-size: 14px; }
.primary-button { border-color: #6366f1; color: #fff; background: #6366f1; }
.ghost-button:hover { border-color: #6366f1; color: #6366f1; }
.toast { position: fixed; right: 24px; bottom: 24px; z-index: 30; padding: 12px 16px; border-radius: 8px; color: #fff; background: #6366f1; box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.toast.error { background: #dc2626; }
.spin { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

:global([data-theme="dark"]) .forum-page { background: var(--bg-app); color: var(--text-primary); }
:global([data-theme="dark"]) .forum-top,
:global([data-theme="dark"]) .section-tabs,
:global([data-theme="dark"]) .post-card,
:global([data-theme="dark"]) .modal { background: var(--bg-panel); border-color: var(--border-color); color: var(--text-primary); }
:global([data-theme="dark"]) .forum-title p,
:global([data-theme="dark"]) .post-time,
:global([data-theme="dark"]) .post-preview,
:global([data-theme="dark"]) .post-foot,
:global([data-theme="dark"]) .detail-meta,
:global([data-theme="dark"]) .reply-head span,
:global([data-theme="dark"]) .reply-empty,
:global([data-theme="dark"]) .reply-meta span { color: var(--text-muted); }
:global([data-theme="dark"]) .section-tabs button,
:global([data-theme="dark"]) .like-button,
:global([data-theme="dark"]) .icon-button,
:global([data-theme="dark"]) input,
:global([data-theme="dark"]) select,
:global([data-theme="dark"]) textarea,
:global([data-theme="dark"]) .pager button { background: var(--bg-input); border-color: var(--border-color-light); color: var(--text-secondary); }
:global([data-theme="dark"]) .reply-item { background: var(--bg-card); border-color: var(--border-color-light); }

/* 与其他学生端功能页保持一致，页面骨架使用全局 workspace/topbar/panel */
.forum-page {
  min-height: 0;
  background: transparent;
  color: inherit;
  font-family: inherit;
}

.forum-top {
  padding: 0;
  background: transparent;
  border-bottom: 0;
}

.forum-title h1 {
  margin: 0;
}

.forum-title .eyebrow,
.forum-title .subtitle {
  margin: 0;
}

.forum-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.section-tabs {
  padding: 16px;
  border-bottom: 0;
}

.post-list {
  padding: 18px;
}

@media (max-width: 680px) {
  .forum-actions {
    justify-content: flex-start;
  }
}


:global([data-theme="dark"]) .forum-page .forum-top,
:global([data-theme="dark"]) .forum-page .section-tabs,
:global([data-theme="dark"]) .forum-page .post-list,
:global([data-theme="dark"]) .forum-page .post-card,
:global([data-theme="dark"]) .forum-page .modal,
:global([data-theme="dark"]) .forum-page .reply-item,
:global([data-theme="dark"]) .forum-page .icon-button,
:global([data-theme="dark"]) .forum-page .like-button,
:global([data-theme="dark"]) .forum-page .pager button,
:global([data-theme="dark"]) .forum-page input,
:global([data-theme="dark"]) .forum-page select,
:global([data-theme="dark"]) .forum-page textarea,
:global([data-theme="dark"]) .forum-page .ghost-button {
  background: #1e293b;
  border-color: rgba(51, 65, 85, 0.95);
  color: #f8fafc;
}

:global([data-theme="dark"]) .forum-page .post-preview,
:global([data-theme="dark"]) .forum-page .post-foot,
:global([data-theme="dark"]) .forum-page .post-time,
:global([data-theme="dark"]) .forum-page .detail-meta,
:global([data-theme="dark"]) .forum-page .reply-head span,
:global([data-theme="dark"]) .forum-page .reply-empty,
:global([data-theme="dark"]) .forum-page .reply-meta span,
:global([data-theme="dark"]) .forum-page label span,
:global([data-theme="dark"]) .forum-page .pager span {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .forum-page .post-card:hover,
:global([data-theme="dark"]) .forum-page .reply-item:hover,
:global([data-theme="dark"]) .forum-page .ghost-button:hover,
:global([data-theme="dark"]) .forum-page .like-button:hover {
  background: #243449;
}

:global([data-theme="dark"]) .forum-page,
:global([data-theme="dark"]) .forum-top {
  background: transparent !important;
  border-bottom: 0 !important;
}

</style>
