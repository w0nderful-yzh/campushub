export function formatDateTime(value?: string): string {
  if (!value) return '暂无时间'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function formatCount(value?: number): string {
  const count = Number(value || 0)
  if (count < 1000) return String(count)
  if (count < 10000) return `${(count / 1000).toFixed(1)}k`
  return `${(count / 10000).toFixed(1)}w`
}
