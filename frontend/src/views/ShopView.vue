<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getPage, request } from '../api'
import { errorMessage, today } from '../format'
import { productCategories } from '../resources'
import { isAdmin, notify, state } from '../state'
import type { Row } from '../types'
import AppIcon from '../components/AppIcon.vue'
import AppModal from '../components/AppModal.vue'

const products = ref<Row[]>([]), cards = ref<Row[]>([]), total = ref(0), current = ref(1)
const name = ref(''), category = ref(''), loading = ref(false), error = ref('')
const selected = ref<Row | null>(null), cardId = ref(''), quantity = ref(1), rentalDays = ref(1)
const saving = ref(false), formError = ref(''), requestNo = ref('')
let controller: AbortController | undefined
const rental = computed(() => Number(selected.value?.category) <= 2)
const amount = computed(() => Math.round(Number(selected.value?.price || 0) * 100) * quantity.value * (rental.value ? rentalDays.value : 1) / 100)
const chosenCard = computed(() => cards.value.find(card => String(card.id) === cardId.value))
const categoryLabel = (value: number) => productCategories.find(item => item.value === value)?.label || '商品'
const money = (value: unknown) => Number(value || 0).toFixed(2)

async function load(page = 1) {
  controller?.abort(); const active = new AbortController(); controller = active
  current.value = page; loading.value = true; error.value = ''
  try {
    const result = await getPage('product', { name: name.value.trim(), category: category.value, current: page, size: 12 }, active.signal)
    products.value = result.records; total.value = result.total
  } catch (e) { if (!active.signal.aborted) error.value = errorMessage(e) }
  finally { if (!active.signal.aborted) loading.value = false }
}

async function open(product: Row) {
  selected.value = product; quantity.value = 1; rentalDays.value = 1; formError.value = ''; cards.value = []; cardId.value = ''
  requestNo.value = crypto.randomUUID()
  saving.value = true
  try {
    const result = await getPage('membershipCard', { status: 1, size: 100 })
    const localToday = today()
    cards.value = result.records.filter(card => Number(card.cardType) >= 1 && Number(card.cardType) <= 4 && (Number(card.cardType) === 4 || (card.startDate <= localToday && card.endDate >= localToday)))
    cardId.value = cards.value[0] ? String(cards.value[0].id) : ''
    if (!cards.value.length) formError.value = '当前没有可用会员卡，请联系前台办卡或检查有效期。'
  } catch (e) { formError.value = errorMessage(e) }
  finally { saving.value = false }
}

async function checkout() {
  if (saving.value || !selected.value) return
  formError.value = ''
  if (!chosenCard.value) { formError.value = '请选择可用会员卡。'; return }
  if (Number(chosenCard.value.balance || 0) < amount.value) { formError.value = '会员卡余额不足，请联系前台充值。'; return }
  saving.value = true
  try {
    await request('shopOrder', 'checkout', { productId: selected.value.id, cardId: Number(cardId.value), quantity: quantity.value, rentalDays: rental.value ? rentalDays.value : 0, requestNo: requestNo.value })
    selected.value = null; notify('结算成功，可在「我的购物与租用」查看订单。'); await load(current.value)
  } catch (e) { formError.value = errorMessage(e) }
  finally { saving.value = false }
}
onMounted(() => load()); onBeforeUnmount(() => controller?.abort())
</script>

<template>
  <div class="page-enter">
    <div class="page-heading"><div><div class="eyebrow">EVERGREEN SHOP</div><h1>健身商城<span class="heading-dot"></span></h1><p>会员卡余额购买商品，储物柜与充电宝按天租用。</p></div><RouterLink :to="isAdmin ? '/shopOrder' : '/member/orders'" class="button secondary">查看订单</RouterLink></div>
    <div class="info-banner"><AppIcon name="card" /><span>租用品按件 / 天预付，租期 1–30 天。归还请到前台确认，预付租金不退还。</span></div>
    <section class="panel">
      <form class="filter-toolbar" @submit.prevent="load(1)"><div class="search-input"><AppIcon name="search" /><input v-model="name" aria-label="搜索商品名称" placeholder="搜索商品名称" maxlength="100" /></div><label class="shop-category">商品分类<select v-model="category" aria-label="商品分类" @change="load(1)"><option value="">全部分类</option><option v-for="item in productCategories" :key="item.value" :value="item.value">{{ item.label }}</option></select></label><button class="button primary" type="submit">查询</button></form>
      <div v-if="error" class="empty-state" role="alert"><p>{{ error }}</p><button class="button secondary" @click="load(current)">重新加载</button></div>
      <div v-else-if="loading" class="empty-state" role="status">正在加载商品…</div>
      <div v-else-if="!products.length" class="empty-state"><AppIcon name="shop" :size="32" /><h3>暂无符合条件的商品</h3></div>
      <div v-else class="shop-grid"><article v-for="product in products" :key="product.id" class="shop-product"><span class="shop-product-icon"><AppIcon :name="product.category <= 2 ? 'card' : 'shop'" :size="32" /></span><span class="badge badge-neutral">{{ categoryLabel(product.category) }}</span><h2>{{ product.name }}</h2><p>{{ product.remark || '前台领取，方便您的每次训练。' }}</p><div class="shop-price">¥{{ money(product.price) }}<small>{{ product.category <= 2 ? '/ 件 / 天' : '/ 件' }}</small></div><div class="shop-stock">可{{ product.category <= 2 ? '租' : '售' }}库存：{{ product.stock }}</div><button class="button primary" :disabled="product.stock <= 0 || isAdmin || state.session?.role !== 3" @click="open(product)">{{ product.stock <= 0 ? '暂无库存' : isAdmin ? '请用会员账号结算' : state.session?.role !== 3 ? '请先关联会员' : product.category <= 2 ? '立即租用' : '立即购买' }}</button></article></div>
      <footer class="pagination"><span>共 {{ total }} 件商品</span><div class="pagination-controls"><button class="button secondary" :disabled="current <= 1 || loading" @click="load(current - 1)">上一页</button><span>{{ current }} / {{ Math.max(1, Math.ceil(total / 12)) }}</span><button class="button secondary" :disabled="current * 12 >= total || loading" @click="load(current + 1)">下一页</button></div></footer>
    </section>
    <AppModal v-if="selected" :title="rental ? '租用结算' : '购买结算'" :busy="saving" @close="selected = null"><form @submit.prevent="checkout"><fieldset class="modal-body shop-checkout" :disabled="saving"><h2>{{ selected.name }}</h2><label>付款会员卡<select v-model="cardId" required aria-label="付款会员卡"><option value="" disabled>请选择会员卡</option><option v-for="card in cards" :key="card.id" :value="String(card.id)">{{ card.cardNo }} · 余额 ¥{{ money(card.balance) }}</option></select></label><label>数量<input v-model.number="quantity" type="number" required min="1" :max="Math.min(100, selected.stock)" step="1" aria-label="数量" /></label><label v-if="rental">租用天数<input v-model.number="rentalDays" type="number" required min="1" max="30" step="1" aria-label="租用天数" /></label><p>单价：¥{{ money(selected.price) }}{{ rental ? ' / 件 / 天' : ' / 件' }}</p><strong>合计：¥{{ money(amount) }}</strong><p>从所选会员卡余额扣除。{{ rental ? '物品归还后请前台确认。' : '购买后请到前台领取。' }}</p><div v-if="formError" class="error-banner" role="alert">{{ formError }}</div></fieldset><footer class="modal-footer"><button type="button" class="button secondary" :disabled="saving" @click="selected = null">取消</button><button type="submit" class="button primary" :disabled="saving || !cards.length">{{ saving ? '处理中…' : '确认支付' }}</button></footer></form></AppModal>
  </div>
</template>

<style scoped>
.shop-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; padding: 24px; }
.shop-product { display: flex; flex-direction: column; align-items: flex-start; padding: 22px; border: 1px solid var(--border, #e5e9e3); border-radius: 14px; gap: 12px; }
.shop-product h2 { font-size: 19px; margin: 0; }.shop-product p { color: #68786e; font-size: 14px; line-height: 1.7; flex: 1; }
.shop-product-icon { padding: 18px; background: #edf3ed; color: #39765c; border-radius: 12px; }.shop-price { font-size: 24px; color: #39765c; font-weight: 600; }.shop-price small { font-size: 12px; color: #68786e; margin-left: 8px; }
.shop-stock { font-size: 13px; color: #68786e; }.shop-product .button { width: 100%; }.shop-category { display: flex; gap: 8px; align-items: center; white-space: nowrap; }
.shop-checkout { display: grid; gap: 16px; }.shop-checkout label { display: grid; gap: 8px; }.shop-checkout input, .shop-checkout select, .shop-category select { border: 1px solid #dce3dc; border-radius: 8px; padding: 10px; background: white; min-width: 0; max-width: 100%; }
@media (max-width: 1100px) { .shop-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 600px) { .shop-grid { grid-template-columns: 1fr; padding: 16px; }.shop-category { width: 100%; }.filter-toolbar { flex-wrap: wrap; }.shop-category select { flex: 1; } }
</style>
