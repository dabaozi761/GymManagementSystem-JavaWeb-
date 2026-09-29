<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getPage } from '../api'
import { state } from '../state'
import { dateText } from '../format'
import type { PageData } from '../types'
import AppIcon from '../components/AppIcon.vue'
const loading = ref(true), failed = ref(false), updated = ref(''), data = ref<Record<string, PageData>>({})
let controller: AbortController | undefined
const metrics = [
  { key: 'members', title: '在籍会员', label: '每一份信任，都认真对待', icon: 'users', unit: '位', path: '/member', tint: 'green' },
  { key: 'courses', title: '正在开设的课程', label: '找到适合每个人的训练', icon: 'calendar', unit: '门', path: '/course', tint: 'blue' },
  { key: 'cards', title: '正常使用的会员卡', label: '持续陪伴每一次到来', icon: 'card', unit: '张', path: '/membershipCard', tint: 'sand' },
  { key: 'equipment', title: '待维护器材', label: '留意维护，让训练更安心', icon: 'dumbbell', unit: '台', path: '/equipment', tint: 'rose' },
]
const categories = [{ key: 'group', title: '团体课', color: '#39765c' }, { key: 'private', title: '私教课', color: '#86a790' }, { key: 'other', title: '其他课程', color: '#d9c9a5' }]
const categoryTotal = computed(() => categories.reduce((sum, item) => sum + (data.value[item.key]?.total || 0), 0))
const categoriesAvailable = computed(() => categories.every(item => data.value[item.key]))
const ring = computed(() => {
  let offset = 0
  return categories.map(item => { const percent = categoryTotal.value ? (data.value[item.key]?.total || 0) / categoryTotal.value * 100 : 0; const result = { ...item, percent, offset }; offset += percent; return result })
})
const shortcuts = [{ title: '登记新会员', subtitle: '开始一段新的陪伴', icon: 'users', path: '/member?new=1' }, { title: '办理会员卡', subtitle: '为坚持多添一份动力', icon: 'card', path: '/membershipCard?new=1' }, { title: '新增训练计划', subtitle: '让目标更加清晰', icon: 'target', path: '/trainingPlan?new=1' }, { title: '登记训练记录', subtitle: '记录今天的小进步', icon: 'activity', path: '/trainingLog?new=1' }]
async function load() {
  controller?.abort(); const active = new AbortController(); controller = active
  loading.value = true; failed.value = false; data.value = {}
  const queries: [string, string, Record<string, any>][] = [
    ['members', 'member', { status: 1, size: 1 }], ['courses', 'course', { status: 1, size: 1 }], ['cards', 'membershipCard', { status: 1, size: 1 }], ['equipment', 'equipment', { status: 2, size: 1 }],
    ['enrollments', 'courseEnrollment', { size: 5 }], ['recentMembers', 'member', { size: 4 }], ['plans', 'trainingPlan', { status: 1, size: 1 }],
    ['group', 'course', { status: 1, type: 1, size: 1 }], ['private', 'course', { status: 1, type: 2, size: 1 }], ['other', 'course', { status: 1, type: 3, size: 1 }],
  ]
  await Promise.allSettled(queries.map(async ([key, resource, params]) => {
    try { const page = await getPage(resource, params, active.signal); if (!active.signal.aborted) data.value[key] = page }
    catch { if (!active.signal.aborted) failed.value = true }
  }))
  if (!active.signal.aborted) { loading.value = false; updated.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }
}
onMounted(load); onBeforeUnmount(() => controller?.abort())
</script>
<template>
  <div class="page-enter dashboard">
    <div class="page-heading"><div><div class="eyebrow">YOUR EVERYDAY, IN BALANCE</div><h1>今天，也一起向前。<span class="heading-dot"></span></h1><p>{{ state.session?.name || state.session?.username }}，欢迎回来。这是健身房此刻的运营情况。</p></div><RouterLink to="/member?new=1" class="button primary"><AppIcon name="plus" :size="18" />登记新会员</RouterLink></div>
    <div v-if="failed" class="error-banner" role="alert"><AppIcon name="alert" :size="19" /><span>部分数据暂时无法加载，未获取的指标显示为「—」。</span><button class="text-button" @click="load">重新加载</button></div>
    <div class="metrics-grid"><RouterLink v-for="metric in metrics" :key="metric.key" :to="metric.path" class="metric-card"><div class="metric-top"><span>{{ metric.title }}</span><span class="metric-icon" :class="`tint-${metric.tint}`"><AppIcon :name="metric.icon" :size="21" /></span></div><div class="metric-value"><span v-if="loading" class="skeleton metric-skeleton"></span><template v-else>{{ data[metric.key]?.total.toLocaleString('zh-CN') ?? '—' }}<small>{{ metric.unit }}</small></template></div><div class="metric-footer"><span>{{ metric.label }}</span><AppIcon name="arrow" :size="16" /></div></RouterLink></div>
    <div class="dashboard-columns"><div class="dashboard-primary"><section class="panel"><div class="section-header"><div><span class="section-kicker">RECENT ACTIVITY</span><h2>最近的课程报名</h2></div><RouterLink class="text-button" to="/courseEnrollment">查看全部<AppIcon name="arrow" :size="16" /></RouterLink></div><div v-if="loading" class="table-loading"><div v-for="n in 5" :key="n" class="skeleton-row"><span class="skeleton"></span><span class="skeleton"></span><span class="skeleton"></span></div></div><div v-else-if="!data.enrollments?.records.length" class="dashboard-empty"><AppIcon name="clipboard" :size="30" /><p>{{ data.enrollments ? '还没有报名记录，新的参与将在这里出现。' : '报名记录暂时未能加载。' }}</p></div><div v-else class="table-scroll"><table class="activity-table"><thead><tr><th>课程 / 会员</th><th>报名日期</th><th>状态</th></tr></thead><tbody><tr v-for="(row, index) in data.enrollments.records" :key="row.id"><td><div class="person-cell"><span class="course-avatar" :class="`tint-${['green', 'sand', 'blue', 'rose'][index % 4]}`"><AppIcon :name="['activity', 'leaf', 'dumbbell', 'target'][index % 4]!" :size="21" /></span><div><strong>{{ row.courseName || '课程已删除' }}</strong><small>{{ row.memberName || '未命名会员' }}</small></div></div></td><td class="muted tabular">{{ dateText(row.enrollDate) }}</td><td><span class="badge" :class="row.status === 1 ? 'badge-green' : 'badge-neutral'"><i></i>{{ { 1: '已报名', 2: '已取消', 3: '已完成' }[row.status as 1 | 2 | 3] || '未知' }}</span></td></tr></tbody></table></div></section>
      <section class="panel shortcuts-panel"><div class="section-header"><div><span class="section-kicker">MAKE IT SIMPLE</span><h2>日常事务，快捷一步</h2></div><AppIcon name="leaf" :size="21" class="muted" /></div><div class="shortcuts-grid"><RouterLink v-for="item in shortcuts" :key="item.path" :to="item.path" class="shortcut"><span class="shortcut-icon"><AppIcon :name="item.icon" :size="22" /></span><div><strong>{{ item.title }}</strong><small>{{ item.subtitle }}</small></div><AppIcon name="right" :size="16" /></RouterLink></div></section>
    </div><div class="dashboard-secondary"><section class="panel course-mix"><div class="section-header"><div><span class="section-kicker">A LITTLE OF EVERYTHING</span><h2>开课类型分布</h2></div><AppIcon name="calendar" :size="20" class="muted" /></div><div class="donut-wrap"><svg viewBox="0 0 160 160" role="img" aria-label="当前开课课程的类型占比"><circle cx="80" cy="80" r="62" fill="none" stroke="#edf0ec" stroke-width="14" /><g v-if="!loading && categoriesAvailable && categoryTotal" transform="rotate(-90 80 80)"><circle v-for="part in ring" :key="part.key" cx="80" cy="80" r="62" fill="none" :stroke="part.color" stroke-width="14" pathLength="100" :stroke-dasharray="`${part.percent} ${100 - part.percent}`" :stroke-dashoffset="-part.offset" /></g></svg><div class="donut-center"><strong>{{ loading || !categoriesAvailable ? '—' : categoryTotal }}</strong><span>开设课程</span></div></div><div class="chart-legend"><div v-for="item in categories" :key="item.key"><span><i :style="{ background: item.color }"></i>{{ item.title }}</span><strong>{{ loading ? '—' : data[item.key]?.total ?? '—' }}<small> 门</small></strong></div></div></section><section class="focus-note"><span class="eyebrow">STEADY PROGRESS</span><h2>每一个小目标，<br />都在慢慢靠近。</h2><p>目前有 <strong>{{ loading ? '—' : data.plans?.total ?? '—' }}</strong> 份训练计划正在进行中，<br />记得为坚持的会员送上一句鼓励。</p><RouterLink to="/trainingPlan">查看训练计划<AppIcon name="arrow" :size="16" /></RouterLink><AppIcon name="leaf" :size="92" class="note-leaf" /></section></div></div>
    <section class="panel recent-members"><div class="section-header"><div><span class="section-kicker">NEW FACES, NEW STORIES</span><h2>最近加入的伙伴</h2></div><RouterLink class="text-button" to="/member">全部会员<AppIcon name="arrow" :size="16" /></RouterLink></div><div v-if="loading" class="members-preview"><span v-for="n in 4" :key="n" class="skeleton member-skeleton"></span></div><div v-else-if="!data.recentMembers?.records.length" class="small-empty">{{ data.recentMembers ? '等待第一位伙伴加入。' : '会员数据暂时未能加载。' }}</div><div v-else class="members-preview"><RouterLink v-for="(member, index) in data.recentMembers.records" :key="member.id" to="/member" class="member-preview"><span class="avatar large" :class="`avatar-${index}`">{{ (member.name || '会').slice(0, 1) }}</span><div><strong>{{ member.name }}</strong><small>{{ dateText(member.joinDate) }} 加入</small></div><AppIcon name="right" :size="15" /></RouterLink></div></section>
    <div class="dashboard-updated"><span><i class="tiny-dot"></i>{{ loading ? '正在同步数据…' : `数据更新于 ${updated}` }}</span><button class="text-button" :disabled="loading" @click="load"><AppIcon name="refresh" :size="14" />刷新概览</button></div>
  </div>
</template>
