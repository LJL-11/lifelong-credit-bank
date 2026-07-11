<script setup>
import { FileUp, X } from "@lucide/vue";

const props = defineProps({
  loading: Boolean,
  token: String,
  applyDialog: Object,
  resumeUploading: Boolean,
});

const emit = defineEmits(["close", "submit", "show-toast", "upload-resume-file"]);
</script>

<template>
  <div v-if="applyDialog.open" class="modal-backdrop" @click.self="emit('close')">
    <form class="modal compact" @submit.prevent="emit('submit')">
      <div class="modal-head"><h2>投递 {{ applyDialog.job?.positionName }}</h2>
        <button type="button" class="icon-button" @click="emit('close')">
          <X :size="18"/>
        </button>
      </div>
      <label class="single-field"><span>简历摘要</span><textarea v-model="applyDialog.resumeSummary"
                                                                 placeholder="简要说明你的学习经历、技能和求职意向"></textarea></label>
      <label class="single-field"><span>简历文件</span>
        <input class="visually-hidden-file" type="file" accept=".pdf,.doc,.docx,image/*" @change="emit('upload-resume-file', $event)" />
        <button class="ghost-button" type="button" :disabled="resumeUploading" @click="$event.currentTarget.previousElementSibling.click()">
          <FileUp :size="16" /> {{ resumeUploading ? '上传中...' : '选择简历文件' }}
        </button>
        <small v-if="applyDialog.resumeFileName" class="field-hint">{{ applyDialog.resumeFileName }}</small>
      </label>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close')">取消</button>
        <button class="primary-button" type="submit">提交投递</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* All modal, modal-head, modal-actions, single-field, etc. styles are in base.css */

:global([data-theme="dark"]) .modal.compact {
  background: #1e293b;
  border-color: rgba(51, 65, 85, 0.95);
  color: #f8fafc;
}

:global([data-theme="dark"]) .modal.compact .modal-head h2 {
  color: #f8fafc;
}

:global([data-theme="dark"]) .modal.compact .single-field span,
:global([data-theme="dark"]) .modal.compact .field-hint,
:global([data-theme="dark"]) .modal.compact textarea::placeholder {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .modal.compact textarea {
  background: rgba(15, 23, 42, 0.72);
  border-color: rgba(51, 65, 85, 0.95);
  color: #e2e8f0;
}
</style>
