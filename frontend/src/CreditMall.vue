<script setup>
import { computed, onMounted, ref, watch } from "vue";
import {
  Award, Coins, LoaderCircle, Minus, Package, Plus, ReceiptText, RefreshCw,
  ShoppingBag, ShoppingCart, Store, Trash2, Undo2, UserCheck, X, Sparkles,
  Zap, Clock,
} from "@lucide/vue";

const props = defineProps({
  learnerId: { type: Number, required: true },
  token: { type: String, default: "" },
  showIdInput: { type: Boolean, default: false },
});

const learnerId = ref(props.learnerId);
const activeTab = ref("mall");

// 秒杀
const flashSales = ref([]);
const flashLoading = ref(false);
const flashCountdowns = ref({});
let flashTimer = 0;
const loading = ref(false);
const saving = ref(false);
const products = ref([]);
const orders = ref([]);
const account = ref(null);
const toast = ref({ text: "", type: "ok" });
let toastTimer = 0;

const orderPage = ref({ current: 1, size: 10, total: 0 });
const orderDialog = ref({ open: false, product: null, quantity: 1, remark: "" });
const detailDialog = ref({ open: false, product: null });
const cartItems = ref([]);
const cartChecked = ref([]);
const showCart = ref(false);

// 结算确认页
const checkoutView = ref(false);
const checkoutTimer = ref(900); // 15分钟 = 900秒
const checkoutTimerText = ref("15:00");
let checkoutInterval = 0;

const typeLabels = { COURSE: "课程", CERTIFICATE: "认证", MERCHANDISE: "实物", SERVICE: "服务" };
const typeIcons = { COURSE: "📚", CERTIFICATE: "🏅", MERCHANDISE: "🎁", SERVICE: "💼" };
const statusMap = {
  PENDING: { label: "待支付", class: "pending" },
  PAID: { label: "已支付", class: "success" },
  DELIVERED: { label: "已发货", class: "success" },
  CANCELLED: { label: "已取消", class: "neutral" },
  REFUNDED: { label: "已退款", class: "danger" },
};

// ==================== API ====================
async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(props.token ? { Authorization: `Bearer ${props.token}` } : {}),
    ...(options.headers || {}),
  };
  const response = await fetch(url, { ...options, headers });
  const result = await response.json();
  if (response.status === 401) {
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
    window.location.reload();
    throw new Error(result.message || "登录已过期");
  }
  if (!response.ok || result.code !== 200) throw new Error(result.message || "请求失败");
  return result.data;
}

async function loadProducts() {
  loading.value = true;
  try {
    const url = props.showIdInput ? "/api/admin/products/active?current=1&size=50" : "/api/student/products?current=1&size=50";
    products.value = (await request(url)).records || [];
  } catch (err) { showToast(err.message, "error"); }
  finally { loading.value = false; }
}

async function loadAccount() {
  try {
    if (props.showIdInput) {
      const list = (await request("/api/admin/credit-accounts?current=1&size=500")).records || [];
      account.value = list.find(a => String(a.learnerId) === String(learnerId.value)) || null;
    } else {
      account.value = await request("/api/student/account");
    }
  } catch { account.value = null; }
}

async function loadOrders() {
  try {
    const url = props.showIdInput
      ? `/api/admin/orders/learner/${learnerId.value}?current=${orderPage.value.current}&size=${orderPage.value.size}`
      : `/api/student/orders?current=${orderPage.value.current}&size=${orderPage.value.size}`;
    const data = await request(url);
    orders.value = data.records || [];
    orderPage.value.total = data.total || 0;
  } catch { orders.value = []; }
}

async function loadAll() { await Promise.all([loadProducts(), loadAccount(), loadOrders()]); }

// ==================== 秒杀 ====================
async function loadFlashSales() {
  flashLoading.value = true;
  try {
    flashSales.value = await request("/api/flash/active");
    // 开始倒计时
    clearInterval(flashTimer);
    flashTimer = setInterval(updateCountdowns, 1000);
  } catch { flashSales.value = []; }
  finally { flashLoading.value = false; }
}

function updateCountdowns() {
  const now = Date.now();
  const newCounts = {};
  for (const s of flashSales.value) {
    const end = new Date(s.endTime).getTime();
    const begin = new Date(s.beginTime).getTime();
    if (now < begin) {
      const diff = Math.floor((begin - now) / 1000);
      newCounts[s.id] = { text: formatCountdown(diff), state: "upcoming" };
    } else if (now < end) {
      const diff = Math.floor((end - now) / 1000);
      newCounts[s.id] = { text: formatCountdown(diff), state: "active" };
    }
  }
  flashCountdowns.value = newCounts;
}

function formatCountdown(seconds) {
  if (seconds <= 0) return "已结束";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  return `${h}:${String(m).padStart(2,"0")}:${String(s).padStart(2,"0")}`;
}

async function doSeckill(sale) {
  if (!account.value) { showToast("请先开通积分账户", "error"); return; }
  const cd = flashCountdowns.value[sale.id];
  if (cd?.state === "upcoming") { showToast("秒杀尚未开始", "error"); return; }
  saving.value = true;
  try {
    const orderId = await request(`/api/flash/seckill/${sale.id}`, { method: "POST" });
    showToast(`秒杀成功！订单号: ${orderId}`);
    await Promise.all([loadAccount(), loadOrders()]);
    // 刷新秒杀库存
    const stockKey = `flash:stock:${sale.id}`;
    sale.redisStock = Math.max(0, (sale.redisStock || sale.stock) - 1);
  } catch (err) {
    showToast(err.message, "error");
    if (err.message?.includes("不能重复参与")) {
      sale.redisStock = -1; // 标记已参与
    }
  }
  finally { saving.value = false; }
}

onMounted(() => { loadAll(); loadFlashSales(); });

// ==================== 购物车 ====================
async function loadCart() {
  try { cartItems.value = await request("/api/student/cart"); }
  catch { cartItems.value = []; }
}

const cartTotal = computed(() => {
  let total = 0;
  for (const id of cartChecked.value) {
    const item = cartItems.value.find(c => c.id === id);
    if (item) total += item.creditPrice * item.num;
  }
  return total;
});

const pendingOrders = computed(() => orders.value.filter(o => o.orderStatus === 'PENDING'));
const doneOrders = computed(() => orders.value.filter(o => o.orderStatus !== 'PENDING'));

function toggleCartCheck(id) {
  const idx = cartChecked.value.indexOf(id);
  if (idx >= 0) cartChecked.value.splice(idx, 1);
  else cartChecked.value.push(id);
}

function selectAllCart() {
  if (cartChecked.value.length === cartItems.value.length) cartChecked.value = [];
  else cartChecked.value = cartItems.value.map(c => c.id);
}

async function addToCart(product) {
  if (!props.token) { showToast("请先登录", "error"); return; }
  try {
    await request("/api/student/cart", { method: "POST", body: JSON.stringify({ productId: product.id, num: 1 }) });
    showToast("已加入购物车");
    await loadCart();
  } catch (err) { showToast(err.message, "error"); }
}

async function updateCartNum(item, delta) {
  const newNum = item.num + delta;
  if (newNum <= 0) {
    await request(`/api/student/cart/${item.id}`, { method: "DELETE" });
    cartChecked.value = cartChecked.value.filter(cid => cid !== item.id);
  } else {
    await request(`/api/student/cart/${item.id}`, { method: "PUT", body: JSON.stringify({ num: newNum }) });
  }
  await loadCart();
}

async function removeCartItem(id) {
  await request(`/api/student/cart/${id}`, { method: "DELETE" });
  cartChecked.value = cartChecked.value.filter(cid => cid !== id);
  await loadCart();
  showToast("已移除");
}

const checkoutOrders = ref([]); // 结算中订单列表

async function checkoutFromCart() {
  if (cartChecked.value.length === 0) { showToast("请先选择商品", "error"); return; }
  if (!account.value) { showToast("请先开通积分账户", "error"); return; }
  if (cartTotal.value > account.value.availableCredits) { showToast("积分不足", "error"); return; }

  saving.value = true;
  try {
    // 先下单
    const url = props.showIdInput ? "/api/admin/orders/place" : "/api/student/orders/place";
    const data = await request(url, {
      method: "POST",
      body: JSON.stringify({ learnerId: Number(learnerId.value), cartIds: [...cartChecked.value], remark: "购物车下单" }),
    });
    // data 可能是单个 order 或 order 数组
    checkoutOrders.value = Array.isArray(data) ? data : [data];
    // 清空购物车选中项
    cartChecked.value = [];
    showCart.value = false;
    await Promise.all([loadAccount(), loadCart()]);
    // 打开倒计时确认页
    checkoutView.value = true;
    startCheckoutTimer();
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

// ==================== 结算确认页 ====================
const checkoutItems = computed(() =>
  cartItems.value.filter(c => cartChecked.value.includes(c.id))
);
const checkoutTotalAmount = computed(() =>
  checkoutOrders.value.reduce((sum, o) => sum + (o.totalAmount || o.creditAmount), 0)
);

function startCheckoutTimer() {
  checkoutTimer.value = 900;
  checkoutTimerText.value = "15:00";
  clearInterval(checkoutInterval);
  checkoutInterval = setInterval(async () => {
    checkoutTimer.value--;
    const m = Math.floor(checkoutTimer.value / 60);
    const s = checkoutTimer.value % 60;
    checkoutTimerText.value = `${m}:${String(s).padStart(2, "0")}`;
    if (checkoutTimer.value <= 0) {
      clearInterval(checkoutInterval);
      // 超时自动取消所有订单
      for (const o of checkoutOrders.value) {
        try { await cancelOrderById(o.id); } catch {}
      }
      checkoutView.value = false;
      await Promise.all([loadAccount(), loadOrders()]);
      showToast("支付超时，订单已取消", "error");
    }
  }, 1000);
}

async function cancelCheckout() {
  clearInterval(checkoutInterval);
  saving.value = true;
  try {
    for (const o of checkoutOrders.value) {
      try { await cancelOrderById(o.id); } catch {}
    }
    showToast("订单已取消，积分已退回");
    checkoutView.value = false;
    await Promise.all([loadAccount(), loadOrders()]);
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function confirmCheckout() {
  clearInterval(checkoutInterval);
  saving.value = true;
  try {
    for (const o of checkoutOrders.value) {
      try { await payOrderById(o.id); } catch {}
    }
    showToast("支付成功！");
    checkoutView.value = false;
    await Promise.all([loadAccount(), loadOrders()]);
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function cancelOrderById(orderId) {
  const base = props.showIdInput ? "/api/admin/orders" : "/api/student/orders";
  await request(`${base}/${orderId}/cancel`, { method: "POST" });
}
async function payOrderById(orderId) {
  const base = props.showIdInput ? "/api/admin/orders" : "/api/student/orders";
  await request(`${base}/${orderId}/pay`, { method: "POST" });
}

// ==================== 账户 ====================
async function openAccount() {
  saving.value = true;
  try {
    if (props.showIdInput) await request(`/api/admin/credit-accounts/open/${learnerId.value}`, { method: "POST" });
    else await request("/api/student/account/open", { method: "POST" });
    showToast("账户已开通！");
    await loadAccount();
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

// ==================== 订单 ====================
function toggleCart() { showCart.value = !showCart.value; if (showCart.value) loadCart(); }
function openDetailDialog(product) { detailDialog.value = { open: true, product }; }
function closeDetailDialog() { detailDialog.value.open = false; }
function orderFromDetail() { closeDetailDialog(); openOrderDialog(detailDialog.value.product); }
function openOrderDialog(product) { orderDialog.value = { open: true, product, quantity: 1, remark: "" }; }
function closeOrderDialog() { orderDialog.value.open = false; }

const totalPrice = computed(() => {
  const p = orderDialog.value.product;
  return p ? p.creditPrice * orderDialog.value.quantity : 0;
});

async function placeOrder() {
  saving.value = true;
  try {
    const url = props.showIdInput ? "/api/admin/orders/place" : "/api/student/orders/place";
    await request(url, {
      method: "POST",
      body: JSON.stringify({ learnerId: Number(learnerId.value), productId: orderDialog.value.product.id, quantity: orderDialog.value.quantity, remark: orderDialog.value.remark }),
    });
    showToast("下单成功，积分已冻结！");
    closeOrderDialog();
    await Promise.all([loadAccount(), loadOrders()]);
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function payOrder(order) {
  if (!confirm("确认支付？")) return;
  saving.value = true;
  try { const base = props.showIdInput ? "/api/admin/orders" : "/api/student/orders"; await request(`${base}/${order.id}/pay`, { method: "POST" }); showToast("支付成功！"); await Promise.all([loadAccount(), loadOrders()]); }
  catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function cancelOrder(order) {
  if (!confirm("确认取消？积分将退回。")) return;
  saving.value = true;
  try { const base = props.showIdInput ? "/api/admin/orders" : "/api/student/orders"; await request(`${base}/${order.id}/cancel`, { method: "POST" }); showToast("已取消，积分已退回！"); await Promise.all([loadAccount(), loadOrders()]); }
  catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

async function refundOrder(order) {
  if (!confirm("确认退款？")) return;
  saving.value = true;
  try { await request(`/api/admin/orders/${order.id}/refund`, { method: "POST" }); showToast("退款成功！"); await Promise.all([loadAccount(), loadOrders()]); }
  catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

// ==================== 工具 ====================
function formatTime(v) { if (!v) return "-"; return String(v).replace("T", " ").slice(0, 16); }
function showToast(text, type = "ok") { toast.value = { text, type }; clearTimeout(toastTimer); toastTimer = setTimeout(() => { toast.value.text = ""; }, 2800); }
function switchLearner() { if (!Number(learnerId.value) || Number(learnerId.value) < 1) { showToast("无效ID", "error"); return; } loadAll(); }
watch(() => props.learnerId, (newId) => { learnerId.value = newId; loadAll(); loadFlashSales(); });
</script>

<template>
  <div class="mall-layout">
    <!-- ====== 顶部栏 ====== -->
    <header class="mall-header">
      <div class="mall-brand"><Store :size="28" /><div><h1>积分商城</h1><p>Credit Mall</p></div></div>
      <div v-if="showIdInput" class="mall-learner">
        <label><span>学员ID</span><input v-model="learnerId" type="number" min="1" @keyup.enter="switchLearner" /></label>
        <button class="primary-button" @click="switchLearner"><UserCheck :size="16" /> 切换</button>
      </div>
      <div class="mall-nav">
        <button :class="{ active: activeTab === 'mall' }" @click="activeTab = 'mall'"><ShoppingBag :size="17" /> 商品</button>
        <button :class="{ active: activeTab === 'flash' }" @click="activeTab = 'flash'; loadFlashSales()"><Zap :size="17" /> 秒杀</button>
        <button :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'"><Package :size="17" /> 订单</button>
        <button class="ghost-button cart-btn" @click="toggleCart"><ShoppingCart :size="16" /> 购物车 ({{ cartItems.length }})</button>
        <button class="ghost-button" @click="loadAll"><RefreshCw :size="16" /> 刷新</button>
      </div>
    </header>

    <!-- ====== 余额条 ====== -->
    <section class="balance-bar">
      <div v-if="account" class="balance-cards">
        <div class="balance-card primary"><Coins :size="24" /><div><span>可用</span><strong>{{ account.availableCredits.toLocaleString() }}</strong></div></div>
        <div class="balance-card"><ReceiptText :size="24" /><div><span>冻结</span><strong>{{ account.frozenCredits.toLocaleString() }}</strong></div></div>
        <div class="balance-card"><Award :size="24" /><div><span>累计</span><strong>{{ account.totalCredits.toLocaleString() }}</strong></div></div>
      </div>
      <div v-else class="no-account"><p>尚未开通积分账户</p><button class="primary-button" :disabled="saving" @click="openAccount"><Sparkles :size="16" /> 立即开通</button></div>
    </section>

    <div class="mall-body">
      <!-- ====== 购物车抽屉 ====== -->
      <aside v-if="showCart" class="cart-panel">
        <div class="cart-head"><ShoppingCart :size="18" /><span>购物车 ({{ cartItems.length }})</span><button class="icon-button small" @click="showCart = false"><X :size="14" /></button></div>
        <div v-if="cartItems.length === 0" class="cart-empty">购物车是空的</div>
        <template v-else>
          <div class="cart-list">
            <div v-for="item in cartItems" :key="item.id" class="cart-row">
              <input type="checkbox" :checked="cartChecked.includes(item.id)" @change="toggleCartCheck(item.id)" />
              <div class="cart-item-info">
                <strong>{{ item.productName }}</strong>
                <span>{{ item.creditPrice.toLocaleString() }} 积分</span>
              </div>
              <div class="cart-qty">
                <button @click="updateCartNum(item, -1)"><Minus :size="12" /></button>
                <span>{{ item.num }}</span>
                <button @click="updateCartNum(item, 1)"><Plus :size="12" /></button>
              </div>
              <button class="icon-button small" @click="removeCartItem(item.id)"><Trash2 :size="13" /></button>
            </div>
          </div>
          <div class="cart-foot">
            <label class="select-all"><input type="checkbox" :checked="cartChecked.length === cartItems.length && cartItems.length > 0" @change="selectAllCart" /> 全选</label>
            <div class="cart-total">
              <span>合计: <strong>{{ cartTotal.toLocaleString() }}</strong> 积分</span>
              <button class="primary-button" :disabled="saving || cartChecked.length === 0" @click="checkoutFromCart">结算</button>
            </div>
          </div>
        </template>
      </aside>

      <!-- ====== 商品网格 ====== -->
      <section v-if="activeTab === 'mall'" class="mall-content">
        <div v-if="loading" class="state-block"><LoaderCircle class="spin" :size="24" /> 加载中...</div>
        <div v-else-if="products.length === 0" class="state-block"><ShoppingCart :size="32" /> 暂无商品</div>
        <div v-else class="product-grid">
          <article v-for="p in products" :key="p.id" class="product-card">
            <div class="product-cover"><span class="product-emoji">{{ typeIcons[p.productType] || "🎁" }}</span><span class="product-type-badge">{{ typeLabels[p.productType] || p.productType }}</span></div>
            <div class="product-body"><h3>{{ p.productName }}</h3><p v-if="p.description" class="product-desc">{{ p.description }}</p><div class="product-meta"><span class="product-price"><Coins :size="16" />{{ p.creditPrice.toLocaleString() }}</span><span class="product-stock">库存: {{ p.stock === -1 ? "不限" : p.stock }}</span></div></div>
            <div class="product-foot">
              <button class="ghost-button full" type="button" @click="openDetailDialog(p)">查看详情</button>
              <button class="ghost-button full" type="button" @click="addToCart(p)"><ShoppingCart :size="15" /> 加入购物车</button>
              <button class="primary-button full" :disabled="!account || account.availableCredits < p.creditPrice || p.stock === 0" @click="openOrderDialog(p)"><ShoppingBag :size="16" /> {{ !account ? "请先开通" : account.availableCredits < p.creditPrice ? "积分不足" : p.stock === 0 ? "已售罄" : "立即兑换" }}</button>
            </div>
          </article>
        </div>
      </section>

      <!-- ====== 秒杀专区 ====== -->
      <section v-if="activeTab === 'flash'" class="mall-content">
        <div v-if="flashLoading" class="state-block"><LoaderCircle class="spin" :size="24" /> 加载中...</div>
        <div v-else-if="flashSales.length === 0" class="state-block"><Zap :size="32" /> 暂无明显秒杀活动</div>
        <div v-else class="flash-grid">
          <article v-for="sale in flashSales" :key="sale.id" class="flash-card">
            <div class="flash-cover">
              <span class="flash-badge">秒杀</span>
              <span class="flash-emoji">⚡</span>
              <div v-if="flashCountdowns[sale.id]" :class="['flash-cd', flashCountdowns[sale.id].state]">
                <Clock :size="14" /> {{ flashCountdowns[sale.id]?.text }}
              </div>
            </div>
            <div class="flash-body">
              <h3>{{ sale.productName }}</h3>
              <div class="flash-prices">
                <span class="flash-origin">{{ sale.originPrice }} 积分</span>
                <span class="flash-price">{{ sale.flashPrice }} 积分</span>
              </div>
              <div class="flash-stock">
                剩余: {{ sale.redisStock !== undefined ? sale.redisStock : sale.stock }}
              </div>
            </div>
            <div class="flash-foot">
              <button
                class="primary-button full"
                :disabled="saving || !account || (sale.redisStock !== undefined ? sale.redisStock <= 0 : sale.stock <= 0) || (flashCountdowns[sale.id]?.state === 'upcoming')"
                @click="doSeckill(sale)"
              >
                <Zap :size="16" />
                {{ !account ? "请先开通账户" : sale.redisStock === -1 ? "已参与" : flashCountdowns[sale.id]?.state === 'upcoming' ? "未开始" : (sale.redisStock !== undefined ? sale.redisStock <= 0 : sale.stock <= 0) ? "已售罄" : "立即秒杀" }}
              </button>
            </div>
          </article>
        </div>
      </section>

      <!-- ====== 订单列表 ====== -->
      <section v-if="activeTab === 'orders'" class="mall-content">
        <div v-if="orders.length === 0" class="state-block"><Package :size="32" /> 暂无订单</div>
        <div v-else class="order-scene">

          <!-- 待支付订单 -->
          <div v-if="pendingOrders.length > 0" class="order-section">
            <div class="order-section-title">
              <ReceiptText :size="18" />
              <span>待支付</span>
              <strong>({{ pendingOrders.length }})</strong>
            </div>
            <div v-for="o in pendingOrders" :key="o.id" class="order-card pending">
              <div class="order-main">
                <div class="order-info">
                  <strong class="order-product">{{ o.productName }}</strong>
                  <small>订单号: {{ o.orderNo }}</small>
                  <span class="order-time">{{ formatTime(o.createdAt) }}</span>
                </div>
                <div class="order-amount">
                  <span class="order-amount-value">{{ o.totalAmount ? o.totalAmount.toLocaleString() : o.creditAmount.toLocaleString() }}</span>
                  <span class="order-amount-unit">积分</span>
                </div>
              </div>
              <div class="order-actions">
                <button class="primary-button" :disabled="saving" @click="payOrder(o)">确认支付</button>
                <button class="ghost-button" :disabled="saving" @click="cancelOrder(o)"><Undo2 :size="15" />取消订单</button>
              </div>
            </div>
          </div>

          <!-- 已完成订单 -->
          <div v-if="doneOrders.length > 0" class="order-section">
            <div class="order-section-title done">
              <Package :size="18" />
              <span>已完成</span>
              <strong>({{ doneOrders.length }})</strong>
            </div>
            <div v-for="o in doneOrders" :key="o.id" class="order-card done">
              <div class="order-main">
                <div class="order-info">
                  <strong class="order-product">{{ o.productName }}</strong>
                  <small>订单号: {{ o.orderNo }}</small>
                  <span class="order-time">{{ formatTime(o.createdAt) }}</span>
                </div>
                <div class="order-amount">
                  <span :class="['status-pill', statusMap[o.orderStatus]?.class || 'neutral']">{{ statusMap[o.orderStatus]?.label || o.orderStatus }}</span>
                  <span class="order-amount-value">{{ o.totalAmount ? o.totalAmount.toLocaleString() : o.creditAmount.toLocaleString() }} 积分</span>
                </div>
              </div>
              <div v-if="o.orderStatus === 'DELIVERED' && o.deliveredAt" class="order-timeline">
                <div class="timeline-step on"><span></span>已下单</div>
                <div class="timeline-step on"><span></span>已支付</div>
                <div class="timeline-step on"><span></span>已发货</div>
              </div>
              <div v-if="o.orderStatus === 'PAID'" class="order-timeline">
                <div class="timeline-step on"><span></span>已下单</div>
                <div class="timeline-step on"><span></span>已支付</div>
                <div class="timeline-step"><span></span>待发货</div>
              </div>
            </div>
          </div>

          <footer v-if="orderPage.total > orderPage.size" class="pager">
            <button :disabled="orderPage.current <= 1" @click="orderPage.current--; loadOrders()">上一页</button>
            <span>第 {{ orderPage.current }} / {{ Math.ceil(orderPage.total / orderPage.size) }} 页</span>
            <button :disabled="orderPage.current >= Math.ceil(orderPage.total / orderPage.size)" @click="orderPage.current++; loadOrders()">下一页</button>
          </footer>
        </div>
      </section>
    </div>

    <!-- ====== 下单弹窗 ====== -->
    <div v-if="orderDialog.open" class="modal-backdrop" @click.self="closeOrderDialog">
      <div class="modal compact"><div class="modal-head"><h2>确认兑换</h2><button type="button" class="icon-button" @click="closeOrderDialog"><X :size="18" /></button></div>
        <div class="confirm-info"><div class="confirm-row"><span>商品</span><strong>{{ orderDialog.product?.productName }}</strong></div><div class="confirm-row"><span>单价</span><strong>{{ orderDialog.product?.creditPrice }} 积分</strong></div>
          <div class="confirm-row"><span>数量</span><div class="qty-control"><button @click="orderDialog.quantity = Math.max(1, orderDialog.quantity - 1)">-</button><span>{{ orderDialog.quantity }}</span><button @click="orderDialog.quantity = orderDialog.quantity + 1">+</button></div></div>
          <div class="confirm-row total"><span>合计</span><strong>{{ totalPrice }} 积分</strong></div>
        </div>
        <div class="modal-actions"><button class="ghost-button" type="button" @click="closeOrderDialog">取消</button><button class="primary-button" :disabled="saving || (account && account.availableCredits < totalPrice)" @click="placeOrder">{{ saving ? "提交中..." : "确认下单" }}</button></div>
      </div>
    </div>

    <!-- ====== 结算确认页 ====== -->
    <div v-if="checkoutView" class="checkout-overlay">
      <div class="checkout-page">
        <div class="checkout-header">
          <div>
            <h2>订单已生成，请尽快支付</h2>
            <p>倒计时结束未支付将自动取消订单</p>
          </div>
          <div class="checkout-timer" :class="{ urgent: checkoutTimer <= 60 }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/></svg>
            <span>{{ checkoutTimerText }}</span>
          </div>
        </div>

        <div class="checkout-body">
          <div class="checkout-list">
            <div v-for="o in checkoutOrders" :key="o.id" class="checkout-item">
              <span class="checkout-item-name">{{ o.productName }}</span>
              <span class="checkout-item-price">{{ (o.totalAmount || o.creditAmount).toLocaleString() }} 积分</span>
            </div>
          </div>

          <div class="checkout-summary">
            <div class="checkout-row">
              <span>订单数量</span>
              <span>{{ checkoutOrders.length }} 笔</span>
            </div>
            <div class="checkout-row">
              <span>当前可用积分</span>
              <span>{{ account?.availableCredits?.toLocaleString() || 0 }} 积分</span>
            </div>
            <div class="checkout-row total">
              <span>已冻结积分</span>
              <strong>{{ checkoutTotalAmount.toLocaleString() }}</strong>
            </div>
          </div>
        </div>

        <div class="checkout-actions">
          <button class="ghost-button large" @click="cancelCheckout" :disabled="saving">取消订单，退还积分</button>
          <button class="primary-button large" :disabled="saving" @click="confirmCheckout">
            {{ saving ? "处理中..." : `确认支付 ${checkoutTotalAmount.toLocaleString()} 积分` }}
          </button>
        </div>
      </div>
    </div>

    <!-- ====== 详情弹窗 ====== -->
    <div v-if="detailDialog.open" class="modal-backdrop" @click.self="closeDetailDialog">
      <div class="modal"><div class="modal-head"><h2>{{ detailDialog.product?.productName }}</h2><button type="button" class="icon-button" @click="closeDetailDialog"><X :size="18" /></button></div>
        <div class="detail-body"><div class="detail-row"><span>类型</span><strong>{{ typeLabels[detailDialog.product?.productType] || detailDialog.product?.productType }}</strong></div><div class="detail-row"><span>积分</span><strong class="detail-price">{{ detailDialog.product?.creditPrice?.toLocaleString() }} 积分</strong></div><div class="detail-row"><span>库存</span><strong>{{ detailDialog.product?.stock === -1 ? "不限" : detailDialog.product?.stock }}</strong></div><div class="detail-row" v-if="detailDialog.product?.description"><span>描述</span><p>{{ detailDialog.product?.description }}</p></div></div>
        <div class="modal-actions"><button class="ghost-button" type="button" @click="closeDetailDialog">关闭</button><button class="primary-button" type="button" @click="orderFromDetail">立即兑换</button></div>
      </div>
    </div>

    <div v-if="toast.text" :class="['toast', toast.type]">{{ toast.text }}</div>
  </div>
</template>

<style scoped>
.mall-layout { background: var(--bg); color: var(--ink); font-family: "Microsoft YaHei","PingFang SC","Segoe UI",sans-serif; }

.mall-header { display: flex; align-items: center; gap: 20px; padding: 14px 28px; background: #123c39; color: #ecf7f5; flex-wrap: wrap; }
.mall-brand { display: flex; align-items: center; gap: 10px; }
.mall-brand h1 { font-size: 20px; margin: 0; }
.mall-brand p { margin: 0; font-size: 11px; color: #a8c8c3; text-transform: uppercase; letter-spacing: 1px; }
.mall-learner { display: flex; align-items: flex-end; gap: 8px; }
.mall-learner input { height: 36px; width: 90px; border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; padding: 0 10px; background: rgba(255,255,255,0.1); color: #fff; }
.mall-nav { display: flex; align-items: center; gap: 8px; margin-left: auto; }
.mall-nav button { display: flex; align-items: center; gap: 6px; min-height: 36px; padding: 0 14px; border: 1px solid rgba(255,255,255,0.2); border-radius: 8px; color: #cfe4e1; background: transparent; cursor: pointer; }
.mall-nav button:hover, .mall-nav button.active { color: #fff; background: rgba(255,255,255,0.13); }
.cart-btn { color: #f0c46a !important; border-color: rgba(240,196,106,0.35) !important; }

.balance-bar { padding: 14px 28px; background: #fff; border-bottom: 1px solid var(--line); }
.balance-cards { display: flex; gap: 14px; }
.balance-card { display: flex; align-items: center; gap: 12px; flex: 1; padding: 12px 16px; border: 1px solid var(--line); border-radius: 8px; background: #f9fbfd; }
.balance-card.primary { border-color: var(--primary); background: var(--success-bg); }
.balance-card span { display: block; font-size: 12px; color: var(--muted); }
.balance-card strong { font-size: 22px; }
.no-account { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; }

.mall-body { display: grid; grid-template-columns: minmax(0, 1fr); }
.mall-body:has(.cart-panel) { grid-template-columns: minmax(0, 1fr) 320px; }

/* cart panel */
.cart-panel { border-left: 1px solid var(--line); background: #fff; padding: 16px; display: flex; flex-direction: column; max-height: calc(100vh - 160px); overflow-y: auto; }
.cart-head { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--line); font-weight: 700; }
.cart-head button { margin-left: auto; }
.cart-empty { text-align: center; color: var(--muted); padding: 40px 0; }
.cart-list { display: grid; gap: 10px; flex: 1; }
.cart-row { display: flex; align-items: center; gap: 10px; padding: 10px; border: 1px solid var(--line); border-radius: 8px; }
.cart-row input[type="checkbox"] { width: 18px; height: 18px; cursor: pointer; }
.cart-item-info { flex: 1; min-width: 0; }
.cart-item-info strong { display: block; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cart-item-info span { font-size: 12px; color: var(--muted); }
.cart-qty { display: flex; align-items: center; gap: 4px; }
.cart-qty button { width: 26px; height: 26px; border: 1px solid var(--line); border-radius: 6px; background: #f7f9fc; cursor: pointer; display: grid; place-items: center; }
.cart-qty span { width: 28px; text-align: center; font-weight: 700; font-size: 14px; }
.cart-foot { padding-top: 12px; border-top: 1px solid var(--line); }
.select-all { display: flex; align-items: center; gap: 6px; margin-bottom: 10px; font-size: 13px; cursor: pointer; }
.cart-total { display: flex; align-items: center; justify-content: space-between; }
.cart-total strong { font-size: 18px; color: var(--primary-dark); }

.mall-content { padding: 18px 28px; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 14px; }
.product-card { border: 1px solid var(--line); border-radius: 8px; background: var(--panel); overflow: hidden; }
.product-cover { height: 80px; background: linear-gradient(135deg, #eef3f8, #d8e1ec); display: grid; place-items: center; position: relative; }
.product-emoji { font-size: 36px; }
.product-type-badge { position: absolute; top: 8px; right: 8px; padding: 2px 8px; border-radius: 999px; background: var(--primary); color: #fff; font-size: 10px; font-weight: 700; }
.product-body { padding: 12px; }
.product-body h3 { margin: 0 0 4px; font-size: 15px; }
.product-desc { margin: 0 0 8px; font-size: 12px; color: var(--muted); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.product-meta { display: flex; justify-content: space-between; align-items: center; }
.product-price { display: flex; align-items: center; gap: 4px; font-weight: 700; color: var(--primary-dark); font-size: 16px; }
.product-stock { font-size: 12px; color: var(--muted); }
.product-foot { padding: 0 12px 12px; display: grid; gap: 6px; }
button.full { width: 100%; justify-content: center; }

.order-scene { display: grid; gap: 24px; }
.order-section { display: grid; gap: 10px; }
.order-section-title { display: flex; align-items: center; gap: 8px; padding: 0 4px; font-size: 15px; color: var(--warning); }
.order-section-title strong { font-weight: 800; }
.order-section-title.done { color: var(--primary-dark); }

.order-card { border: 1px solid var(--line); border-radius: 10px; background: var(--panel); overflow: hidden; }
.order-card.pending { border-color: #f0c46a; box-shadow: 0 2px 8px rgba(240,196,106,0.12); }
.order-main { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; gap: 16px; }
.order-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.order-product { font-size: 16px; }
.order-info small { font-size: 12px; color: var(--muted); }
.order-time { font-size: 12px; color: var(--muted); }
.order-amount { text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.order-amount-value { font-size: 22px; font-weight: 800; color: var(--primary-dark); }
.order-amount-unit { font-size: 12px; color: var(--muted); }

.order-card .order-actions { display: flex; gap: 8px; padding: 0 16px 14px; justify-content: flex-end; }

/* 时间线 */
.order-timeline { display: flex; gap: 0; padding: 0 16px 14px; }
.timeline-step { flex: 1; display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--muted); }
.timeline-step span { width: 10px; height: 10px; border-radius: 50%; background: var(--line); flex-shrink: 0; }
.timeline-step.on { color: var(--primary-dark); font-weight: 600; }
.timeline-step.on span { background: var(--primary); box-shadow: 0 0 0 3px rgba(22,106,95,0.15); }

button.danger { color: var(--danger); border-color: var(--danger-bg); }

.modal-backdrop { position: fixed; inset: 0; z-index: 20; display: grid; place-items: center; padding: 22px; background: rgba(10,18,30,0.48); }
.modal { width: min(600px, 100%); max-height: calc(100vh - 44px); overflow: auto; padding: 20px; border-radius: 10px; background: var(--panel); }
.modal.compact { width: min(480px, 100%); }
.modal-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.icon-button { display: grid; place-items: center; width: 34px; height: 34px; border: 1px solid var(--line); border-radius: 8px; background: var(--panel); cursor: pointer; }
.icon-button.small { width: 28px; height: 28px; }
.confirm-info { display: grid; gap: 8px; padding: 8px 0; }
.confirm-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--line); }
.confirm-row.total { border-bottom: 0; }
.confirm-row.total strong { font-size: 20px; color: var(--primary-dark); }
.qty-control { display: flex; align-items: center; border: 1px solid var(--line); border-radius: 8px; overflow: hidden; }
.qty-control button { width: 30px; height: 30px; border: 0; background: #f7f9fc; cursor: pointer; font-weight: 700; font-size: 15px; }
.qty-control span { width: 36px; text-align: center; font-weight: 700; }
.detail-body { display: grid; gap: 10px; padding: 4px 0 8px; }
.detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--line); }
.detail-row p { margin: 6px 0 0; font-size: 14px; line-height: 1.6; }
.detail-price { color: var(--primary-dark); font-size: 18px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }

.state-block { min-height: 160px; display: grid; place-items: center; gap: 8px; border: 1px dashed var(--line); border-radius: 10px; color: var(--muted); }
.pager { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
.pager button { display: inline-flex; align-items: center; height: 34px; padding: 0 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; cursor: pointer; }
.pager button:disabled { opacity: 0.5; cursor: not-allowed; }
.pager span { line-height: 34px; color: var(--muted); }

.primary-button, .ghost-button { display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 12px; border-radius: 8px; border: 1px solid var(--line); background: var(--panel); cursor: pointer; font-size: 13px; }
.primary-button { border-color: var(--primary); color: #fff; background: var(--primary); }
.ghost-button:hover { border-color: var(--primary); color: var(--primary); }
.toast { position: fixed; right: 24px; bottom: 24px; z-index: 30; padding: 12px 16px; border-radius: 8px; color: #fff; background: var(--primary); box-shadow: 0 8px 28px rgba(0,0,0,0.2); }
.toast.error { background: #b42318; }
.spin { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ====== 结算确认页 ====== */
.checkout-overlay {
  position: fixed; inset: 0; z-index: 25; background: rgba(10,18,30,0.55);
  display: grid; place-items: center; padding: 24px;
}
.checkout-page {
  width: min(560px, 100%); max-height: calc(100vh - 48px); overflow-y: auto;
  background: var(--panel); border-radius: 12px; box-shadow: 0 24px 70px rgba(10,18,30,0.30);
}
.checkout-header {
  display: flex; align-items: flex-start; justify-content: space-between; gap: 16px;
  padding: 22px 24px 16px; border-bottom: 1px solid var(--line);
}
.checkout-header h2 { margin: 0 0 4px; font-size: 20px; }
.checkout-header p { margin: 0; font-size: 13px; color: var(--muted); }

.checkout-timer {
  display: flex; align-items: center; gap: 8px; padding: 10px 16px;
  border-radius: 10px; background: var(--success-bg); color: var(--primary-dark);
  font-size: 20px; font-weight: 800; font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.checkout-timer.urgent {
  background: var(--danger-bg); color: var(--danger); animation: pulse 1s ease-in-out infinite;
}
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.6} }

.checkout-body { padding: 16px 24px; }
.checkout-list { display: grid; gap: 0; border: 1px solid var(--line); border-radius: 10px; overflow: hidden; margin-bottom: 16px; }
.checkout-item {
  display: flex; align-items: center; gap: 12px; padding: 12px 14px;
  border-bottom: 1px solid var(--line); background: #fff;
}
.checkout-item:last-child { border-bottom: 0; }
.checkout-item-name { flex: 1; font-size: 14px; font-weight: 600; }
.checkout-item-qty { color: var(--muted); font-size: 13px; min-width: 32px; text-align: center; }
.checkout-item-price { font-weight: 700; color: var(--primary-dark); font-size: 15px; white-space: nowrap; }

.checkout-summary { display: grid; gap: 8px; padding: 14px 16px; border-radius: 10px; background: #f9fbfd; border: 1px solid var(--line); }
.checkout-row { display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.checkout-row span:first-child { color: var(--muted); }
.checkout-row.total { padding-top: 10px; border-top: 1px solid var(--line); margin-top: 4px; }
.checkout-row.total strong { font-size: 24px; color: var(--primary-dark); }

.checkout-actions {
  display: flex; gap: 12px; padding: 16px 24px 22px; border-top: 1px solid var(--line);
}
.checkout-actions button { flex: 1; }
button.large { height: 46px; font-size: 15px; justify-content: center; border-radius: 10px; }

/* ====== 秒杀 ====== */
.flash-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
.flash-card { border: 2px solid #f0c46a; border-radius: 10px; background: var(--panel); overflow: hidden; position: relative; }
.flash-card:hover { box-shadow: 0 6px 20px rgba(240,196,106,0.20); }
.flash-cover { height: 90px; background: linear-gradient(135deg, #fef3c7, #fde68a, #f0c46a); display: grid; place-items: center; position: relative; }
.flash-badge { position: absolute; top: 10px; left: 10px; padding: 3px 10px; border-radius: 999px; background: #b42318; color: #fff; font-size: 11px; font-weight: 800; letter-spacing: 1px; }
.flash-emoji { font-size: 38px; }
.flash-cd { position: absolute; right: 10px; bottom: 10px; display: flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 700; font-variant-numeric: tabular-nums; }
.flash-cd.active { background: #fff; color: #b42318; }
.flash-cd.upcoming { background: rgba(255,255,255,0.7); color: #92400e; }
.flash-body { padding: 12px 14px; }
.flash-body h3 { margin: 0 0 8px; font-size: 16px; }
.flash-prices { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.flash-origin { font-size: 13px; color: var(--muted); text-decoration: line-through; }
.flash-price { font-size: 22px; font-weight: 800; color: #b42318; }
.flash-stock { font-size: 12px; color: var(--muted); margin-bottom: 4px; }
.flash-foot { padding: 0 14px 14px; }

@media (max-width: 860px) { .mall-header { flex-direction: column; align-items: stretch; } .mall-nav { margin-left: 0; flex-wrap: wrap; } .balance-cards { flex-direction: column; } .product-grid, .flash-grid { grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); } .mall-body:has(.cart-panel) { grid-template-columns: 1fr; } .checkout-header { flex-direction: column; } }
</style>
