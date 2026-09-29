import type { Column, Row } from './types'

// Backend JDBC uses Asia/Shanghai. Date-only business values must not shift
// when java.util.Date is serialized as UTC or the browser uses another zone.
const businessDate = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' })
function formatBusinessDate(date: Date) {
  const parts = Object.fromEntries(businessDate.formatToParts(date).map(part => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day}`
}
export const today = () => formatBusinessDate(new Date())
export function dateText(value: unknown): string {
  if (!value) return '—'
  if (/^\d{4}-\d{2}-\d{2}$/.test(String(value))) return String(value)
  const date = new Date(value as string | number)
  return Number.isNaN(date.getTime()) ? '—' : formatBusinessDate(date)
}
export function display(row: Row, column: Column): string {
  const value = row[column.key]
  if (value === null || value === undefined || value === '') return '—'
  if (column.options) return column.options.find(item => String(item.value) === String(value))?.label || String(value)
  if (column.type === 'date') return dateText(value)
  if (column.type === 'datetime') return String(value).replace('T', ' ').slice(0, 16)
  if (column.type === 'money') return `¥${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
  return String(value)
}
export const errorMessage = (error: unknown) => error instanceof Error ? error.message : '操作失败，请稍后再试。'
