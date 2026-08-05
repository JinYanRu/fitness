<template>
  <div class="history-page">
    <!-- 日期选择器 -->
    <div class="date-picker">
      <div class="date-nav">
        <button class="nav-btn" @click="prevMonth">‹</button>
        <span class="current-month">{{ currentMonthText }}</span>
        <button class="nav-btn" @click="nextMonth">›</button>
      </div>
      <div class="weekdays">
        <span>日</span><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span>
      </div>
      <div class="date-grid">
        <div
          v-for="day in calendarDays"
          :key="day.date"
          :class="['date-cell', { today: day.isToday, selected: day.date === selectedDate, hasRecord: day.hasRecord }]"
          @click="selectDate(day.date)"
        >
          <span class="day-num">{{ day.dayNum }}</span>
          <span class="day-dot" v-if="day.hasRecord"></span>
        </div>
      </div>
    </div>

    <!-- 统计汇总 -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ dailyStats.recordCount || 0 }}</span>
        <span class="stat-label">记录数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ dailyStats.totalCalories || 0 }}</span>
        <span class="stat-label">热量(kcal)</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ dailyStats.totalProtein || 0 }}g</span>
        <span class="stat-label">蛋白质</span>
      </div>
    </div>

    <!-- 当日记录列表 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">{{ selectedDateLabel }}</span>
        <span class="section-count">{{ dayRecords.length }} 条记录</span>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="dayRecords.length === 0" class="empty-state">
        <span class="empty-icon">🍽️</span>
        <span class="empty-text">当天暂无记录</span>
      </div>

      <div v-else class="record-list">
        <div
          v-for="record in dayRecords"
          :key="record.id"
          class="record-item"
          @click="viewDetail(record)"
        >
          <div class="record-meal">{{ getMealTypeLabel(record.mealType) }}</div>
          <div class="record-content">
            <div class="record-name">{{ record.foodName }}</div>
            <div class="record-detail">
              <span v-if="record.servingAmount">{{ record.servingAmount }}{{ record.servingUnit || 'g' }}</span>
            </div>
          </div>
          <div class="record-stats">
            <div class="calories">{{ record.calories || 0 }} kcal</div>
            <div class="macros">
              <span>P: {{ record.protein || 0 }}g</span>
              <span>F: {{ record.fat || 0 }}g</span>
              <span>C: {{ record.carbohydrates || 0 }}g</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除按钮 -->
    <div class="action-bar" v-if="dayRecords.length > 0">
      <button class="btn-delete" @click="confirmClearDay">清空当天记录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { nutritionApi } from '@/services/api/nutrition.js'

const router = useRouter()

// 日期相关
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth())
const selectedDate = ref(new Date().toISOString().split('T')[0])

// 数据
const loading = ref(false)
const dayRecords = ref([])
const dailyStats = ref({})

// 日历计算
const currentMonthText = computed(() => {
  return `${currentYear.value}年${currentMonth.value + 1}月`
})

const selectedDateLabel = computed(() => {
  const date = new Date(selectedDate.value)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (selectedDate.value === today.toISOString().split('T')[0]) return '今天'
  if (selectedDate.value === yesterday.toISOString().split('T')[0]) return '昨天'

  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${date.getMonth() + 1}月${date.getDate()}日 ${weekDays[date.getDay()]}`
})

const calendarDays = computed(() => {
  const days = []
  const firstDay = new Date(currentYear.value, currentMonth.value, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0)
  const today = new Date().toISOString().split('T')[0]

  // 计算第一天是周几，补充前面的空白
  const startWeekDay = firstDay.getDay()

  // 上个月的天数
  const prevLastDay = new Date(currentYear.value, currentMonth.value, 0)
  for (let i = startWeekDay - 1; i >= 0; i--) {
    days.push({
      dayNum: prevLastDay.getDate() - i,
      date: null,
      isToday: false,
      hasRecord: false
    })
  }

  // 当月天数
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(currentYear.value, currentMonth.value, i).toISOString().split('T')[0]
    days.push({
      dayNum: i,
      date,
      isToday: date === today,
      hasRecord: false // TODO: 从记录中计算
    })
  }

  return days
})

// 用餐类型
const getMealTypeLabel = (type) => {
  const labels = {
    breakfast: '🌅 早餐',
    lunch: '☀️ 午餐',
    dinner: '🌙 晚餐',
    snack: '🍪 加餐',
    workout: '💪 健身餐'
  }
  return labels[type] || '🍽️'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [recordsRes, statsRes] = await Promise.all([
      nutritionApi.getByDate(selectedDate.value),
      nutritionApi.getStatsByDate(selectedDate.value)
    ])

    dayRecords.value = recordsRes.code === 200 ? (recordsRes.data || []) : []
    dailyStats.value = statsRes.code === 200 ? (statsRes.data || {}) : {}
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 日期导航
const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const selectDate = (date) => {
  if (date) {
    selectedDate.value = date
  }
}

// 查看详情
const viewDetail = (record) => {
  router.push(`/record/${record.id}`)
}

// 清空当天
const confirmClearDay = async () => {
  if (!confirm(`确定要清空 ${selectedDateLabel.value} 的所有记录吗？`)) return

  try {
    for (const record of dayRecords.value) {
      await nutritionApi.delete(record.id)
    }
    loadData()
    alert('已清空')
  } catch (error) {
    alert('清空失败')
  }
}

// 监听日期变化
watch(selectedDate, loadData)

onMounted(loadData)
</script>

<style scoped>
.history-page {
  padding: calc(var(--safe-top) + 14px) 16px calc(var(--tab-h) + var(--safe-bottom) + 28px);
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 24%);
  min-height: 100vh;
}

/* 日历 */
.date-picker {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
}

.date-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 14px;
}

.nav-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: var(--fill);
  border-radius: 50%;
  font-size: 18px;
  color: var(--text-2);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, transform 0.12s;
}
.nav-btn:active { transform: scale(0.88); background: var(--primary-100); }

.current-month {
  font-size: 17px;
  font-weight: 600;
  min-width: 130px;
  text-align: center;
  color: var(--text-1);
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 6px;
}
.weekdays span {
  text-align: center;
  font-size: 12px;
  color: var(--text-3);
  font-weight: 500;
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.date-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  position: relative;
  transition: background 0.15s, color 0.15s;
}

.date-cell:hover { background: var(--fill); }
.date-cell.today { background: var(--primary-50); }
.date-cell.today .day-num { color: var(--primary-600); font-weight: 600; }
.date-cell.selected {
  background: var(--primary-gradient);
  box-shadow: var(--shadow-primary);
}
.date-cell.selected .day-num { color: #fff; font-weight: 700; }
.date-cell.selected .day-dot { background: #fff; }

.day-num { font-size: 14px; color: var(--text-2); }
.day-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 3px;
}

/* 统计行 */
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 16px 10px;
  text-align: center;
  box-shadow: var(--shadow-xs);
  position: relative;
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--primary-gradient);
}
.stat-card:nth-child(2)::before { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.stat-card:nth-child(3)::before { background: linear-gradient(90deg, #3b82f6, #60a5fa); }

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-1);
  display: block;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 5px;
  display: block;
}

/* 记录列表 */
.section { margin-bottom: 16px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 2px;
}
.section-title { font-size: 16px; font-weight: 600; color: var(--text-1); }
.section-count { font-size: 13px; color: var(--text-3); }

.record-list {
  background: var(--card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}

.record-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--divider);
  cursor: pointer;
  transition: background 0.15s;
}
.record-item:last-child { border-bottom: none; }
.record-item:active { background: var(--bg-soft); }

.record-meal {
  font-size: 22px;
  margin-right: 12px;
  width: 40px;
  text-align: center;
}

.record-content { flex: 1; min-width: 0; }
.record-name { font-size: 15px; color: var(--text-1); font-weight: 500; }
.record-detail { font-size: 12px; color: var(--text-3); margin-top: 4px; }

.record-stats { text-align: right; flex-shrink: 0; }
.record-stats .calories {
  font-size: 17px;
  font-weight: 700;
  color: var(--primary);
}
.record-stats .macros {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 4px;
}
.record-stats .macros span { margin-left: 8px; }

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: center;
  padding: 8px 16px 4px;
}

.btn-delete {
  padding: 11px 24px;
  background: var(--card);
  border: 1.5px solid var(--danger);
  color: var(--danger);
  border-radius: 999px;
  font-size: 14px;
  font-weight: 500;
  transition: transform 0.12s, background 0.15s;
}
.btn-delete:active { transform: scale(0.96); background: #fef2f2; }

/* 状态 */
.loading { text-align: center; padding: 40px; color: var(--text-3); }

.empty-state {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 44px 24px;
  text-align: center;
  box-shadow: var(--shadow-xs);
}
.empty-icon { font-size: 46px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 15px; color: var(--text-2); }
</style>