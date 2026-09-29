import { computed, reactive } from 'vue'
import type { Session } from './types'

const storageKey = 'evergreen-gym-session'
function readSession(): Session | null {
  try {
    const saved = JSON.parse(sessionStorage.getItem(storageKey) || 'null')
    return saved?.token && [1, 2, 3].includes(saved.role) && saved.id ? saved : null
  } catch { return null }
}
export const state = reactive({ session: readSession(), toast: '', toastType: 'success' })
export const isAdmin = computed(() => state.session?.role === 1)
export function setSession(session: Session | null) {
  state.session = session
  if (session) sessionStorage.setItem(storageKey, JSON.stringify(session))
  else sessionStorage.removeItem(storageKey)
}
let toastTimer: ReturnType<typeof setTimeout>
export function notify(message: string, type = 'success') {
  state.toast = message; state.toastType = type
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { state.toast = '' }, 4500)
}
