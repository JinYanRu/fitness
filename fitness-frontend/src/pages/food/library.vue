<template>
  <div class="food-library-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索食物名称或品牌"
        class="search-input"
        @input="handleSearch"
      />
    </div>

    <!-- 我的食物库 -->
    <div class="food-list">
      <div class="list-header">
        <span class="count">共 {{ myFoods.length }} 种食物</span>
        <div class="action-buttons">
          <button class="btn-ocr" @click="showOcrModal = true">📷 拍照添加</button>
          <button class="btn-ai" @click="showAiModal = true">🤖 AI识别</button>
          <button class="btn-add" @click="showAddModal = true">+ 手动添加</button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="myFoods.length === 0" class="empty">
        <p>暂无食物，点击上方按钮添加</p>
        <div class="empty-actions">
          <button class="btn-ocr-large" @click="showOcrModal = true">📷 拍照识别添加</button>
          <button class="btn-ai-large" @click="showAiModal = true">🤖 AI识别添加</button>
          <button class="btn-add-large" @click="showAddModal = true">✏️ 手动输入添加</button>
        </div>
      </div>

      <div v-else class="food-items">
        <div
          v-for="food in myFoods"
          :key="food.id"
          class="food-card"
          @click="selectFood(food)"
        >
          <div class="food-name">{{ food.foodName }}</div>
          <div v-if="food.brand" class="food-brand">{{ food.brand }}</div>
          <div class="food-nutrition">
            <span>热量: {{ food.calories || 0 }} kcal</span>
            <span>蛋白质: {{ food.protein || 0 }}g</span>
          </div>
          <div v-if="food.source === 'ocr'" class="food-source">📷 OCR识别</div>
        </div>
      </div>
    </div>

    <!-- 手动添加食物弹窗 -->
    <div v-if="showAddModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加食物</h3>
          <button class="btn-close" @click="showAddModal = false">×</button>
        </div>

        <div class="modal-body">
          <div class="form-item">
            <label>食物名称 *</label>
            <div class="input-with-button">
              <input v-model="newFood.foodName" type="text" placeholder="请输入食物名称" />
              <button
                class="btn-ai-fill"
                :disabled="!newFood.foodName.trim() || isAiFilling"
                @click="handleAiFill"
              >
                {{ isAiFilling ? '填充中...' : '🤖 AI填充' }}
              </button>
            </div>
          </div>

          <div class="form-item">
            <label>品牌</label>
            <input v-model="newFood.brand" type="text" placeholder="品牌（可选）" />
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>份量</label>
              <input v-model="newFood.servingSize" type="number" placeholder="100" />
            </div>
            <div class="form-item">
              <label>单位</label>
              <select v-model="newFood.servingUnit">
                <option value="g">克(g)</option>
                <option value="ml">毫升(ml)</option>
                <option value="份">份</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>热量 (kcal)</label>
              <input v-model.number="newFood.calories" type="number" />
            </div>
            <div class="form-item">
              <label>蛋白质 (g)</label>
              <input v-model.number="newFood.protein" type="number" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>脂肪 (g)</label>
              <input v-model.number="newFood.fat" type="number" />
            </div>
            <div class="form-item">
              <label>碳水 (g)</label>
              <input v-model.number="newFood.carbohydrates" type="number" />
            </div>
          </div>

          <div class="unit-editor-wrap">
            <FoodUnitEditor v-model="newFood.units" :serving-unit="newFood.servingUnit" />
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="showAddModal = false">取消</button>
          <button class="btn-confirm" @click="handleAddFood">保存</button>
        </div>
      </div>
    </div>

    <!-- 拍照添加弹窗 -->
    <div v-if="showOcrModal" class="modal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <h3>📷 拍照添加食物</h3>
          <button class="btn-close" @click="closeOcrModal">×</button>
        </div>

        <div class="modal-body">
          <!-- 步骤指示器 -->
          <div class="step-indicator">
            <div class="step" :class="{ active: ocrStep >= 1 }">
              <div class="step-dot">1</div>
              <span class="step-text">选择图片</span>
            </div>
            <div class="step-line" :class="{ active: ocrStep >= 2 }"></div>
            <div class="step" :class="{ active: ocrStep >= 2 }">
              <div class="step-dot">2</div>
              <span class="step-text">识别营养</span>
            </div>
            <div class="step-line" :class="{ active: ocrStep >= 3 }"></div>
            <div class="step" :class="{ active: ocrStep >= 3 }">
              <div class="step-dot">3</div>
              <span class="step-text">确认保存</span>
            </div>
          </div>

          <!-- 步骤1: 选择图片 -->
          <div v-show="ocrStep === 1" class="step-content">
            <ImagePicker
              ref="imagePickerRef"
              @change="handleImageChange"
              @error="handleImageError"
            />
            <div class="step-actions">
              <button class="btn-next" :disabled="!selectedImage" @click="startOcr">
                下一步：识别营养信息
              </button>
            </div>
          </div>

          <!-- 步骤2: OCR 识别 -->
          <div v-show="ocrStep === 2" class="step-content">
            <div class="image-preview-mini">
              <img :src="selectedImage" class="preview-img" />
            </div>
            <div class="ocr-status" v-if="isRecognizing">
              <div class="loading-spinner"></div>
              <span class="loading-text">正在识别中...</span>
            </div>
            <div class="ocr-result" v-if="ocrResult && !isRecognizing">
              <NutritionForm
                ref="ocrFormRef"
                :ocrResult="ocrResult"
                @update="handleOcrFormUpdate"
              />
            </div>
            <div class="step-actions" v-if="!isRecognizing">
              <button class="btn-secondary" @click="resetOcr">重新选择</button>
              <button class="btn-primary" @click="goToOcrConfirm" :disabled="!ocrResult?.success">
                下一步
              </button>
            </div>
          </div>

          <!-- 步骤3: 确认保存 -->
          <div v-show="ocrStep === 3" class="step-content">
            <NutritionForm
              ref="confirmFormRef"
              :initialData="ocrFormData"
              @update="handleOcrFormUpdate"
            />
            <div class="step-actions">
              <button class="btn-secondary" @click="ocrStep = 2">返回修改</button>
              <button class="btn-primary" @click="saveOcrFood" :disabled="isSaving">
                {{ isSaving ? '保存中...' : '保存到我的食物库' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI 识别弹窗 -->
    <div v-if="showAiModal" class="modal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <h3>🤖 AI 食谱识别</h3>
          <button class="btn-close" @click="closeAiModal">×</button>
        </div>

        <div class="modal-body">
          <!-- 输入方式切换 -->
          <div class="ai-tabs">
            <div :class="['ai-tab', aiInputMode === 'text' ? 'active' : '']" @click="aiInputMode = 'text'">
              📝 文本输入
            </div>
            <div :class="['ai-tab', aiInputMode === 'image' ? 'active' : '']" @click="aiInputMode = 'image'">
              📷 拍照识别
            </div>
          </div>

          <!-- 步骤指示器 -->
          <div class="step-indicator">
            <div class="step" :class="{ active: aiStep >= 1 }">
              <div class="step-dot">1</div>
              <span class="step-text">{{ aiInputMode === 'text' ? '输入食谱' : '选择图片' }}</span>
            </div>
            <div class="step-line" :class="{ active: aiStep >= 2 }"></div>
            <div class="step" :class="{ active: aiStep >= 2 }">
              <div class="step-dot">2</div>
              <span class="step-text">AI 分析</span>
            </div>
            <div class="step-line" :class="{ active: aiStep >= 3 }"></div>
            <div class="step" :class="{ active: aiStep >= 3 }">
              <div class="step-dot">3</div>
              <span class="step-text">确认保存</span>
            </div>
          </div>

          <!-- 步骤1: 输入 -->
          <div v-show="aiStep === 1" class="step-content">
            <!-- 文本输入模式 -->
            <div v-if="aiInputMode === 'text'" class="text-input-area">
              <textarea
                v-model="aiRecipeText"
                class="recipe-textarea"
                placeholder="请输入食谱内容，例如：
手工鸡胸肉丸子：
500g鸡胸肉
5克味精
5克盐
10克淀粉
..."
                rows="8"
              ></textarea>
            </div>

            <!-- 图片输入模式 -->
            <div v-else class="image-input-area">
              <ImagePicker
                ref="aiImagePickerRef"
                @change="handleAiImageChange"
                @error="handleAiImageError"
              />
              <!-- 补充文本输入 -->
              <div class="supplement-input">
                <label class="supplement-label">补充说明（可选）</label>
                <textarea
                  v-model="aiSupplementText"
                  class="supplement-textarea"
                  placeholder="如有缺失信息可在此补充，例如：
- 食谱名称
- 食材用量
- 烹饪方式等"
                  rows="3"
                ></textarea>
              </div>
            </div>

            <div class="step-actions">
              <button
                class="btn-next"
                :disabled="aiInputMode === 'text' ? !aiRecipeText.trim() : !aiSelectedImage"
                @click="startAiParse"
              >
                下一步：AI 分析营养
              </button>
            </div>
          </div>

          <!-- 步骤2: AI 分析 -->
          <div v-show="aiStep === 2" class="step-content">
            <div v-if="aiInputMode === 'image'" class="image-preview-mini">
              <img :src="aiSelectedImage" class="preview-img" />
            </div>
            <div class="ocr-status" v-if="isAiParsing">
              <div class="loading-spinner"></div>
              <span class="loading-text">AI 正在分析中...</span>
            </div>
            <div class="ocr-result" v-if="aiResult && !isAiParsing">
              <NutritionForm
                ref="aiFormRef"
                :ocrResult="aiResult"
                @update="handleAiFormUpdate"
              />
            </div>
            <div class="step-actions" v-if="!isAiParsing">
              <button class="btn-secondary" @click="resetAi">重新输入</button>
              <button class="btn-primary" @click="goToAiConfirm" :disabled="!aiResult?.success">
                下一步
              </button>
            </div>
          </div>

          <!-- 步骤3: 确认保存 -->
          <div v-show="aiStep === 3" class="step-content">
            <NutritionForm
              ref="aiConfirmFormRef"
              :initialData="aiFormData"
              @update="handleAiFormUpdate"
            />
            <div class="step-actions">
              <button class="btn-secondary" @click="aiStep = 2">返回修改</button>
              <button class="btn-primary" @click="saveAiFood" :disabled="isAiSaving">
                {{ isAiSaving ? '保存中...' : '保存到我的食物库' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userFoodApi } from '@/services/api/food.js'
import { aiApi } from '@/services/api/ai.js'
import ImagePicker from '@/components/ImagePicker.vue'
import NutritionForm from '@/components/NutritionForm.vue'
import FoodUnitEditor from '@/components/FoodUnitEditor.vue'
import ocrManager from '@/services/ocr/init.js'

import { showToast } from '@/utils/toast.js'

const router = useRouter()

const searchKeyword = ref('')
const loading = ref(false)

const myFoods = ref([])

// 手动添加
const showAddModal = ref(false)
const isAiFilling = ref(false)
const newFood = ref({
  foodName: '',
  brand: '',
  servingSize: 100,
  servingUnit: 'g',
  calories: null,
  protein: null,
  fat: null,
  carbohydrates: null,
  units: [] // 常用单位列表
})

// OCR 添加
const showOcrModal = ref(false)
const ocrStep = ref(1)
const imagePickerRef = ref(null)
const selectedImage = ref('')
const isRecognizing = ref(false)
const ocrResult = ref(null)
const ocrFormRef = ref(null)
const confirmFormRef = ref(null)
const ocrFormData = ref({})
const isSaving = ref(false)

// AI 识别添加
const showAiModal = ref(false)
const aiInputMode = ref('text') // 'text' 或 'image'
const aiStep = ref(1)
const aiRecipeText = ref('')
const aiSupplementText = ref('') // 图片模式的补充文本
const aiImagePickerRef = ref(null)
const aiSelectedImage = ref('')
const isAiParsing = ref(false)
const aiResult = ref(null)
const aiFormRef = ref(null)
const aiConfirmFormRef = ref(null)
const aiFormData = ref({})
const isAiSaving = ref(false)

// 加载我的食物库
const loadMyFoods = async () => {
  loading.value = true
  try {
    const response = await userFoodApi.getList()
    myFoods.value = response.data || []
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadMyFoods()
    return
  }

  loading.value = true
  try {
    const response = await userFoodApi.search(searchKeyword.value)
    myFoods.value = response.data || []
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// AI 智能填充营养成分
const handleAiFill = async () => {
  if (!newFood.value.foodName.trim()) {
    return
  }

  isAiFilling.value = true
  try {
    const response = await aiApi.fillNutrition(newFood.value.foodName.trim())
    if (response.code === 200 && response.data) {
      const foodInfo = response.data
      // 填充营养成分
      if (foodInfo.nutrition) {
        newFood.value.calories = foodInfo.nutrition.energyKcal || null
        newFood.value.protein = foodInfo.nutrition.protein || null
        newFood.value.fat = foodInfo.nutrition.fat || null
        newFood.value.carbohydrates = foodInfo.nutrition.carbohydrate || null
      }
      // 填充份量
      if (foodInfo.servingSize) {
        newFood.value.servingSize = foodInfo.servingSize
      }
    } else {
      showToast('AI 填充失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    showToast('AI 填充失败: ' + error.message)
  } finally {
    isAiFilling.value = false
  }
}

// 手动添加食物
const handleAddFood = async () => {
  if (!newFood.value.foodName) {
    showToast('请输入食物名称')
    return
  }

  // 过滤掉未填写完整的单位
  const validUnits = (newFood.value.units || []).filter(u => u.unitName && u.unitValue)

  try {
    await userFoodApi.create({
      ...newFood.value,
      units: validUnits
    })
    showAddModal.value = false
    loadMyFoods()

    // 重置表单
    newFood.value = {
      foodName: '',
      brand: '',
      servingSize: 100,
      servingUnit: 'g',
      calories: null,
      protein: null,
      fat: null,
      carbohydrates: null,
      units: []
    }
  } catch (error) {
    showToast('添加失败: ' + error.message)
  }
}

// 选择食物（跳转到编辑页面）
const selectFood = (food) => {
  router.push({
    path: `/food/edit/${food.id}`
  })
}

// OCR 相关方法
const handleImageChange = (path) => {
  selectedImage.value = path
}

const handleImageError = (error) => {
  console.error('图片错误:', error)
}

const startOcr = async () => {
  if (!selectedImage.value) {
    showToast('请先选择图片')
    return
  }
  ocrStep.value = 2
  isRecognizing.value = true
  try {
    const result = await ocrManager.recognize(selectedImage.value)
    ocrResult.value = result
  } catch (error) {
    ocrResult.value = { success: false, error: error.message }
  } finally {
    isRecognizing.value = false
  }
}

const resetOcr = () => {
  ocrStep.value = 1
  selectedImage.value = ''
  ocrResult.value = null
  imagePickerRef.value?.clear()
}

const goToOcrConfirm = () => {
  if (ocrFormRef.value?.validate?.()) {
    ocrFormData.value = { ...ocrFormData.value, ...ocrFormRef.value.getData() }
    ocrStep.value = 3
  }
}

const handleOcrFormUpdate = (data) => {
  ocrFormData.value = { ...ocrFormData.value, ...data }
}

const saveOcrFood = async () => {
  isSaving.value = true
  try {
    const data = confirmFormRef.value?.getData() || ocrFormData.value
    // 调用新增的 from-ocr 接口
    await userFoodApi.createFromOcr({
      foodName: data.foodName,
      brand: data.brand || '',
      servingSize: data.servingSize || 100,
      servingUnit: data.servingUnit || 'g',
      calories: data.calories,
      protein: data.protein,
      fat: data.fat,
      carbohydrates: data.carbohydrates,
      fiber: data.fiber,
      sodium: data.sodium,
      sugar: data.sugar,
      remark: data.rawText || ''
    })
    showToast('保存成功')
    closeOcrModal()
    loadMyFoods()
  } catch (error) {
    showToast('保存失败: ' + error.message)
  } finally {
    isSaving.value = false
  }
}

const closeOcrModal = () => {
  showOcrModal.value = false
  ocrStep.value = 1
  selectedImage.value = ''
  ocrResult.value = null
  ocrFormData.value = {}
}

// AI 识别相关方法
const handleAiImageChange = (path) => {
  aiSelectedImage.value = path
}

const handleAiImageError = (error) => {
  console.error('AI 图片错误:', error)
}

const startAiParse = async () => {
  aiStep.value = 2
  isAiParsing.value = true
  try {
    let result
    if (aiInputMode.value === 'text') {
      // 文本模式：直接调用 AI 解析
      const response = await aiApi.parseRecipe(aiRecipeText.value)
      result = {
        success: response.code === 200,
        text: aiRecipeText.value,
        foodInfo: response.data
      }
    } else {
      // 图片模式：先 OCR 再 AI，带上补充文本
      const response = await aiApi.parseRecipeImage(aiSelectedImage.value, aiSupplementText.value)
      result = {
        success: response.code === 200,
        text: aiSupplementText.value,
        foodInfo: response.data
      }
    }
    aiResult.value = result
  } catch (error) {
    aiResult.value = { success: false, error: error.message || 'AI 解析失败' }
  } finally {
    isAiParsing.value = false
  }
}

const resetAi = () => {
  aiStep.value = 1
  aiRecipeText.value = ''
  aiSupplementText.value = ''
  aiSelectedImage.value = ''
  aiResult.value = null
  if (aiInputMode.value === 'image') {
    aiImagePickerRef.value?.clear()
  }
}

const goToAiConfirm = () => {
  if (aiFormRef.value?.validate?.()) {
    aiFormData.value = { ...aiFormData.value, ...aiFormRef.value.getData() }
    aiStep.value = 3
  }
}

const handleAiFormUpdate = (data) => {
  aiFormData.value = { ...aiFormData.value, ...data }
}

const saveAiFood = async () => {
  isAiSaving.value = true
  try {
    const data = aiConfirmFormRef.value?.getData() || aiFormData.value
    // 复用 OCR 的入库接口
    await userFoodApi.createFromOcr({
      foodName: data.foodName,
      brand: data.brand || '',
      servingSize: data.servingSize || 100,
      servingUnit: data.servingUnit || 'g',
      calories: data.calories,
      protein: data.protein,
      fat: data.fat,
      carbohydrates: data.carbohydrates,
      fiber: data.fiber,
      sodium: data.sodium,
      sugar: data.sugar,
      remark: data.rawText || aiRecipeText.value || ''
    })
    showToast('保存成功')
    closeAiModal()
    loadMyFoods()
  } catch (error) {
    showToast('保存失败: ' + error.message)
  } finally {
    isAiSaving.value = false
  }
}

const closeAiModal = () => {
  showAiModal.value = false
  aiStep.value = 1
  aiInputMode.value = 'text'
  aiRecipeText.value = ''
  aiSupplementText.value = ''
  aiSelectedImage.value = ''
  aiResult.value = null
  aiFormData.value = {}
}

onMounted(() => {
  loadMyFoods()
})
</script>

<style scoped>
.food-library-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 22%);
  padding-bottom: calc(var(--tab-h) + var(--safe-bottom) + 24px);
}

/* 搜索栏 */
.search-bar {
  position: sticky;
  top: 0;
  z-index: 20;
  padding: calc(var(--safe-top) + 12px) 16px 12px;
  background: rgba(243, 246, 243, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.search-input {
  width: 100%;
  padding: 11px 16px 11px 40px;
  border: 1.5px solid var(--border);
  border-radius: 999px;
  font-size: 14px;
  color: var(--text-1);
  background: var(--card) url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='%239aa3a0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='11' cy='11' r='7'/%3E%3Cpath d='m21 21-4.3-4.3'/%3E%3C/svg%3E") no-repeat 14px center;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-input::placeholder { color: var(--text-4); }
.search-input:focus { border-color: var(--primary); box-shadow: 0 0 0 4px var(--primary-100); }

/* 列表头 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px 10px;
}
.count { color: var(--text-3); font-size: 13px; }
.action-buttons { display: flex; gap: 8px; }

.btn-ocr, .btn-ai, .btn-add {
  border: none;
  color: #fff;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 7px 13px;
  transition: transform 0.12s, opacity 0.2s;
}
.btn-ocr { background: linear-gradient(135deg, #3b82f6, #60a5fa); box-shadow: 0 4px 10px rgba(59,130,246,0.28); }
.btn-ai { background: linear-gradient(135deg, #8b5cf6, #a78bfa); box-shadow: 0 4px 10px rgba(139,92,246,0.28); }
.btn-add { background: var(--primary-gradient); box-shadow: 0 4px 10px rgba(67,160,71,0.28); }
.btn-ocr:active, .btn-ai:active, .btn-add:active { transform: scale(0.94); }

.loading, .empty {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-3);
}
.empty p { margin-bottom: 6px; }

.empty-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 22px;
}
.btn-ocr-large, .btn-ai-large, .btn-add-large {
  padding: 14px 24px;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: transform 0.12s;
}
.btn-ocr-large { background: linear-gradient(135deg, #3b82f6, #60a5fa); box-shadow: 0 8px 18px rgba(59,130,246,0.28); }
.btn-ai-large { background: linear-gradient(135deg, #8b5cf6, #a78bfa); box-shadow: 0 8px 18px rgba(139,92,246,0.28); }
.btn-add-large { background: var(--primary-gradient); box-shadow: var(--shadow-primary); }
.btn-ocr-large:active, .btn-ai-large:active, .btn-add-large:active { transform: scale(0.97); }

/* 食物列表 */
.food-items { padding: 0 16px; }

.food-card {
  background: var(--card);
  padding: 14px 16px;
  margin-bottom: 10px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  position: relative;
  box-shadow: var(--shadow-xs);
  transition: transform 0.12s, box-shadow 0.2s;
}
.food-card:hover { box-shadow: var(--shadow-sm); }
.food-card:active { transform: scale(0.99); }

.food-name { font-size: 16px; font-weight: 600; color: var(--text-1); padding-right: 70px; }
.food-brand { font-size: 12px; color: var(--text-3); margin-top: 4px; }
.food-nutrition {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-2);
}
.food-source {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  color: #3b82f6;
  background: #e7f0fe;
  padding: 2px 8px;
  border-radius: 6px;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 16px;
}

.modal-content {
  width: 100%;
  max-width: 420px;
  background: var(--card);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  animation: modalIn 0.24s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes modalIn { from { transform: scale(0.94) translateY(8px); opacity: 0; } to { transform: scale(1) translateY(0); opacity: 1; } }

.modal-large {
  max-width: 520px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  border-bottom: 1px solid var(--divider);
}
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 600; color: var(--text-1); }
.btn-close {
  background: var(--fill);
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-size: 18px;
  color: var(--text-2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background 0.15s;
}
.btn-close:active { background: var(--divider); }

.modal-body { padding: 18px; max-height: 60vh; overflow-y: auto; }

.form-item { margin-bottom: 16px; }
.form-item label { display: block; margin-bottom: 6px; font-size: 13px; color: var(--text-2); font-weight: 500; }
.form-item input, .form-item select {
  width: 100%;
  padding: 11px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-1);
  background: var(--bg-soft);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.form-item input:focus, .form-item select:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 4px var(--primary-100); }

.form-row { display: flex; gap: 12px; }
.form-row .form-item { flex: 1; }

.modal-footer { display: flex; gap: 12px; padding: 16px 18px; border-top: 1px solid var(--divider); }
.btn-cancel, .btn-confirm {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.12s;
}
.btn-cancel { background: var(--fill); color: var(--text-2); }
.btn-confirm { background: var(--primary-gradient); color: #fff; box-shadow: var(--shadow-primary); }
.btn-cancel:active, .btn-confirm:active { transform: scale(0.97); }

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

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: var(--fill);
  border-radius: var(--radius);
  margin-bottom: 16px;
}
.step { display: flex; flex-direction: column; align-items: center; }
.step-dot {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: var(--divider);
  color: var(--text-3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s;
}
.step.active .step-dot { background: var(--primary-gradient); color: #fff; box-shadow: 0 4px 10px rgba(67,160,71,0.3); }
.step-text { font-size: 11px; color: var(--text-3); margin-top: 5px; }
.step.active .step-text { color: var(--primary-600); font-weight: 500; }
.step-line { width: 30px; height: 2px; background: var(--divider); margin: 0 6px 19px; transition: background 0.2s; }
.step-line.active { background: var(--primary); }

.step-content { background: var(--card); }
.step-actions { display: flex; gap: 12px; padding-top: 16px; }
.btn-primary, .btn-next {
  flex: 1;
  padding: 12px;
  background: var(--primary-gradient);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-primary);
  transition: transform 0.12s;
}
.btn-primary:active, .btn-next:active { transform: scale(0.97); }
.btn-primary:disabled, .btn-next:disabled { background: var(--text-4); box-shadow: none; cursor: not-allowed; }
.btn-secondary {
  flex: 1;
  padding: 12px;
  background: var(--card);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-2);
  cursor: pointer;
  transition: transform 0.12s;
}
.btn-secondary:active { transform: scale(0.97); }

/* OCR 样式 */
.image-preview-mini {
  height: 110px;
  background: var(--fill);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  overflow: hidden;
}
.preview-img { max-height: 100px; border-radius: var(--radius-sm); }

.ocr-status { display: flex; flex-direction: column; align-items: center; padding: 36px; }
.loading-spinner {
  width: 36px; height: 36px;
  border: 3px solid var(--primary-100);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.loading-text { font-size: 14px; color: var(--text-2); margin-top: 12px; }

/* AI 识别输入方式切换 */
.ai-tabs {
  display: flex;
  background: var(--fill);
  border-radius: var(--radius);
  padding: 4px;
  margin-bottom: 16px;
}
.ai-tab {
  flex: 1;
  padding: 10px;
  text-align: center;
  border-radius: var(--radius-sm);
  color: var(--text-2);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.ai-tab.active {
  background: var(--card);
  color: #8b5cf6;
  font-weight: 600;
  box-shadow: var(--shadow-xs);
}

/* 文本输入区域 */
.text-input-area { margin-bottom: 16px; }
.recipe-textarea {
  width: 100%;
  padding: 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  outline: none;
  background: var(--bg-soft);
  color: var(--text-1);
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.recipe-textarea:focus { border-color: #8b5cf6; background: #fff; box-shadow: 0 0 0 4px #f3eefe; }

.image-input-area { margin-bottom: 16px; }

/* 补充文本输入 */
.supplement-input { margin-top: 12px; }
.supplement-label { display: block; font-size: 13px; color: var(--text-2); margin-bottom: 6px; font-weight: 500; }
.supplement-textarea {
  width: 100%;
  padding: 10px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  line-height: 1.5;
  resize: vertical;
  outline: none;
  background: var(--bg-soft);
  color: var(--text-1);
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.supplement-textarea:focus { border-color: #8b5cf6; background: #fff; box-shadow: 0 0 0 4px #f3eefe; }

/* 常用单位编辑器 */
.unit-editor-wrap { margin-top: 8px; padding-top: 14px; border-top: 1px solid var(--divider); }
</style>
