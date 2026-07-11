<script setup>
import { ref } from "vue";

const props = defineProps({
  loading: Boolean,
  token: String,
  myAchievements: Array,
  achievementForm: Object,
  proofUploading: Boolean,
  proofFileName: String,
});

const emit = defineEmits(["submit-achievement", "upload-proof-file", "update:achievement-form", "show-toast"]);

const proofFileInput = ref(null);

function badgeClass(v) {
  const t = String(v || "").toLowerCase();
  if (/active|open|published|approved|passed|increase|paid|delivered/.test(t)) return "success";
  if (/pending|learning|upcoming/.test(t)) return "pending";
  if (/reject|disable|closed|ended|failed|consume|refunded|cancelled/.test(t)) return "danger";
  return "neutral";
}

function updateForm(key, value) {
  emit("update:achievement-form", key, value);
}
</script>

<template>
  <main class="workspace student-dark-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Student Portal</p>
        <h1>成果认定</h1>
        <p class="subtitle">提交证书、竞赛、实践等学习成果，审核通过后获得积分和存证</p>
      </div>
    </header>
    <section class="split-grid">
      <form class="panel form-grid one" @submit.prevent="emit('submit-achievement')">
        <label><span>成果名称</span><input v-model="achievementForm.achievementName" required/></label>
        <label><span>成果类型</span><input v-model="achievementForm.achievementType"
                                           placeholder="证书 / 竞赛 / 实践"/></label>
        <label><span>建议积分</span><input v-model="achievementForm.suggestedCredits" type="number" min="1"/></label>
        <label><span>证明链接</span><input v-model="achievementForm.proofUrl"
                                           placeholder="可手填链接，也可上传本地文件"/></label>
        <div class="file-upload-field">
          <span>本地证明文件</span>
          <input ref="proofFileInput" class="visually-hidden-file" type="file" accept="image/*,.pdf,.doc,.docx"
                 :disabled="proofUploading" @change="emit('upload-proof-file', $event)"/>
          <button class="ghost-button" type="button" :disabled="proofUploading" @click="proofFileInput?.click()">
            {{ proofUploading ? '上传中...' : (proofFileName ? '重新选择文件' : '选择文件') }}
          </button>
          <small :class="proofFileName ? 'upload-success' : ''">{{
              proofFileName ? `已上传：${proofFileName}` : '支持图片、PDF、Word，最大20MB'
            }}</small>
        </div>
        <a v-if="achievementForm.proofUrl" class="resource-link" :href="achievementForm.proofUrl" target="_blank"
           rel="noopener">查看已上传证明</a>
        <button class="primary-button" type="submit">提交审核</button>
      </form>
      <div class="panel">
        <div class="section-head"><h2>我的成果</h2></div>
        <div class="simple-list">
          <div v-for="a in myAchievements" :key="a.id">
            <strong>{{ a.achievementName }}</strong>
            <span :class="['data-badge', badgeClass(a.auditStatus)]">{{ a.auditStatus }}</span>
            <small>{{ a.achievementType }} / {{ a.suggestedCredits }} 积分 {{ a.rejectReason || '' }}</small>
          </div>
          <p v-if="myAchievements.length===0" class="state-block">暂无成果</p>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
/* All form-grid, simple-list, file-upload-field, etc. styles are in base.css */
</style>
