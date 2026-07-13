<template>
  <div class="food-library-page">
    <!-- 标签切换 -->
    <div class="tabs">
      <div
        :class="['tab', activeTab === 'my' ? 'active' : '']"
        @click="activeTab = 'my'"
      >
        我的食物库
      </div>
      <div
        :class="['tab', activeTab === 'common' ? 'active' : '']"
        @click="activeTab = 'common'"
      >
        公共食物库
      </div>
    </div>

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
    <div v-if="activeTab === 'my'" class="food-list">
      <div class="list-header">
        <span class="count">共 {{ myFoods.length }} 种食物</span>
        <div class="action-buttons">
          <button class="btn-ocr" @click="showOcrModal = true">📷 拍照添加</button>
          <button class="btn-add" @click="showAddModal = true">+ 手动添加</button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="myFoods.length === 0" class="empty">
        <p>暂无食物，点击上方按钮添加</p>
        <div class="empty-actions">
          <button class="btn-ocr-large" @click="showOcrModal = true">📷 拍照识别添加</button>
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

    <!-- 公共食物库 -->
    <div v-if="activeTab === 'common'" class="food-list">
      <div class="categories">
        <span
          v-for="cat in categories"
          :key="cat"
          :class="['category', selectedCategory === cat ? 'active' : '']"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </span>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="commonFoods.length === 0" class="empty">
        <p>暂无数据</p>
      </div>

      <div v-else class="food-items">
        <div
          v-for="food in commonFoods"
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
            <input v-model="newFood.foodName" type="text" placeholder="请输入食物名称" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { userFoodApi, commonFoodApi } from '@/services/api/food.js'
import ImagePicker from '@/components/ImagePicker.vue'
import NutritionForm from '@/components/NutritionForm.vue'
import ocrManager from '@/services/ocr/init.js'

const router = useRouter()

const activeTab = ref('my')
const searchKeyword = ref('')
const loading = ref(false)

const myFoods = ref([])
const commonFoods = ref([])
const categories = ref(['全部', '谷物薯类', '蔬菜类', '水果类', '肉类', '蛋奶类', '豆制品', '坚果类', '饮料类'])
const selectedCategory = ref('全部')

// 手动添加
const showAddModal = ref(false)
const newFood = ref({
  foodName: '',
  brand: '',
  servingSize: 100,
  servingUnit: 'g',
  calories: null,
  protein: null,
  fat: null,
  carbohydrates: null
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

// 加载公共食物库
const loadCommonFoods = async () => {
  loading.value = true
  try {
    const response = await commonFoodApi.getList()
    commonFoods.value = response.data || []
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    if (activeTab.value === 'my') {
      loadMyFoods()
    } else {
      loadCommonFoods()
    }
    return
  }

  loading.value = true
  try {
    if (activeTab.value === 'my') {
      const response = await userFoodApi.search(searchKeyword.value)
      myFoods.value = response.data || []
    } else {
      const response = await commonFoodApi.search(searchKeyword.value, selectedCategory.value)
      commonFoods.value = response.data || []
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (cat) => {
  selectedCategory.value = cat
  handleSearch()
}

// 手动添加食物
const handleAddFood = async () => {
  if (!newFood.value.foodName) {
    alert('请输入食物名称')
    return
  }

  try {
    await userFoodApi.create(newFood.value)
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
      carbohydrates: null
    }
  } catch (error) {
    alert('添加失败: ' + error.message)
  }
}

// 选择食物（跳转到记录页面）
const selectFood = (food) => {
  router.push({
    path: '/record',
    query: {
      foodId: food.id,
      foodName: food.foodName,
      foodType: activeTab.value === 'my' ? 'user' : 'common'
    }
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
    alert('请先选择图片')
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
    alert('保存成功！')
    closeOcrModal()
    loadMyFoods()
  } catch (error) {
    alert('保存失败: ' + error.message)
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

// 监听标签切换
watch(activeTab, (tab) => {
  searchKeyword.value = ''
  if (tab === 'my') {
    loadMyFoods()
  } else {
    loadCommonFoods()
  }
})

onMounted(() => {
  loadMyFoods()
})
</script>

<style scoped>
.food-library-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #eee;
}

.tab {
  flex: 1;
  padding: 12px;
  text-align: center;
  color: #666;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #4CAF50;
  border-bottom-color: #4CAF50;
}

.search-bar {
  padding: 12px 16px;
  background: #fff;
}

.search-input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

.search-input:focus {
  border-color: #4CAF50;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  margin-top: 8px;
}

.count {
  color: #666;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-ocr {
  padding: 6px 12px;
  background: #2196F3;
  color: #fff;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
}

.btn-add {
  padding: 6px 16px;
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
}

.category {
  padding: 6px 12px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.category.active {
  background: #4CAF50;
  color: #fff;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.btn-ocr-large, .btn-add-large {
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.btn-ocr-large {
  background: #2196F3;
  color: #fff;
}

.btn-add-large {
  background: #4CAF50;
  color: #fff;
}

.food-items {
  padding: 8px 16px;
}

.food-card {
  background: #fff;
  padding: 12px 16px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
}

.food-card:hover {
  background: #f9f9f9;
}

.food-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.food-brand {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.food-nutrition {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}

.food-source {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 11px;
  color: #2196F3;
  background: #E3F2FD;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 90%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.modal-large {
  max-width: 500px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 16px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #666;
}

.form-item input, .form-item select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-row .form-item {
  flex: 1;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #eee;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-confirm {
  background: #4CAF50;
  color: #fff;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 16px;
}

.step { display: flex; flex-direction: column; align-items: center; }

.step-dot {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: #ddd;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.step.active .step-dot { background: #4CAF50; color: #fff; }
.step-text { font-size: 11px; color: #999; margin-top: 4px; }
.step.active .step-text { color: #4CAF50; }

.step-line {
  width: 30px; height: 2px;
  background: #ddd;
  margin: 0 6px 18px;
}

.step-line.active { background: #4CAF50; }

.step-content {
  background: #fff;
}

.step-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
}

.btn-primary, .btn-next {
  flex: 1;
  padding: 12px;
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
}

.btn-primary:disabled, .btn-next:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  flex: 1;
  padding: 12px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
}

/* OCR 样式 */
.image-preview-mini {
  height: 100px;
  background: #f8f8f8;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.preview-img { max-height: 100px; }

.ocr-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
}

.loading-spinner {
  width: 32px; height: 32px;
  border: 3px solid #f3f3f3;
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
.loading-text { font-size: 14px; color: #666; margin-top: 10px; }
</style>
