<script setup>
import { ShoppingCart, X, Minus, Plus, Trash2 } from '@lucide/vue'

const props = defineProps({
  showCart: { type: Boolean, default: false },
  cartItems: { type: Array, default: () => [] },
  cartChecked: { type: Array, default: () => [] },
  cartTotal: { type: Number, default: 0 },
  saving: { type: Boolean, default: false },
})

const emit = defineEmits([
  'close',
  'update:cartChecked',
  'update-cart-num',
  'remove-cart-item',
  'checkout',
  'toggle-cart-check',
  'select-all',
])

function toggleCartCheck(id) {
  const newChecked = [...props.cartChecked]
  const idx = newChecked.indexOf(id)
  if (idx >= 0) newChecked.splice(idx, 1)
  else newChecked.push(id)
  emit('update:cartChecked', newChecked)
  emit('toggle-cart-check', id)
}

function selectAllCart() {
  if (props.cartChecked.length === props.cartItems.length) {
    emit('update:cartChecked', [])
  } else {
    emit('update:cartChecked', props.cartItems.map(c => c.id))
  }
  emit('select-all')
}
</script>

<template>
  <aside v-if="showCart" class="cart-sidebar">
    <div class="cart-sidebar-header">
      <div class="cart-title"><ShoppingCart :size="20" /><span>购物车</span><span class="cart-count">{{ cartItems.length }} 件商品</span></div>
      <button class="btn-close" @click="emit('close')"><X :size="18" /></button>
    </div>
    <div v-if="cartItems.length === 0" class="cart-empty"><ShoppingCart :size="40" /><p>购物车是空的</p><button class="btn-ghost" @click="emit('close')">去逛逛</button></div>
    <template v-else>
      <div class="cart-items">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <input type="checkbox" :checked="cartChecked.includes(item.id)" @change="toggleCartCheck(item.id)" />
          <div class="cart-item-detail"><strong>{{ item.productName }}</strong><span class="cart-item-price">{{ item.creditPrice.toLocaleString() }} 积分/件</span></div>
          <div class="cart-qty"><button @click="emit('update-cart-num', item, -1)"><Minus :size="12" /></button><span>{{ item.num }}</span><button @click="emit('update-cart-num', item, 1)"><Plus :size="12" /></button></div>
          <button class="btn-remove" @click="emit('remove-cart-item', item.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
      <div class="cart-sidebar-footer">
        <label class="select-all"><input type="checkbox" :checked="cartChecked.length === cartItems.length && cartItems.length > 0" @change="selectAllCart" /><span>全选</span></label>
        <div class="cart-summary">
          <div class="cart-total-row"><span>合计:</span><strong>{{ cartTotal.toLocaleString() }} 积分</strong></div>
          <button class="btn-primary full" :disabled="saving || cartChecked.length === 0" @click="emit('checkout')">结算 ({{ cartChecked.length }})</button>
        </div>
      </div>
    </template>
  </aside>
</template>

<style scoped>
.cart-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  width: 400px;
  height: 100vh;
  background: #fff;
  border-left: 1px solid #e2e8f0;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.06);
  z-index: 20;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0.8; }
  to { transform: translateX(0); opacity: 1; }
}

.cart-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.cart-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 16px;
  color: #1e293b;
}

.cart-count {
  margin-left: 8px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.cart-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #94a3b8;
}

.cart-empty p {
  margin: 0;
  font-size: 15px;
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  transition: all 0.25s;
}

.cart-item:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.cart-item input[type="checkbox"] {
  width: 20px;
  height: 20px;
  accent-color: #475569;
  cursor: pointer;
}

.cart-item-detail {
  flex: 1;
  min-width: 0;
}

.cart-item-detail strong {
  display: block;
  font-size: 14px;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
}

.cart-item-price {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 2px;
}

.cart-qty {
  display: flex;
  align-items: center;
  gap: 2px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.cart-qty button {
  width: 32px;
  height: 32px;
  border: none;
  background: #f8fafc;
  cursor: pointer;
  display: grid;
  place-items: center;
  color: #64748b;
  transition: all 0.2s;
}

.cart-qty button:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.cart-qty span {
  width: 36px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
}

.btn-remove {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all 0.2s;
}

.btn-remove:hover {
  background: #fee2e2;
  color: #dc2626;
}

.cart-sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  font-weight: 500;
}

.select-all input {
  accent-color: #475569;
}

.cart-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-total-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-total-row span {
  font-size: 14px;
  color: #64748b;
}

.cart-total-row strong {
  font-size: 22px;
  font-weight: 700;
  color: #475569;
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

button.full {
  width: 100%;
}
</style>
