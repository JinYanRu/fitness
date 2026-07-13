<template>
  <div class="image-picker">
    <!-- 图片预览区域 -->
    <div class="image-preview" v-if="imagePath">
      <img
        :src="imagePath"
        class="preview-image"
        @click="previewImage"
      />
      <div class="image-actions">
        <div class="action-btn" @click="removeImage">
          <span class="action-icon">×</span>
        </div>
      </div>
    </div>

    <!-- 选择图片区域 -->
    <div class="pick-area" v-else>
      <div class="pick-buttons">
        <div class="pick-btn camera-btn" @click="takePhoto">
          <span class="btn-icon">📷</span>
          <span class="btn-text">拍照</span>
        </div>
        <div class="pick-btn album-btn" @click="chooseFromAlbum">
          <span class="btn-icon">🖼️</span>
          <span class="btn-text">相册</span>
        </div>
      </div>
      <span class="pick-tip">选择食物营养标签图片</span>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps, defineExpose } from 'vue'

const props = defineProps({
  maxCount: {
    type: Number,
    default: 1
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['change', 'error'])

const imagePath = ref('')

/**
 * 拍照（H5 环境下调用相机）
 */
const takePhoto = async () => {
  if (props.disabled) return

  try {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.capture = 'environment' // 调用后置摄像头
    input.style.display = 'none'
    document.body.appendChild(input)

    input.onchange = (e) => {
      const file = e.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (event) => {
          imagePath.value = event.target.result
          emit('change', imagePath.value)
        }
        reader.readAsDataURL(file)
      }
      input.remove()
    }

    input.click()
  } catch (error) {
    console.error('拍照失败:', error)
    emit('error', error.message || '拍照失败')
  }
}

/**
 * 从相册选择
 */
const chooseFromAlbum = async () => {
  if (props.disabled) return

  try {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.style.display = 'none'
    document.body.appendChild(input)

    input.onchange = (e) => {
      const file = e.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (event) => {
          imagePath.value = event.target.result
          emit('change', imagePath.value)
        }
        reader.readAsDataURL(file)
      }
      input.remove()
    }

    input.click()
  } catch (error) {
    console.error('选择图片失败:', error)
    emit('error', error.message || '选择图片失败')
  }
}

/**
 * 预览图片
 */
const previewImage = () => {
  if (imagePath.value) {
    window.open(imagePath.value, '_blank')
  }
}

/**
 * 删除图片
 */
const removeImage = () => {
  imagePath.value = ''
  emit('change', '')
}

/**
 * 清除图片
 */
const clear = () => {
  removeImage()
}

defineExpose({
  clear,
  getImagePath: () => imagePath.value
})
</script>

<style scoped>
.image-picker {
  padding: 20px;
}

.image-preview {
  position: relative;
  width: 100%;
  min-height: 300px;
  background-color: #f8f8f8;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  cursor: pointer;
}

.image-actions {
  position: absolute;
  top: 10px;
  right: 10px;
}

.action-btn {
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.action-icon {
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.pick-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background-color: #f8f8f8;
  border-radius: 12px;
  border: 2px dashed #ddd;
}

.pick-buttons {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.pick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
}

.pick-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.pick-btn:active {
  transform: scale(0.95);
}

.btn-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.btn-text {
  font-size: 14px;
  color: #666;
}

.pick-tip {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
}
</style>