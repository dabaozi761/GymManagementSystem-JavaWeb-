<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPage, request } from '../api'
import { dateText, display, errorMessage, today } from '../format'
import { resources } from '../resources'
import { isAdmin, notify, state } from '../state'
import type { Column, Field, Row } from '../types'
import AppIcon from '../components/AppIcon.vue'
import AppModal from '../components/AppModal.vue'
import FormField from '../components/FormField.vue'

const props = defineProps<{ resourceKey: string }>()
const config = computed(() => resources[props.resourceKey]!)
const route = useRoute(), router = useRouter()
const rows = ref<Row[]>([]), total = ref(0), current = ref(1), size = ref(10), loading = ref(true), error = ref('')
const filters = reactive<Row>({ ...config.value.defaultFilters }), applied = ref<Row>({ ...config.value.defaultFilters }), search = ref('')
const modal = ref(''), editing = ref(false), selected = ref<Row>({}), form = reactive<Row>({}), formError = ref(''), saving = ref(false)
const filterOpen = ref(false)
let controller: AbortController | undefined
const title = computed(() => !isAdmin.value && props.resourceKey === 'course' ? '发现课程' : !isAdmin.value && props.resourceKey === 'courseEnrollment' ? '我的报名' : config.value.title)
const visibleFilters = computed(() => config.value.filters.filter(field => isAdmin.value || field.key !== 'memberId'))
const activeFilterCount = computed(() => Object.entries(applied.value).filter(([, value]) => value !== '' && value !== undefined).length)
const fields = computed(() => config.value.fields.filter(field => (!field.createOnly || !editing.value) && (!field.editOnly || editing.value) && (!field.visible || field.visible(form))))
const pages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const canWrite = computed(() => isAdmin.value && !config.value.readOnly)
const detailColumns = computed(() => {
  const columns: Column[] = [...config.value.columns]
  const existing = new Set(columns.map(column => column.key))
  for (const field of config.value.fields) {
    if (!existing.has(field.key) && field.type !== 'password') {
      columns.push({ key: field.key, label: field.label, options: field.options, type: field.type === 'date' ? 'date' : undefined }); existing.add(field.key)
    }
  }
  for (const [key, label] of [['id', '记录 ID'], ['courseNo', '课程编号'], ['createTime', '创建时间'], ['updateTime', '更新时间'], ['remark', '备注']]) {
    if (selected.value[key!] != null && !existing.has(key!)) columns.push({ key: key!, label: label!, type: key!.endsWith('Time') ? 'datetime' : undefined })
  }
  return columns
})
const canConsume = computed(() => selected.value.cardType === 4 || selected.value.cardType === 5)
const actionTitle = computed(() => ({ delete: `删除${config.value.singular}`, status: '调整状态', cancel: '取消报名', complete: '完成课程', enroll: '报名课程', recharge: '储值卡充值', consume: selected.value.cardType === 4 ? '次卡核销' : '储值卡消费' }[modal.value] || '确认操作'))
const actionFields = computed<Field[]>(() => {
  if (modal.value === 'status') return [{ key: 'status', label: '调整为', type: 'select', options: config.value.statuses, required: true }]
  if (modal.value === 'recharge' || (modal.value === 'consume' && selected.value.cardType === 5)) return [{ key: 'amount', label: modal.value === 'recharge' ? '充值金额（元）' : '消费金额（元）', type: 'number', required: true, min: 0.01, max: modal.value === 'consume' ? Number(selected.value.balance) : 99999999.99, step: '0.01' }]
  if (modal.value === 'consume' && selected.value.cardType === 4) return [{ key: 'count', label: '核销次数', type: 'number', required: true, min: 1, max: Number(selected.value.remainCount) }]
  if (modal.value === 'enroll') return [{ key: 'remark', label: '报名备注', type: 'textarea', maxLength: 255, placeholder: '有需要教练留意的事项吗？（选填）' }]
  return []
})

async function load() {
  controller?.abort(); const active = new AbortController(); controller = active
  loading.value = true; error.value = ''
  try {
    const page = await getPage(props.resourceKey, { ...applied.value, current: current.value, size: size.value }, active.signal)
    if (page.total > 0 && current.value > Math.ceil(page.total / size.value)) { current.value = Math.ceil(page.total / size.value); await load(); return }
    rows.value = page.records; total.value = page.total
  } catch (e) { if (!active.signal.aborted) error.value = errorMessage(e) }
  finally { if (!active.signal.aborted) loading.value = false }
}
function applyFilters() {
  current.value = 1; applied.value = { ...filters, ...(config.value.searchKey ? { [config.value.searchKey]: search.value.trim() } : {}) }; void load()
}
function resetFilters() {
  Object.keys(filters).forEach(key => delete filters[key]); Object.assign(filters, config.value.defaultFilters); search.value = ''; applyFilters()
}
function pageTo(page: number) { current.value = page; void load() }
function clearForm() { Object.keys(form).forEach(key => delete form[key]); formError.value = '' }
function openForm(row?: Row) {
  clearForm(); editing.value = !!row; selected.value = row || {}
  config.value.fields.forEach(field => { if (field.default !== undefined) form[field.key] = field.default })
  if (props.resourceKey === 'member') form.joinDate = today()
  if (props.resourceKey === 'trainingLog') form.trainDate = today()
  if (props.resourceKey === 'trainingPlan') form.adminId = state.session?.id
  if (props.resourceKey === 'membershipCard') { form.status = 1; form.startDate = today() }
  if (row) Object.assign(form, row)
  config.value.fields.filter(field => field.type === 'date').forEach(field => { if (form[field.key]) form[field.key] = dateText(form[field.key]) })
  modal.value = 'form'
}
async function showDetails(row: Row) {
  selected.value = { ...row }; formError.value = ''; modal.value = 'detail'
  if (config.value.detail) {
    saving.value = true
    try { selected.value = await request(props.resourceKey, 'detail', { id: row.id }, 'GET') }
    catch (e) { formError.value = errorMessage(e) } finally { saving.value = false }
  }
}
function openAction(action: string, row: Row) {
  document.querySelectorAll<HTMLDetailsElement>('.row-menu[open]').forEach(menu => { menu.open = false })
  clearForm(); selected.value = row; form.status = row.status; modal.value = action
  if (action === 'consume') form.count = 1
}
function validate(): string {
  const usesDateRange = props.resourceKey !== 'membershipCard' || [1, 2, 3].includes(Number(form.cardType))
  if (usesDateRange && form.startDate && form.endDate && form.endDate < form.startDate) return '结束日期不能早于开始日期。'
  if (props.resourceKey === 'member' && form.birthday && form.birthday > today()) return '出生日期不能晚于今天。'
  if (props.resourceKey === 'membershipCard' && Number(form.cardType) === 4 && Number(form.remainCount) > Number(form.totalCount)) return '剩余次数不能大于总次数。'
  for (const field of fields.value) {
    if (field.required && (form[field.key] === undefined || form[field.key] === null || String(form[field.key]).trim() === '')) return `请填写${field.label}。`
  }
  return ''
}
async function save() {
  if (saving.value) return
  formError.value = validate(); if (formError.value) return
  saving.value = true
  try {
    const payload: Row = {}
    fields.value.forEach(field => { const value = form[field.key]; payload[field.key] = typeof value === 'string' && field.type !== 'password' ? value.trim() : value })
    if (editing.value) {
      if (props.resourceKey === 'equipment') payload.equipmentNo = selected.value.equipmentNo
      else payload.id = selected.value.id
    }
    if (props.resourceKey === 'membershipCard' && !editing.value) payload.status = 1
    await request(props.resourceKey, editing.value ? 'update' : config.value.createAction || 'save', payload)
    modal.value = ''; notify(`${config.value.singular}${editing.value ? '已更新' : '已新增'}`); await load()
  } catch (e) { formError.value = errorMessage(e) } finally { saving.value = false }
}
async function confirmAction() {
  if (saving.value) return
  saving.value = true; formError.value = ''
  try {
    const key = props.resourceKey, id = selected.value.id
    if (modal.value === 'delete') await request(key, 'delete', key === 'equipment' ? { equipmentNo: selected.value.equipmentNo } : { id }, 'DELETE')
    if (modal.value === 'status') await request(key, 'updateStatus', { id, status: form.status }, 'POST', true)
    if (modal.value === 'cancel') await request('courseEnrollment', 'cancel', { id }, 'POST', true)
    if (modal.value === 'complete') await request('courseEnrollment', 'updateStatus', { id, status: 3 }, 'POST', true)
    if (modal.value === 'enroll') await request('courseEnrollment', 'enroll', { courseId: id, remark: form.remark })
    if (modal.value === 'recharge') await request(key, 'recharge', { id, amount: form.amount })
    if (modal.value === 'consume' && canConsume.value) await request(key, 'consume', selected.value.cardType === 4 ? { id, count: form.count } : { id, amount: form.amount })
    notify(modal.value === 'enroll' ? '报名成功，可在「我的报名」中查看。' : '操作成功，数据已更新。')
    modal.value = ''; await load()
  } catch (e) { formError.value = errorMessage(e) } finally { saving.value = false }
}
function selfAdmin(row: Row) { return props.resourceKey === 'admin' && row.id === state.session?.id }
function badgeClass(value: any) { return Number(value) === 1 ? 'badge-green' : Number(value) === 2 ? 'badge-amber' : 'badge-neutral' }
onMounted(() => { void load(); if (route.query.new === '1' && canWrite.value) { openForm(); void router.replace({ path: route.path }) } })
onBeforeUnmount(() => controller?.abort())
</script>
<template>
  <div class="page-enter">
    <div class="page-heading"><div><div class="eyebrow">{{ isAdmin ? 'DAILY OPERATIONS' : 'YOUR FITNESS JOURNEY' }}</div><h1>{{ title }}<span class="heading-dot"></span></h1><p>{{ config.subtitle }}</p></div><button v-if="canWrite" class="button primary" @click="openForm()"><AppIcon name="plus" :size="18" />新增{{ config.singular }}</button></div>
    <div v-if="!isAdmin && resourceKey === 'course'" class="info-banner"><AppIcon name="leaf" /><span>{{ state.session?.role === 3 ? '找到适合自己的节奏。选择课程，开始下一次训练。' : '欢迎探索课程。请先联系前台办理会员并关联当前账号，再报名参加。' }}</span></div>
    <section class="panel resource-panel">
      <form class="filter-toolbar" @submit.prevent="applyFilters"><div v-if="config.searchKey" class="search-input"><AppIcon name="search" :size="19" /><input v-model="search" :aria-label="config.searchPlaceholder" :placeholder="config.searchPlaceholder" maxlength="100" /><button v-if="search" type="button" class="icon-button" aria-label="清空搜索" @click="search = ''; applyFilters()"><AppIcon name="close" :size="15" /></button></div><div class="toolbar-actions"><button v-if="config.searchKey" type="submit" class="button secondary">查询</button><button type="button" class="button secondary" :class="{ 'is-selected': filterOpen }" :aria-expanded="filterOpen" @click="filterOpen = !filterOpen"><AppIcon name="filter" :size="16" />筛选<span v-if="activeFilterCount" class="filter-count">{{ activeFilterCount }}</span></button><button type="button" class="icon-button bordered" aria-label="刷新列表" :disabled="loading" @click="load"><AppIcon name="refresh" :size="18" :class="{ spin: loading }" /></button></div></form>
      <form v-if="filterOpen" class="expanded-filters" @submit.prevent="applyFilters"><FormField v-for="field in visibleFilters" :key="field.key" v-model="filters[field.key]" :field="field" prefix="filter" filter :no-empty="resourceKey === 'membershipCard' && field.key === 'status'" /><div class="filter-controls"><button class="button primary" type="submit">应用筛选</button><button class="text-button" type="button" @click="resetFilters">重置</button></div></form>
      <div class="list-caption"><div><strong>{{ resourceKey === 'courseEnrollment' ? '报名记录' : `${config.singular}列表` }}</strong><span class="count-pill">{{ loading ? '…' : error ? '—' : total }}</span></div><span>{{ resourceKey === 'membershipCard' ? `当前状态：${config.statuses?.find(item => item.value === applied.status)?.label || '正常'}` : '用心管理每一份记录' }}</span></div>
      <div v-if="error" class="empty-state" role="alert"><span class="empty-icon error"><AppIcon name="alert" :size="30" /></span><h3>数据暂时未能加载</h3><p>{{ error }}</p><button class="button secondary" @click="load">重新加载</button></div>
      <div v-else-if="loading" class="table-loading" role="status" aria-label="正在加载列表"><div v-for="n in 6" :key="n" class="skeleton-row"><span class="skeleton"></span><span class="skeleton"></span><span class="skeleton"></span><span class="skeleton"></span></div></div>
      <div v-else-if="!rows.length" class="empty-state"><span class="empty-icon"><AppIcon :name="config.icon" :size="32" /></span><h3>{{ activeFilterCount ? '没有找到符合条件的记录' : `还没有${config.singular}` }}</h3><p>{{ activeFilterCount ? '换一个关键词或调整筛选条件试试。' : `新的${config.singular}会显示在这里。` }}</p><button v-if="activeFilterCount" class="button secondary" @click="resetFilters">重置筛选</button><button v-else-if="canWrite" class="button secondary" @click="openForm()">新增{{ config.singular }}</button><RouterLink v-else-if="!isAdmin && resourceKey === 'courseEnrollment'" to="/course" class="button secondary">去发现课程</RouterLink></div>
      <div v-else class="table-scroll" tabindex="0" :aria-label="`${title}数据表，可横向滚动`"><table><thead><tr><th v-for="column in config.columns" :key="column.key" scope="col">{{ column.label }}</th><th class="actions-cell" scope="col">操作</th></tr></thead><tbody><tr v-for="row in rows" :key="row.id || row.equipmentNo"><td v-for="column in config.columns" :key="column.key"><span v-if="column.type === 'badge'" class="badge" :class="badgeClass(row[column.key])"><i></i>{{ display(row, column) }}</span><div v-else-if="column.type === 'person'" class="person-cell"><span class="avatar" :class="`avatar-${Number(row.id || 0) % 4}`">{{ String(row[column.key] || '—').slice(0, 1) }}</span><button class="cell-link" @click="showDetails(row)">{{ display(row, column) }}</button></div><span v-else class="cell-text" :class="{ 'tabular': column.type === 'money' || column.type === 'date', 'muted': column.key.endsWith('No') || column.key === 'memberId' }" :title="display(row, column)">{{ column.key === 'capacity' && row.capacity == null ? '不限人数' : display(row, column) }}</span></td><td class="actions-cell"><div class="row-actions"><button class="text-button" @click="showDetails(row)">详情</button><template v-if="canWrite"><button class="text-button" @click="openForm(row)">编辑</button><details class="row-menu"><summary aria-label="更多操作">•••</summary><div class="row-menu-panel"><button v-if="config.statusAction && !selfAdmin(row)" @click="openAction('status', row)">调整状态</button><button v-if="resourceKey === 'membershipCard' && row.cardType === 5 && row.status === 1" @click="openAction('recharge', row)">储值充值</button><button v-if="resourceKey === 'membershipCard' && [4, 5].includes(row.cardType) && row.status === 1" @click="openAction('consume', row)">{{ row.cardType === 4 ? '次卡核销' : '储值消费' }}</button><button v-if="!selfAdmin(row)" class="danger-text" @click="openAction('delete', row)">删除{{ config.singular }}</button><span v-else class="menu-hint">当前登录账号</span></div></details></template><template v-if="resourceKey === 'courseEnrollment' && row.status === 1"><button v-if="isAdmin" class="text-button" @click="openAction('complete', row)">完成</button><button class="text-button muted" @click="openAction('cancel', row)">取消报名</button></template><button v-if="resourceKey === 'course' && !isAdmin && row.status === 1" class="button primary small-button" :disabled="state.session?.role !== 3" @click="openAction('enroll', row)">报名</button></div></td></tr></tbody></table></div>
      <footer v-if="!error" class="pagination"><span>{{ total ? `共 ${total} 条，第 ${current} / ${pages} 页` : '共 0 条记录' }}</span><div class="pagination-controls"><label class="page-size-label">每页<select v-model="size" aria-label="每页条数" :disabled="loading" @change="pageTo(1)"><option :value="10">10 条</option><option :value="20">20 条</option><option :value="50">50 条</option></select></label><button class="icon-button bordered" :disabled="current <= 1 || loading" aria-label="上一页" @click="pageTo(current - 1)"><AppIcon name="left" :size="17" /></button><span class="page-number">{{ current }}</span><button class="icon-button bordered" :disabled="current >= pages || loading" aria-label="下一页" @click="pageTo(current + 1)"><AppIcon name="right" :size="17" /></button></div></footer>
    </section>
    <div class="page-tip"><AppIcon name="shield" :size="15" /><span>{{ isAdmin ? '操作将实时保存，请核对信息后提交。' : '报名状态以系统记录为准，如需帮助请联系前台。' }}</span></div>
    <AppModal v-if="modal === 'form'" :title="`${editing ? '编辑' : '新增'}${config.singular}`" description="请填写以下信息，带 * 的项目为必填项。" :busy="saving" @close="modal = ''"><form @submit.prevent="save"><fieldset :disabled="saving" class="modal-body form-grid"><FormField v-for="field in fields" :key="field.key" v-model="form[field.key]" :field="field" prefix="edit" /><div v-if="formError" class="error-banner field-wide" role="alert"><AppIcon name="alert" :size="18" />{{ formError }}</div></fieldset><footer class="modal-footer"><button type="button" class="button secondary" :disabled="saving" @click="modal = ''">取消</button><button class="button primary" type="submit" :disabled="saving"><AppIcon v-if="saving" name="loading" class="spin" :size="17" />{{ saving ? '保存中…' : '保存信息' }}</button></footer></form></AppModal>
    <AppModal v-else-if="modal === 'detail'" :title="`${config.singular}详情`" :busy="saving" @close="modal = ''"><div class="modal-body"><div v-if="formError" class="error-banner" role="alert">{{ formError }}</div><div v-if="saving" class="loading-inline"><AppIcon name="loading" class="spin" />正在获取最新信息…</div><dl class="detail-grid"><div v-for="column in detailColumns" :key="column.key" :class="{ 'field-wide': ['content', 'remark', 'feeling'].includes(column.key) }"><dt>{{ column.label }}</dt><dd>{{ display(selected, column) }}</dd></div></dl></div><footer class="modal-footer"><button class="button secondary" :disabled="saving" @click="modal = ''">关闭</button><button v-if="canWrite" class="button primary" :disabled="saving" @click="openForm(selected)">编辑信息</button></footer></AppModal>
    <AppModal v-else-if="modal" :title="actionTitle" :busy="saving" small @close="modal = ''"><form @submit.prevent="confirmAction"><fieldset class="modal-body" :disabled="saving"><div class="action-subject"><span class="avatar"><AppIcon :name="config.icon" /></span><div><strong>{{ selected.name || selected.title || selected.courseName || selected.memberName || selected.username || `记录 #${selected.id}` }}</strong><small>{{ selected.cardNo || selected.memberId || selected.courseNo || `#${selected.id}` }}</small></div></div><p v-if="modal === 'delete'" class="confirmation-copy">确定删除这条{{ config.singular }}吗？删除后将不再出现在列表中，请确认该记录已不再需要。</p><p v-if="modal === 'cancel'" class="confirmation-copy">确认取消这次课程报名吗？取消后如需参加，需要重新报名。</p><p v-if="modal === 'complete'" class="confirmation-copy">请确认会员已完成本课程，再将报名标记为已完成。</p><p v-if="modal === 'consume'" class="confirmation-copy">{{ selected.cardType === 4 ? `当前剩余 ${selected.remainCount} 次` : `当前余额 ¥${Number(selected.balance).toFixed(2)}` }}。请核对本次消费。</p><FormField v-for="field in actionFields" :key="field.key" v-model="form[field.key]" :field="field" prefix="action" /><div v-if="formError" class="error-banner" role="alert"><AppIcon name="alert" :size="18" />{{ formError }}</div></fieldset><footer class="modal-footer"><button type="button" class="button secondary" :disabled="saving" @click="modal = ''">返回</button><button type="submit" class="button" :class="modal === 'delete' ? 'danger' : 'primary'" :disabled="saving"><AppIcon v-if="saving" name="loading" class="spin" :size="17" />{{ saving ? '处理中…' : `确认${actionTitle}` }}</button></footer></form></AppModal>
  </div>
</template>
