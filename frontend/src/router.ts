import { createRouter, createWebHistory } from 'vue-router'
import { state } from './state'
import { resources } from './resources'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('./views/LoginView.vue'), meta: { public: true, title: '登录' } },
    { path: '/', redirect: () => state.session?.role === 1 ? '/dashboard' : '/course' },
    { path: '/dashboard', component: () => import('./views/DashboardView.vue'), meta: { admin: true, title: '运营概览' } },
    ...Object.values(resources).map(resource => ({ path: `/${resource.key}`, component: () => import('./views/ResourceView.vue'), props: { resourceKey: resource.key }, meta: { admin: !['course', 'courseEnrollment'].includes(resource.key), title: resource.title } })),
    { path: '/profile', component: () => import('./views/ProfileView.vue'), meta: { title: '个人资料' } },
    { path: '/:pathMatch(.*)*', component: () => import('./views/NotFoundView.vue'), meta: { title: '页面未找到' } },
  ],
  scrollBehavior: () => ({ top: 0 }),
})
router.beforeEach(to => {
  if (!to.meta.public && !state.session) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.path === '/login' && state.session) return '/'
  if (to.meta.admin && state.session?.role !== 1) return '/course'
  document.title = `${to.meta.title || '工作台'} · 常青健身`
})
