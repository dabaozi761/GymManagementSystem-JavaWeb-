import { setSession, state } from './state'
import type { PageData, Row } from './types'

const base = '/api/gym-management-system'
export function clean(data: Row): Row {
  return Object.fromEntries(Object.entries(data).filter(([, value]) => value !== '' && value !== undefined && value !== null))
}
export async function request<T = any>(resource: string, action: string, data: Row = {}, method = 'POST', query = false, signal?: AbortSignal): Promise<T> {
  const controller = new AbortController()
  const abort = () => controller.abort()
  if (signal?.aborted) controller.abort()
  signal?.addEventListener('abort', abort, { once: true })
  const timer = setTimeout(() => controller.abort(), 15000)
  const params = clean(data)
  const dateFields: Record<string, string[]> = { equipment: ['purchaseDate'], trainingPlan: ['startDate', 'endDate'], trainingLog: ['trainDate'] }
  for (const key of dateFields[resource] || []) {
    if (typeof params[key] === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(params[key])) params[key] = `${params[key]}T00:00:00+08:00`
  }
  const inUrl = method === 'GET' || method === 'DELETE' || query
  const search = inUrl ? new URLSearchParams(Object.entries(params).map(([key, value]) => [key, String(value)])).toString() : ''
  try {
    const response = await fetch(`${base}/${resource}/v1/${action}${search ? `?${search}` : ''}`, {
      method, headers: { 'Content-Type': 'application/json', ...(state.session?.token ? { token: state.session.token } : {}) },
      body: inUrl ? undefined : JSON.stringify(params), signal: controller.signal,
    })
    if (response.status === 401 && resource !== 'login') {
      setSession(null); window.dispatchEvent(new Event('gym:unauthorized'))
      throw new Error('登录已过期，请重新登录。')
    }
    if (response.status === 403) throw new Error('当前账号没有执行此操作的权限。')
    if (!response.ok) throw new Error(response.status === 502 || response.status === 504 ? '暂时无法连接服务，请稍后重试。' : `请求失败（${response.status}），请稍后重试。`)
    let result: { code: string; message?: string; data: T }
    try { result = await response.json() } catch { throw new Error('服务返回了无法识别的数据，请联系管理员检查连接。') }
    if (String(result.code) !== '0') throw new Error(result.message || '操作未成功，请检查输入后重试。')
    return result.data
  } catch (error) {
    if (signal?.aborted) throw new DOMException('请求已取消', 'AbortError')
    if (controller.signal.aborted) throw new Error('请求超时，请检查网络后重试。')
    if (error instanceof TypeError) throw new Error('网络连接失败，请检查服务是否已启动。')
    throw error
  } finally {
    clearTimeout(timer); signal?.removeEventListener('abort', abort)
  }
}
export async function getPage(resource: string, params: Row = {}, signal?: AbortSignal): Promise<PageData> {
  const data = await request<PageData>(resource, 'page', { current: 1, size: 10, ...params }, ['admin', 'user'].includes(resource) ? 'GET' : 'POST', false, signal)
  return { records: data?.records || [], total: Number(data?.total || 0), current: Number(data?.current || 1), size: Number(data?.size || params.size || 10), pages: Number(data?.pages || 0) }
}
