<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from './components/AppIcon.vue'
import { isAdmin, notify, setSession, state } from './state'
import { navigation } from './resources'
const route = useRoute(), router = useRouter(), menuOpen = ref(false)
const nav = computed(() => isAdmin.value ? navigation : [{ label: '会员服务', items: [{ key: 'course', title: '发现课程', icon: 'calendar' }, { key: 'courseEnrollment', title: '我的报名', icon: 'clipboard' }, { key: 'profile', title: '个人资料', icon: 'user' }] }])
const name = computed(() => state.session?.name || state.session?.username || '你好')
const roleName = computed(() => isAdmin.value ? '管理员' : state.session?.role === 3 ? '会员' : '普通用户')
function logout() { setSession(null); void router.replace('/login') }
function expired() { notify('登录已过期，请重新登录。', 'error'); void router.replace({ path: '/login', query: { redirect: route.fullPath } }) }
function escape(event: KeyboardEvent) { if (event.key === 'Escape') menuOpen.value = false }
window.addEventListener('gym:unauthorized', expired)
window.addEventListener('keydown', escape)
onBeforeUnmount(() => { window.removeEventListener('gym:unauthorized', expired); window.removeEventListener('keydown', escape) })
watch(() => route.fullPath, () => { menuOpen.value = false })
</script>
<template>
  <div v-if="state.session && route.path !== '/login'" class="app-layout">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <button v-if="menuOpen" class="nav-backdrop" aria-label="关闭导航" @click="menuOpen = false"></button>
    <aside id="sidebar-navigation" class="sidebar" :class="{ open: menuOpen }">
      <RouterLink to="/" class="brand"><span class="brand-mark"><AppIcon name="dumbbell" :size="27" /></span><span><strong>常青健身</strong><small>EVERGREEN GYM</small></span></RouterLink>
      <div class="workspace-label"><span class="tiny-dot"></span>{{ isAdmin ? '健身房管理工作台' : '让运动成为日常' }}</div>
      <nav aria-label="主导航"><div v-for="group in nav" :key="group.label" class="nav-group"><p>{{ group.label }}</p><RouterLink v-for="item in group.items" :key="item.key" :to="`/${item.key}`" class="nav-link"><AppIcon :name="item.icon" /><span>{{ item.title }}</span><span class="nav-active-dot"></span></RouterLink></div></nav>
      <div class="sidebar-bottom"><div class="sidebar-note"><AppIcon name="leaf" :size="18" /><div>把健康，变成日常。<small>专注训练，也用心服务。</small></div></div><button class="logout-button" @click="logout"><AppIcon name="logout" :size="18" />退出登录</button></div>
    </aside>
    <div class="main-shell">
      <header class="topbar"><div class="breadcrumb"><button class="icon-button mobile-menu" aria-label="展开导航" :aria-expanded="menuOpen" aria-controls="sidebar-navigation" @click="menuOpen = !menuOpen"><AppIcon name="menu" /></button><span class="breadcrumb-root">{{ isAdmin ? '工作台' : '会员中心' }}</span><span class="breadcrumb-separator">/</span><span>{{ route.meta.title }}</span></div><div class="topbar-right"><span class="today-label"><AppIcon name="calendar" :size="16" />{{ new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' }) }}</span><RouterLink to="/profile" class="profile-link" aria-label="个人资料"><span class="avatar small">{{ name.slice(0, 1) }}</span><span><strong>{{ name }}</strong><small>{{ roleName }}</small></span><AppIcon name="down" :size="15" /></RouterLink></div></header>
      <main id="main-content" class="main-content"><RouterView :key="route.path" /></main>
      <footer class="app-footer"><span>常青健身 · 让每一份坚持有所收获</span><span>专注日常，从容管理</span></footer>
    </div>
  </div>
  <RouterView v-else />
  <Transition name="toast"><div v-if="state.toast" class="toast-message" :class="{ 'toast-error': state.toastType === 'error' }" :role="state.toastType === 'error' ? 'alert' : 'status'"><AppIcon :name="state.toastType === 'error' ? 'alert' : 'check'" :size="19" /><span>{{ state.toast }}</span><button class="icon-button" aria-label="关闭提示" @click="state.toast = ''"><AppIcon name="close" :size="16" /></button></div></Transition>
</template>
