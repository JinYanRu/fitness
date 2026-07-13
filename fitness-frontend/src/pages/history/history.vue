<template>
  <div class="history-page">
    <!-- 日期选择器 -->
    <div class="date-picker">
      <div class="date-nav">
        <button class="nav-btn" @click="prevMonth">‹</button>
        <span class="current-month">{{ currentMonthText }}</span>
        <button class="nav-btn" @click="nextMonth">›</button>
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
  padding: 16px;
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 80px;
}

/* 日历 */
.date-picker {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.date-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
}

.nav-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
}

.current-month {
  font-size: 18px;
  font-weight: 500;
  min-width: 120px;
  text-align: center;
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
  border-radius: 8px;
  cursor: pointer;
  position: relative;
}

.date-cell:hover { background: #f5f5f5; }
.date-cell.today { background: #E8F5E9; }
.date-cell.selected { background: #4CAF50; color: #fff; }
.date-cell.selected .day-dot { background: #fff; }

.day-num { font-size: 14px; }
.day-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #4CAF50;
  margin-top: 2px;
}

/* 统计行 */
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  display: block;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

/* 记录列表 */
.section { margin-bottom: 16px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title { font-size: 18px; font-weight: 500; }
.section-count { font-size: 14px; color: #999; }

.record-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.record-item:last-child { border-bottom: none; }

.record-meal {
  font-size: 24px;
  margin-right: 12px;
}

.record-content { flex: 1; }
.record-name { font-size: 16px; color: #333; }
.record-detail { font-size: 12px; color: #999; margin-top: 4px; }

.record-stats { text-align: right; }
.record-stats .calories {
  font-size: 18px;
  font-weight: 500;
  color: #4CAF50;
}

.record-stats .macros {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.record-stats .macros span { margin-left: 8px; }

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.btn-delete {
  padding: 12px 24px;
  background: #fff;
  border: 1px solid #f44336;
  color: #f44336;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

/* 状态 */
.loading { text-align: center; padding: 40px; color: #999; }

.empty-state {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
}

.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 16px; color: #666; }
</style>