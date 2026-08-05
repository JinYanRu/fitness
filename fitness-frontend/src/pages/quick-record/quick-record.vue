<template>
  <div class="quick-record-page">
    <!-- 标题区 -->
    <div class="page-header">
      <div class="header-title">快速记录</div>
      <div class="header-sub" v-if="isAutoMeal">
        根据当前时间已选择「{{ currentMealLabel }}」，可手动调整
      </div>
      <div class="header-sub" v-else>今日 {{ todayRecords.length }} 条记录</div>
    </div>

    <!-- 用餐类型 -->
    <div class="meal-type-section">
      <div class="meal-types">
        <div
          v-for="meal in mealTypes"
          :key="meal.value"
          :class="['meal-type', mealType === meal.value ? 'active' : '']"
          @click="selectMeal(meal.value)"
        >
          {{ meal.icon }} {{ meal.label }}
        </div>
      </div>
    </div>

    <!-- 搜索食物 -->
    <div class="search-section">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索食物名称或品牌"
        class="search-input"
      />
    </div>

    <!-- 食物列表 -->
    <div class="food-list">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="filteredFoods.length === 0" class="empty">
        <p class="empty-text">暂无食物</p>
        <p class="empty-tip">去食物库添加一些食物吧</p>
      </div>
      <div v-else>
        <div
          v-for="food in filteredFoods"
          :key="food.id"
          :class="['food-item', selectedFood?.id === food.id ? 'selected' : '']"
          @click="selectFood(food)"
        >
          <div class="food-info">
            <div class="food-name">{{ food.foodName }}</div>
            <div v-if="food.brand" class="food-brand">{{ food.brand }}</div>
            <div class="food-serving">
              每 {{ food.servingSize || 100 }}{{ food.servingUnit || 'g' }}
              <span v-if="food.units && food.units.length > 0" class="has-units">• 有快捷单位</span>
            </div>
          </div>
          <div class="food-calories">{{ food.calories || 0 }} kcal</div>
        </div>
      </div>
    </div>

    <!-- 食用量（选中食物后内联展开，不跳转） -->
    <transition name="slide-up">
      <div v-if="selectedFood" class="serving-section">
        <div class="serving-header">
          <div class="serving-name">{{ selectedFood.foodName }}</div>
          <button class="btn-clear" @click="clearSelection" aria-label="取消选择">×</button>
        </div>

        <div class="amount-input-group">
          <input
            v-model.number="servingAmount"
            type="number"
            min="0"
            step="1"
            class="amount-input"
          />
          <select v-model="selectedUnitName" class="unit-select" @change="onUnitChange">
            <option
              v-for="unit in availableUnits"
              :key="unit.unitName"
              :value="unit.unitName"
            >
              {{ unit.unitName }}
            </option>
            <option value="">{{ selectedFood.servingUnit || 'g' }}</option>
          </select>
        </div>

        <div v-if="selectedUnitName && currentUnitValue" class="unit-hint">
          1{{ selectedUnitName }} = {{ currentUnitValue }}{{ selectedFood.servingUnit || 'g' }}
          <span v-if="servingAmount" class="unit-total">= {{ actualGrams }}{{ selectedFood.servingUnit || 'g' }}</span>
        </div>

        <div class="nutrition-preview">
          <div class="preview-item">
            <span class="preview-label">热量</span>
            <span class="preview-value">{{ calculatedCalories }} kcal</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">蛋白质</span>
            <span class="preview-value">{{ calculatedProtein }}g</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">脂肪</span>
            <span class="preview-value">{{ calculatedFat }}g</span>
          </div>
          <div class="preview-item">
            <span class="preview-label">碳水</span>
            <span class="preview-value">{{ calculatedCarbs }}g</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 添加按钮（固定在底部，选中食物后可用） -->
    <div class="add-bar">
      <button class="btn-add" :disabled="!selectedFood || isSaving" @click="addRecord">
        <span v-if="isSaving">添加中...</span>
        <span v-else>+ 添加到{{ currentMealLabel }}</span>
      </button>
    </div>

    <!-- 今日已添加 -->
    <div class="today-section" v-if="todayRecords.length > 0">
      <div class="today-header">
        <span class="today-title">今日记录 ({{ todayRecords.length }})</span>
        <span class="today-calories">共 {{ todayTotalCalories }} kcal</span>
      </div>
      <div class="today-list">
        <div v-for="record in todayRecords" :key="record.id" class="today-item">
          <span class="today-meal">{{ getMealLabel(record.mealType) }}</span>
          <span class="today-name">{{ record.foodName }}</span>
          <span class="today-amount">{{ record.servingAmount }}{{ record.servingUnit || 'g' }}</span>
          <span class="today-cal">{{ record.calories }} kcal</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { nutritionApi } from '@/services/api/nutrition.js'
import { userFoodApi } from '@/services/api/food.js'
import { today } from '@/utils/date.js'
import { showToast } from '@/utils/toast.js'

// 用餐类型
const mealTypes = [
  { value: 'breakfast', label: '早餐', icon: '🌅' },
  { value: 'lunch', label: '午餐', icon: '☀️' },
  { value: 'dinner', label: '晚餐', icon: '🌙' },
  { value: 'snack', label: '加餐', icon: '🍪' },
  { value: 'workout', label: '健身餐', icon: '💪' }
]

const mealLabelMap = {
  breakfast: '早餐',
  lunch: '午餐',
  dinner: '晚餐',
  snack: '加餐',
  workout: '健身餐'
}

// 根据当前时间判断用餐类型：早餐/午餐/晚餐（加餐和健身餐需用户手动选择）
const getMealTypeByTime = () => {
  const h = new Date().getHours()
  if (h < 10) return 'breakfast'
  if (h < 14) return 'lunch'
  return 'dinner'
}

const mealType = ref('lunch')
const isAutoMeal = ref(true)

const currentMealLabel = computed(() => mealLabelMap[mealType.value] || '记录')
const getMealLabel = (type) => mealLabelMap[type] || '🍽️'

// 手动选择用餐类型后，不再显示自动提示
const selectMeal = (value) => {
  mealType.value = value
  isAutoMeal.value = false
}

// 食物选择
const searchKeyword = ref('')
const myFoods = ref([])
const loading = ref(false)
const selectedFood = ref(null)
const servingAmount = ref(100)
const selectedUnitName = ref('')
const isSaving = ref(false)

// 今日记录
const todayRecords = ref([])
const todayTotalCalories = computed(() =>
  todayRecords.value.reduce((sum, r) => sum + (Number(r.calories) || 0), 0)
)

const filteredFoods = computed(() => {
  if (!searchKeyword.value) return myFoods.value
  const keyword = searchKeyword.value.toLowerCase()
  return myFoods.value.filter(f =>
    f.foodName?.toLowerCase().includes(keyword) ||
    f.brand?.toLowerCase().includes(keyword)
  )
})

const availableUnits = computed(() => selectedFood.value?.units || [])

const currentUnitValue = computed(() => {
  if (!selectedUnitName.value || !selectedFood.value?.units) return null
  const unit = selectedFood.value.units.find(u => u.unitName === selectedUnitName.value)
  return unit?.unitValue || null
})

const actualGrams = computed(() => {
  if (!selectedUnitName.value || !currentUnitValue.value) return servingAmount.value
  return servingAmount.value * currentUnitValue.value
})

const calculatedCalories = computed(() => {
  if (!selectedFood.value || !servingAmount.value) return 0
  const ratio = actualGrams.value / (selectedFood.value.servingSize || 100)
  return Math.round((selectedFood.value.calories || 0) * ratio)
})
const calculatedProtein = computed(() => {
  if (!selectedFood.value || !servingAmount.value) return 0
  const ratio = actualGrams.value / (selectedFood.value.servingSize || 100)
  return Math.round((selectedFood.value.protein || 0) * ratio * 10) / 10
})
const calculatedFat = computed(() => {
  if (!selectedFood.value || !servingAmount.value) return 0
  const ratio = actualGrams.value / (selectedFood.value.servingSize || 100)
  return Math.round((selectedFood.value.fat || 0) * ratio * 10) / 10
})
const calculatedCarbs = computed(() => {
  if (!selectedFood.value || !servingAmount.value) return 0
  const ratio = actualGrams.value / (selectedFood.value.servingSize || 100)
  return Math.round((selectedFood.value.carbohydrates || 0) * ratio * 10) / 10
})

// 加载食物库
const loadFoods = async () => {
  loading.value = true
  try {
    const res = await userFoodApi.getList()
    myFoods.value = res.data || []
  } catch (error) {
    console.error('加载食物失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载今日记录（用于页面底部反馈）
const loadTodayRecords = async () => {
  try {
    const res = await nutritionApi.getByDate(today())
    if (res.code === 200 && res.data) {
      todayRecords.value = res.data
    }
  } catch (error) {
    console.error('加载今日记录失败:', error)
  }
}

// 选择食物
const selectFood = (food) => {
  selectedFood.value = food
  const units = food.units || []
  if (units.length > 0) {
    selectedUnitName.value = units[0].unitName
    servingAmount.value = 1
  } else {
    selectedUnitName.value = ''
    servingAmount.value = food.servingSize || 100
  }
}

const clearSelection = () => {
  selectedFood.value = null
  servingAmount.value = 100
  selectedUnitName.value = ''
}

const onUnitChange = () => {
  if (selectedUnitName.value) {
    servingAmount.value = 1
  } else {
    servingAmount.value = selectedFood.value?.servingSize || 100
  }
}

// 添加记录（保存后留在当前页面，不跳转）
const addRecord = async () => {
  if (!selectedFood.value) {
    showToast('请先选择食物')
    return
  }

  isSaving.value = true
  try {
    const ratio = actualGrams.value / (selectedFood.value.servingSize || 100)
    const saveData = {
      foodName: selectedFood.value.foodName,
      brand: selectedFood.value.brand,
      servingAmount: actualGrams.value,
      servingUnit: selectedFood.value.servingUnit || 'g',
      ...(selectedUnitName.value && {
        displayAmount: servingAmount.value,
        displayUnit: selectedUnitName.value
      }),
      calories: calculatedCalories.value,
      protein: calculatedProtein.value,
      fat: calculatedFat.value,
      carbohydrates: calculatedCarbs.value,
      fiber: Math.round((selectedFood.value.fiber || 0) * ratio * 10) / 10,
      sodium: Math.round((selectedFood.value.sodium || 0) * ratio),
      sugar: Math.round((selectedFood.value.sugar || 0) * ratio * 10) / 10,
      mealType: mealType.value,
      recordDate: today()
    }

    await nutritionApi.save(saveData)
    showToast(`已添加「${selectedFood.value.foodName}」到${currentMealLabel.value}`)
    clearSelection()
    await loadTodayRecords()
  } catch (error) {
    showToast('添加失败: ' + (error.message || '未知错误'))
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  mealType.value = getMealTypeByTime()
  isAutoMeal.value = true
  loadFoods()
  loadTodayRecords()
})
</script>

<style scoped>
.quick-record-page {
  padding: calc(var(--safe-top) + 14px) 16px calc(var(--tab-h) + var(--safe-bottom) + 90px);
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 24%);
  min-height: 100vh;
}

/* 标题区 */
.page-header { margin-bottom: 16px; }
.header-title { font-size: 22px; font-weight: 700; color: var(--text-1); }
.header-sub { font-size: 13px; color: var(--text-3); margin-top: 4px; }

/* 用餐类型 */
.meal-type-section {
  background: var(--card);
  padding: 14px;
  border-radius: var(--radius-lg);
  margin-bottom: 14px;
  box-shadow: var(--shadow-xs);
}
.meal-types { display: flex; flex-wrap: wrap; gap: 8px; }
.meal-type {
  padding: 8px 16px;
  background: var(--fill);
  border-radius: 999px;
  font-size: 14px;
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.2s;
}
.meal-type.active {
  background: var(--primary-gradient);
  color: #fff;
  box-shadow: 0 4px 12px rgba(67, 160, 71, 0.3);
}

/* 搜索 */
.search-section { margin-bottom: 12px; }
.search-input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--text-1);
  background: var(--card);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-input::placeholder { color: var(--text-4); }
.search-input:focus { border-color: var(--primary); box-shadow: 0 0 0 4px var(--primary-100); }

/* 食物列表 */
.food-list {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 6px 0;
  max-height: 320px;
  overflow-y: auto;
  box-shadow: var(--shadow-xs);
}
.loading, .empty { text-align: center; padding: 32px; color: var(--text-3); }
.empty-text { font-size: 15px; }
.empty-tip { font-size: 13px; color: var(--primary); margin-top: 8px; }

.food-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--divider);
  cursor: pointer;
  transition: background 0.15s;
}
.food-item:last-child { border-bottom: none; }
.food-item.selected { background: var(--primary-50); }
.food-info { flex: 1; min-width: 0; }
.food-name { font-size: 15px; color: var(--text-1); font-weight: 500; }
.food-brand { font-size: 12px; color: var(--text-3); margin-top: 2px; }
.food-serving { font-size: 12px; color: var(--text-3); margin-top: 4px; }
.has-units { color: #3b82f6; font-size: 11px; }
.food-calories { font-size: 14px; color: var(--primary); font-weight: 600; flex-shrink: 0; margin-left: 12px; }

/* 食用量面板 */
.serving-section {
  background: var(--card);
  padding: 16px;
  border-radius: var(--radius-lg);
  margin-top: 14px;
  box-shadow: var(--shadow-sm);
}
.serving-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.serving-name { font-size: 16px; font-weight: 600; color: var(--text-1); }
.btn-clear {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--fill);
  color: var(--text-2);
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-clear:active { transform: scale(0.9); }

.amount-input-group { display: flex; gap: 10px; }
.amount-input {
  flex: 1;
  padding: 12px 14px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  font-size: 16px;
  color: var(--text-1);
  background: var(--card);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.amount-input:focus { border-color: var(--primary); box-shadow: 0 0 0 4px var(--primary-100); }
.unit-select {
  padding: 12px 14px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  font-size: 14px;
  color: var(--text-1);
  background: var(--card);
  outline: none;
  min-width: 90px;
}

.unit-hint {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 10px;
}
.unit-total { color: var(--primary-600); font-weight: 500; }

.nutrition-preview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-top: 14px;
}
.preview-item {
  background: var(--primary-50);
  border-radius: var(--radius-sm);
  padding: 10px 6px;
  text-align: center;
}
.preview-label { display: block; font-size: 11px; color: var(--text-3); margin-bottom: 4px; }
.preview-value { font-size: 14px; font-weight: 600; color: var(--text-1); }

/* 添加按钮栏 */
.add-bar {
  position: fixed;
  bottom: calc(var(--tab-h) + var(--safe-bottom) + 14px);
  left: 16px;
  right: 16px;
  z-index: 50;
}
.btn-add {
  width: 100%;
  padding: 15px;
  border-radius: var(--radius-lg);
  background: var(--primary-gradient);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  box-shadow: var(--shadow-primary);
  transition: transform 0.12s, opacity 0.2s;
}
.btn-add:active { transform: scale(0.98); }
.btn-add:disabled { opacity: 0.5; box-shadow: none; cursor: not-allowed; }

/* 今日记录 */
.today-section {
  margin-top: 18px;
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 14px 16px;
  box-shadow: var(--shadow-xs);
}
.today-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.today-title { font-size: 15px; font-weight: 600; color: var(--text-1); }
.today-calories { font-size: 13px; color: var(--primary); font-weight: 600; }
.today-list { display: flex; flex-direction: column; gap: 8px; }
.today-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.today-meal {
  font-size: 11px;
  color: var(--primary-600);
  background: var(--primary-50);
  padding: 2px 8px;
  border-radius: 6px;
  flex-shrink: 0;
}
.today-name { flex: 1; color: var(--text-1); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.today-amount { color: var(--text-3); flex-shrink: 0; }
.today-cal { color: var(--primary); font-weight: 600; flex-shrink: 0; }

/* 展开过渡 */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.25s ease; }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(12px); }
</style>
