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
  },
  /** 压缩阈值（字节），超过此大小则压缩，默认 500KB */
  compressThreshold: {
    type: Number,
    default: 500 * 1024
  },
  /** 压缩后最大宽度，默认 1280 */
  maxWidth: {
    type: Number,
    default: 1280
  },
  /** 压缩后最大高度，默认 1280 */
  maxHeight: {
    type: Number,
    default: 1280
  },
  /** 压缩质量 0-1，默认 0.6 */
  quality: {
    type: Number,
    default: 0.6
  }
})

const emit = defineEmits(['change', 'error'])

const imagePath = ref('')

/**
 * 压缩图片：当文件大小超过阈值时，通过 Canvas 缩放并降低质量
 * @param {File} file - 原始图片文件
 * @returns {Promise<string>} - 压缩后的 base64 data URL
 */
const compressImage = (file) => {
  return new Promise((resolve, reject) => {
    // 未超过阈值，直接读取原文件
    if (file.size <= props.compressThreshold) {
      const reader = new FileReader()
      reader.onload = (e) => resolve(e.target.result)
      reader.onerror = () => reject(new Error('读取图片失败'))
      reader.readAsDataURL(file)
      return
    }

    console.log(`图片大小 ${(file.size / 1024 / 1024).toFixed(2)}MB 超过阈值，开始压缩...`)

    const img = new Image()
    const url = URL.createObjectURL(file)

    img.onload = () => {
      URL.revokeObjectURL(url)

      // 计算缩放后的尺寸
      let { width, height } = img
      if (width > props.maxWidth) {
        height = Math.round(height * (props.maxWidth / width))
        width = props.maxWidth
      }
      if (height > props.maxHeight) {
        width = Math.round(width * (props.maxHeight / height))
        height = props.maxHeight
      }

      // Canvas 绘制并导出
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)

      // 尝试压缩，如果仍然超过阈值则逐步降低质量
      let quality = props.quality
      let dataUrl = canvas.toDataURL('image/jpeg', quality)
      let compressedSize = Math.round((dataUrl.length - 'data:image/jpeg;base64,'.length) * 3 / 4)

      // 如果压缩后仍超过阈值，继续降低质量直到满足要求（最低 0.1）
      while (compressedSize > props.compressThreshold && quality > 0.1) {
        quality -= 0.1
        dataUrl = canvas.toDataURL('image/jpeg', quality)
        compressedSize = Math.round((dataUrl.length - 'data:image/jpeg;base64,'.length) * 3 / 4)
      }

      console.log(`压缩完成: ${(compressedSize / 1024 / 1024).toFixed(2)}MB, 质量: ${(quality * 100).toFixed(0)}% (原始 ${(file.size / 1024 / 1024).toFixed(2)}MB)`)
      resolve(dataUrl)
    }

    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片加载失败'))
    }

    img.src = url
  })
}

/**
 * 处理选中的文件：超过阈值时自动压缩
 */
const processFile = async (file) => {
  try {
    const dataUrl = await compressImage(file)
    imagePath.value = dataUrl
    emit('change', imagePath.value)
  } catch (error) {
    console.error('图片处理失败:', error)
    emit('error', error.message || '图片处理失败')
  }
}

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
        processFile(file)
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
        processFile(file)
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
