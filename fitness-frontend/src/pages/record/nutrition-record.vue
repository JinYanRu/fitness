<template>
  <div class="nutrition-record-page">
    <!-- 返回按钮 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <span class="page-title">{{ editId ? '编辑记录' : '添加记录' }}</span>
    </div>

    <!-- 用餐类型选择 -->
    <div class="meal-type-section">
      <div class="section-title">用餐类型</div>
      <div class="meal-types">
        <div
          v-for="meal in mealTypes"
          :key="meal.value"
          :class="['meal-type', formData.mealType === meal.value ? 'active' : '']"
          @click="formData.mealType = meal.value"
        >
          {{ meal.icon }} {{ meal.label }}
        </div>
      </div>
    </div>

    <!-- 记录日期 -->
    <div class="record-date-section">
      <div class="section-title">记录日期</div>
      <div class="date-pickable">
        <span class="date-main">{{ recordDateMain }}</span>
        <span class="date-sub">{{ formData.recordDate }}</span>
        <input
          type="date"
          class="date-native-input"
          :value="formData.recordDate"
          :max="todayDateStr"
          @change="formData.recordDate = $event.target.value"
        />
      </div>
    </div>

    <!-- 搜索食物（仅新增模式显示） -->
    <div v-if="!editId" class="search-section">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索食物名称"
        class="search-input"
        @input="searchFoods"
      />
    </div>

    <!-- 食物列表（仅新增模式显示） -->
    <div v-if="!editId" class="food-list">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="filteredFoods.length === 0" class="empty">
        <p>暂无食物</p>
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

    <!-- 食用量输入 -->
    <div v-if="selectedFood" class="serving-section">
      <div class="section-title">食用量</div>

      <!-- 单位选择器 -->
      <div class="amount-input-group">
        <input
          v-model.number="servingAmount"
          type="number"
          min="0"
          step="1"
          class="amount-input"
        />
        <select v-model="selectedUnitName" class="unit-select" @change="onUnitChange">
          <!-- 快捷单位优先展示 -->
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

      <!-- 单位换算提示 -->
      <div v-if="selectedUnitName && currentUnitValue" class="unit-hint">
        1{{ selectedUnitName }} = {{ currentUnitValue }}{{ selectedFood.servingUnit || 'g' }}
        <span v-if="servingAmount" class="unit-total">
          = {{ actualGrams }}{{ selectedFood.servingUnit || 'g' }}
        </span>
      </div>

      <!-- 营养预览 -->
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

    <!-- 保存按钮 -->
    <div class="save-section">
      <button class="btn-save" @click="saveRecord" :disabled="!selectedFood || isSaving">
        {{ isSaving ? '保存中...' : '保存记录' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { nutritionApi } from '@/services/api/nutrition.js'
import { userFoodApi } from '@/services/api/food.js'
import { today, addDays, parseDate } from '@/utils/date.js'

const router = useRouter()
const route = useRoute()

// 用餐类型
const mealTypes = [
  { value: 'breakfast', label: '早餐', icon: '🌅' },
  { value: 'lunch', label: '午餐', icon: '☀️' },
  { value: 'dinner', label: '晚餐', icon: '🌙' },
  { value: 'snack', label: '加餐', icon: '🍪' },
  { value: 'workout', label: '健身餐', icon: '💪' }
]

// 表单数据（recordDate 优先取首页传入的日期，默认今天）
const formData = ref({
  mealType: 'lunch',
  recordDate: route.query.date || today()
})

// 食物选择相关
const searchKeyword = ref('')
const myFoods = ref([])
const loading = ref(false)
const selectedFood = ref(null)
const servingAmount = ref(100)
const selectedUnitName = ref('') // 选中的单位名称

// 保存状态
const isSaving = ref(false)
const editId = ref(null)

// 记录日期展示
const todayDateStr = today()
const recordDateMain = computed(() => {
  const date = formData.value.recordDate
  if (date === todayDateStr) return '今天'
  if (date === addDays(todayDateStr, -1)) return '昨天'
  const d = parseDate(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

// 可用单位列表（不包含基准单位）
const availableUnits = computed(() => {
  if (!selectedFood.value?.units) return []
  return selectedFood.value.units
})

// 当前选中单位的换算值
const currentUnitValue = computed(() => {
  if (!selectedUnitName.value || !selectedFood.value?.units) return null
  const unit = selectedFood.value.units.find(u => u.unitName === selectedUnitName.value)
  return unit?.unitValue || null
})

// 实际克数（根据单位换算）
const actualGrams = computed(() => {
  if (!selectedUnitName.value || !currentUnitValue.value) {
    return servingAmount.value
  }
  return servingAmount.value * currentUnitValue.value
})

// 计算属性
const filteredFoods = computed(() => {
  if (!searchKeyword.value) return myFoods.value
  const keyword = searchKeyword.value.toLowerCase()
  return myFoods.value.filter(f =>
    f.foodName?.toLowerCase().includes(keyword) ||
    f.brand?.toLowerCase().includes(keyword)
  )
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

// 加载食物数据
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

// 搜索食物（本地搜索，通过 filteredFoods 计算）
const searchFoods = () => {
  // 本地搜索已通过 filteredFoods 计算属性实现
}

// 选择食物
const selectFood = (food) => {
  selectedFood.value = food
  // 有自定义(快捷)单位时，默认选中第一个自定义单位
  const units = food.units || []
  if (units.length > 0) {
    selectedUnitName.value = units[0].unitName
    servingAmount.value = 1 // 自定义单位默认数量为 1（如：1包）
  } else {
    // 没有自定义单位，使用基准单位
    selectedUnitName.value = ''
    servingAmount.value = food.servingSize || 100
  }
}

// 单位切换时，重置数量为1（如果切换到自定义单位）
const onUnitChange = () => {
  if (selectedUnitName.value) {
    // 切换到自定义单位时，默认数量为1
    servingAmount.value = 1
  } else {
    // 切换回基准单位时，恢复为基准份量
    servingAmount.value = selectedFood.value?.servingSize || 100
  }
}

// 保存记录
const saveRecord = async () => {
  if (!selectedFood.value) {
    alert('请先选择食物')
    return
  }

  isSaving.value = true
  try {
    const ratio = actualGrams.value / (selectedFood.value.servingSize || 100)
    const saveData = {
      foodName: selectedFood.value.foodName,
      brand: selectedFood.value.brand,
      servingAmount: actualGrams.value, // 保存实际克数
      servingUnit: selectedFood.value.servingUnit || 'g',
      // 如果使用了自定义单位，记录额外信息
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
      mealType: formData.value.mealType,
      recordDate: formData.value.recordDate
    }

    if (editId.value) {
      await nutritionApi.update(editId.value, saveData)
    } else {
      await nutritionApi.save(saveData)
    }
    alert('保存成功！')
    router.push('/')
  } catch (error) {
    alert('保存失败: ' + error.message)
  } finally {
    isSaving.value = false
  }
}

const goBack = () => router.back()

// 加载编辑数据
const loadEditData = async (id) => {
  try {
    const res = await nutritionApi.getById(id)
    const record = res.data
    if (!record) return

    // 填充用餐类型和日期
    formData.value.mealType = record.mealType || 'lunch'
    formData.value.recordDate = record.recordDate || today()

    // 将记录中的营养值反算为"每 servingSize 单位"的基准值
    // 这样用户修改食用量时，计算属性能正确重新计算
    const amount = record.servingAmount || 100
    const baseRatio = 100 / amount // 反算到每 100g 的比例

    // 还原快捷单位：若记录用了快捷单位，按 记录克数 ÷ 数量 反推 unitValue，
    // 保证编辑时展示原单位，且 actualGrams = 数量 × unitValue = 原克数，营养与记录一致
    let units = []
    const dispCnt = Number(record.displayAmount)
    if (record.displayUnit && dispCnt > 0) {
      units = [{
        unitName: record.displayUnit,
        unitValue: amount / dispCnt,
        isDefault: true
      }]
    }

    // 构造 selectedFood 对象，用于显示已选食物和计算营养
    selectedFood.value = {
      id: record.foodId || record.id,
      foodName: record.foodName,
      brand: record.brand,
      servingSize: 100, // 统一以 100g 为基准
      servingUnit: record.servingUnit || 'g',
      calories: Math.round((record.calories || 0) * baseRatio),
      protein: Math.round((record.protein || 0) * baseRatio * 10) / 10,
      fat: Math.round((record.fat || 0) * baseRatio * 10) / 10,
      carbohydrates: Math.round((record.carbohydrates || 0) * baseRatio * 10) / 10,
      fiber: record.fiber != null ? Math.round(record.fiber * baseRatio * 10) / 10 : null,
      sodium: record.sodium != null ? Math.round(record.sodium * baseRatio) : null,
      sugar: record.sugar != null ? Math.round(record.sugar * baseRatio * 10) / 10 : null,
      units: units
    }

    // 还原单位选择和数量：有快捷单位则选中原单位、数量取记录的数量；否则按克
    if (units.length > 0) {
      selectedUnitName.value = record.displayUnit
      servingAmount.value = dispCnt
    } else {
      selectedUnitName.value = ''
      servingAmount.value = amount
    }
  } catch (error) {
    console.error('加载编辑数据失败:', error)
    alert('加载记录数据失败，请重试')
  }
}

onMounted(() => {
  if (route.params.id) {
    editId.value = route.params.id
    // 加载编辑数据
    loadEditData(route.params.id)
  }

  // 如果从食物库跳转过来，预选食物
  if (route.query.foodId && route.query.foodName) {
    // 先加载食物列表，然后选中指定食物
    loadFoods().then(() => {
      const food = myFoods.value.find(f => f.id == route.query.foodId)
      if (food) {
        selectFood(food)
      }
    })
  } else if (!route.params.id) {
    // 新增模式才加载食物列表，编辑模式不需要（已有选中食物）
    loadFoods()
  }
})
</script>

<style scoped>
.nutrition-record-page {
  padding: calc(var(--safe-top) + 14px) 16px calc(var(--safe-bottom) + 24px);
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 24%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}
.back-btn {
  background: var(--card);
  border: none;
  font-size: 15px;
  color: var(--primary);
  cursor: pointer;
  padding: 6px 14px 6px 10px;
  border-radius: 999px;
  box-shadow: var(--shadow-xs);
  transition: transform 0.12s;
}
.back-btn:active { transform: scale(0.95); }
.page-title { font-size: 18px; font-weight: 600; margin-left: 12px; color: var(--text-1); }

/* 用餐类型 */
.meal-type-section {
  background: var(--card);
  padding: 16px;
  border-radius: var(--radius-lg);
  margin-bottom: 14px;
  box-shadow: var(--shadow-xs);
}
.section-title { font-size: 13px; color: var(--text-3); margin-bottom: 12px; font-weight: 500; }
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
.meal-type.active { background: var(--primary-gradient); color: #fff; box-shadow: 0 4px 12px rgba(67,160,71,0.3); }

/* 记录日期 */
.record-date-section {
  background: var(--card);
  padding: 16px;
  border-radius: var(--radius-lg);
  margin-bottom: 14px;
  box-shadow: var(--shadow-xs);
}
.date-pickable {
  position: relative;
  display: flex;
  align-items: baseline;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0;
}
.date-main { font-size: 16px; font-weight: 600; color: var(--text-1); }
.date-sub { font-size: 13px; color: var(--text-3); }
.date-native-input {
  position: absolute; inset: 0; width: 100%; height: 100%;
  opacity: 0; border: none; background: transparent; cursor: pointer;
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
  max-height: 300px;
  overflow-y: auto;
  box-shadow: var(--shadow-xs);
}
.loading, .empty { text-align: center; padding: 32px; color: var(--text-3); }
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

/* 食用量 */
.serving-section {
  background: var(--card);
  padding: 16px;
  border-radius: var(--radius-lg);
  margin-top: 14px;
  box-shadow: var(--shadow-xs);
}
.amount-input-group { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.amount-input {
  width: 110px;
  padding: 11px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 16px;
  color: var(--text-1);
  background: var(--bg-soft);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.amount-input:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 4px var(--primary-100); }
.unit-select {
  padding: 11px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-1);
  background: var(--bg-soft);
  outline: none;
  cursor: pointer;
  min-width: 90px;
}
.unit-hint {
  font-size: 12px;
  color: var(--text-2);
  margin-bottom: 12px;
  padding: 8px 12px;
  background: var(--fill);
  border-radius: var(--radius-sm);
}
.unit-total { color: var(--primary); font-weight: 600; }

.nutrition-preview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  background: var(--fill);
  padding: 14px;
  border-radius: var(--radius);
}
.preview-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.preview-label { font-size: 12px; color: var(--text-3); }
.preview-value { font-size: 17px; font-weight: 700; color: var(--text-1); }

/* 保存按钮 */
.save-section { margin-top: 24px; }
.btn-save {
  width: 100%;
  padding: 15px;
  background: var(--primary-gradient);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-size: 16px;
  font-weight: 600;
  box-shadow: var(--shadow-primary);
  cursor: pointer;
  transition: transform 0.12s;
}
.btn-save:active { transform: scale(0.98); }
.btn-save:disabled { background: var(--text-4); box-shadow: none; cursor: not-allowed; }
</style>