<template>
  <div class="index-page">
    <!-- 用户信息 -->
    <div class="user-info" v-if="userInfo">
      <span class="greeting">{{ getGreeting() }}，{{ userInfo.nickname || '用户' }}</span>
    </div>

    <!-- 日期选择 -->
    <div class="date-bar">
      <button class="date-arrow" @click="shiftDay(-1)" aria-label="前一天">‹</button>
      <div class="date-pickable">
        <div class="date-text">
          <span class="date-main">{{ selectedDateMain }}</span>
          <span class="date-sub">{{ selectedDateSub }}</span>
        </div>
        <span class="date-cal">📅</span>
        <input
          type="date"
          class="date-native-input"
          :value="selectedDate"
          :max="todayDateStr"
          @change="onPickerChange"
        />
      </div>
      <button class="date-arrow" :disabled="isToday" @click="shiftDay(1)" aria-label="后一天">›</button>
    </div>
    <div v-if="!isToday" class="back-today-row">
      <button class="back-today-btn" @click="goToday">回到今天</button>
    </div>

    <!-- 热量统计卡片 -->
    <div class="calorie-card">
      <div class="calorie-header">
        <span class="calorie-title">{{ isToday ? '今日热量' : selectedDateMain + '热量' }}</span>
        <span class="calorie-date">{{ selectedDateShort }}</span>
      </div>
      <div class="calorie-main">
        <div class="calorie-circle" :style="{ '--p': Math.min(calorieProgress, 100) }" :class="{ over: calorieProgress > 100 }">
          <div class="calorie-value">{{ dayStats.totalCalories || 0 }}</div>
          <div class="calorie-unit">kcal</div>
        </div>
        <div class="calorie-target" v-if="targetCalories">
          <span class="target-label">目标</span>
          <span class="target-value">{{ targetCalories }} kcal</span>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: calorieProgress + '%' }"
              :class="{ warning: calorieProgress > 100 }"
            ></div>
          </div>
          <span class="progress-text">{{ calorieProgress }}%</span>
        </div>
      </div>
    </div>

    <!-- 营养素统计 -->
    <div class="nutrition-stats">
      <div class="nutrient-item">
        <div class="nutrient-header">
          <span class="nutrient-name">蛋白质</span>
          <span class="nutrient-value">{{ dayStats.totalProtein || 0 }}g</span>
        </div>
        <div class="nutrient-bar">
          <div class="nutrient-fill protein" :style="{ width: proteinProgress + '%' }"></div>
        </div>
      </div>
      <div class="nutrient-item">
        <div class="nutrient-header">
          <span class="nutrient-name">脂肪</span>
          <span class="nutrient-value">{{ dayStats.totalFat || 0 }}g</span>
        </div>
        <div class="nutrient-bar">
          <div class="nutrient-fill fat" :style="{ width: fatProgress + '%' }"></div>
        </div>
      </div>
      <div class="nutrient-item">
        <div class="nutrient-header">
          <span class="nutrient-name">碳水</span>
          <span class="nutrient-value">{{ dayStats.totalCarbohydrates || 0 }}g</span>
        </div>
        <div class="nutrient-bar">
          <div class="nutrient-carbs" :style="{ width: carbsProgress + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- AI 分析饮食 -->
    <button class="ai-analysis-btn" :disabled="analyzing" @click="handleAnalyzeDiet">
      <span class="ai-icon">🤖</span>
      <span class="ai-text">{{ analyzing ? '分析中…' : 'AI 分析今日饮食' }}</span>
    </button>

    <!-- 快捷操作 -->
    <div class="action-buttons">
      <div class="action-btn primary" @click="goToRecord">
        <span class="action-icon">📝</span>
        <span class="action-text">快速记录</span>
      </div>
      <div class="action-btn" @click="goToFoodLibrary">
        <span class="action-icon">🍎</span>
        <span class="action-text">食物库</span>
      </div>
    </div>

    <!-- 今日记录列表 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">{{ isToday ? '今日记录' : selectedDateMain + '记录' }} ({{ dayRecords.length }})</span>
        <div class="section-actions">
          <button v-if="isToday" class="import-btn" @click="handleImportYesterday">📥 导入昨天</button>
          <button v-if="dayRecords.length > 0" class="export-btn" @click="handleExportDiet">📋 导出饮食</button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div class="record-list" v-else-if="dayRecords.length > 0">
        <div
          v-for="record in dayRecords"
          :key="record.id"
          class="record-swipe-wrapper"
        >
          <div
            class="record-swipe-content"
            :class="{ swiping: swipingId === record.id, uneaten: record.eaten === false }"
            :style="{ transform: `translateX(${swipeState[record.id] || 0}px)` }"
            @click="onRecordClick(record)"
            @touchstart="onTouchStart($event, record)"
            @touchmove="onTouchMove($event, record)"
            @touchend="onTouchEnd(record)"
          >
            <div class="record-info">
              <span class="record-meal">
                {{ getMealTypeLabel(record.mealType) }}
                <span v-if="record.eaten === false" class="eaten-tag">待吃</span>
              </span>
              <span class="record-name">{{ record.foodName }}</span>
              <span class="record-amount" v-if="record.servingAmount">
                <template v-if="record.displayUnit">
                  {{ record.displayAmount }}{{ record.displayUnit }}
                  <span class="amount-grams">({{ record.servingAmount }}{{ record.servingUnit || 'g' }})</span>
                </template>
                <template v-else>
                  {{ record.servingAmount }}{{ record.servingUnit || 'g' }}
                </template>
              </span>
            </div>
            <div class="record-right">
              <button
                v-if="record.eaten === false"
                class="eaten-check"
                @click.stop="markAsEaten(record)"
                aria-label="标记已吃"
              >✓</button>
              <div class="record-calories">
                {{ record.calories || 0 }} kcal
              </div>
            </div>
          </div>
          <button class="record-delete-btn" @click.stop="handleDelete(record)">删除</button>
        </div>
      </div>

      <div class="empty-state" v-else>
        <span class="empty-icon">🍽️</span>
        <span class="empty-text">{{ isToday ? '今日暂无记录' : selectedDateMain + '暂无记录' }}</span>
        <span class="empty-tip">点击上方按钮开始记录</span>
      </div>
    </div>

    <!-- AI 分析结果弹窗 -->
    <DietAnalysisModal
      :visible="showAnalysis"
      :result="analysisResult"
      :loading="analyzing"
      @close="showAnalysis = false"
    />

    <!-- 导出饮食报告弹窗 -->
    <DietExportModal
      :visible="showExport"
      :report-text="exportText"
      :copied="exportCopied"
      @close="showExport = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { nutritionApi } from '@/services/api/nutrition.js'
import { authApi } from '@/services/api/auth.js'
import { aiApi } from '@/services/api/ai.js'
import DietAnalysisModal from '@/components/DietAnalysisModal.vue'
import DietExportModal from '@/components/DietExportModal.vue'
import { generateDietReport } from '@/utils/dietReport.js'
import { copyText } from '@/utils/clipboard.js'

import { showToast } from '@/utils/toast.js'
import { today, addDays, parseDate } from '@/utils/date.js'

const router = useRouter()

// 用户信息
const userInfo = ref(null)
const userProfile = ref(null)
const targetCalories = ref(null)
const targetProtein = ref(null)
const targetFat = ref(null)
const targetCarbs = ref(null)

// AI 饮食分析
const showAnalysis = ref(false)
const analyzing = ref(false)
const analysisResult = ref(null)

// 导出饮食报告
const showExport = ref(false)
const exportText = ref('')
const exportCopied = ref(false)

// 统计数据
const loading = ref(false)
const dayStats = ref({
  recordCount: 0,
  totalCalories: 0,
  totalProtein: 0,
  totalFat: 0,
  totalCarbohydrates: 0
})
const dayRecords = ref([])

// 日期选择
const todayDateStr = today()
const selectedDate = ref(todayDateStr)

const isToday = computed(() => selectedDate.value === todayDateStr)

// 主标签：今天 / 昨天 / M月D日
const selectedDateMain = computed(() => {
  if (selectedDate.value === todayDateStr) return '今天'
  if (selectedDate.value === addDays(todayDateStr, -1)) return '昨天'
  const d = parseDate(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

// 副标签：yyyy-MM-dd · 周X
const selectedDateSub = computed(() => {
  const d = parseDate(selectedDate.value)
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${selectedDate.value} · ${weekDays[d.getDay()]}`
})

// 热量卡片右侧短日期
const selectedDateShort = computed(() => {
  const d = parseDate(selectedDate.value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

// 日期切换
const shiftDay = (n) => {
  selectedDate.value = addDays(selectedDate.value, n)
}
const goToday = () => {
  selectedDate.value = todayDateStr
}
const onPickerChange = (e) => {
  const val = e.target.value
  if (val) selectedDate.value = val
}

// 进度计算
const calorieProgress = computed(() => {
  if (!targetCalories.value) return 0
  const progress = Math.round((dayStats.value.totalCalories / targetCalories.value) * 100)
  return Math.min(progress, 150)
})

const proteinProgress = computed(() => {
  if (!targetProtein.value) return 0
  return Math.min(Math.round((dayStats.value.totalProtein / targetProtein.value) * 100), 100)
})

const fatProgress = computed(() => {
  if (!targetFat.value) return 0
  return Math.min(Math.round((dayStats.value.totalFat / targetFat.value) * 100), 100)
})

const carbsProgress = computed(() => {
  if (!targetCarbs.value) return 0
  return Math.min(Math.round((dayStats.value.totalCarbohydrates / targetCarbs.value) * 100), 100)
})

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

// 用餐类型标签
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

// 加载当日数据（随所选日期变化）
const loadDayData = async () => {
  loading.value = true
  // 切换日期时收起所有左滑状态
  Object.keys(swipeState).forEach((id) => delete swipeState[id])
  try {
    const [statsRes, recordsRes] = await Promise.all([
      nutritionApi.getStatsByDate(selectedDate.value),
      nutritionApi.getByDate(selectedDate.value)
    ])

    // 处理统计
    if (statsRes.code === 200 && statsRes.data) {
      dayStats.value = {
        recordCount: statsRes.data.recordCount || 0,
        totalCalories: statsRes.data.totalCalories || 0,
        totalProtein: statsRes.data.totalProtein || 0,
        totalFat: statsRes.data.totalFat || 0,
        totalCarbohydrates: statsRes.data.totalCarbohydrates || 0
      }
    }

    // 处理记录列表
    if (recordsRes.code === 200 && recordsRes.data) {
      dayRecords.value = recordsRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载用户档案与目标（与日期无关，仅加载一次）
const loadProfile = async () => {
  try {
    const profileRes = await authApi.getProfileDetail()
    if (profileRes.code === 200 && profileRes.data) {
      userProfile.value = profileRes.data
      targetCalories.value = profileRes.data.targetCalories
      targetProtein.value = profileRes.data.targetProtein
      targetFat.value = profileRes.data.targetFat
      targetCarbs.value = profileRes.data.targetCarbs
    }
  } catch (error) {
    console.error('加载档案失败:', error)
  }
}

// 加载用户信息
const loadUserInfo = () => {
  const saved = localStorage.getItem('userInfo')
  if (saved) {
    try {
      userInfo.value = JSON.parse(saved)
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

// 跳转
const goToRecord = () => router.push({ path: '/record', query: { date: selectedDate.value } })
const goToFoodLibrary = () => router.push('/food/library')

// AI 分析当日饮食
const handleAnalyzeDiet = async () => {
  // 先打开弹窗显示 loading，给用户即时反馈
  showAnalysis.value = true
  analyzing.value = true
  analysisResult.value = null
  try {
    const res = await aiApi.analyzeDiet(selectedDate.value)
    if (res.code === 200 && res.data) {
      analysisResult.value = res.data
    } else {
      showAnalysis.value = false
      showToast(res.message || 'AI 分析失败')
    }
  } catch (error) {
    showAnalysis.value = false
    showToast('AI 分析失败: ' + (error.message || '未知错误'))
  } finally {
    analyzing.value = false
  }
}

const viewDetail = (record) => {
  router.push(`/record/${record.id}`)
}

// 左滑删除
const DELETE_WIDTH = 80 // 删除按钮宽度
const swipeState = reactive({}) // 每条记录当前的横向偏移量，按 id 索引
const swipingId = ref(null) // 正在拖拽的记录 id（用于拖拽时去掉过渡动画）
const swipeStart = reactive({ x: 0, y: 0, base: 0, id: null, horizontal: null })
let suppressClick = false // 拖拽发生后阻止本次 click 跳转详情

const onTouchStart = (e, record) => {
  const t = e.touches[0]
  swipeStart.x = t.clientX
  swipeStart.y = t.clientY
  swipeStart.base = swipeState[record.id] || 0
  swipeStart.id = record.id
  swipeStart.horizontal = null
  swipingId.value = record.id
  // 收起其它已展开的项，保证同一时刻只有一项展开
  Object.keys(swipeState).forEach((id) => {
    if (id !== String(record.id) && swipeState[id] !== 0) swipeState[id] = 0
  })
}

const onTouchMove = (e, record) => {
  if (swipeStart.id !== record.id) return
  const t = e.touches[0]
  const dx = t.clientX - swipeStart.x
  const dy = t.clientY - swipeStart.y
  // 首次移动判定方向：垂直滑动则交给页面滚动，不触发左滑
  if (swipeStart.horizontal === null) {
    if (Math.abs(dx) < 5 && Math.abs(dy) < 5) return
    swipeStart.horizontal = Math.abs(dx) > Math.abs(dy)
    if (!swipeStart.horizontal) {
      swipingId.value = null
      return
    }
  }
  if (!swipeStart.horizontal) return
  suppressClick = true
  let next = swipeStart.base + dx
  if (next > 0) next = 0 // 不允许右滑超过原位
  if (next < -DELETE_WIDTH) next = -DELETE_WIDTH
  swipeState[record.id] = next
}

const onTouchEnd = (record) => {
  if (swipeStart.id !== record.id) return
  const current = swipeState[record.id] || 0
  // 滑过一半则展开删除按钮，否则回弹收起
  swipeState[record.id] = current < -DELETE_WIDTH / 2 ? -DELETE_WIDTH : 0
  swipingId.value = null
  swipeStart.id = null
}

const onRecordClick = (record) => {
  if (suppressClick) {
    suppressClick = false
    return
  }
  // 点击已展开的项时收起，不跳转详情
  if ((swipeState[record.id] || 0) !== 0) {
    swipeState[record.id] = 0
    return
  }
  viewDetail(record)
}

const handleDelete = async (record) => {
  if (!confirm(`确定要删除「${record.foodName}」这条记录吗？`)) return
  try {
    const res = await nutritionApi.delete(record.id)
    if (res.code === 200) {
      delete swipeState[record.id]
      await loadDayData()
    } else {
      showToast(res.message || '删除失败')
    }
  } catch (error) {
    showToast('删除失败: ' + (error.message || '未知错误'))
  }
}

// 导入昨天的记录到今天（导入的记录标记为未吃，需逐条点 ✓ 确认）
const handleImportYesterday = async () => {
  const yesterday = addDays(todayDateStr, -1)
  try {
    // 先查昨天记录数，用于确认提示
    const yRes = await nutritionApi.getByDate(yesterday)
    const yCount = (yRes.code === 200 && yRes.data) ? yRes.data.length : 0
    if (yCount === 0) {
      showToast('昨天没有可导入的记录')
      return
    }
    if (!confirm(`将导入昨天 ${yCount} 条记录？\n导入的记录默认为「未吃」，需逐条点 ✓ 确认后才计入今日热量。`)) return
    const res = await nutritionApi.importFrom(yesterday)
    if (res.code === 200) {
      showToast(`已导入 ${res.data} 条记录，吃完后记得点 ✓ 确认`)
      await loadDayData()
    } else {
      showToast(res.message || '导入失败')
    }
  } catch (error) {
    showToast('导入失败: ' + (error.message || '未知错误'))
  }
}

// 导出当日饮食为可视化文本并复制到剪切板
const handleExportDiet = async () => {
  if (!dayRecords.value.length) return
  exportText.value = generateDietReport({
    records: dayRecords.value,
    targets: {
      calories: targetCalories.value,
      protein: targetProtein.value,
      fat: targetFat.value,
      carbs: targetCarbs.value
    },
    dateLabel: selectedDateSub.value
  })
  showExport.value = true
  // 在点击手势内直接复制到剪切板
  exportCopied.value = await copyText(exportText.value)
}

// 本地把一条记录的营养计入/移出当日统计（标记已吃时即时更新，避免整页刷新）
const round1 = (n) => Math.round(n * 10) / 10
const addRecordToStats = (record) => {
  const s = dayStats.value
  s.recordCount = (s.recordCount || 0) + 1
  s.totalCalories = round1((Number(s.totalCalories) || 0) + (Number(record.calories) || 0))
  s.totalProtein = round1((Number(s.totalProtein) || 0) + (Number(record.protein) || 0))
  s.totalFat = round1((Number(s.totalFat) || 0) + (Number(record.fat) || 0))
  s.totalCarbohydrates = round1((Number(s.totalCarbohydrates) || 0) + (Number(record.carbohydrates) || 0))
}
const removeRecordFromStats = (record) => {
  const s = dayStats.value
  s.recordCount = Math.max(0, (s.recordCount || 0) - 1)
  s.totalCalories = round1((Number(s.totalCalories) || 0) - (Number(record.calories) || 0))
  s.totalProtein = round1((Number(s.totalProtein) || 0) - (Number(record.protein) || 0))
  s.totalFat = round1((Number(s.totalFat) || 0) - (Number(record.fat) || 0))
  s.totalCarbohydrates = round1((Number(s.totalCarbohydrates) || 0) - (Number(record.carbohydrates) || 0))
}

// 标记记录为已吃（乐观更新：先即时变更视觉与统计，后台异步落库，失败回滚）
const markAsEaten = async (record) => {
  record.eaten = true
  addRecordToStats(record)
  try {
    const res = await nutritionApi.markEaten(record.id, true)
    if (res.code !== 200) {
      record.eaten = false
      removeRecordFromStats(record)
      showToast(res.message || '标记失败')
    }
  } catch (error) {
    record.eaten = false
    removeRecordFromStats(record)
    showToast('标记失败: ' + (error.message || '未知错误'))
  }
}

// 日期变化时重新加载当日数据
watch(selectedDate, loadDayData)

onMounted(() => {
  loadUserInfo()
  loadProfile()
  loadDayData()
})
</script>

<style scoped>
.index-page {
  padding: calc(var(--safe-top) + 14px) 16px calc(var(--tab-h) + var(--safe-bottom) + 28px);
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 24%);
  min-height: 100vh;
}

/* 问候 */
.user-info {
  margin: 2px 2px 14px;
}
.greeting {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-1);
  letter-spacing: 0.3px;
}

/* 日期选择条 */
.date-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 9px 12px;
  margin-bottom: 14px;
  box-shadow: var(--shadow-sm);
}

.date-arrow {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  background: var(--fill);
  border-radius: 50%;
  font-size: 20px;
  color: var(--text-2);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background 0.2s, transform 0.12s;
}
.date-arrow:active { transform: scale(0.88); background: var(--primary-100); }
.date-arrow:disabled { opacity: 0.25; cursor: not-allowed; }

.date-pickable {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  padding: 2px 0;
}

.date-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.date-main { font-size: 16px; font-weight: 600; color: var(--text-1); }
.date-sub { font-size: 11px; color: var(--text-3); margin-top: 2px; }
.date-cal { font-size: 16px; opacity: 0.6; }

.date-native-input {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.back-today-row {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}
.back-today-btn {
  background: var(--card);
  color: var(--primary);
  border-radius: 999px;
  padding: 5px 18px;
  font-size: 13px;
  font-weight: 500;
  box-shadow: var(--shadow-xs);
  transition: transform 0.12s;
}
.back-today-btn:active { transform: scale(0.95); }

/* 热量卡片 */
.calorie-card {
  position: relative;
  background: var(--primary-gradient);
  border-radius: var(--radius-xl);
  padding: 22px;
  color: #fff;
  margin-bottom: 14px;
  box-shadow: var(--shadow-primary);
  overflow: hidden;
}
.calorie-card::before {
  content: '';
  position: absolute;
  top: -44px;
  right: -34px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.13);
}
.calorie-card::after {
  content: '';
  position: absolute;
  bottom: -54px;
  left: -24px;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.calorie-header {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.calorie-title { font-size: 15px; font-weight: 600; }
.calorie-date { font-size: 13px; opacity: 0.82; }

.calorie-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24px;
}

.calorie-circle {
  width: 108px;
  height: 108px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  background: conic-gradient(rgba(255, 255, 255, 0.95) calc(var(--p, 0) * 1%), rgba(255, 255, 255, 0.18) 0);
  transition: background 0.4s ease;
}
.calorie-circle.over {
  background: conic-gradient(#ffd54f calc(var(--p, 0) * 1%), rgba(255, 255, 255, 0.18) 0);
}
.calorie-circle::before {
  content: '';
  position: absolute;
  inset: 9px;
  border-radius: 50%;
  background: #fff;
}
.calorie-value { position: relative; z-index: 1; font-size: 28px; font-weight: 800; line-height: 1; color: var(--primary-700); }
.calorie-unit { position: relative; z-index: 1; font-size: 12px; color: var(--text-3); margin-top: 3px; }

.calorie-target { flex: 1; min-width: 0; }
.target-label { font-size: 12px; opacity: 0.82; display: block; }
.target-value { font-size: 15px; font-weight: 600; display: block; margin: 4px 0 2px; }

.progress-bar {
  height: 7px;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 7px;
  overflow: hidden;
  margin: 8px 0;
}
.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 7px;
  transition: width 0.4s ease;
}
.progress-fill.warning { background: #ffd54f; }
.progress-text { font-size: 12px; opacity: 0.9; }

/* 营养素统计 */
.nutrition-stats {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 14px;
  display: flex;
  gap: 10px;
  box-shadow: var(--shadow-xs);
}
.nutrient-item { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.nutrient-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.nutrient-name {
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  align-items: center;
}
.nutrient-name::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.nutrient-item:nth-child(1) .nutrient-name::before { background: var(--protein); }
.nutrient-item:nth-child(2) .nutrient-name::before { background: var(--fat); }
.nutrient-item:nth-child(3) .nutrient-name::before { background: var(--carbs); }
.nutrient-value { font-size: 16px; font-weight: 700; color: var(--text-1); }
.nutrient-bar {
  height: 6px;
  background: var(--fill);
  border-radius: 6px;
  overflow: hidden;
}
.nutrient-fill { height: 100%; border-radius: 6px; transition: width 0.4s ease; }
.nutrient-fill.protein { background: var(--protein); }
.nutrient-fill.fat { background: var(--fat); }
.nutrient-carbs { height: 100%; background: var(--carbs); border-radius: 6px; transition: width 0.4s ease; }

/* AI 分析按钮 */
.ai-analysis-btn {
  width: 100%;
  border-radius: var(--radius-lg);
  padding: 15px;
  margin-bottom: 14px;
  background: var(--ai-gradient);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 10px 24px rgba(99, 102, 241, 0.28);
  transition: transform 0.12s, box-shadow 0.2s;
}
.ai-analysis-btn:active { transform: scale(0.98); }
.ai-analysis-btn:disabled { opacity: 0.7; cursor: not-allowed; }
.ai-icon { font-size: 18px; }

/* 快捷操作 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.action-btn {
  flex: 1;
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: var(--shadow-xs);
  transition: transform 0.12s, box-shadow 0.2s;
}
.action-btn:active { transform: translateY(1px) scale(0.98); }
.action-btn.primary {
  background: var(--primary-gradient);
  color: #fff;
  box-shadow: var(--shadow-primary);
}
.action-icon { font-size: 26px; }
.action-text { font-size: 14px; font-weight: 500; }

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
.section-actions { display: flex; align-items: center; gap: 12px; }
.import-btn,
.export-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  font-size: 13px;
  color: var(--primary-600);
  background: var(--primary-50);
  border: 1px solid var(--primary-100);
  border-radius: 999px;
  line-height: 1.2;
  transition: transform 0.12s;
}
.import-btn:active,
.export-btn:active { transform: scale(0.96); }

.record-list {
  background: var(--card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
.record-swipe-wrapper { position: relative; overflow: hidden; }
.record-swipe-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: var(--card);
  border-bottom: 1px solid var(--divider);
  cursor: pointer;
  transition: transform 0.25s ease;
}
.record-swipe-content.swiping { transition: none; }
.record-swipe-wrapper:last-child .record-swipe-content { border-bottom: none; }
.record-delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 80px;
  border: none;
  background: linear-gradient(135deg, #ff5b5b, #ef4444);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

.record-info { flex: 1; display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.record-meal {
  font-size: 11px;
  color: var(--primary-600);
  background: var(--primary-50);
  padding: 2px 8px;
  border-radius: 6px;
  align-self: flex-start;
  font-weight: 500;
}
.record-name { font-size: 15px; color: var(--text-1); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.record-amount { font-size: 12px; color: var(--text-3); }
.amount-grams { font-size: 11px; color: var(--text-4); }

.record-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.record-calories { font-size: 16px; font-weight: 700; color: var(--primary); }

.record-swipe-content.uneaten { background: var(--bg-soft); }
.record-swipe-content.uneaten .record-name { color: var(--text-3); }
.record-swipe-content.uneaten .record-calories { color: var(--text-4); }

.eaten-tag {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 6px;
  font-size: 11px;
  color: var(--accent);
  background: #fff6e6;
  border-radius: 8px;
  vertical-align: middle;
}

.eaten-check {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  padding: 0;
  border-radius: 50%;
  border: 1.5px solid var(--primary);
  background: var(--card);
  color: var(--primary);
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s, background 0.15s;
}
.eaten-check:active { transform: scale(0.88); background: var(--primary-100); }

/* 空状态 */
.empty-state {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 44px 24px;
  text-align: center;
  box-shadow: var(--shadow-xs);
}
.empty-icon { font-size: 46px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 15px; color: var(--text-2); display: block; }
.empty-tip { font-size: 13px; color: var(--text-3); display: block; margin-top: 6px; }

.loading { text-align: center; padding: 40px; color: var(--text-3); }
</style>
