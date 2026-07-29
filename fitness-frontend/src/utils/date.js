/**
 * 本地日期工具
 * 统一日期字符串为 yyyy-MM-dd（本地时区），避免 toISOString 的 UTC 偏移问题
 */

/** Date -> 'yyyy-MM-dd'（按本地时区） */
export const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/** 今天的 'yyyy-MM-dd' */
export const today = () => formatDate(new Date())

/** 'yyyy-MM-dd' -> Date（按本地时区解析，避免 new Date(str) 的 UTC 解析导致 weekday 错位） */
export const parseDate = (str) => {
  const [y, m, d] = String(str).split('-').map(Number)
  return new Date(y, m - 1, d)
}

/** 在某日期上增减 n 天，返回新的 'yyyy-MM-dd' */
export const addDays = (str, n) => {
  const d = parseDate(str)
  d.setDate(d.getDate() + n)
  return formatDate(d)
}
