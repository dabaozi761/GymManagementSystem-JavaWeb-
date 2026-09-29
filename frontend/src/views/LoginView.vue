<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { request } from '../api'
import { notify, setSession } from '../state'
import { errorMessage } from '../format'
import type { Session } from '../types'
import AppIcon from '../components/AppIcon.vue'
const router = useRouter(), route = useRoute()
const form = reactive({ username: '', password: '', confirm: '', nickname: '', phone: '', role: 1 })
const registering = ref(false), busy = ref(false), error = ref(''), showPassword = ref(false)
function switchMode() { registering.value = !registering.value; error.value = ''; form.password = ''; form.confirm = '' }
async function submit() {
  if (busy.value) return
  error.value = ''
  if (registering.value && form.password !== form.confirm) { error.value = '两次输入的密码不一致。'; return }
  busy.value = true
  try {
    if (registering.value) {
      await request('user', 'register', { username: form.username.trim(), password: form.password, phone: form.phone.trim(), nickname: form.nickname.trim() })
      registering.value = false; form.role = 2; form.password = ''; form.confirm = ''; notify('注册成功，请使用新账号登录。')
    } else {
      const session = await request<Session>('login', 'login', { username: form.username.trim(), password: form.password, role: form.role })
      if (!session?.token || ![1, 2, 3].includes(session.role)) throw new Error('登录信息不完整，请联系管理员。')
      setSession(session)
      const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/') && !route.query.redirect.startsWith('//') && !route.query.redirect.startsWith('/login') ? route.query.redirect : '/'
      await router.replace(redirect)
    }
  } catch (e) { error.value = errorMessage(e) } finally { busy.value = false }
}
</script>
<template>
  <div class="login-page">
    <section class="login-story"><div class="brand"><span class="brand-mark"><AppIcon name="dumbbell" :size="28" /></span><span><strong>常青健身</strong><small>EVERGREEN GYM</small></span></div><div class="story-content"><span class="eyebrow">MOVE WELL. LIVE WELL.</span><h1>让运动成为习惯，<br />让管理回归简单。</h1><p>从一位会员，到一间充满活力的健身房。<br />把日常交给常青，把更多时间留给热爱。</p><div class="story-illustration" aria-hidden="true"><div class="orbit orbit-one"></div><div class="orbit orbit-two"></div><div class="orbit orbit-three"></div><div class="illustration-center"><AppIcon name="dumbbell" :size="72" /></div><span class="floating-note note-one"><AppIcon name="check" :size="18" />每一点进步，都算数</span><span class="floating-note note-two"><AppIcon name="leaf" :size="20" />保持热爱 · 持续生长</span></div></div><div class="story-footer">专注日常，从容管理 <span>EST. 2026</span></div></section>
    <section class="login-form-side"><div class="login-card"><span class="eyebrow">{{ registering ? 'START YOUR JOURNEY' : 'WELCOME BACK' }}</span><h2>{{ registering ? '开启你的运动日常' : '欢迎回来' }}</h2><p class="login-subtitle">{{ registering ? '创建个人账号，发现适合自己的课程。' : '新的一天，从这里开始。' }}</p>
      <div v-if="!registering" class="role-switch" aria-label="选择登录身份"><button :class="{ active: form.role === 1 }" :aria-pressed="form.role === 1" :disabled="busy" @click="form.role = 1"><AppIcon name="shield" :size="17" />管理员</button><button :class="{ active: form.role === 2 }" :aria-pressed="form.role === 2" :disabled="busy" @click="form.role = 2"><AppIcon name="user" :size="17" />会员 / 用户</button></div>
      <form @submit.prevent="submit"><fieldset :disabled="busy"><div class="form-field"><label for="login-username">用户名</label><input id="login-username" v-model="form.username" required maxlength="50" autocomplete="username" placeholder="请输入用户名" /></div><div v-if="registering" class="form-field"><label for="register-nickname">昵称</label><input id="register-nickname" v-model="form.nickname" maxlength="50" autocomplete="nickname" placeholder="怎么称呼你（选填）" /></div><div v-if="registering" class="form-field"><label for="register-phone">手机号</label><input id="register-phone" v-model="form.phone" type="tel" maxlength="20" autocomplete="tel" placeholder="联系电话（选填）" /></div><div class="form-field"><label for="login-password">密码</label><div class="password-input"><input id="login-password" v-model="form.password" :type="showPassword ? 'text' : 'password'" required :minlength="registering ? 6 : undefined" maxlength="72" :autocomplete="registering ? 'new-password' : 'current-password'" :placeholder="registering ? '请设置至少 6 位密码' : '请输入密码'" /><button type="button" class="icon-button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword"><AppIcon :name="showPassword ? 'eyeOff' : 'eye'" :size="18" /></button></div></div><div v-if="registering" class="form-field"><label for="register-confirm">确认密码</label><input id="register-confirm" v-model="form.confirm" type="password" required maxlength="72" autocomplete="new-password" placeholder="再次输入密码" /></div><p v-if="error" class="error-banner" role="alert"><AppIcon name="alert" :size="18" />{{ error }}</p><button type="submit" class="button primary login-submit" :disabled="busy"><AppIcon v-if="busy" name="loading" class="spin" :size="18" />{{ busy ? '正在处理…' : registering ? '创建账号' : '登 录' }}<AppIcon v-if="!busy" name="arrow" :size="18" /></button></fieldset></form>
      <p class="login-switch">{{ registering ? '已经有账号？' : '还没有个人账号？' }}<button class="text-button" :disabled="busy" @click="switchMode">{{ registering ? '返回登录' : '立即注册' }}</button></p><div class="login-help"><AppIcon name="shield" :size="16" /><span>{{ registering ? '注册后可联系前台关联会员档案。' : '忘记密码或需要帮助，请联系健身房管理员。' }}</span></div>
    </div><footer class="login-footer">常青健身管理系统 · 用心服务每一位会员</footer></section>
  </div>
</template>
