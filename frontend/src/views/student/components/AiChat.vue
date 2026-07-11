<script setup>
import { ref } from "vue";
import { Sparkles, Bot, UserCircle } from "@lucide/vue";

const props = defineProps({
  loading: Boolean,
  token: String,
});

const emit = defineEmits(["show-toast"]);

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

  aiMessages.value.push({ isUser: true, content: msg });
  const botIdx = aiMessages.value.length;
  aiMessages.value.push({ isUser: false, content: "", isTyping: true });
  scrollAiToBottom();

  try {
    const response = await fetch("/api/student/ai/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${props.token}`,
      },
      body: JSON.stringify({ message: msg }),
    });

    if (!response.ok) {
      aiMessages.value[botIdx].content = "抱歉，AI 助手暂时无法响应，请稍后再试。";
      aiMessages.value[botIdx].isTyping = false;
      aiSending.value = false;
      return;
    }

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
</script>

<template>
  <main class="workspace">
    <header class="topbar">
      <div>
        <p class="eyebrow">Student Portal</p>
        <h1>
          <Sparkles :size="22" class="ai-title-icon"/>
          AI 学习助手
        </h1>
        <p class="subtitle">问课程、查积分、推荐学习路径 — 你的专属学习伙伴</p>
      </div>
      <div class="top-actions">
        <button class="ghost-button" type="button" @click="newAiChat">新对话</button>
      </div>
    </header>
    <section class="panel ai-chat-panel">
      <div class="ai-msg-list" ref="aiMsgListRef">
        <div v-if="aiMessages.length === 0" class="ai-welcome">
          <Bot :size="48" class="ai-bot-icon"/>
          <h3>你好！我是学分助手 🎓</h3>
          <p>试试问我：</p>
          <div class="ai-hints">
            <button v-for="h in ['有哪些Java课程？','我的积分是多少？','推荐适合我的课程','我学了哪些课程？']" :key="h"
                    class="ghost-button small" @click="aiInput = h; sendAiMessage()">{{ h }}
            </button>
          </div>
        </div>
        <div v-for="(msg, idx) in aiMessages" :key="idx"
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
        <button class="primary-button" :disabled="aiSending || !aiInput.trim()" @click="sendAiMessage">
          {{ aiSending ? '发送中...' : '发送' }}
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.ai-title-icon {
  vertical-align: -4px;
  margin-right: 4px;
  color: #d97706;
}

.ai-chat-panel {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
  min-height: 400px;
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
  transition: color 0.3s ease;
}

.ai-welcome h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.ai-welcome p {
  color: var(--text-muted);
  margin: 0 0 16px;
  transition: color 0.3s ease;
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

.ai-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-bot {
  align-self: flex-start;
}

.ai-msg-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: rgba(99, 102, 241, 0.06);
  display: grid;
  place-items: center;
  color: var(--text-accent);
  transition: color 0.3s ease;
}

.ai-user .ai-msg-avatar {
  background: rgba(100, 116, 139, 0.1);
  color: var(--text-muted);
  transition: color 0.3s ease;
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
  transition: background-color 0.3s ease, color 0.3s ease;
}

.ai-bot .ai-msg-body {
  background: var(--bg-ai-bot);
  color: var(--text-secondary);
  border-bottom-left-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.ai-typing {
  color: var(--text-muted);
  font-style: italic;
  transition: color 0.3s ease;
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
  transition: border-color 0.3s ease;
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
  transition: all 0.2s ease;
}

.ai-input-bar input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.08);
}

.ai-input-bar input::placeholder {
  color: var(--text-muted);
}
</style>
