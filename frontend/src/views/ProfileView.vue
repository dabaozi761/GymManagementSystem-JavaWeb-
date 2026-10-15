<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { request } from '../api'
import { errorMessage } from '../format'
import { isAdmin, notify, setSession, state } from '../state'
import type { Field, Row } from '../types'
import FormField from '../components/FormField.vue'
import AppIcon from '../components/AppIcon.vue'
const form = reactive<Row>({}), loading = ref(true), busy = ref(false), error = ref('')
const fields = computed<Field[]>(() => isAdmin.value ? [{ key: 'name', label: '姓名', required: true, maxLength: 50 }, { key: 'phone', label: '联系电话', type: 'tel', maxLength: 20 }] : [{ key: 'nickname', label: '昵称', maxLength: 50 }, { key: 'phone', label: '联系电话', type: 'tel', maxLength: 20 }])
const passwords = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordBusy = ref(false), passwordError = ref('')
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
    for (const field of fields.value) payload[field.key] = form[field.key]?.trim()
    await request(isAdmin.value ? 'admin' : 'user', 'update', payload)
    setSession({ ...state.session, name: isAdmin.value ? form.name.trim() : form.nickname?.trim() || state.session.username })
    notify('个人资料已保存。')
  } catch (e) { error.value = errorMessage(e) } finally { busy.value = false }
}
async function changePassword() {
  if (passwordBusy.value || !state.session) return
  passwordError.value = ''
  if (!passwords.oldPassword.trim()) { passwordError.value = '请输入原密码'; return }
  if (!passwords.newPassword.trim() || passwords.newPassword.length < 6 || passwords.newPassword.length > 72) { passwordError.value = '新密码长度应为6至72位'; return }
  if (passwords.newPassword !== passwords.confirmPassword) { passwordError.value = '两次输入的新密码不一致'; return }
  if (passwords.newPassword === passwords.oldPassword) { passwordError.value = '新密码不能与原密码相同'; return }
  passwordBusy.value = true
  try {
    await request('account', 'changePassword', { ...passwords })
    Object.assign(passwords, { oldPassword: '', newPassword: '', confirmPassword: '' })
    notify('密码已修改，下次登录请使用新密码。')
  } catch (e) { passwordError.value = errorMessage(e) } finally { passwordBusy.value = false }
}
onMounted(load)
</script>
<template>
  <div class="page-enter">
    <div class="page-heading"><div><div class="eyebrow">A LITTLE ABOUT YOU</div><h1>个人资料<span class="heading-dot"></span></h1><p>保持资料准确，让每一次联系更加顺畅。</p></div></div>
    <section class="panel profile-panel">
      <div class="profile-intro"><span class="avatar profile-avatar">{{ (state.session?.name || state.session?.username || '你').slice(0, 1) }}</span><div><h2>{{ state.session?.username }}</h2><p>{{ isAdmin ? '管理员' : state.session?.role === 3 ? '会员账号' : '普通用户' }}<span class="profile-id">ID {{ state.session?.id }}</span></p></div><AppIcon name="leaf" :size="32" /></div>
      <div v-if="loading" class="loading-inline"><AppIcon name="loading" class="spin" />正在加载资料…</div>
      <template v-else>
        <form @submit.prevent="save">
          <fieldset class="profile-form form-grid" :disabled="busy || !form.id">
            <FormField v-for="field in fields" :key="field.key" v-model="form[field.key]" :field="field" prefix="profile" />
          </fieldset>
          <div v-if="error" class="error-banner profile-error" role="alert">{{ error }}<button type="button" class="text-button" @click="load">重新加载</button></div>
          <footer class="modal-footer"><button class="button primary" :disabled="busy || !form.id"><AppIcon v-if="busy" name="loading" class="spin" :size="17" />{{ busy ? '保存中…' : '保存修改' }}</button></footer>
        </form>
        <form class="password-section" aria-labelledby="password-heading" @submit.prevent="changePassword">
          <fieldset class="profile-form form-grid" :disabled="passwordBusy">
            <div class="field-wide"><h2 id="password-heading">修改密码</h2><p id="password-hint" class="field-hint">验证原密码后即可设置新密码，长度为6至72位。</p></div>
            <div class="form-field field-wide"><label for="profile-oldPassword">原密码<span class="required"> *</span></label><input id="profile-oldPassword" v-model="passwords.oldPassword" type="password" autocomplete="current-password" placeholder="请输入原密码" required /></div>
            <div class="form-field"><label for="profile-newPassword">新密码<span class="required"> *</span></label><input id="profile-newPassword" v-model="passwords.newPassword" type="password" autocomplete="new-password" placeholder="请输入新密码" maxlength="72" aria-describedby="password-hint" required /></div>
            <div class="form-field"><label for="profile-confirmPassword">确认新密码<span class="required"> *</span></label><input id="profile-confirmPassword" v-model="passwords.confirmPassword" type="password" autocomplete="new-password" placeholder="请再次输入新密码" maxlength="72" required /></div>
          </fieldset>
          <div v-if="passwordError" class="error-banner profile-error" role="alert">{{ passwordError }}</div>
          <footer class="modal-footer"><button class="button primary" :disabled="passwordBusy"><AppIcon v-if="passwordBusy" name="loading" class="spin" :size="17" />{{ passwordBusy ? '修改中…' : '修改密码' }}</button></footer>
        </form>
      </template>
    </section>
  </div>
</template>
<style scoped>
.password-section{border-top:1px solid var(--line)}
.password-section h2{font-size:16px;color:#586f45}
</style>
