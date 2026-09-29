<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { request } from '../api'
import { errorMessage } from '../format'
import { isAdmin, notify, setSession, state } from '../state'
import type { Row } from '../types'
import FormField from '../components/FormField.vue'
import AppIcon from '../components/AppIcon.vue'
const form = reactive<Row>({}), loading = ref(true), busy = ref(false), error = ref('')
const fields = computed(() => isAdmin.value ? [{ key: 'name', label: '姓名', required: true, maxLength: 50 }, { key: 'phone', label: '联系电话', type: 'tel' as const, maxLength: 20 }, { key: 'password', label: '新密码', type: 'password' as const, hint: '留空则不修改密码。', maxLength: 72 }] : [{ key: 'nickname', label: '昵称', maxLength: 50 }, { key: 'phone', label: '联系电话', type: 'tel' as const, maxLength: 20 }])
async function load() {
  error.value = ''; loading.value = true
  try {
    if (isAdmin.value) {
      const data = await request('admin', 'page', { current: 1, size: 20, username: state.session?.username }, 'GET')
      const current = data.records?.find((row: Row) => row.id === state.session?.id)
      if (!current) throw new Error('未找到当前管理员资料，请重新登录。')
      Object.assign(form, current)
    } else Object.assign(form, await request('user', 'detail', { id: state.session?.id }, 'GET'))
  } catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
async function save() {
  if (busy.value || !state.session) return
  busy.value = true; error.value = ''
  try {
    const payload: Row = { id: state.session.id }
    for (const field of fields.value) payload[field.key] = field.type === 'password' ? form[field.key] : form[field.key]?.trim()
    await request(isAdmin.value ? 'admin' : 'user', 'update', payload)
    setSession({ ...state.session, name: isAdmin.value ? form.name.trim() : form.nickname?.trim() || state.session.username })
    form.password = ''; notify('个人资料已保存。')
  } catch (e) { error.value = errorMessage(e) } finally { busy.value = false }
}
onMounted(load)
</script>
<template><div class="page-enter"><div class="page-heading"><div><div class="eyebrow">A LITTLE ABOUT YOU</div><h1>个人资料<span class="heading-dot"></span></h1><p>保持资料准确，让每一次联系更加顺畅。</p></div></div><section class="panel profile-panel"><div class="profile-intro"><span class="avatar profile-avatar">{{ (state.session?.name || state.session?.username || '你').slice(0, 1) }}</span><div><h2>{{ state.session?.username }}</h2><p>{{ isAdmin ? '管理员' : state.session?.role === 3 ? '会员账号' : '普通用户' }}<span class="profile-id">ID {{ state.session?.id }}</span></p></div><AppIcon name="leaf" :size="32" /></div><div v-if="loading" class="loading-inline"><AppIcon name="loading" class="spin" />正在加载资料…</div><form v-else @submit.prevent="save"><fieldset class="profile-form form-grid" :disabled="busy || !form.id"><FormField v-for="field in fields" :key="field.key" v-model="form[field.key]" :field="field" prefix="profile" /><p v-if="!isAdmin" class="field-hint field-wide">如需修改登录密码或会员档案，请联系健身房管理员。</p></fieldset><div v-if="error" class="error-banner profile-error" role="alert">{{ error }}<button type="button" class="text-button" @click="load">重新加载</button></div><footer class="modal-footer"><button class="button primary" :disabled="busy || !form.id"><AppIcon v-if="busy" name="loading" class="spin" :size="17" />{{ busy ? '保存中…' : '保存修改' }}</button></footer></form></section></div></template>
