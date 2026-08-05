<template>
  <div class="food-edit-page">
    <!-- 返回按钮 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <span class="page-title">{{ isNew ? '添加食物' : '编辑食物' }}</span>
    </div>

    <!-- 食物基本信息 -->
    <div class="section">
      <div class="section-title">基本信息</div>
      <div class="form-item">
        <label>食物名称 *</label>
        <div class="input-with-button">
          <input v-model="foodData.foodName" type="text" placeholder="请输入食物名称" />
          <button
            class="btn-ai-fill"
            :disabled="!foodData.foodName.trim() || isAiFilling"
            @click="handleAiFill"
          >
            {{ isAiFilling ? '填充中...' : '🤖 AI填充' }}
          </button>
        </div>
      </div>
      <div class="form-item">
        <label>品牌</label>
        <input v-model="foodData.brand" type="text" placeholder="品牌（可选）" />
      </div>
      <div class="form-row">
        <div class="form-item">
          <label>基准份量</label>
          <input v-model.number="foodData.servingSize" type="number" placeholder="100" />
        </div>
        <div class="form-item">
          <label>基准单位</label>
          <select v-model="foodData.servingUnit">
            <option value="g">克(g)</option>
            <option value="ml">毫升(ml)</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 常用单位 -->
    <div class="section">
      <FoodUnitEditor v-model="foodData.units" :serving-unit="foodData.servingUnit" />
    </div>

    <!-- 营养成分 -->
    <div class="section">
      <div class="section-title">营养成分 (每 {{ foodData.servingSize || 100 }}{{ foodData.servingUnit || 'g' }})</div>

      <div class="nutrition-grid">
        <div class="nutrition-item">
          <label>热量 (kcal)</label>
          <input v-model.number="foodData.calories" type="number" min="0" placeholder="0" />
        </div>
        <div class="nutrition-item">
          <label>蛋白质 (g)</label>
          <input v-model.number="foodData.protein" type="number" min="0" step="0.1" placeholder="0" />
        </div>
        <div class="nutrition-item">
          <label>脂肪 (g)</label>
          <input v-model.number="foodData.fat" type="number" min="0" step="0.1" placeholder="0" />
        </div>
        <div class="nutrition-item">
          <label>碳水 (g)</label>
          <input v-model.number="foodData.carbohydrates" type="number" min="0" step="0.1" placeholder="0" />
        </div>
        <div class="nutrition-item">
          <label>膳食纤维 (g)</label>
          <input v-model.number="foodData.fiber" type="number" min="0" step="0.1" placeholder="0" />
        </div>
        <div class="nutrition-item">
          <label>糖 (g)</label>
          <input v-model.number="foodData.sugar" type="number" min="0" step="0.1" placeholder="0" />
        </div>
        <div class="nutrition-item">
          <label>钠 (mg)</label>
          <input v-model.number="foodData.sodium" type="number" min="0" placeholder="0" />
        </div>
      </div>
    </div>

    <!-- 营养预览 -->
    <div class="section preview-section">
      <div class="section-title">营养占比预览</div>
      <div class="macro-chart">
        <div class="macro-bar">
          <div class="macro-segment protein" :style="{ width: proteinPercent + '%' }"></div>
          <div class="macro-segment fat" :style="{ width: fatPercent + '%' }"></div>
          <div class="macro-segment carbs" :style="{ width: carbsPercent + '%' }"></div>
        </div>
        <div class="macro-legend">
          <div class="legend-item">
            <span class="legend-dot protein"></span>
            <span>蛋白质 {{ foodData.protein || 0 }}g ({{ proteinPercent }}%)</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot fat"></span>
            <span>脂肪 {{ foodData.fat || 0 }}g ({{ fatPercent }}%)</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot carbs"></span>
            <span>碳水 {{ foodData.carbohydrates || 0 }}g ({{ carbsPercent }}%)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="btn-delete" v-if="!isNew" @click="handleDelete">删除食物</button>
      <button class="btn-save" @click="handleSave" :disabled="isSaving">
        {{ isSaving ? '保存中...' : '保存' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userFoodApi } from '@/services/api/food.js'
import { aiApi } from '@/services/api/ai.js'
import FoodUnitEditor from '@/components/FoodUnitEditor.vue'

const router = useRouter()
const route = useRoute()

// 状态
const isNew = ref(true)
const foodId = ref(null)
const isSaving = ref(false)
const isAiFilling = ref(false)

// 食物数据
const foodData = ref({
  foodName: '',
  brand: '',
  servingSize: 100,
  servingUnit: 'g',
  calories: null,
  protein: null,
  fat: null,
  carbohydrates: null,
  fiber: null,
  sugar: null,
  sodium: null,
  units: [] // 常用单位列表
})

// 计算碳氮脂比例
const totalMacro = computed(() => {
  const protein = foodData.value.protein || 0
  const fat = foodData.value.fat || 0
  const carbs = foodData.value.carbohydrates || 0
  return protein + fat + carbs
})

const proteinPercent = computed(() => {
  if (totalMacro.value === 0) return 0
  return Math.round((foodData.value.protein || 0) / totalMacro.value * 100)
})

const fatPercent = computed(() => {
  if (totalMacro.value === 0) return 0
  return Math.round((foodData.value.fat || 0) / totalMacro.value * 100)
})

const carbsPercent = computed(() => {
  if (totalMacro.value === 0) return 0
  return Math.round((foodData.value.carbohydrates || 0) / totalMacro.value * 100)
})

// AI 智能填充
const handleAiFill = async () => {
  if (!foodData.value.foodName.trim()) return

  isAiFilling.value = true
  try {
    const response = await aiApi.fillNutrition(foodData.value.foodName.trim())
    if (response.code === 200 && response.data) {
      const info = response.data
      if (info.nutrition) {
        foodData.value.calories = info.nutrition.energyKcal || null
        foodData.value.protein = info.nutrition.protein || null
        foodData.value.fat = info.nutrition.fat || null
        foodData.value.carbohydrates = info.nutrition.carbohydrate || null
      }
      if (info.servingSize) {
        foodData.value.servingSize = info.servingSize
      }
    } else {
      alert('AI 填充失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    alert('AI 填充失败: ' + error.message)
  } finally {
    isAiFilling.value = false
  }
}

// 保存食物
const handleSave = async () => {
  if (!foodData.value.foodName?.trim()) {
    alert('请输入食物名称')
    return
  }

  // 过滤掉空的单位
  const validUnits = (foodData.value.units || []).filter(u => u.unitName && u.unitValue)

  isSaving.value = true
  try {
    const saveData = {
      ...foodData.value,
      units: validUnits
    }

    if (isNew.value) {
      await userFoodApi.create(saveData)
    } else {
      await userFoodApi.update(foodId.value, saveData)
    }
    alert('保存成功！')
    router.push('/food/library')
  } catch (error) {
    alert('保存失败: ' + error.message)
  } finally {
    isSaving.value = false
  }
}

// 删除食物
const handleDelete = async () => {
  if (!confirm('确定要删除这个食物吗？')) return

  try {
    await userFoodApi.delete(foodId.value)
    alert('删除成功！')
    router.push('/food/library')
  } catch (error) {
    alert('删除失败: ' + error.message)
  }
}

const goBack = () => router.back()

// 加载食物数据
const loadFoodData = async (id) => {
  try {
    const response = await userFoodApi.getById(id)
    if (response.data) {
      foodData.value = {
        foodName: response.data.foodName || '',
        brand: response.data.brand || '',
        servingSize: response.data.servingSize || 100,
        servingUnit: response.data.servingUnit || 'g',
        calories: response.data.calories || null,
        protein: response.data.protein || null,
        fat: response.data.fat || null,
        carbohydrates: response.data.carbohydrates || null,
        fiber: response.data.fiber || null,
        sugar: response.data.sugar || null,
        sodium: response.data.sodium || null,
        units: response.data.units || []
      }
    }
  } catch (error) {
    console.error('加载食物数据失败:', error)
    alert('加载失败，请重试')
    router.back()
  }
}

onMounted(() => {
  if (route.params.id) {
    isNew.value = false
    foodId.value = route.params.id
    loadFoodData(route.params.id)
  }
})
</script>

<style scoped>
.food-edit-page {
  padding: calc(var(--safe-top) + 14px) 16px calc(var(--tab-h) + var(--safe-bottom) + 28px);
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 24%);
  min-height: 100vh;
}

.page-header { display: flex; align-items: center; margin-bottom: 18px; }
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

.section {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-xs);
}
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.section-title { font-size: 13px; color: var(--text-3); margin-bottom: 12px; font-weight: 500; letter-spacing: 0.3px; }
.section-header .section-title { margin-bottom: 0; }

.form-item { margin-bottom: 14px; }
.form-item:last-child { margin-bottom: 0; }
.form-item label { display: block; font-size: 13px; color: var(--text-2); margin-bottom: 6px; font-weight: 500; }
.form-item input,
.form-item select {
  width: 100%;
  padding: 11px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-1);
  background: var(--bg-soft);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.form-item input:focus,
.form-item select:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 4px var(--primary-100); }

.form-row { display: flex; gap: 12px; }
.form-row .form-item { flex: 1; }

/* AI 填充按钮 */
.input-with-button { display: flex; gap: 8px; }
.input-with-button input { flex: 1; }
.btn-ai-fill {
  padding: 10px 12px;
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.2s, transform 0.12s;
}
.btn-ai-fill:active { transform: scale(0.95); }
.btn-ai-fill:disabled { background: var(--text-4); cursor: not-allowed; }

/* 常用单位 */
.unit-tip { font-size: 12px; color: var(--text-3); margin-bottom: 12px; }
.btn-add-unit {
  padding: 7px 14px;
  background: var(--primary-gradient);
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.12s;
}
.btn-add-unit:active { transform: scale(0.95); }

.units-list { display: flex; flex-direction: column; gap: 12px; }
.unit-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  padding: 12px;
  background: var(--fill);
  border-radius: var(--radius);
}
.unit-inputs { flex: 1; display: flex; gap: 12px; }
.unit-input-group { flex: 1; }
.unit-input-group label { display: block; font-size: 12px; color: var(--text-3); margin-bottom: 4px; }
.unit-input-group input {
  width: 100%;
  padding: 9px 10px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-1);
  background: var(--card);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.unit-input-group input:focus { border-color: var(--primary); box-shadow: 0 0 0 4px var(--primary-100); }
.unit-value-input { display: flex; align-items: center; gap: 6px; }
.unit-value-input input { flex: 1; }
.unit-base { font-size: 14px; color: var(--text-2); white-space: nowrap; }
.btn-remove-unit {
  padding: 8px 12px;
  background: transparent;
  color: var(--danger);
  border: 1.5px solid var(--danger);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-remove-unit:active { background: #fef2f2; }
.no-units { text-align: center; padding: 16px; color: var(--text-3); font-size: 13px; }

/* 营养成分网格 */
.nutrition-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.nutrition-item label { font-size: 12px; color: var(--text-3); margin-bottom: 4px; display: block; }
.nutrition-item input {
  width: 100%;
  padding: 9px 10px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-1);
  background: var(--bg-soft);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.nutrition-item input:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 4px var(--primary-100); }

/* 营养预览 */
.preview-section { background: var(--primary-50); }
.macro-chart { margin-top: 8px; }
.macro-bar { height: 22px; background: rgba(0,0,0,0.05); border-radius: 11px; display: flex; overflow: hidden; }
.macro-segment { height: 100%; transition: width 0.3s; }
.macro-segment.protein { background: var(--protein); }
.macro-segment.fat { background: var(--fat); }
.macro-segment.carbs { background: var(--carbs); }
.macro-legend { display: flex; flex-wrap: wrap; gap: 14px; margin-top: 12px; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-2); }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; }
.legend-dot.protein { background: var(--protein); }
.legend-dot.fat { background: var(--fat); }
.legend-dot.carbs { background: var(--carbs); }

/* 操作按钮 */
.action-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-save {
  flex: 1;
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
.btn-delete {
  padding: 15px 20px;
  background: var(--card);
  color: var(--danger);
  border: 1.5px solid var(--danger);
  border-radius: var(--radius);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, transform 0.12s;
}
.btn-delete:active { transform: scale(0.97); background: #fef2f2; }
</style>