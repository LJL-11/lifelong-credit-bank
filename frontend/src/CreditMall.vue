<script setup>
import { computed, onMounted, ref, watch } from "vue";
import {
  Award, Coins, LoaderCircle, Package, ReceiptText, RefreshCw,
  ShoppingBag, ShoppingCart, Store, UserCheck, Sparkles,
  Search, CheckCircle2, AlertCircle,
} from "@lucide/vue";
import FlashSaleSection from "./components/mall/FlashSaleSection.vue";
import CartSidebar from "./components/mall/CartSidebar.vue";
import CheckoutOverlay from "./components/mall/CheckoutOverlay.vue";
import ProductDialogs from "./components/mall/ProductDialogs.vue";

const props = defineProps({
  learnerId: { type: Number, required: true },
  token: { type: String, default: "" },
  showIdInput: { type: Boolean, default: false },
});

const learnerId = ref(props.learnerId);
const activeTab = ref("mall");
const searchQuery = ref("");
const selectedType = ref("ALL");

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

const checkoutView = ref(false);
const checkoutTimer = ref(900);
const checkoutTimerText = ref("15:00");
let checkoutInterval = 0;

const typeLabels = { COURSE: "课程", CERTIFICATE: "认证", MERCHANDISE: "实物", SERVICE: "服务" };
const typeIcons = { COURSE: "📚", CERTIFICATE: "🏅", MERCHANDISE: "🎁", SERVICE: "💼" };
const typeColors = { COURSE: "#4f7df3", CERTIFICATE: "#e8a838", MERCHANDISE: "#e85d75", SERVICE: "#38b2ac" };
const statusMap = {
  PENDING: { label: "待支付", class: "pending" },
  PAID: { label: "已支付", class: "success" },
  DELIVERED: { label: "已发货", class: "success" },
  CANCELLED: { label: "已取消", class: "neutral" },
  REFUNDED: { label: "已退款", class: "danger" },
};

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

async function loadFlashSales() {
  flashLoading.value = true;
  try {
    flashSales.value = await request("/api/flash/active");
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
    sale.redisStock = Math.max(0, (sale.redisStock || sale.stock) - 1);
  } catch (err) {
    showToast(err.message, "error");
    if (err.message?.includes("不能重复参与")) sale.redisStock = -1;
  }
  finally { saving.value = false; }
}

onMounted(() => { loadAll(); loadFlashSales(); });

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

const filteredProducts = computed(() => {
  let list = products.value;
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(p => p.productName?.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q));
  }
  if (selectedType.value !== "ALL") {
    list = list.filter(p => p.productType === selectedType.value);
  }
  return list;
});

function onCartChecked(newChecked) { cartChecked.value = newChecked; }

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

const checkoutOrders = ref([]);

async function checkoutFromCart() {
  if (cartChecked.value.length === 0) { showToast("请先选择商品", "error"); return; }
  if (!account.value) { showToast("请先开通积分账户", "error"); return; }
  if (cartTotal.value > account.value.availableCredits) { showToast("积分不足", "error"); return; }

  saving.value = true;
  try {
    const url = props.showIdInput ? "/api/admin/orders/place" : "/api/student/orders/place";
    const data = await request(url, {
      method: "POST",
      body: JSON.stringify({ learnerId: Number(learnerId.value), cartIds: [...cartChecked.value], remark: "购物车下单" }),
    });
    checkoutOrders.value = Array.isArray(data) ? data : [data];
    cartChecked.value = [];
    showCart.value = false;
    await Promise.all([loadAccount(), loadCart()]);
    checkoutView.value = true;
    startCheckoutTimer();
  } catch (err) { showToast(err.message, "error"); }
  finally { saving.value = false; }
}

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
      for (const o of checkoutOrders.value) {
        try { await cancelOrderById(o.id); } catch (e) { console.error("超时取消失败:", e); }
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
  let allOk = true;
  for (const o of checkoutOrders.value) {
    try { await cancelOrderById(o.id); } catch (err) { allOk = false; showToast(err.message, "error"); }
  }
  if (allOk) showToast("订单已取消，积分已退回");
  checkoutView.value = false;
  await Promise.all([loadAccount(), loadOrders()]);
  saving.value = false;
}

async function confirmCheckout() {
  clearInterval(checkoutInterval);
  saving.value = true;
  let allOk = true;
  for (const o of checkoutOrders.value) {
    try { await payOrderById(o.id); } catch (err) { allOk = false; showToast(err.message, "error"); }
  }
  if (allOk) showToast("支付成功！");
  checkoutView.value = false;
  await Promise.all([loadAccount(), loadOrders()]);
  saving.value = false;
}

async function cancelOrderById(orderId) {
  const base = props.showIdInput ? "/api/admin/orders" : "/api/student/orders";
  await request(`${base}/${orderId}/cancel`, { method: "POST" });
}
async function payOrderById(orderId) {
  const base = props.showIdInput ? "/api/admin/orders" : "/api/student/orders";
  await request(`${base}/${orderId}/pay`, { method: "POST" });
}

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

function toggleCart() { showCart.value = !showCart.value; if (showCart.value) loadCart(); }
function openDetailDialog(product) { detailDialog.value = { open: true, product }; }
function closeDetailDialog() { detailDialog.value.open = false; }
function orderFromDetail() { closeDetailDialog(); openOrderDialog(detailDialog.value.product); }
function openOrderDialog(product) { orderDialog.value = { open: true, product, quantity: 1, remark: "" }; }
function closeOrderDialog() { orderDialog.value.open = false; }

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

function formatTime(v) { if (!v) return "-"; return String(v).replace("T", " ").slice(0, 16); }
function showToast(text, type = "ok") { toast.value = { text, type }; clearTimeout(toastTimer); toastTimer = setTimeout(() => { toast.value.text = ""; }, 2800); }
function switchLearner() { if (!Number(learnerId.value) || Number(learnerId.value) < 1) { showToast("无效ID", "error"); return; } loadAll(); }
watch(() => props.learnerId, (newId) => { learnerId.value = newId; loadAll(); loadFlashSales(); });
</script>

<template>
  <div class="mall-layout">
    <!-- 顶部导航栏 -->
    <header class="mall-header">
      <div class="header-left">
        <div class="brand">
          <div class="brand-icon"><Store :size="24" /></div>
          <div class="brand-text">
            <h1>积分商城</h1>
            <span>Credit Mall</span>
          </div>
        </div>
        <nav class="tab-nav">
          <button :class="{ active: activeTab === 'mall' }" @click="activeTab = 'mall'">
            <ShoppingBag :size="16" /> 商品
          </button>
          <button :class="{ active: activeTab === 'flash' }" @click="activeTab = 'flash'; loadFlashSales()">
            ⚡ 秒杀
          </button>
          <button :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'">
            <Package :size="16" /> 订单
          </button>
        </nav>
      </div>
      <div class="header-right">
        <div v-if="showIdInput" class="learner-switch">
          <label><span>学员ID</span><input v-model="learnerId" type="number" min="1" @keyup.enter="switchLearner" /></label>
          <button class="btn-secondary" @click="switchLearner"><UserCheck :size="14" /> 切换</button>
        </div>
        <button class="btn-cart" @click="toggleCart">
          <ShoppingCart :size="18" />
          <span class="cart-badge" v-if="cartItems.length > 0">{{ cartItems.length }}</span>
          <span>购物车</span>
        </button>
        <button class="btn-refresh" @click="loadAll"><RefreshCw :size="16" /></button>
      </div>
    </header>

    <!-- 账户余额 -->
    <section class="account-section">
      <div v-if="account" class="account-cards">
        <div class="account-card primary">
          <div class="card-icon" style="background: #eef2ff; color: #4f7df3;"><Coins :size="22" /></div>
          <div class="card-info">
            <span class="card-label">可用积分</span>
            <strong class="card-value">{{ account.availableCredits.toLocaleString() }}</strong>
          </div>
        </div>
        <div class="account-card">
          <div class="card-icon" style="background: #fef3c7; color: #d97706;"><ReceiptText :size="22" /></div>
          <div class="card-info">
            <span class="card-label">冻结积分</span>
            <strong class="card-value">{{ account.frozenCredits.toLocaleString() }}</strong>
          </div>
        </div>
        <div class="account-card">
          <div class="card-icon" style="background: #d1fae5; color: #059669;"><Award :size="22" /></div>
          <div class="card-info">
            <span class="card-label">累计积分</span>
            <strong class="card-value">{{ account.totalCredits.toLocaleString() }}</strong>
          </div>
        </div>
        <div class="account-card">
          <div class="card-icon" style="background: #fce7f3; color: #db2777;"><Sparkles :size="22" /></div>
          <div class="card-info">
            <span class="card-label">账户状态</span>
            <strong class="card-value status-active">正常</strong>
          </div>
        </div>
      </div>
      <div v-else class="account-empty">
        <div class="empty-icon"><Sparkles :size="32" /></div>
        <p>您尚未开通积分账户</p>
        <button class="btn-primary" :disabled="saving" @click="openAccount"><Sparkles :size="16" /> 立即开通账户</button>
      </div>
    </section>

    <!-- 主体内容 -->
    <main class="mall-main">
      <!-- 商品列表 -->
      <section v-if="activeTab === 'mall'" class="content-section">
        <div class="toolbar">
          <div class="search-box">
            <Search :size="16" />
            <input v-model="searchQuery" type="text" placeholder="搜索商品名称..." />
          </div>
          <div class="filter-tabs">
            <button :class="{ active: selectedType === 'ALL' }" @click="selectedType = 'ALL'">全部</button>
            <button :class="{ active: selectedType === 'COURSE' }" @click="selectedType = 'COURSE'">课程</button>
            <button :class="{ active: selectedType === 'CERTIFICATE' }" @click="selectedType = 'CERTIFICATE'">认证</button>
            <button :class="{ active: selectedType === 'MERCHANDISE' }" @click="selectedType = 'MERCHANDISE'">实物</button>
            <button :class="{ active: selectedType === 'SERVICE' }" @click="selectedType = 'SERVICE'">服务</button>
          </div>
        </div>
        <div v-if="loading" class="loading-state"><LoaderCircle class="spin" :size="28" /><p>正在加载商品...</p></div>
        <div v-else-if="filteredProducts.length === 0" class="empty-state"><ShoppingBag :size="48" /><p>{{ searchQuery ? '未找到匹配的商品' : '暂无商品上架' }}</p></div>
        <div v-else class="product-grid">
          <article v-for="p in filteredProducts" :key="p.id" class="product-card">
            <div class="product-image">
              <span class="product-emoji">{{ typeIcons[p.productType] || "🎁" }}</span>
              <span class="product-type-tag" :style="{ background: typeColors[p.productType] + '15', color: typeColors[p.productType] }">{{ typeLabels[p.productType] || p.productType }}</span>
            </div>
            <div class="product-content">
              <h3>{{ p.productName }}</h3>
              <p v-if="p.description" class="product-desc">{{ p.description }}</p>
              <div class="product-footer">
                <div class="product-price"><Coins :size="16" /><strong>{{ p.creditPrice.toLocaleString() }}</strong><span>积分</span></div>
                <div class="product-stock" :class="{ low: p.stock !== -1 && p.stock <= 5 }">{{ p.stock === -1 ? '库存充足' : `剩余 ${p.stock}` }}</div>
              </div>
            </div>
            <div class="product-actions">
              <button class="btn-ghost" @click="openDetailDialog(p)">查看详情</button>
              <button class="btn-ghost" @click="addToCart(p)"><ShoppingCart :size="14" /> 加入购物车</button>
              <button class="btn-primary" :disabled="!account || (account && account.availableCredits < p.creditPrice) || p.stock === 0" @click="openOrderDialog(p)">
                <ShoppingBag :size="14" />
                {{ !account ? '请先开通' : account.availableCredits < p.creditPrice ? '积分不足' : p.stock === 0 ? '已售罄' : '立即兑换' }}
              </button>
            </div>
          </article>
        </div>
      </section>

      <!-- 秒杀 -->
      <section v-if="activeTab === 'flash'" class="content-section">
        <div v-if="flashLoading" class="loading-state"><LoaderCircle class="spin" :size="28" /><p>加载秒杀活动中...</p></div>
        <FlashSaleSection v-else :flash-sales="flashSales" :flash-countdowns="flashCountdowns" :saving="saving" :account="account" @seckill="doSeckill" />
      </section>

      <!-- 订单 -->
      <section v-if="activeTab === 'orders'" class="content-section">
        <div class="section-header">
          <h2><Package :size="20" /> 我的订单</h2>
          <p>查看和管理您的所有订单</p>
        </div>
        <div v-if="orders.length === 0" class="empty-state"><Package :size="48" /><p>暂无订单记录</p></div>
        <div v-else class="orders-container">
          <div v-if="pendingOrders.length > 0" class="order-group">
            <div class="group-title"><AlertCircle :size="16" style="color: #e8a838;" /><span>待支付</span><span class="group-count">{{ pendingOrders.length }}</span></div>
            <div class="order-list">
              <div v-for="o in pendingOrders" :key="o.id" class="order-item pending">
                <div class="order-item-main">
                  <div class="order-item-info"><strong>{{ o.productName }}</strong><div class="order-meta"><span>订单号: {{ o.orderNo }}</span><span>{{ formatTime(o.createdAt) }}</span></div></div>
                  <div class="order-item-price"><strong>{{ (o.totalAmount || o.creditAmount).toLocaleString() }}</strong><span>积分</span></div>
                </div>
                <div class="order-item-actions"><button class="btn-primary" :disabled="saving" @click="payOrder(o)">确认支付</button><button class="btn-ghost" :disabled="saving" @click="cancelOrder(o)">取消订单</button></div>
              </div>
            </div>
          </div>
          <div v-if="doneOrders.length > 0" class="order-group">
            <div class="group-title done"><CheckCircle2 :size="16" style="color: #059669;" /><span>已完成</span><span class="group-count">{{ doneOrders.length }}</span></div>
            <div class="order-list">
              <div v-for="o in doneOrders" :key="o.id" class="order-item">
                <div class="order-item-main">
                  <div class="order-item-info"><strong>{{ o.productName }}</strong><div class="order-meta"><span>订单号: {{ o.orderNo }}</span><span>{{ formatTime(o.createdAt) }}</span></div></div>
                  <div class="order-item-right">
                    <span :class="['status-tag', statusMap[o.orderStatus]?.class || 'neutral']">{{ statusMap[o.orderStatus]?.label || o.orderStatus }}</span>
                    <strong class="order-price">{{ (o.totalAmount || o.creditAmount).toLocaleString() }} 积分</strong>
                  </div>
                </div>
                <div v-if="o.orderStatus === 'DELIVERED'" class="order-timeline">
                  <div class="timeline-step completed"><span></span>已下单</div><div class="timeline-line"></div>
                  <div class="timeline-step completed"><span></span>已支付</div><div class="timeline-line"></div>
                  <div class="timeline-step completed"><span></span>已发货</div>
                </div>
                <div v-else-if="o.orderStatus === 'PAID'" class="order-timeline">
                  <div class="timeline-step completed"><span></span>已下单</div><div class="timeline-line"></div>
                  <div class="timeline-step completed"><span></span>已支付</div><div class="timeline-line"></div>
                  <div class="timeline-step"><span></span>待发货</div>
                </div>
              </div>
            </div>
          </div>
          <footer v-if="orderPage.total > orderPage.size" class="pagination">
            <button :disabled="orderPage.current <= 1" @click="orderPage.current--; loadOrders()">上一页</button>
            <span>第 {{ orderPage.current }} / {{ Math.ceil(orderPage.total / orderPage.size) }} 页</span>
            <button :disabled="orderPage.current >= Math.ceil(orderPage.total / orderPage.size)" @click="orderPage.current++; loadOrders()">下一页</button>
          </footer>
        </div>
      </section>
    </main>

    <!-- 购物车侧边栏 -->
    <CartSidebar :show-cart="showCart" :cart-items="cartItems" :cart-checked="cartChecked" :cart-total="cartTotal" :saving="saving"
      @close="showCart = false"
      @update:cart-checked="onCartChecked"
      @update-cart-num="updateCartNum"
      @remove-cart-item="removeCartItem"
      @checkout="checkoutFromCart" />

    <!-- 结算确认页 -->
    <CheckoutOverlay :checkout-view="checkoutView" :checkout-timer-text="checkoutTimerText" :checkout-timer="checkoutTimer"
      :checkout-orders="checkoutOrders" :checkout-total-amount="checkoutTotalAmount" :saving="saving" :account="account"
      @cancel="cancelCheckout" @confirm="confirmCheckout" />

    <!-- 弹窗 -->
    <ProductDialogs :order-dialog="orderDialog" :detail-dialog="detailDialog" :account="account" :saving="saving"
      :type-icons="typeIcons" :type-labels="typeLabels" :type-colors="typeColors"
      @close-order="closeOrderDialog" @close-detail="closeDetailDialog" @place-order="placeOrder"
      @update:order-quantity="orderDialog.quantity = $event" @order-from-detail="orderFromDetail" />

    <!-- Toast -->
    <div v-if="toast.text" :class="['toast', toast.type]">
      <component :is="toast.type === 'error' ? AlertCircle : CheckCircle2" :size="16" />
      {{ toast.text }}
    </div>
  </div>
</template>

<style scoped>
/* ====== 积分商城样式 ====== */
.mall-layout {
  min-height: 100vh;
  background: #f1f5f9;
  color: #334155;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* 顶部导航栏 */
.mall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 68px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.header-left { display: flex; align-items: center; gap: 40px; }

.brand { display: flex; align-items: center; gap: 14px; }
.brand-icon {
  width: 42px; height: 42px; border-radius: 12px;
  background: linear-gradient(135deg, #64748b, #94a3b8);
  display: grid; place-items: center; color: #fff;
  box-shadow: 0 2px 8px rgba(100, 116, 139, 0.18);
}
.brand-text h1 { font-size: 19px; font-weight: 700; margin: 0; color: #1e293b; letter-spacing: -0.02em; }
.brand-text span { font-size: 11px; color: #94a3b8; text-transform: uppercase; letter-spacing: 1.2px; }

.tab-nav { display: flex; align-items: center; gap: 4px; background: #f1f5f9; padding: 4px; border-radius: 12px; }
.tab-nav button {
  display: flex; align-items: center; gap: 8px; padding: 9px 20px; border: none; border-radius: 10px;
  background: transparent; color: #64748b; font-size: 14px; font-weight: 500; cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.tab-nav button:hover { color: #475569; background: rgba(255, 255, 255, 0.6); }
.tab-nav button.active { background: #ffffff; color: #475569; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06); font-weight: 600; }

.header-right { display: flex; align-items: center; gap: 14px; }

.learner-switch { display: flex; align-items: center; gap: 10px; }
.learner-switch label { display: flex; align-items: center; gap: 8px; }
.learner-switch label span { font-size: 13px; color: #64748b; font-weight: 500; }
.learner-switch input {
  width: 90px; height: 38px; padding: 0 12px; border: 1px solid #e2e8f0; border-radius: 10px;
  background: #f8fafc; font-size: 14px; color: #334155; outline: none; transition: all 0.2s;
}
.learner-switch input:focus { border-color: #94a3b8; box-shadow: 0 0 0 3px rgba(148, 163, 184, 0.12); }

.btn-cart {
  display: flex; align-items: center; gap: 8px; padding: 9px 16px; border: 1px solid #e2e8f0;
  border-radius: 10px; background: #fff; color: #64748b; font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all 0.25s; position: relative;
}
.btn-cart:hover { border-color: #cbd5e1; background: #f8fafc; color: #475569; }
.cart-badge {
  position: absolute; top: -7px; right: -7px; min-width: 20px; height: 20px; padding: 0 6px;
  border-radius: 999px; background: #f87171; color: #fff; font-size: 11px; font-weight: 700;
  display: grid; place-items: center; box-shadow: 0 2px 6px rgba(248, 113, 113, 0.3);
}

.btn-refresh {
  width: 38px; height: 38px; border: 1px solid #e2e8f0; border-radius: 10px;
  background: #fff; color: #94a3b8; cursor: pointer; display: grid; place-items: center; transition: all 0.25s;
}
.btn-refresh:hover { border-color: #cbd5e1; color: #64748b; background: #f8fafc; transform: rotate(180deg); }

/* 账户余额区 */
.account-section { padding: 28px 40px; background: #ffffff; border-bottom: 1px solid #e2e8f0; }
.account-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.account-card {
  display: flex; align-items: center; gap: 16px; padding: 22px 24px; border-radius: 14px;
  background: #f8fafc; border: 1px solid #e2e8f0; transition: all 0.3s;
}
.account-card:hover { border-color: #cbd5e1; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04); transform: translateY(-2px); }
.account-card.primary { background: linear-gradient(135deg, #64748b, #475569); border-color: transparent; color: #fff; }
.account-card.primary .card-label { color: rgba(255, 255, 255, 0.65); }
.account-card.primary .card-value { color: #fff; }
.card-icon { width: 48px; height: 48px; border-radius: 12px; display: grid; place-items: center; flex-shrink: 0; }
.card-info { display: flex; flex-direction: column; gap: 4px; }
.card-label { font-size: 13px; color: #94a3b8; font-weight: 500; }
.card-value { font-size: 24px; font-weight: 700; color: #1e293b; letter-spacing: -0.02em; }
.status-active { color: #10b981; }

.account-empty { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 40px; text-align: center; }
.empty-icon { width: 72px; height: 72px; border-radius: 20px; background: #f1f5f9; color: #94a3b8; display: grid; place-items: center; }
.account-empty p { color: #64748b; font-size: 15px; margin: 0; }

/* 主体内容 */
.mall-main { padding: 28px 40px; max-width: 1440px; margin: 0 auto; }
.content-section { animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
.section-header { margin-bottom: 24px; }
.section-header h2 { display: flex; align-items: center; gap: 10px; font-size: 20px; font-weight: 700; color: #1e293b; margin: 0 0 6px; }
.section-header p { font-size: 14px; color: #94a3b8; margin: 0; }

/* 工具栏 */
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-bottom: 24px; }
.search-box {
  display: flex; align-items: center; gap: 12px; padding: 0 16px; height: 44px;
  border: 1px solid #e2e8f0; border-radius: 12px; background: #fff; width: 320px; transition: all 0.25s;
}
.search-box:focus-within { border-color: #94a3b8; box-shadow: 0 0 0 4px rgba(148, 163, 184, 0.08); }
.search-box svg { color: #94a3b8; flex-shrink: 0; }
.search-box input { border: none; background: transparent; font-size: 14px; color: #334155; outline: none; width: 100%; }
.search-box input::placeholder { color: #cbd5e1; }

.filter-tabs { display: flex; gap: 6px; }
.filter-tabs button {
  padding: 10px 20px; border: 1px solid #e2e8f0; border-radius: 10px; background: #fff;
  color: #64748b; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.25s;
}
.filter-tabs button:hover { border-color: #cbd5e1; color: #475569; background: #f8fafc; }
.filter-tabs button.active { background: #475569; border-color: #475569; color: #fff; box-shadow: 0 2px 8px rgba(71, 85, 105, 0.2); }

/* 商品网格 */
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.product-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1); display: flex; flex-direction: column;
}
.product-card:hover { border-color: #cbd5e1; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06); transform: translateY(-4px); }
.product-image {
  height: 160px; background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  display: flex; align-items: center; justify-content: center; position: relative;
}
.product-emoji { font-size: 52px; filter: grayscale(0.2); }
.product-type-tag { position: absolute; top: 14px; right: 14px; padding: 5px 12px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.product-content { padding: 20px; flex: 1; display: flex; flex-direction: column; gap: 10px; }
.product-content h3 { font-size: 16px; font-weight: 600; color: #1e293b; margin: 0; line-height: 1.4; }
.product-desc { font-size: 13px; color: #64748b; line-height: 1.6; margin: 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.product-footer { display: flex; align-items: center; justify-content: space-between; margin-top: auto; padding-top: 12px; }
.product-price { display: flex; align-items: center; gap: 6px; color: #475569; }
.product-price strong { font-size: 22px; font-weight: 700; }
.product-price span { font-size: 12px; color: #94a3b8; }
.product-stock { font-size: 12px; color: #10b981; font-weight: 500; padding: 4px 10px; border-radius: 999px; background: #ecfdf5; }
.product-stock.low { color: #f87171; background: #fef2f2; }
.product-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; padding: 0 20px 20px; }
.product-actions .btn-primary { grid-column: 1 / -1; }

/* 订单列表 */
.orders-container { display: flex; flex-direction: column; gap: 28px; }
.order-group { display: flex; flex-direction: column; gap: 12px; }
.group-title { display: flex; align-items: center; gap: 10px; font-size: 15px; font-weight: 600; color: #92400e; padding: 0 4px; }
.group-title.done { color: #059669; }
.group-count { margin-left: auto; padding: 3px 10px; border-radius: 999px; background: #f1f5f9; color: #64748b; font-size: 12px; font-weight: 600; }
.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-item {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.order-item:hover { border-color: #cbd5e1; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04); }
.order-item.pending { border-color: #fef3c7; background: linear-gradient(to right, #fff, #fffbeb); }
.order-item-main { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }
.order-item-info { flex: 1; min-width: 0; }
.order-item-info strong { display: block; font-size: 16px; color: #1e293b; margin-bottom: 8px; font-weight: 600; }
.order-meta { display: flex; gap: 20px; font-size: 13px; color: #94a3b8; }
.order-item-price { text-align: right; white-space: nowrap; }
.order-item-price strong { font-size: 22px; font-weight: 700; color: #475569; }
.order-item-price span { font-size: 12px; color: #94a3b8; }
.order-item-right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.order-price { font-size: 20px; font-weight: 700; color: #1e293b; }
.order-item-actions { display: flex; gap: 10px; margin-top: 14px; justify-content: flex-end; }

.status-tag { display: inline-flex; align-items: center; padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-tag.success { background: #d1fae5; color: #059669; }
.status-tag.pending { background: #fef3c7; color: #92400e; }
.status-tag.danger { background: #fee2e2; color: #dc2626; }
.status-tag.neutral { background: #f1f5f9; color: #64748b; }

.order-timeline { display: flex; align-items: center; gap: 0; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.timeline-step { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #94a3b8; white-space: nowrap; font-weight: 500; }
.timeline-step span { width: 12px; height: 12px; border-radius: 50%; background: #e2e8f0; flex-shrink: 0; transition: all 0.3s; }
.timeline-step.completed { color: #059669; font-weight: 600; }
.timeline-step.completed span { background: #059669; box-shadow: 0 0 0 4px rgba(5, 150, 105, 0.12); }
.timeline-line { flex: 1; height: 2px; background: #e2e8f0; margin: 0 10px; min-width: 24px; }

/* 按钮系统 */
.btn-primary, .btn-secondary, .btn-ghost {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  height: 40px; padding: 0 18px; border-radius: 10px; border: 1px solid transparent;
  font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.25s; white-space: nowrap;
}
.btn-primary { background: #475569; color: #fff; border-color: #475569; }
.btn-primary:hover:not(:disabled) { background: #334155; border-color: #334155; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(71, 85, 105, 0.2); }
.btn-secondary { background: #f1f5f9; color: #475569; border-color: #e2e8f0; }
.btn-secondary:hover:not(:disabled) { background: #e2e8f0; border-color: #cbd5e1; }
.btn-ghost { background: #fff; color: #64748b; border-color: #e2e8f0; }
.btn-ghost:hover:not(:disabled) { border-color: #475569; color: #475569; background: #f8fafc; }
button:disabled { opacity: 0.45; cursor: not-allowed; transform: none !important; box-shadow: none !important; }

/* 状态 */
.loading-state { min-height: 240px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; color: #94a3b8; }
.empty-state { min-height: 280px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; color: #94a3b8; border: 2px dashed #e2e8f0; border-radius: 20px; background: #fff; }
.empty-state p { font-size: 15px; margin: 0; }

/* 分页 */
.pagination { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.pagination button {
  display: inline-flex; align-items: center; height: 40px; padding: 0 16px; border: 1px solid #e2e8f0;
  border-radius: 10px; background: #fff; color: #475569; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.pagination button:hover:not(:disabled) { border-color: #475569; color: #475569; }
.pagination button:disabled { opacity: 0.35; cursor: not-allowed; }
.pagination span { font-size: 14px; color: #94a3b8; }

/* Toast */
.toast {
  position: fixed; right: 28px; bottom: 28px; z-index: 40; display: flex; align-items: center; gap: 10px;
  padding: 14px 22px; border-radius: 12px; color: #fff; background: #10b981;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12); font-size: 14px; font-weight: 500;
  animation: slideUp 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.toast.error { background: #f87171; }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }

.spin { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 响应式 */
@media (max-width: 1024px) {
  .account-cards { grid-template-columns: repeat(2, 1fr); }
  .mall-header { flex-direction: column; height: auto; padding: 14px 20px; gap: 14px; }
  .header-left { flex-direction: column; gap: 14px; width: 100%; }
  .tab-nav { width: 100%; justify-content: center; }
  .header-right { width: 100%; justify-content: space-between; }
  .mall-main { padding: 20px; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .search-box { width: 100%; }
  .filter-tabs { overflow-x: auto; padding-bottom: 4px; }
}

@media (max-width: 640px) {
  .account-cards { grid-template-columns: 1fr; }
  .product-grid { grid-template-columns: 1fr; }
  .order-item-main { flex-direction: column; gap: 12px; }
  .order-item-actions { justify-content: flex-start; }
}
</style>
