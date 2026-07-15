<script setup>
import { X, Save } from "@lucide/vue";
import { displayLabel } from "@/utils/displayLabels.js";

defineProps({
  editor: { type: Object, default: () => ({ open: false, row: null, form: {} }) },
  creditDialog: { type: Object, default: () => ({ open: false, type: "increase", form: {} }) },
  accountDialog: { type: Object, default: () => ({ open: false, learnerId: "" }) },
  freezeDialog: { type: Object, default: () => ({ open: false, type: "freeze", form: { learnerId: "", amount: "" } }) },
  currentModule: { type: Object, default: () => ({}) },
  saving: { type: Boolean, default: false },
});

const emit = defineEmits([
  "close-editor",
  "submit-editor",
  "close-credit-dialog",
  "submit-credit-dialog",
  "close-account-dialog",
  "submit-account-dialog",
  "close-freeze-dialog",
  "submit-freeze-dialog",
  "update:editor-form",
]);
</script>

<template>
  <!-- Editor Modal -->
  <div v-if="editor.open" class="modal-backdrop" @click.self="emit('close-editor')">
    <form class="modal" @submit.prevent="emit('submit-editor')">
      <div class="modal-head">
        <h2>{{ editor.row?.id ? "编辑" : "新增" }}{{ currentModule.title }}</h2>
        <button type="button" class="icon-button" @click="emit('close-editor')">
          <X :size="18"/>
        </button>
      </div>
      <div class="form-grid">
        <label v-for="f in currentModule.fields" :key="f.key">
          <span>{{ f.label }}</span>
          <textarea v-if="f.type === 'textarea'" v-model="editor.form[f.key]"
                    :placeholder="f.placeholder || f.label"></textarea>
          <div v-else-if="f.type === 'radio'" class="radio-group">
            <label v-for="option in f.options" :key="option" class="radio-option">
              <input v-model="editor.form[f.key]" type="radio" :value="option"/>
              <span>{{ displayLabel(option) }}</span>
            </label>
          </div>
          <input v-else v-model="editor.form[f.key]" :type="f.type || 'text'"
                 :placeholder="f.placeholder || f.label"/>
        </label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close-editor')">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">
          <Save :size="17"/>
          {{ saving ? "保存中..." : "保存" }}
        </button>
      </div>
    </form>
  </div>

  <!-- Credit Dialog -->
  <div v-if="creditDialog.open" class="modal-backdrop" @click.self="emit('close-credit-dialog')">
    <form class="modal compact" @submit.prevent="emit('submit-credit-dialog')">
      <div class="modal-head">
        <h2>{{ creditDialog.type === "increase" ? "增加积分" : "消费积分" }}</h2>
        <button type="button" class="icon-button" @click="emit('close-credit-dialog')">
          <X :size="18"/>
        </button>
      </div>
      <div class="form-grid one">
        <label><span>学员 ID</span><input v-model="creditDialog.form.learnerId" type="number" min="1"
                                          required/></label>
        <label><span>积分数量</span><input v-model="creditDialog.form.amount" type="number" min="1"
                                           required/></label>
        <label><span>来源类型</span><input v-model="creditDialog.form.sourceType"
                                           placeholder="课程 / 手动处理"/></label>
        <label><span>来源编号</span><input v-model="creditDialog.form.sourceNo" placeholder="业务编号"/></label>
        <label><span>备注</span><textarea v-model="creditDialog.form.remark"
                                          placeholder="操作说明"></textarea></label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close-credit-dialog')">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">提交</button>
      </div>
    </form>
  </div>

  <!-- Account Dialog -->
  <div v-if="accountDialog.open" class="modal-backdrop" @click.self="emit('close-account-dialog')">
    <form class="modal compact" @submit.prevent="emit('submit-account-dialog')">
      <div class="modal-head">
        <h2>开通积分账户</h2>
        <button type="button" class="icon-button" @click="emit('close-account-dialog')">
          <X :size="18"/>
        </button>
      </div>
      <div class="form-grid one">
        <label><span>学员 ID</span><input v-model="accountDialog.learnerId" type="number" min="1" required/></label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close-account-dialog')">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">开通</button>
      </div>
    </form>
  </div>

  <!-- Freeze Dialog -->
  <div v-if="freezeDialog.open" class="modal-backdrop" @click.self="emit('close-freeze-dialog')">
    <form class="modal compact" @submit.prevent="emit('submit-freeze-dialog')">
      <div class="modal-head">
        <h2>{{ freezeDialog.type === 'freeze' ? '冻结积分' : '解冻积分' }}</h2>
        <button type="button" class="icon-button" @click="emit('close-freeze-dialog')">
          <X :size="18"/>
        </button>
      </div>
      <div class="form-grid one">
        <label><span>学员 ID</span><input v-model="freezeDialog.form.learnerId" type="number" min="1"
                                          required/></label>
        <label><span>积分数量</span><input v-model="freezeDialog.form.amount" type="number" min="1"
                                           required/></label>
      </div>
      <div class="modal-actions">
        <button class="ghost-button" type="button" @click="emit('close-freeze-dialog')">取消</button>
        <button class="primary-button" type="submit" :disabled="saving">
          {{ freezeDialog.type === 'freeze' ? '确认冻结' : '确认解冻' }}
        </button>
      </div>
    </form>
  </div>
</template>
