<script setup>
import { Timer } from '@lucide/vue'

defineProps({
  checkoutView: { type: Boolean, default: false },
  checkoutTimerText: { type: String, default: '15:00' },
  checkoutTimer: { type: Number, default: 900 },
  checkoutOrders: { type: Array, default: () => [] },
  checkoutTotalAmount: { type: Number, default: 0 },
  saving: { type: Boolean, default: false },
  account: { type: Object, default: null },
})

const emit = defineEmits(['cancel', 'confirm'])
</script>

<template>
  <div v-if="checkoutView" class="checkout-overlay">
    <div class="checkout-card">
      <div class="checkout-header">
        <div><h2>订单已生成</h2><p>请在倒计时结束前完成支付</p></div>
        <div class="checkout-timer" :class="{ urgent: checkoutTimer <= 60 }"><Timer :size="18" /><span>{{ checkoutTimerText }}</span></div>
      </div>
      <div class="checkout-body">
        <div class="checkout-orders">
          <div v-for="o in checkoutOrders" :key="o.id" class="checkout-order-item"><span class="order-name">{{ o.productName }}</span><span class="order-amount">{{ (o.totalAmount || o.creditAmount).toLocaleString() }} 积分</span></div>
        </div>
        <div class="checkout-summary-box">
          <div class="summary-row"><span>订单数量</span><span>{{ checkoutOrders.length }} 笔</span></div>
          <div class="summary-row"><span>当前可用积分</span><span>{{ account?.availableCredits?.toLocaleString() || 0 }} 积分</span></div>
          <div class="summary-row total"><span>已冻结积分</span><strong>{{ checkoutTotalAmount.toLocaleString() }} 积分</strong></div>
        </div>
      </div>
      <div class="checkout-footer"><button class="btn-ghost large" @click="emit('cancel')" :disabled="saving">取消订单</button><button class="btn-primary large" :disabled="saving" @click="emit('confirm')">{{ saving ? "处理中..." : `确认支付 ${checkoutTotalAmount.toLocaleString()} 积分` }}</button></div>
    </div>
  </div>
</template>

<style scoped>
.checkout-overlay {
  position: fixed;
  inset: 0;
  z-index: 25;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  display: grid;
  place-items: center;
  padding: 24px;
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.checkout-card {
  width: min(520px, 100%);
  max-height: calc(100vh - 48px);
  overflow: auto;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.12);
  animation: scaleIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.checkout-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px 18px;
  border-bottom: 1px solid #e2e8f0;
}

.checkout-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.checkout-header p {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
}

.checkout-timer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border-radius: 12px;
  background: #fef3c7;
  color: #92400e;
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  letter-spacing: -0.02em;
}

.checkout-timer.urgent {
  background: #fee2e2;
  color: #dc2626;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.65; }
}

.checkout-body {
  padding: 20px 28px;
}

.checkout-orders {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
}

.checkout-order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
}

.checkout-order-item:last-child {
  border-bottom: none;
}

.order-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.order-amount {
  font-weight: 700;
  color: #475569;
  font-size: 15px;
}

.checkout-summary-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.summary-row span:first-child {
  color: #64748b;
}

.summary-row.total {
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  margin-top: 4px;
}

.summary-row.total strong {
  font-size: 24px;
  color: #475569;
  font-weight: 700;
}

.checkout-footer {
  display: flex;
  gap: 14px;
  padding: 18px 28px 24px;
  border-top: 1px solid #f1f5f9;
}

.checkout-footer button {
  flex: 1;
}

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

button.large {
  height: 48px;
  font-size: 15px;
  border-radius: 12px;
}
</style>
