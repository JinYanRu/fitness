/**
 * 生成可视化饮食报告文本（纯文本 + emoji，适合微信/短信等聊天气泡展示）
 */

const MEAL_ORDER = ['breakfast', 'lunch', 'dinner', 'snack', 'workout']
const MEAL_LABELS = {
  breakfast: '🌅 早餐',
  lunch: '☀️ 午餐',
  dinner: '🌙 晚餐',
  snack: '🍪 加餐',
  workout: '💪 健身餐'
}
const OTHER_LABEL = '🍽️ 其它'

const round1 = (n) => Math.round((Number(n) || 0) * 10) / 10

// 份量描述：快捷单位 + 克数
const servingText = (r) => {
  const grams = round1(r.servingAmount)
  const unit = r.servingUnit || 'g'
  if (r.displayUnit && r.displayAmount != null && r.displayAmount !== '') {
    return `${r.displayAmount}${r.displayUnit}·${grams}${unit}`
  }
  return `${grams}${unit}`
}

/**
 * 生成可视化饮食报告
 * @param {Object} opts
 * @param {Array}  opts.records   当日记录列表
 * @param {Object} opts.targets   预留参数（当前不展示目标，保持调用兼容）
 * @param {String} opts.dateLabel 日期显示文本（如 "2026-08-05 · 周三"）
 * @returns {String}
 */
export function generateDietReport({ records = [], targets = {}, dateLabel = '' }) {
  const lines = []
  lines.push('🍽️ 饮食记录')
  if (dateLabel) lines.push(`📅 ${dateLabel}`)
  lines.push('')

  // 按餐次分组
  const groups = {}
  for (const r of records) {
    const key = MEAL_ORDER.includes(r.mealType) ? r.mealType : '_other'
    ;(groups[key] = groups[key] || []).push(r)
  }
  const orderedKeys = MEAL_ORDER.filter((k) => groups[k])
  if (groups._other) orderedKeys.push('_other')

  for (const key of orderedKeys) {
    lines.push(key === '_other' ? OTHER_LABEL : MEAL_LABELS[key])
    for (const r of groups[key]) {
      const name = r.foodName || '未命名'
      const tag = r.eaten === false ? '（待吃）' : ''
      lines.push(`  ${name} ${servingText(r)}${tag}`)
    }
    lines.push('')
  }

  // 合计：仅汇总热量与碳蛋脂
  const sum = (field) => round1(records.reduce((s, r) => s + (Number(r[field]) || 0), 0))
  lines.push('📊 合计')
  lines.push(`  🔥 ${sum('calories')} kcal`)
  lines.push(`  🥩 蛋白${sum('protein')}g  🧈 脂肪${sum('fat')}g  🍚 碳水${sum('carbohydrates')}g`)

  return lines.join('\n')
}

export default generateDietReport
