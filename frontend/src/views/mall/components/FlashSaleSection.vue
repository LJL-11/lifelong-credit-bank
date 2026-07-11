<script setup>
import { Zap, LoaderCircle, Clock } from '@lucide/vue'

const props = defineProps({
  flashSales: { type: Array, default: () => [] },
  flashCountdowns: { type: Object, default: () => ({}) },
  saving: { type: Boolean, default: false },
  account: { type: Object, default: null },
})

const emit = defineEmits(['seckill'])

function formatCountdown(seconds) {
  if (seconds <= 0) return '已结束'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}
</script>

<template>
  <section class="content-section">
    <div class="section-header">
      <h2><Zap :size="20" style="color: #e8a838;" /> 限时秒杀</h2>
      <p>超值商品限时抢购，手慢无</p>
    </div>
    <div v-if="flashSales.length === 0" class="empty-state"><Zap :size="48" /><p>暂无明显秒杀活动</p></div>
    <div v-else class="flash-grid">
      <article v-for="sale in flashSales" :key="sale.id" class="flash-card">
        <div class="flash-header">
          <span class="flash-badge">限时秒杀</span>
          <div v-if="flashCountdowns[sale.id]" :class="['flash-timer', flashCountdowns[sale.id].state]"><Clock :size="13" /> {{ flashCountdowns[sale.id]?.text }}</div>
        </div>
        <div class="flash-body">
          <h3>{{ sale.productName }}</h3>
          <div class="flash-prices"><span class="flash-origin">{{ sale.originPrice }} 积分</span><span class="flash-price">{{ sale.flashPrice }} 积分</span></div>
          <div class="flash-stock-bar">
            <div class="stock-track"><div class="stock-fill" :style="{ width: Math.max(0, Math.min(100, ((sale.redisStock !== undefined ? sale.redisStock : sale.stock) / sale.stock) * 100)) + '%' }"></div></div>
            <span>剩余 {{ sale.redisStock !== undefined ? sale.redisStock : sale.stock }} / {{ sale.stock }}</span>
          </div>
        </div>
        <div class="flash-actions">
          <button class="btn-primary full" :disabled="saving || !account || (sale.redisStock !== undefined ? sale.redisStock <= 0 : sale.stock <= 0) || (flashCountdowns[sale.id]?.state === 'upcoming')" @click="emit('seckill', sale)">
            <Zap :size="15" />
            {{ !account ? '请先开通账户' : sale.redisStock === -1 ? '已参与' : flashCountdowns[sale.id]?.state === 'upcoming' ? '未开始' : (sale.redisStock !== undefined ? sale.redisStock <= 0 : sale.stock <= 0) ? '已售罄' : '立即秒杀' }}
          </button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.content-section {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.section-header {
  margin-bottom: 24px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.section-header p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.loading-state {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #94a3b8;
}

.empty-state {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #94a3b8;
  border: 2px dashed #e2e8f0;
  border-radius: 20px;
  background: #fff;
}

.empty-state p {
  font-size: 15px;
  margin: 0;
}

.flash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.flash-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.flash-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
  transform: translateY(-4px);
}

.flash-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 0;
}

.flash-badge {
  padding: 5px 12px;
  border-radius: 999px;
  background: #fef3c7;
  color: #92400e;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.flash-timer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.flash-timer.active {
  background: #fef3c7;
  color: #92400e;
}

.flash-timer.upcoming {
  background: #f1f5f9;
  color: #64748b;
}

.flash-body {
  padding: 14px 20px;
}

.flash-body h3 {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #1e293b;
}

.flash-prices {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.flash-origin {
  font-size: 14px;
  color: #94a3b8;
  text-decoration: line-through;
}

.flash-price {
  font-size: 26px;
  font-weight: 700;
  color: #f87171;
}

.flash-stock-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-track {
  flex: 1;
  height: 8px;
  border-radius: 4px;
  background: #f1f5f9;
  overflow: hidden;
}

.stock-fill {
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.flash-stock-bar span {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  font-weight: 500;
}

.flash-actions {
  padding: 0 20px 20px;
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
