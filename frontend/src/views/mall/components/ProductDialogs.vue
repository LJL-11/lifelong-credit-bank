<script setup>
import { computed } from 'vue'
import { displayLabel } from "@/utils/displayLabels.js"
import { X, AlertCircle } from '@lucide/vue'

const props = defineProps({
  orderDialog: { type: Object, default: () => ({ open: false, product: null, quantity: 1, remark: '' }) },
  detailDialog: { type: Object, default: () => ({ open: false, product: null }) },
  account: { type: Object, default: null },
  saving: { type: Boolean, default: false },
  typeIcons: { type: Object, default: () => ({}) },
  typeLabels: { type: Object, default: () => ({}) },
  typeColors: { type: Object, default: () => ({}) },
})

const emit = defineEmits([
  'close-order',
  'close-detail',
  'place-order',
  'update:order-quantity',
  'order-from-detail',
])

const totalPrice = computed(() => {
  const p = props.orderDialog.product
  return p ? p.creditPrice * props.orderDialog.quantity : 0
})

function decreaseQuantity() {
  emit('update:order-quantity', Math.max(1, props.orderDialog.quantity - 1))
}

function increaseQuantity() {
  emit('update:order-quantity', props.orderDialog.quantity + 1)
}
</script>

<template>
  <!-- 下单弹窗 -->
  <div v-if="orderDialog.open" class="modal-overlay" @click.self="emit('close-order')">
    <div class="modal-card compact">
      <div class="modal-header"><h3>确认兑换</h3><button class="btn-close" @click="emit('close-order')"><X :size="18" /></button></div>
      <div class="modal-body">
        <div class="confirm-product">
          <div class="confirm-product-icon">{{ typeIcons[orderDialog.product?.productType] || "🎁" }}</div>
          <div class="confirm-product-info"><strong>{{ orderDialog.product?.productName }}</strong><span>{{ orderDialog.product?.creditPrice }} 积分/件</span></div>
        </div>
        <div class="confirm-row"><span>数量</span><div class="qty-stepper"><button @click="decreaseQuantity">&minus;</button><span>{{ orderDialog.quantity }}</span><button @click="increaseQuantity">+</button></div></div>
        <div class="confirm-row total"><span>合计</span><strong>{{ totalPrice.toLocaleString() }} 积分</strong></div>
        <div v-if="account && account.availableCredits < totalPrice" class="confirm-warning"><AlertCircle :size="14" /> 积分不足，当前可用 {{ account.availableCredits.toLocaleString() }} 积分</div>
      </div>
      <div class="modal-footer"><button class="btn-ghost" @click="emit('close-order')">取消</button><button class="btn-primary" :disabled="saving || (account && account.availableCredits < totalPrice)" @click="emit('place-order')">{{ saving ? "提交中..." : "确认下单" }}</button></div>
    </div>
  </div>

  <!-- 详情弹窗 -->
  <div v-if="detailDialog.open" class="modal-overlay" @click.self="emit('close-detail')">
    <div class="modal-card">
      <div class="modal-header"><h3>商品详情</h3><button class="btn-close" @click="emit('close-detail')"><X :size="18" /></button></div>
      <div class="modal-body">
        <div class="detail-hero">
          <span class="detail-emoji">{{ typeIcons[detailDialog.product?.productType] || "🎁" }}</span>
          <h2>{{ detailDialog.product?.productName }}</h2>
          <span class="detail-type" :style="{ background: typeColors[detailDialog.product?.productType] + '15', color: typeColors[detailDialog.product?.productType] }">{{ typeLabels[detailDialog.product?.productType] || displayLabel(detailDialog.product?.productType) }}</span>
        </div>
        <div class="detail-rows">
          <div class="detail-row"><span>积分价格</span><strong class="detail-price">{{ detailDialog.product?.creditPrice?.toLocaleString() }} 积分</strong></div>
          <div class="detail-row"><span>库存</span><strong>{{ detailDialog.product?.stock === -1 ? "不限" : detailDialog.product?.stock }}</strong></div>
          <div v-if="detailDialog.product?.description" class="detail-row"><span>商品描述</span><p>{{ detailDialog.product?.description }}</p></div>
        </div>
      </div>
      <div class="modal-footer"><button class="btn-ghost" @click="emit('close-detail')">关闭</button><button class="btn-primary" @click="emit('order-from-detail')">立即兑换</button></div>
    </div>
  </div>
</template>

<style scoped>
/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 30;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-card {
  width: min(520px, 100%);
  max-height: calc(100vh - 48px);
  overflow: auto;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.12);
  animation: scaleIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-card.compact {
  width: min(440px, 100%);
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.btn-close {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  color: #64748b;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all 0.2s;
}

.btn-close:hover {
  border-color: #f87171;
  color: #f87171;
  background: #fef2f2;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px 20px;
  border-top: 1px solid #f1f5f9;
}

/* 确认商品 */
.confirm-product {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
  margin-bottom: 20px;
}

.confirm-product-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  display: grid;
  place-items: center;
  font-size: 26px;
}

.confirm-product-info strong {
  display: block;
  font-size: 16px;
  color: #1e293b;
  margin-bottom: 3px;
  font-weight: 600;
}

.confirm-product-info span {
  font-size: 14px;
  color: #94a3b8;
}

.confirm-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.confirm-row span {
  font-size: 14px;
  color: #64748b;
}

.confirm-row.total {
  border-bottom: none;
  padding-top: 16px;
}

.confirm-row.total strong {
  font-size: 24px;
  font-weight: 700;
  color: #475569;
}

.confirm-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fef3c7;
  color: #92400e;
  font-size: 13px;
  margin-top: 12px;
  font-weight: 500;
}

.qty-stepper {
  display: flex;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.qty-stepper button {
  width: 40px;
  height: 40px;
  border: none;
  background: #f8fafc;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: #64748b;
  transition: all 0.2s;
}

.qty-stepper button:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.qty-stepper span {
  width: 48px;
  text-align: center;
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
}

/* 详情弹窗 */
.detail-hero {
  text-align: center;
  padding: 24px 0 20px;
}

.detail-emoji {
  font-size: 64px;
  display: block;
  margin-bottom: 14px;
  filter: grayscale(0.15);
}

.detail-hero h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 10px;
}

.detail-type {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.detail-rows {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-row span {
  font-size: 14px;
  color: #64748b;
  min-width: 80px;
  font-weight: 500;
}

.detail-row strong {
  font-size: 15px;
  color: #1e293b;
  font-weight: 600;
}

.detail-row p {
  margin: 0;
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
  text-align: right;
  max-width: 300px;
}

.detail-price {
  font-size: 22px !important;
  font-weight: 700 !important;
  color: #475569 !important;
}

/* 按钮系统 */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  padding: 0 18px;
  border-radius: 10px;
  border: 1px solid transparent;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  letter-spacing: 0.2px;
  background: #475569;
  color: #fff;
  border-color: #475569;
}

.btn-primary:hover:not(:disabled) {
  background: #334155;
  border-color: #334155;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(71, 85, 105, 0.2);
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  padding: 0 18px;
  border-radius: 10px;
  border: 1px solid transparent;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  letter-spacing: 0.2px;
  background: #fff;
  color: #64748b;
  border-color: #e2e8f0;
}

.btn-ghost:hover:not(:disabled) {
  border-color: #475569;
  color: #475569;
  background: #f8fafc;
}

button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}
</style>
