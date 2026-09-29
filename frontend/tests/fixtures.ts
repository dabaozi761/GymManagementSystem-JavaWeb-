import { expect, type Page } from '@playwright/test'

export type TestRow = Record<string, any>
export const adminSession = { id: 1, username: 'admin', name: '林嘉', role: 1, token: 'test-admin-token' }
export const memberSession = { id: 8, username: 'member', name: '陈可', role: 3, token: 'test-member-token' }
export const ordinarySession = { ...memberSession, role: 2 }
export function fixtures(): Record<string, TestRow[]> {
  const members = ['陈可', '李沐', '张晨', '王宁', '林悦', '赵一', '周妍', '许安', '吴桐', '宋青', '郑远', '温然', '何悦'].map((name, index) => ({ id: index + 1, userId: index + 8, memberId: `M202609${String(index + 1).padStart(4, '0')}`, name, phone: `1380013${String(index).padStart(4, '0')}`, gender: index % 2 + 1, joinDate: `2026-09-${String(29 - index).padStart(2, '0')}`, birthday: '1996-04-12', status: index === 11 ? 0 : 1, remark: index ? '' : '希望改善核心力量', createTime: '2026-09-29T09:15:00' }))
  return {
    member: members,
    membershipCard: [
      { id: 1, cardNo: 'C202609290001', memberId: 1, memberName: '陈可', cardType: 5, balance: 1280, status: 1 },
      { id: 2, cardNo: 'C202609290002', memberId: 2, memberName: '李沐', cardType: 4, totalCount: 20, remainCount: 12, status: 1 },
      { id: 3, cardNo: 'C202609290003', memberId: 3, memberName: '张晨', cardType: 3, startDate: '2026-09-01', endDate: '2027-09-01', status: 1 },
      { id: 4, cardNo: 'C202609290004', memberId: 4, memberName: '王宁', cardType: 1, startDate: '2026-08-01', endDate: '2026-09-01', status: 3 },
    ],
    course: ['基础瑜伽', '力量入门', '核心塑形', '活力有氧', '全身力量训练', '拉伸与放松'].map((name, i) => ({ id: i + 1, courseNo: `K202609${i + 1}`, name, type: i % 3 + 1, coachName: ['林教练', '周教练', '许教练'][i % 3], price: 80 + i * 20, capacity: 12, status: 1, remark: '循序渐进，找到自己的训练节奏。' })),
    courseEnrollment: ['基础瑜伽', '力量入门', '核心塑形', '活力有氧', '全身力量训练'].map((courseName, i) => ({ id: i + 1, courseId: i + 1, courseName, memberId: 1, memberName: ['陈可', '李沐', '张晨', '王宁', '林悦'][i], enrollDate: '2026-09-29', status: i === 2 ? 3 : 1, remark: '' })),
    trainingPlan: [{ id: 1, memberId: 1, adminId: 1, title: '四周体能提升计划', goal: '提升心肺与核心力量', startDate: '2026-09-01T00:00:00.000+00:00', endDate: '2026-10-01T00:00:00.000+00:00', status: 1, content: '每周三次训练，循序渐进增加运动强度。', remark: '' }],
    trainingLog: [{ id: 1, trainingPlanId: 1, trainDate: '2026-09-29T00:00:00.000+00:00', durationMinutes: 45, content: '热身 10 分钟，力量训练 30 分钟，拉伸 5 分钟。', feeling: '状态良好', remark: '注意补充水分' }],
    equipment: [{ id: 1, equipmentNo: 'EQ20260001', name: '商用跑步机 A01', type: 1, status: 1, purchaseDate: '2026-03-12', purchasePrice: 12800, remark: '有氧区' }, { id: 2, equipmentNo: 'EQ20260002', name: '坐姿推胸训练器', type: 2, status: 2, purchaseDate: '2026-03-12', purchasePrice: 8600, remark: '待更换把手' }],
    user: [{ id: 8, username: 'member', nickname: '陈可', phone: '13800130000', status: 1, createTime: '2026-09-01T09:00:00' }],
    admin: [{ id: 1, username: 'admin', name: '林嘉', phone: '13900130000', status: 1, createTime: '2026-08-01T09:00:00' }, { id: 2, username: 'staff', name: '许教练', phone: '13900130001', status: 1, createTime: '2026-08-02T09:00:00' }],
  }
}

export async function mockApi(page: Page) {
  const db = fixtures(), requests: { resource: string; action: string; method: string; data: TestRow; token?: string }[] = []
  const control = { fail: '', expire: '', businessError: '', latency: 0 }
  await page.route('**/api/gym-management-system/**', async route => {
    const request = route.request(), url = new URL(request.url()), segments = url.pathname.split('/')
    const resource = segments[3]!, action = segments[5]!, method = request.method()
    const data = { ...Object.fromEntries(url.searchParams), ...(request.postDataJSON() || {}) }
    requests.push({ resource, action, method, data, token: request.headers().token })
    if (control.latency) await new Promise(resolve => setTimeout(resolve, control.latency))
    if (control.expire === resource) return route.fulfill({ status: 401 })
    if (control.fail === resource) return route.fulfill({ status: 502 })
    const success = (value: any = null) => route.fulfill({ json: { code: '0', data: value, message: null } })
    if (control.businessError && action !== 'page') return route.fulfill({ json: { code: 'A000001', message: control.businessError, data: null } })
    if (resource === 'login') return success(data.role === 1 ? adminSession : memberSession)
    if (action === 'page') {
      expect(method).toBe(['user', 'admin'].includes(resource) ? 'GET' : 'POST')
      let records = [...(db[resource] || [])]
      const params = { ...data }; if (resource === 'membershipCard' && params.status === undefined) params.status = 1
      for (const [key, value] of Object.entries(params)) {
        if (['current', 'size'].includes(key) || value === '' || value == null) continue
        records = records.filter(row => ['name', 'title', 'username', 'nickname', 'phone', 'cardNo', 'coachName'].includes(key) || (resource === 'member' && key === 'memberId') ? String(row[key] || '').includes(String(value)) : String(row[key]) === String(value))
      }
      const current = Number(data.current || 1), size = Number(data.size || 10), total = records.length
      return success({ current, size, total, pages: Math.ceil(total / size), records: records.slice((current - 1) * size, current * size) })
    }
    if (action === 'detail') { expect(method).toBe('GET'); return success(db[resource]?.find(row => row.id === Number(data.id))) }
    if (action === 'delete') {
      expect(method).toBe('DELETE')
      db[resource] = db[resource]!.filter(row => resource === 'equipment' ? row.equipmentNo !== data.equipmentNo : row.id !== Number(data.id)); return success()
    }
    if (action === 'save' || action === 'register') {
      expect(method).toBe('POST')
      db[resource]!.unshift({ ...data, id: 100, status: data.status ?? 1, memberId: data.memberId || 'MNEW100', cardNo: 'CNEW100', equipmentNo: 'EQNEW100' }); return success()
    }
    if (action === 'update') {
      const row = db[resource]!.find(row => resource === 'equipment' ? row.equipmentNo === data.equipmentNo : row.id === Number(data.id))
      Object.assign(row!, data); return success()
    }
    if (['updateStatus', 'cancel'].includes(action)) {
      expect(request.postData()).toBeNull(); expect(method).toBe('POST')
      const row = db[resource]!.find(row => row.id === Number(data.id)); row!.status = action === 'cancel' ? 2 : Number(data.status); return success()
    }
    if (action === 'recharge' || action === 'consume') {
      const row = db[resource]!.find(row => row.id === Number(data.id))!
      if (action === 'recharge') row.balance += Number(data.amount)
      else if (row.cardType === 4) row.remainCount -= Number(data.count)
      else row.balance -= Number(data.amount)
      return success()
    }
    if (action === 'enroll') { expect(data.memberId).toBeUndefined(); return success() }
    throw new Error(`Unexpected endpoint ${method} ${url.pathname}`)
  })
  return { db, requests, control }
}
export async function login(page: Page, session = adminSession) {
  await page.addInitScript(value => sessionStorage.setItem('evergreen-gym-session', JSON.stringify(value)), session)
}
