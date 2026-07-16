<script setup>
import { ref, onMounted, nextTick } from "vue";
import { Sparkles, Bot, UserCircle, MessageSquare, Trash2, Pencil, Check, X, ChevronsLeft, ChevronsRight } from "@lucide/vue";

const props = defineProps({
  loading: Boolean,
  token: String,
});

const emit = defineEmits(["show-toast"]);

function toast(msg, type = "error") {
  emit("show-toast", msg, type);
}

// ——— 对话列表 ———
const conversations = ref([]);
const activeConvId = ref(0);
const renamingId = ref(0);
const renameInput = ref("");
const historyCollapsed = ref(false);

// ——— 当前对话的消息（本地缓存，按 conversationId 存储） ———
const msgStore = ref({});

const aiInput = ref("");
const aiSending = ref(false);
const aiMsgListRef = ref(null);

function currentMessages() {
  if (!msgStore.value[activeConvId.value]) {
    msgStore.value[activeConvId.value] = [];
  }
  return msgStore.value[activeConvId.value];
}

function scrollAiToBottom() {
  requestAnimationFrame(() => {
    if (aiMsgListRef.value) {
      aiMsgListRef.value.scrollTop = aiMsgListRef.value.scrollHeight;
    }
  });
}

// ——— 加载对话列表 ———
async function loadConversations() {
  try {
    const res = await fetch("/api/student/ai/conversations", {
      headers: { Authorization: `Bearer ${props.token}` },
    });
    const data = await res.json();
    if (data.code === 200) conversations.value = data.data || [];
    if (!activeConvId.value && conversations.value.length > 0) {
      const firstId = conversations.value[0].id;
      await loadMessages(firstId);
      activeConvId.value = firstId;
      await nextTick();
      scrollAiToBottom();
    }
  } catch (e) {
    console.error("加载对话列表失败:", e);
  }
}

// ——— 加载某个对话的历史消息 ———
async function loadMessages(convId) {
  try {
    const res = await fetch(`/api/student/ai/conversations/${convId}/messages`, {
      headers: { Authorization: `Bearer ${props.token}` },
    });
    const data = await res.json();
    if (data.code === 200) {
      msgStore.value[convId] = (data.data || []).map(m => ({
        isUser: m.isUser,
        content: m.content,
      }));
    }
  } catch (e) {
    console.error("加载历史消息失败:", e);
  }
}

// ——— 切换对话 ———
async function switchConversation(convId) {
  if (activeConvId.value === convId) return;
  activeConvId.value = convId;
  // 每次切换都从后端加载，不使用本地缓存
  await loadMessages(convId);
  await nextTick();
  scrollAiToBottom();
}

// ——— 新建对话 ———
async function newConversation() {
  try {
    const res = await fetch("/api/student/ai/conversations", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${props.token}`,
      },
      body: JSON.stringify({ title: "新对话" }),
    });
    const data = await res.json();
    if (data.code === 200) {
      const conv = data.data;
      conversations.value.unshift(conv);
      activeConvId.value = conv.id;
      msgStore.value[conv.id] = [];
    } else {
      toast(data.message || "创建对话失败");
      console.error("创建对话失败:", data);
    }
  } catch (e) {
    console.error("创建对话异常:", e);
    toast("网络错误，请检查后端服务");
  }
}

// ——— 删除对话 ———
async function deleteConversation(convId) {
  try {
    const res = await fetch(`/api/student/ai/conversations/${convId}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${props.token}` },
    });
    const data = await res.json();
    if (data.code !== 200) {
      toast(data.message || "删除失败");
      return;
    }
    conversations.value = conversations.value.filter(c => c.id !== convId);
    delete msgStore.value[convId];
    if (activeConvId.value === convId) {
      activeConvId.value = 0;
      if (conversations.value.length > 0) {
        activeConvId.value = conversations.value[0].id;
      }
    }
  } catch (e) {
    console.error("删除对话异常:", e);
  }
}

// ——— 重命名 ———
function startRename(conv) {
  renamingId.value = conv.id;
  renameInput.value = conv.title;
}
async function confirmRename(convId) {
  const title = renameInput.value.trim();
  if (!title) { renamingId.value = 0; return; }
  try {
    const res = await fetch(`/api/student/ai/conversations/${convId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${props.token}`,
      },
      body: JSON.stringify({ title }),
    });
    const data = await res.json();
    if (data.code === 200) {
      const conv = conversations.value.find(c => c.id === convId);
      if (conv) conv.title = title;
    } else {
      toast(data.message || "重命名失败");
    }
  } catch (e) {
    console.error("重命名异常:", e);
  }
  renamingId.value = 0;
}
function cancelRename() { renamingId.value = 0; }
function toggleHistorySidebar() { historyCollapsed.value = !historyCollapsed.value; }

// ——— 发送消息 ———
async function sendAiMessage() {
  const msg = aiInput.value.trim();
  if (!msg || aiSending.value) return;

  // 如果没有活跃对话，先创建
  if (!activeConvId.value) {
    await newConversation();
    if (!activeConvId.value) {
      toast("请先创建一个对话");
      return;
    }
  }

  aiInput.value = "";
  aiSending.value = true;

  const msgs = currentMessages();
  msgs.push({ isUser: true, content: msg });
  const botIdx = msgs.length;
  msgs.push({ isUser: false, content: "", isTyping: true });
  await nextTick();
  scrollAiToBottom();

  try {
    const response = await fetch("/api/student/ai/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${props.token}`,
      },
      body: JSON.stringify({ message: msg, conversationId: activeConvId.value }),
    });

    const isStream = response.headers.get("content-type")?.includes("text/stream");

    if (!response.ok) {
      // 错误响应：读 JSON 拿错误信息
      let errMsg = "AI 助手暂时无法响应";
      try {
        const errResult = await response.json();
        errMsg = errResult.message || errMsg;
      } catch (_) {}
      msgs[botIdx].content = errMsg;
      msgs[botIdx].isTyping = false;
      aiSending.value = false;
      return;
    }

    if (!isStream) {
      // 非流式响应（如未配置 API Key 的提示文本）
      const text = await response.text();
      msgs[botIdx].content = text || "无响应";
      msgs[botIdx].isTyping = false;
      aiSending.value = false;
      return;
    }

    // 流式读取 SSE
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      msgs[botIdx].content += decoder.decode(value, { stream: true });
      scrollAiToBottom();
    }
    msgs[botIdx].isTyping = false;
    // 后台刷新对话列表（更新排序）
    loadConversations();
  } catch (err) {
    console.error("发送消息异常:", err);
    msgs[botIdx].content = "网络错误，请稍后重试。";
    msgs[botIdx].isTyping = false;
  } finally {
    aiSending.value = false;
  }
}

function quickSend(text) {
  aiInput.value = text;
  sendAiMessage();
}

onMounted(() => {
  loadConversations();
});
</script>

<template>
  <div class="ai-layout">
    <!-- ====== 左侧对话列表 ====== -->
    <aside :class="['ai-sidebar', { collapsed: historyCollapsed }]">
      <div class="sidebar-header">
        <div class="sidebar-title-row">
          <span>历史对话</span>
          <button class="sidebar-icon-button" type="button" title="收起历史对话" @click="toggleHistorySidebar">
            <ChevronsLeft :size="16"/>
          </button>
        </div>
        <button class="ghost-button small" type="button" @click="newConversation">
          <MessageSquare :size="16"/> 新对话
        </button>
      </div>
      <div class="conv-list">
        <div
          v-for="conv in conversations" :key="conv.id"
          :class="['conv-item', { active: activeConvId === conv.id }]"
          @click="switchConversation(conv.id)"
        >
          <MessageSquare :size="15" class="conv-icon"/>
          <div class="conv-info">
            <span v-if="renamingId !== conv.id" class="conv-title">{{ conv.title || "新对话" }}</span>
            <input
              v-else
              v-model="renameInput"
              class="rename-input"
              @keyup.enter="confirmRename(conv.id)"
              @keyup.escape="cancelRename"
              @click.stop
              autofocus
            />
          </div>
          <div class="conv-actions" @click.stop>
            <template v-if="renamingId === conv.id">
              <button class="icon-btn-sm" @click="confirmRename(conv.id)" title="确认"><Check :size="13"/></button>
              <button class="icon-btn-sm" @click="cancelRename" title="取消"><X :size="13"/></button>
            </template>
            <template v-else>
              <button class="icon-btn-sm" @click="startRename(conv)" title="重命名"><Pencil :size="12"/></button>
              <button class="icon-btn-sm danger" @click="deleteConversation(conv.id)" title="删除"><Trash2 :size="12"/></button>
            </template>
          </div>
        </div>
        <div v-if="conversations.length === 0" class="conv-empty">暂无对话，点击上方按钮开始</div>
      </div>
    </aside>

    <!-- ====== 右侧聊天区 ====== -->
    <main class="ai-main">
      <header class="topbar ai-chat-topbar">
        <button
          v-if="historyCollapsed"
          class="sidebar-expand-button"
          type="button"
          title="展开历史对话"
          @click="toggleHistorySidebar"
        >
          <ChevronsRight :size="17"/>
        </button>
        <div>
          <p class="eyebrow">学生端</p>
          <h1>
            <Sparkles :size="22" class="ai-title-icon"/>
            AI 学习助手
          </h1>
          <p class="subtitle">问课程、查积分、推荐学习路径 — 你的专属学习伙伴</p>
        </div>
      </header>
      <section class="panel ai-chat-panel">
        <div class="ai-msg-list" ref="aiMsgListRef">
          <div v-if="currentMessages().length === 0" class="ai-welcome">
            <Bot :size="48" class="ai-bot-icon"/>
            <h3>你好！我是学分助手 &#127891;</h3>
            <p>试试问我：</p>
            <div class="ai-hints">
              <button v-for="h in ['有哪些Java课程？','我的积分是多少？','推荐适合我的课程','我学了哪些课程？']" :key="h"
                      class="ghost-button small" type="button" @click="quickSend(h)">{{ h }}
              </button>
            </div>
          </div>
          <div v-for="(msg, idx) in currentMessages()" :key="idx"
               :class="['ai-msg', msg.isUser ? 'ai-user' : 'ai-bot']">
            <div class="ai-msg-avatar">
              <UserCircle v-if="msg.isUser" :size="28"/>
              <Bot v-else :size="28"/>
            </div>
            <div class="ai-msg-body">
              <div class="ai-msg-content"
                   v-html="msg.content || (msg.isTyping ? '<span class=ai-typing>思考中<span class=dotting>...</span></span>' : '')"></div>
            </div>
          </div>
        </div>
        <div class="ai-input-bar">
          <input v-model="aiInput" type="text" placeholder="输入你的问题..."
                 :disabled="aiSending"
                 @keyup.enter="sendAiMessage"/>
          <button class="primary-button" :disabled="aiSending || !aiInput.trim()" type="button" @click="sendAiMessage">
            {{ aiSending ? '发送中...' : '发送' }}
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.ai-layout {
  display: flex;
  height: calc(100vh - 60px);
  min-height: 500px;
  background: var(--bg-app);
}

.ai-sidebar {
  width: 248px;
  min-width: 248px;
  border-right: 1px solid var(--border-color);
  background: linear-gradient(180deg, #f7f3ea 0%, #f1ece1 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 1px 0 0 rgba(0, 0, 0, 0.02);
  transition: width 0.18s ease, min-width 0.18s ease, opacity 0.12s ease;
}
.ai-sidebar.collapsed {
  width: 0;
  min-width: 0;
  opacity: 0;
  overflow: hidden;
  border-right: none;
}

.sidebar-header {
  display: grid;
  gap: 10px;
  padding: 14px 12px;
  border-bottom: 1px solid var(--border-color-light);
}
.sidebar-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 30px;
}
.sidebar-title-row span {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
}
.sidebar-icon-button,
.sidebar-expand-button {
  display: inline-grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color-light);
  border-radius: 6px;
  background: #fbf8f1;
  color: var(--text-muted);
  cursor: pointer;
  transition: border-color 0.12s ease, color 0.12s ease, background 0.12s ease;
}
.sidebar-icon-button:hover,
.sidebar-expand-button:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
  background: var(--badge-danger-bg);
}
.sidebar-header .ghost-button {
  width: 100%;
  justify-content: center;
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
  margin-bottom: 4px;
}
.conv-item:hover { background: #fbf8f1; border-color: #ded6c8; }
.conv-item.active { background: #fff4ed; border-color: rgba(185, 28, 28, 0.18); }

.conv-icon {
  flex-shrink: 0;
  color: var(--text-muted);
}
.conv-item.active .conv-icon { color: var(--accent-primary); }

.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-title {
  font-size: 13px;
  color: var(--text-secondary);
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-item.active .conv-title { color: var(--text-accent); font-weight: 600; }

.rename-input {
  width: 100%;
  height: 24px;
  padding: 0 6px;
  font-size: 12px;
  border: 1px solid var(--accent-primary);
  border-radius: 4px;
  background: #fbf8f1;
  color: var(--text-primary);
  outline: none;
}

.conv-actions {
  display: none;
  gap: 2px;
  flex-shrink: 0;
}
.conv-item:hover .conv-actions { display: flex; }

.icon-btn-sm {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}
.icon-btn-sm:hover { background: #efe8dc; color: var(--text-secondary); }
.icon-btn-sm.danger:hover { color: var(--accent-primary); }

.conv-empty {
  padding: 24px 12px;
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
}

/* ====== 聊天区 ====== */
.ai-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 24px 0;
  overflow: hidden;
}
.ai-main .topbar {
  margin-bottom: 16px;
}
.ai-chat-topbar {
  align-items: center;
  justify-content: flex-start;
}
.sidebar-expand-button {
  flex: 0 0 auto;
  margin-top: 2px;
}

.ai-chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.ai-title-icon {
  vertical-align: -4px;
  margin-right: 4px;
  color: #d97706;
}

.ai-msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-welcome {
  text-align: center;
  padding: 48px 20px 32px;
}
.ai-bot-icon {
  color: var(--text-accent);
  margin-bottom: 12px;
}
.ai-welcome h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: var(--text-primary);
}
.ai-welcome p {
  color: var(--text-muted);
  margin: 0 0 16px;
}
.ai-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.ai-hints .ghost-button.small {
  font-size: 12px;
  padding: 6px 14px;
}

.ai-msg {
  display: flex;
  gap: 12px;
  max-width: 90%;
}
.ai-user { align-self: flex-end; flex-direction: row-reverse; }
.ai-bot  { align-self: flex-start; }

.ai-msg-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: rgba(99,102,241,0.06);
  display: grid;
  place-items: center;
  color: var(--text-accent);
}
.ai-user .ai-msg-avatar {
  background: rgba(100,116,139,0.1);
  color: var(--text-muted);
}

.ai-msg-body {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
}
.ai-user .ai-msg-body {
  background: var(--bg-ai-user);
  color: var(--text-secondary);
  border-bottom-right-radius: 4px;
}
.ai-bot .ai-msg-body {
  background: var(--bg-ai-bot);
  color: var(--text-secondary);
  border-bottom-left-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-typing {
  color: var(--text-muted);
  font-style: italic;
}
.dotting::after {
  content: '';
  animation: aiDots 1.4s steps(3, end) infinite;
}
@keyframes aiDots {
  0% { content: ''; }
  33% { content: '.'; }
  66% { content: '..'; }
  100% { content: '...'; }
}

.ai-input-bar {
  display: flex;
  gap: 10px;
  padding: 16px 0 0;
  border-top: 1px solid var(--border-color);
  margin-top: auto;
}
.ai-input-bar input {
  flex: 1;
  height: 46px;
  padding: 0 16px;
  border: 1px solid var(--border-color-light);
  border-radius: 12px;
  font-size: 14px;
  background: var(--bg-input);
  color: var(--text-secondary);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.ai-input-bar input:focus {
  border-color: rgba(99,102,241,0.4);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.08);
}
.ai-input-bar input::placeholder { color: var(--text-muted); }

@media (max-width: 760px) {
  .ai-layout {
    height: calc(100vh - 48px);
  }
  .ai-sidebar {
    width: 220px;
    min-width: 220px;
  }
  .ai-main {
    padding: 16px 14px 0;
  }
  .ai-msg {
    max-width: 100%;
  }
}
</style>
