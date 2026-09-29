import type { Field, Option, Resource } from './types'

export const options = (labels: string[], start = 1): Option[] => labels.map((label, index) => ({ label, value: index + start }))
const normal = [{ label: '正常', value: 1 }, { label: '已禁用', value: 0 }]
const memberStatus = [{ label: '在籍', value: 1 }, { label: '已退会', value: 0 }]
const courseStatus = [{ label: '开课中', value: 1 }, { label: '已停课', value: 0 }]
const cardTypes = options(['月卡', '季卡', '年卡', '次卡', '储值卡'])
const cardStatus = options(['正常', '已冻结', '已过期', '已注销'])
const equipmentTypes = options(['有氧器材', '力量器材', '自由重量', '其他'])
const equipmentStatus = options(['可用', '维修中', '已报废'])
const planStatus = options(['进行中', '已完成', '已取消'])
const enrollStatus = options(['已报名', '已取消', '已完成'])
const courseTypes = options(['团体课', '私教课', '其他'])
const statusField = (statuses: Option[]): Field => ({ key: 'status', label: '状态', type: 'select', options: statuses, default: 1, required: true })
const nameField: Field = { key: 'name', label: '姓名', required: true, maxLength: 50 }
const phoneField: Field = { key: 'phone', label: '联系电话', type: 'tel', maxLength: 20 }
const remark: Field = { key: 'remark', label: '备注', type: 'textarea', maxLength: 255, placeholder: '补充需要留意的信息（选填）' }
const relation = (key: string, label: string, resource: string, searchKey = 'name', labelKey = 'name', required = true): Field => ({ key, label, type: 'relation', resource, searchKey, labelKey, required })
const accountFields: Field[] = [{ key: 'username', label: '用户名', required: true, createOnly: true, maxLength: 50 }, { key: 'password', label: '密码', type: 'password', required: true, createOnly: true, maxLength: 72 }]

export const resources: Record<string, Resource> = {
  member: {
    key: 'member', title: '会员管理', singular: '会员', icon: 'users', subtitle: '每一份坚持，都值得被认真记录。', searchKey: 'name', searchPlaceholder: '搜索会员姓名', detail: true, statusAction: true, statuses: memberStatus,
    filters: [{ key: 'phone', label: '手机号', type: 'tel', placeholder: '联系电话' }, { key: 'memberId', label: '会员编号', placeholder: '会员编号' }, statusField(memberStatus)],
    columns: [{ key: 'name', label: '会员姓名', type: 'person' }, { key: 'memberId', label: '会员编号' }, { key: 'phone', label: '联系电话' }, { key: 'gender', label: '性别', options: options(['男', '女']) }, { key: 'joinDate', label: '入会日期', type: 'date' }, { key: 'status', label: '会员状态', type: 'badge', options: memberStatus }],
    fields: [nameField, { ...phoneField, required: true }, { key: 'gender', label: '性别', type: 'select', options: options(['男', '女']) }, { key: 'birthday', label: '出生日期', type: 'date' }, { key: 'joinDate', label: '入会日期', type: 'date', required: true }, statusField(memberStatus), { ...relation('userId', '关联登录账号', 'user', 'username', 'username', false), hint: '关联后，该账号可使用会员身份登录并报名课程。' }, remark],
  },
  membershipCard: {
    key: 'membershipCard', title: '会员卡管理', singular: '会员卡', icon: 'card', subtitle: '办卡、续费与消费，一处轻松管理。', searchKey: 'cardNo', searchPlaceholder: '搜索会员卡号', statuses: cardStatus, statusAction: true, defaultFilters: { status: 1 },
    filters: [{ key: 'cardType', label: '卡类型', type: 'select', options: cardTypes }, { ...statusField(cardStatus), hint: '后端按单一状态查询，默认展示正常卡。' }],
    columns: [{ key: 'cardNo', label: '会员卡号' }, { key: 'memberName', label: '所属会员', type: 'person' }, { key: 'cardType', label: '卡类型', options: cardTypes }, { key: 'balance', label: '余额', type: 'money' }, { key: 'remainCount', label: '剩余次数' }, { key: 'endDate', label: '到期日期', type: 'date' }, { key: 'status', label: '状态', type: 'badge', options: cardStatus }],
    fields: [relation('memberId', '所属会员', 'member'), { key: 'cardType', label: '卡类型', type: 'select', options: cardTypes, required: true, default: 1 }, { key: 'startDate', label: '生效日期', type: 'date', required: true, visible: r => [1, 2, 3].includes(Number(r.cardType)) }, { key: 'endDate', label: '到期日期', type: 'date', required: true, visible: r => [1, 2, 3].includes(Number(r.cardType)) }, { key: 'totalCount', label: '总次数', type: 'number', min: 1, max: 100000, required: true, visible: r => Number(r.cardType) === 4 }, { key: 'remainCount', label: '剩余次数', type: 'number', min: 0, max: 100000, required: true, visible: r => Number(r.cardType) === 4 }, { key: 'balance', label: '初始余额（元）', type: 'number', min: 0, max: 99999999.99, step: '0.01', default: 0, required: true, visible: r => Number(r.cardType) === 5 }, { ...statusField(cardStatus), editOnly: true }],
  },
  course: {
    key: 'course', title: '课程管理', singular: '课程', icon: 'calendar', subtitle: '让每一次训练，都有合适的选择。', searchKey: 'name', searchPlaceholder: '搜索课程名称', detail: true, statuses: courseStatus, statusAction: true,
    filters: [{ key: 'type', label: '课程类型', type: 'select', options: courseTypes }, { key: 'coachName', label: '教练', placeholder: '教练姓名' }, statusField(courseStatus)],
    columns: [{ key: 'name', label: '课程名称', type: 'person' }, { key: 'type', label: '课程类型', options: courseTypes }, { key: 'coachName', label: '授课教练' }, { key: 'price', label: '课程价格', type: 'money' }, { key: 'capacity', label: '人数上限' }, { key: 'status', label: '课程状态', type: 'badge', options: courseStatus }],
    fields: [{ ...nameField, label: '课程名称', maxLength: 100 }, { key: 'type', label: '课程类型', type: 'select', options: courseTypes, required: true, default: 1 }, { key: 'coachName', label: '教练姓名', maxLength: 50 }, { key: 'price', label: '价格（元）', type: 'number', step: '0.01', min: 0, max: 99999999.99, default: 0 }, { key: 'capacity', label: '人数上限', type: 'number', min: 1, max: 100000, hint: '留空表示不限人数。' }, statusField(courseStatus), remark],
  },
  courseEnrollment: {
    key: 'courseEnrollment', title: '课程报名', singular: '报名记录', icon: 'clipboard', subtitle: '连接课程与会员，安排好每一次参与。', readOnly: true, detail: true, statuses: enrollStatus,
    filters: [relation('courseId', '课程', 'course', 'name', 'name', false), relation('memberId', '会员', 'member', 'name', 'name', false), statusField(enrollStatus)],
    columns: [{ key: 'courseName', label: '课程名称', type: 'person' }, { key: 'memberName', label: '报名会员' }, { key: 'enrollDate', label: '报名日期', type: 'date' }, { key: 'status', label: '报名状态', type: 'badge', options: enrollStatus }, { key: 'remark', label: '备注' }], fields: [],
  },
  trainingPlan: {
    key: 'trainingPlan', title: '训练计划', singular: '训练计划', icon: 'target', subtitle: '把目标变成计划，把计划变成进步。', searchKey: 'title', searchPlaceholder: '搜索计划名称', statuses: planStatus,
    filters: [relation('memberId', '会员', 'member', 'name', 'name', false), statusField(planStatus)],
    columns: [{ key: 'title', label: '计划名称', type: 'person' }, { key: 'memberId', label: '会员 ID' }, { key: 'goal', label: '训练目标' }, { key: 'startDate', label: '开始日期', type: 'date' }, { key: 'endDate', label: '结束日期', type: 'date' }, { key: 'status', label: '计划状态', type: 'badge', options: planStatus }],
    fields: [{ key: 'title', label: '计划名称', required: true, maxLength: 100 }, { key: 'goal', label: '训练目标', maxLength: 50, placeholder: '例如：提升体能、增肌、减脂' }, relation('memberId', '所属会员', 'member'), relation('adminId', '负责管理员', 'admin'), { key: 'startDate', label: '开始日期', type: 'date' }, { key: 'endDate', label: '结束日期', type: 'date' }, statusField(planStatus), { key: 'content', label: '计划内容', type: 'textarea', maxLength: 10000 }, remark],
  },
  trainingLog: {
    key: 'trainingLog', title: '训练记录', singular: '训练记录', icon: 'activity', subtitle: '记录每一滴汗水，见证每一点改变。',
    filters: [relation('trainingPlanId', '训练计划', 'trainingPlan', 'title', 'title', false), { key: 'trainDate', label: '训练日期', type: 'date' }],
    columns: [{ key: 'trainDate', label: '训练日期', type: 'date' }, { key: 'trainingPlanId', label: '计划 ID' }, { key: 'durationMinutes', label: '时长（分钟）' }, { key: 'content', label: '训练内容' }, { key: 'feeling', label: '身体感受' }, { key: 'remark', label: '备注' }],
    fields: [relation('trainingPlanId', '训练计划', 'trainingPlan', 'title', 'title'), { key: 'trainDate', label: '训练日期', type: 'date', required: true }, { key: 'durationMinutes', label: '训练时长（分钟）', type: 'number', min: 1, max: 1440 }, { key: 'feeling', label: '身体感受', maxLength: 255 }, { key: 'content', label: '训练内容', type: 'textarea', required: true, maxLength: 10000 }, remark],
  },
  equipment: {
    key: 'equipment', title: '器材管理', singular: '器材', icon: 'dumbbell', subtitle: '保持器材的好状态，守护训练的每一天。', searchKey: 'name', searchPlaceholder: '搜索器材名称', statuses: equipmentStatus,
    filters: [{ key: 'type', label: '器材类型', type: 'select', options: equipmentTypes }, statusField(equipmentStatus)],
    columns: [{ key: 'name', label: '器材名称', type: 'person' }, { key: 'equipmentNo', label: '器材编号' }, { key: 'type', label: '类型', options: equipmentTypes }, { key: 'purchaseDate', label: '购入日期', type: 'date' }, { key: 'purchasePrice', label: '购入价格', type: 'money' }, { key: 'status', label: '器材状态', type: 'badge', options: equipmentStatus }],
    fields: [{ ...nameField, label: '器材名称', maxLength: 100 }, { key: 'type', label: '器材类型', type: 'select', options: equipmentTypes, required: true, default: 1 }, statusField(equipmentStatus), { key: 'purchaseDate', label: '购入日期', type: 'date' }, { key: 'purchasePrice', label: '购入价格（元）', type: 'number', min: 0, max: 99999999.99, step: '0.01' }, remark],
  },
  user: {
    key: 'user', title: '用户账号', singular: '用户', icon: 'user', subtitle: '管理登录账号，为会员服务做好连接。', searchKey: 'username', searchPlaceholder: '搜索用户名', createAction: 'register', detail: true, statusAction: true, statuses: normal,
    filters: [{ key: 'nickname', label: '昵称', placeholder: '搜索昵称' }],
    columns: [{ key: 'username', label: '用户名', type: 'person' }, { key: 'id', label: '用户 ID' }, { key: 'nickname', label: '昵称' }, { key: 'phone', label: '联系电话' }, { key: 'createTime', label: '创建时间', type: 'datetime' }, { key: 'status', label: '状态', type: 'badge', options: normal }],
    fields: [...accountFields, { key: 'nickname', label: '昵称', maxLength: 50 }, phoneField],
  },
  admin: {
    key: 'admin', title: '管理员', singular: '管理员', icon: 'shield', subtitle: '分工有序，让日常协作更加顺畅。', searchKey: 'username', searchPlaceholder: '搜索管理员账号', statuses: normal, statusAction: true,
    filters: [{ key: 'name', label: '姓名', placeholder: '搜索姓名' }],
    columns: [{ key: 'name', label: '管理员姓名', type: 'person' }, { key: 'username', label: '登录账号' }, { key: 'phone', label: '联系电话' }, { key: 'createTime', label: '创建时间', type: 'datetime' }, { key: 'status', label: '状态', type: 'badge', options: normal }],
    fields: [...accountFields, nameField, phoneField, { key: 'password', label: '新密码', type: 'password', editOnly: true, hint: '留空则不修改密码。', maxLength: 72 }],
  },
}

export const navigation = [
  { label: '工作空间', items: [{ key: 'dashboard', title: '运营概览', icon: 'layout' }] },
  { label: '日常运营', items: ['member', 'membershipCard', 'course', 'courseEnrollment', 'trainingPlan', 'trainingLog', 'equipment'].map(key => resources[key]!) },
  { label: '系统管理', items: ['user', 'admin'].map(key => resources[key]!) },
]
