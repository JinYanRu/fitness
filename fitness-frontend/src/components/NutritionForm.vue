<template>
  <div class="nutrition-form">
    <!-- 食物基本信息 -->
    <div class="form-section">
      <div class="section-title">基本信息</div>

      <!-- 食物名称 -->
      <div class="form-item">
        <label class="form-label">食物名称 *</label>
        <input
          class="form-input"
          v-model="formData.foodName"
          placeholder="请输入食物名称"
          maxlength="50"
        />
      </div>

      <!-- 食物类型 -->
      <div class="form-item">
        <label class="form-label">食物类型</label>
        <select class="form-select" v-model="formData.foodType">
          <option value="">请选择类型</option>
          <option v-for="type in foodTypes" :key="type.value" :value="type.value">
            {{ type.label }}
          </option>
        </select>
      </div>

      <!-- 份量 -->
      <div class="form-item">
        <label class="form-label">份量</label>
        <div class="input-group">
          <input
            class="form-input number-input"
            v-model="formData.servingSize"
            type="number"
            placeholder="份量"
            step="0.1"
          />
          <select class="unit-select" v-model="formData.servingUnit">
            <option v-for="unit in servingUnits" :key="unit" :value="unit">{{ unit }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 营养成分信息 -->
    <div class="form-section">
      <div class="section-title">营养成分</div>

      <!-- 热量 -->
      <div class="form-item">
        <label class="form-label">热量 (kcal)</label>
        <input
          class="form-input number-input"
          v-model="formData.calories"
          type="number"
          placeholder="热量"
          step="0.1"
        />
      </div>

      <!-- 蛋白质 -->
      <div class="form-item">
        <label class="form-label">蛋白质 (g)</label>
        <input
          class="form-input number-input"
          v-model="formData.protein"
          type="number"
          placeholder="蛋白质"
          step="0.1"
        />
      </div>

      <!-- 脂肪 -->
      <div class="form-item">
        <label class="form-label">脂肪 (g)</label>
        <input
          class="form-input number-input"
          v-model="formData.fat"
          type="number"
          placeholder="脂肪"
          step="0.1"
        />
      </div>

      <!-- 碳水化合物 -->
      <div class="form-item">
        <label class="form-label">碳水化合物 (g)</label>
        <input
          class="form-input number-input"
          v-model="formData.carbohydrates"
          type="number"
          placeholder="碳水化合物"
          step="0.1"
        />
      </div>

      <!-- 膳食纤维 -->
      <div class="form-item">
        <label class="form-label">膳食纤维 (g)</label>
        <input
          class="form-input number-input"
          v-model="formData.fiber"
          type="number"
          placeholder="膳食纤维"
          step="0.1"
        />
      </div>

      <!-- 钠 -->
      <div class="form-item">
        <label class="form-label">钠 (mg)</label>
        <input
          class="form-input number-input"
          v-model="formData.sodium"
          type="number"
          placeholder="钠"
          step="0.1"
        />
      </div>

      <!-- 糖 -->
      <div class="form-item">
        <label class="form-label">糖 (g)</label>
        <input
          class="form-input number-input"
          v-model="formData.sugar"
          type="number"
          placeholder="糖"
          step="0.1"
        />
      </div>
    </div>

    <!-- 原始识别文本 -->
    <div class="form-section raw-text-section" v-if="showRawText && formData.rawText">
      <div class="section-header">
        <div class="section-title">原始识别文本</div>
        <span class="toggle-btn" @click="toggleRawText">
          {{ showRawTextContent ? '收起' : '展开' }}
        </span>
      </div>
      <div class="raw-text-content" v-if="showRawTextContent">
        <pre class="raw-text">{{ formData.rawText }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, defineProps, defineEmits, defineExpose } from 'vue'

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({})
  },
  ocrResult: {
    type: Object,
    default: () => null
  },
  showRawText: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update', 'submit', 'cancel'])

// 食物类型选项
const foodTypes = [
  { label: '主食', value: 'staple' },
  { label: '蔬菜', value: 'vegetable' },
  { label: '水果', value: 'fruit' },
  { label: '肉类', value: 'meat' },
  { label: '乳制品', value: 'dairy' },
  { label: '零食', value: 'snack' },
  { label: '饮料', value: 'drink' },
  { label: '其他', value: 'other' }
]

const servingUnits = ['g', 'ml', 'kg', 'L', '份', '个']

// 表单数据
const formData = reactive({
  foodName: '',
  foodType: '',
  servingSize: '',
  servingUnit: 'g',
  calories: '',
  protein: '',
  fat: '',
  carbohydrates: '',
  fiber: '',
  sodium: '',
  sugar: '',
  rawText: ''
})

// 展开状态
const showRawTextContent = ref(false)

/**
 * 从 OCR 结果填充数据
 */
const fillFromOCR = (result) => {
  const text = result.text || result.full_text || ''
  const foodInfo = result.foodInfo || {}

  console.log('[NutritionForm] OCR解析结果:', foodInfo)

  // 填充食物基本信息
  if (foodInfo.foodName) {
    formData.foodName = foodInfo.foodName
  }
  if (foodInfo.foodCategory) {
    formData.foodType = foodInfo.foodCategory
  }
  if (foodInfo.servingSize) {
    formData.servingSize = foodInfo.servingSize
  }

  // 填充营养成分数据（直接使用数值，无需提取）
  const nutrition = foodInfo.nutrition || {}
  if (nutrition.energyKcal) {
    formData.calories = nutrition.energyKcal
  } else if (nutrition.energyKj) {
    // 如果只有千焦，转换为千卡
    formData.calories = Math.round(nutrition.energyKj / 4.184)
  }
  formData.protein = nutrition.protein || ''
  formData.fat = nutrition.fat || ''
  formData.carbohydrates = nutrition.carbohydrate || ''
  formData.fiber = nutrition.dietaryFiber || ''
  formData.sodium = nutrition.sodium || ''
  formData.sugar = nutrition.sugar || ''

  // 默认份量
  formData.servingUnit = 'g'
  formData.rawText = text

  console.log('[NutritionForm] 填充后的表单数据:', formData)
}

/**
 * 展开/收起原始文本
 */
const toggleRawText = () => {
  showRawTextContent.value = !showRawTextContent.value
}

// 监听 OCR 结果变化
watch(() => props.ocrResult, (result) => {
  console.log('[NutritionForm] watch 触发, ocrResult:', result)
  if (result?.success) {
    fillFromOCR(result)
  }
}, { immediate: true })

// 监听初始数据
watch(() => props.initialData, (data) => {
  if (data) {
    Object.assign(formData, data)
  }
}, { immediate: true, deep: true })

/**
 * 验证表单
 */
const validate = () => {
  if (!formData.foodName) {
    alert('请输入食物名称')
    return false
  }
  return true
}

/**
 * 获取表单数据
 */
const getData = () => {
  return {
    ...formData,
    foodName: formData.foodName,
    foodType: formData.foodType,
    servingSize: formData.servingSize ? parseFloat(formData.servingSize) : null,
    servingUnit: formData.servingUnit,
    calories: formData.calories ? parseFloat(formData.calories) : null,
    protein: formData.protein ? parseFloat(formData.protein) : null,
    fat: formData.fat ? parseFloat(formData.fat) : null,
    carbohydrates: formData.carbohydrates ? parseFloat(formData.carbohydrates) : null,
    fiber: formData.fiber ? parseFloat(formData.fiber) : null,
    sodium: formData.sodium ? parseFloat(formData.sodium) : null,
    sugar: formData.sugar ? parseFloat(formData.sugar) : null,
    rawText: formData.rawText
  }
}

defineExpose({
  validate,
  getData,
  fillFromOCR
})

// 监听数据变化
watch(formData, () => {
  emit('update', getData())
}, { deep: true })
</script>

<style scoped>
.nutrition-form {
  padding: 0;
}

.form-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 15px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item {
  margin-bottom: 15px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.form-input {
  width: 100%;
  height: 44px;
  background-color: #f8f8f8;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 15px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus {
  border-color: #4CAF50;
  background-color: #fff;
  outline: none;
}

.form-select {
  width: 100%;
  height: 44px;
  background-color: #f8f8f8;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 15px;
  font-size: 14px;
  cursor: pointer;
}

.number-input {
  text-align: right;
}

.input-group {
  display: flex;
  gap: 10px;
}

.input-group .form-input {
  flex: 1;
}

.unit-select {
  width: 80px;
  height: 44px;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 14px;
  cursor: pointer;
}

.raw-text-section {
  background-color: #f5f5f5;
}

.toggle-btn {
  font-size: 14px;
  color: #4CAF50;
  cursor: pointer;
}

.raw-text-content {
  margin-top: 10px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
}

.raw-text {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>