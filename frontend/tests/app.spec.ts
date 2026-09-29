import { test, expect } from '@playwright/test'
import { adminSession, login, memberSession, mockApi, ordinarySession } from './fixtures'
import { dateText } from '../src/format'

test('login, role selection, session and logout', async ({ page }, testInfo) => {
  const api = await mockApi(page)
  await page.goto('/member')
  await expect(page).toHaveURL(/\/login/)
  await page.getByLabel('用户名', { exact: true }).fill('admin')
  await page.getByLabel('密码', { exact: true }).fill('secret123')
  await page.getByRole('button', { name: '登 录', exact: true }).click()
  await expect(page.getByRole('heading', { name: '会员管理', exact: true })).toBeVisible()
  expect(api.requests.find(item => item.resource === 'login')?.data).toEqual({ username: 'admin', password: 'secret123', role: 1 })
  expect(api.requests.find(item => item.resource === 'member')?.token).toBe(adminSession.token)
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: '展开导航' }).click()
  await page.getByRole('button', { name: '退出登录' }).click()
  await expect(page).toHaveURL(/\/login/)
  expect(await page.evaluate(() => sessionStorage.getItem('evergreen-gym-session'))).toBeNull()
})

test('register validates password confirmation and submits only user fields', async ({ page }) => {
  const api = await mockApi(page)
  await page.goto('/login')
  await page.getByRole('button', { name: '立即注册' }).click()
  await page.getByLabel('用户名', { exact: true }).fill('new_user')
  await page.getByLabel('密码', { exact: true }).fill('new_pass')
  await page.getByLabel('确认密码').fill('wrongpass')
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('不一致')
  await page.getByLabel('确认密码').fill('new_pass')
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await expect(page.getByRole('button', { name: '会员 / 用户' })).toHaveAttribute('aria-pressed', 'true')
  const registered = api.requests.find(item => item.action === 'register')!
  expect(registered.data.username).toBe('new_user')
  expect(registered.data.role).toBeUndefined()
  expect(registered.data.confirm).toBeUndefined()
})

test('dashboard has real totals and renders at the target viewport', async ({ page }, testInfo) => {
  await mockApi(page); await login(page); await page.goto('/dashboard')
  await expect(page.getByText('数据更新于', { exact: false })).toBeVisible()
  await expect(page.locator('.metric-value').first()).toContainText('12')
  await expect(page.locator('.metric-value').nth(1)).toContainText('6')
  await expect(page.locator('.metric-value').nth(3)).toContainText('1')
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy()
  await page.screenshot({ path: testInfo.outputPath('dashboard.png'), fullPage: true })
})

test('member search, pagination and reset use backend pagination', async ({ page }) => {
  const api = await mockApi(page); await login(page); await page.goto('/member')
  await expect(page.getByText('共 13 条，第 1 / 2 页')).toBeVisible()
  await page.getByRole('button', { name: '下一页', exact: true }).click()
  await expect(page.getByText('共 13 条，第 2 / 2 页')).toBeVisible()
  expect(api.requests.filter(item => item.resource === 'member').at(-1)?.data.current).toBe(2)
  await page.getByRole('textbox', { name: '搜索会员姓名' }).fill('陈可')
  await page.getByRole('button', { name: '查询', exact: true }).click()
  await expect(page.getByText('共 1 条，第 1 / 1 页')).toBeVisible()
  await page.getByRole('button', { name: '清空搜索' }).click()
  await expect(page.getByText('共 13 条，第 1 / 2 页')).toBeVisible()
})

test('create, edit and delete a member through forms', async ({ page }) => {
  const api = await mockApi(page); await login(page); await page.goto('/member')
  await page.getByRole('button', { name: '新增会员', exact: true }).click()
  await page.getByLabel('姓名', { exact: false }).last().fill('测试伙伴')
  await page.getByLabel('联系电话', { exact: false }).fill('13800001111')
  await page.getByRole('button', { name: '保存信息' }).click()
  const row = page.getByRole('row').filter({ hasText: '测试伙伴' })
  await expect(row).toBeVisible()
  const saved = api.requests.find(item => item.resource === 'member' && item.action === 'save')!
  expect(saved.data.status).toBe(1); expect(saved.data.id).toBeUndefined()
  await row.getByRole('button', { name: '编辑', exact: true }).click()
  await page.getByLabel('备注').fill('优先晚间训练')
  await page.getByRole('button', { name: '保存信息' }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await row.locator('summary').click()
  await row.getByRole('button', { name: '删除会员', exact: true }).click()
  await page.getByRole('button', { name: '确认删除会员' }).click()
  await expect(row).toHaveCount(0)
})

test('all remaining editable resources submit actual DTO fields', async ({ page }) => {
  const api = await mockApi(page); await login(page)
  const cases = [
    { key: 'course', title: '课程', data: { name: '基础课程', price: '100' } },
    { key: 'equipment', title: '器材', data: { name: '测试哑铃', purchasePrice: '200' } },
    { key: 'trainingPlan', title: '训练计划', data: { title: '测试体能计划' } },
    { key: 'trainingLog', title: '训练记录', data: { content: '今天训练四十分钟' } },
    { key: 'user', title: '用户', data: { username: 'test_user', password: 'pass1234' } },
    { key: 'admin', title: '管理员', data: { username: 'test_admin', password: 'pass1234', name: '新管理员' } },
  ]
  for (const item of cases) {
    await page.goto(`/${item.key}`)
    await page.getByRole('button', { name: `新增${item.title}`, exact: true }).click()
    for (const [key, value] of Object.entries(item.data)) await page.locator(`#edit-${key}`).fill(value!)
    if (item.key === 'trainingPlan') await page.locator('#edit-memberId').selectOption('1')
    if (item.key === 'trainingLog') await page.locator('#edit-trainingPlanId').selectOption('1')
    await page.getByRole('button', { name: '保存信息' }).click()
    await expect(page.getByRole('dialog')).toHaveCount(0)
    const saved = api.requests.findLast(entry => entry.resource === item.key && ['save', 'register'].includes(entry.action))!
    expect(saved).toBeTruthy()
    if (item.key === 'trainingPlan') expect(saved.data.adminId).toBe(1)
    if (item.key === 'equipment') expect(saved.data.equipmentNo).toBeUndefined()
    if (item.key === 'trainingLog') expect(saved.data.trainDate).toMatch(/^\d{4}-\d{2}-\d{2}T00:00:00\+08:00$/)
  }
})

test('business dates stay on the same day after UTC serialization', () => {
  expect(dateText('2026-09-28T16:00:00.000+00:00')).toBe('2026-09-29')
  expect(dateText('2026-09-29')).toBe('2026-09-29')
  expect(dateText('2026-09-29T00:00:00+08:00')).toBe('2026-09-29')
  expect(dateText(null)).toBe('—')
})

test('card forms switch types and validate dates and remaining count', async ({ page }) => {
  const api = await mockApi(page); await login(page); await page.goto('/membershipCard')
  await page.getByRole('button', { name: '新增会员卡' }).click()
  await page.locator('#edit-memberId').selectOption('1')
  await page.locator('#edit-startDate').fill('2026-09-29')
  await page.locator('#edit-endDate').fill('2026-08-01')
  await page.getByRole('button', { name: '保存信息' }).click()
  await expect(page.getByRole('alert')).toContainText('结束日期不能早于开始日期')
  await page.locator('#edit-cardType').selectOption('4')
  await expect(page.locator('#edit-startDate')).toHaveCount(0)
  await page.locator('#edit-totalCount').fill('10'); await page.locator('#edit-remainCount').fill('11')
  await page.getByRole('button', { name: '保存信息' }).click()
  await expect(page.getByRole('alert')).toContainText('剩余次数不能大于总次数')
  await page.locator('#edit-remainCount').fill('10')
  await page.getByRole('button', { name: '保存信息' }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  const payload = api.requests.find(item => item.resource === 'membershipCard' && item.action === 'save')!.data
  expect(payload).toEqual({ memberId: 1, cardType: 4, totalCount: 10, remainCount: 10, status: 1 })
})

test('card recharge and count consumption use separate payloads', async ({ page }) => {
  const api = await mockApi(page); await login(page); await page.goto('/membershipCard')
  const stored = page.getByRole('row').filter({ hasText: 'C202609290001' })
  await stored.locator('summary').click(); await stored.getByRole('button', { name: '储值充值' }).click()
  await page.getByLabel('充值金额（元）').fill('50')
  await page.getByRole('button', { name: '确认储值卡充值' }).click()
  await expect(stored).toContainText('1,330.00')
  const counted = page.getByRole('row').filter({ hasText: 'C202609290002' })
  await counted.locator('summary').click(); await counted.getByRole('button', { name: '次卡核销' }).click()
  await page.getByLabel('核销次数').fill('2')
  await page.getByRole('button', { name: '确认次卡核销' }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  expect(api.requests.find(item => item.action === 'consume')?.data).toEqual({ id: 2, count: 2 })
  await page.getByRole('button', { name: /筛选/ }).click()
  await page.locator('#filter-status').selectOption('3')
  await page.getByRole('button', { name: '应用筛选' }).click()
  await expect(page.getByRole('row').filter({ hasText: 'C202609290004' })).toBeVisible()
  await expect(page.locator('#filter-status option[value=""]')).toHaveCount(0)
})

test('member role can enroll and cancel, cannot access administrative routes', async ({ page }, testInfo) => {
  const api = await mockApi(page); await login(page, memberSession); await page.goto('/admin')
  await expect(page).toHaveURL('/course')
  await expect(page.getByRole('button', { name: '新增课程' })).toHaveCount(0)
  await page.getByRole('button', { name: '报名', exact: true }).first().click()
  await page.getByLabel('报名备注').fill('初次参加')
  await page.getByRole('button', { name: '确认报名课程' }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  expect(api.requests.find(item => item.action === 'enroll')?.data).toEqual({ courseId: 1, remark: '初次参加' })
  await page.goto('/courseEnrollment')
  await page.getByRole('button', { name: '取消报名', exact: true }).first().click()
  await page.getByRole('button', { name: '确认取消报名' }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await page.getByRole('button', { name: /筛选/ }).click()
  await expect(page.locator('#filter-memberId')).toHaveCount(0)
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: '展开导航' }).click()
  await expect(page.getByRole('link', { name: '用户账号', exact: true })).toHaveCount(0)
})

test('ordinary user sees explanation and disabled booking', async ({ page }) => {
  await mockApi(page); await login(page, ordinarySession); await page.goto('/course')
  await expect(page.getByText(/请先联系前台办理会员/)).toBeVisible()
  await expect(page.getByRole('button', { name: '报名', exact: true }).first()).toBeDisabled()
})

test('API failures are retryable and never display success', async ({ page }) => {
  const api = await mockApi(page); api.control.fail = 'member'; await login(page); await page.goto('/member')
  await expect(page.getByRole('heading', { name: '数据暂时未能加载' })).toBeVisible()
  api.control.fail = ''; await page.getByRole('button', { name: '重新加载', exact: true }).click()
  await expect(page.getByText('共 13 条，第 1 / 2 页')).toBeVisible()
  api.control.businessError = '联系电话已被其他会员使用'
  await page.getByRole('button', { name: '编辑', exact: true }).first().click()
  await page.getByRole('button', { name: '保存信息' }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await expect(page.getByRole('alert')).toContainText('联系电话已被其他会员使用')
})

test('expired session redirects to login and removes token', async ({ page }) => {
  const api = await mockApi(page); api.control.expire = 'member'; await login(page); await page.goto('/member')
  await expect(page).toHaveURL(/\/login/)
  expect(await page.evaluate(() => sessionStorage.getItem('evergreen-gym-session'))).toBeNull()
})

test('empty state and dashboard partial failures remain truthful', async ({ page }) => {
  const api = await mockApi(page); api.db.member = []; await login(page); await page.goto('/member')
  await expect(page.getByRole('heading', { name: '还没有会员' })).toBeVisible()
  api.control.fail = 'equipment'; await page.goto('/dashboard')
  await expect(page.getByRole('alert')).toContainText('部分数据暂时无法加载')
  await expect(page.locator('.metric-value').nth(3)).toContainText('—')
  await expect(page.locator('.metric-value').first()).toContainText('0')
})

test('all pages, dialogs and login fit desktop and mobile without console errors', async ({ page }, testInfo) => {
  await mockApi(page); await login(page)
  const errors: string[] = []; page.on('pageerror', error => errors.push(error.message))
  for (const path of ['member', 'membershipCard', 'course', 'courseEnrollment', 'trainingPlan', 'trainingLog', 'equipment', 'user', 'admin', 'profile']) {
    await page.goto(`/${path}`)
    await expect(page.locator('.skeleton-row,.loading-inline')).toHaveCount(0)
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), `overflow on ${path}`).toBeTruthy()
    await page.screenshot({ path: testInfo.outputPath(`${path}.png`), fullPage: true })
  }
  await page.goto('/member?new=1')
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.screenshot({ path: testInfo.outputPath('member-form.png'), fullPage: true })
  const box = await page.getByRole('dialog').boundingBox()
  expect(box!.x).toBeGreaterThanOrEqual(0)
  expect(box!.x + box!.width).toBeLessThanOrEqual(page.viewportSize()!.width)
  await page.keyboard.press('Escape'); await expect(page.getByRole('dialog')).toHaveCount(0)
  expect(errors).toEqual([])
})

test('login layout and password visibility are accessible', async ({ page }, testInfo) => {
  await mockApi(page); await page.goto('/login')
  await page.getByLabel('密码', { exact: true }).fill('secret123')
  await page.getByRole('button', { name: '显示密码' }).click()
  await expect(page.getByLabel('密码', { exact: true })).toHaveAttribute('type', 'text')
  await page.getByRole('button', { name: '隐藏密码' }).click()
  await page.getByLabel('密码', { exact: true }).fill('')
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy()
  await page.screenshot({ path: testInfo.outputPath('login.png'), fullPage: true })
})

test('profile saves current identity and unknown page offers a return path', async ({ page }) => {
  const api = await mockApi(page); await login(page, memberSession); await page.goto('/profile')
  await page.getByLabel('昵称').fill('陈可同学')
  await page.getByRole('button', { name: '保存修改' }).click()
  await expect(page.getByRole('status')).toContainText('个人资料已保存')
  expect(api.requests.find(item => item.resource === 'user' && item.action === 'update')?.data.id).toBe(memberSession.id)
  await page.goto('/not-a-real-page')
  await expect(page.getByRole('heading', { name: '这个页面暂时找不到了' })).toBeVisible()
  await page.getByRole('link', { name: '返回首页' }).click()
  await expect(page).toHaveURL('/course')
})
