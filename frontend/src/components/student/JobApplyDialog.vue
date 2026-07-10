<script setup>
import { X } from "@lucide/vue";

const props = defineProps({
  loading: Boolean,
  token: String,
  applyDialog: Object,
});

const emit = defineEmits(["close", "submit", "show-toast"]);
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
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close')">取消</button>
        <button class="primary-button" type="submit">提交投递</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* All modal, modal-head, modal-actions, single-field, etc. styles are in base.css */
</style>
