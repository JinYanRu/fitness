/**
 * 后端 OCR 服务策略实现
 * 通过 Spring Boot 后端服务进行 OCR 识别
 * 后端会调用 RapidOCR 并进行营养成分智能解析
 */
import BaseOCRStrategy from './base.js'

class BackendOCRStrategy extends BaseOCRStrategy {
  constructor(config = {}) {
    super(config)
    this.name = 'backend'
    this.description = '后端 OCR 服务 (智能解析)'
    // 后端服务地址，通过 Vite 代理访问
    this.baseUrl = config.baseUrl || '/api'
  }

  /**
   * 识别图片中的文字
   * @param {string} imagePath - 图片路径（可以是 base64 或 URL）
   * @returns {Promise<{success: boolean, text: string, nutrition: Object, error?: string}>}
   */
  async recognize(imagePath) {
    try {
      console.log('[BackendOCR] 开始识别图片:', imagePath.substring(0, 50))

      // 转换为 base64
      let base64Image = imagePath
      if (!imagePath.startsWith('data:image')) {
        base64Image = await this.imageToBase64(imagePath)
        base64Image = `data:image/jpeg;base64,${base64Image}`
      }

      // 调用后端 API
      const result = await this.callBackendApi(base64Image)
      return result

    } catch (error) {
      console.error('[BackendOCR] 识别失败:', error)
      return {
        success: false,
        text: '',
        nutrition: null,
        error: error.message
      }
    }
  }

  /**
   * 调用后端 API
   */
  async callBackendApi(base64Image) {
    const url = `${this.baseUrl}/ocr/recognize`

    console.log('[BackendOCR] 调用后端接口:', url)

    // 获取 Token
    const token = uni.getStorageSync('token')
    const headers = {
      'Content-Type': 'application/json'
    }
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          imageBase64: base64Image,
          parseNutrition: true
        })
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('[BackendOCR] HTTP错误:', response.status, errorText)
        throw new Error(`HTTP错误: ${response.status}`)
      }

      const data = await response.json()
      console.log('[BackendOCR] 返回数据:', data)

      // 解析后端返回格式
      // 格式: { code: 200, message: "success", data: { success, text, texts, foodInfo, elapseMs } }
      if (data && data.code === 200 && data.data) {
        const resultData = data.data
        return {
          success: resultData.success,
          text: resultData.text || '',
          texts: resultData.texts || null,
          foodInfo: resultData.foodInfo || null,
          elapseMs: resultData.elapseMs || null
        }
      }

      // 错误响应
      if (data && data.code !== 200) {
        throw new Error(data.message || 'OCR识别失败')
      }

      console.warn('[BackendOCR] 未知返回格式:', data)
      throw new Error('OCR返回数据格式异常')

    } catch (error) {
      console.error('[BackendOCR] API调用失败:', error)
      throw error
    }
  }

  /**
   * 图片转 Base64
   */
  imageToBase64(imagePath) {
    return new Promise((resolve, reject) => {
      if (imagePath.startsWith('data:image')) {
        resolve(imagePath.split(',')[1])
        return
      }

      // 对于 blob URL 或其他 URL
      fetch(imagePath)
        .then(res => {
          if (!res.ok) {
            throw new Error(`获取图片失败: ${res.status}`)
          }
          return res.blob()
        })
        .then(blob => {
          const reader = new FileReader()
          reader.onload = () => {
            const base64 = reader.result.split(',')[1]
            resolve(base64)
          }
          reader.onerror = () => reject(new Error('FileReader 错误'))
          reader.readAsDataURL(blob)
        })
        .catch(reject)
    })
  }

  /**
   * 验证配置
   */
  validateConfig() {
    return !!this.baseUrl
  }

  /**
   * 检查服务状态
   */
  async checkHealth() {
    try {
      const url = `${this.baseUrl}/ocr/health`
      console.log('[BackendOCR] 检查服务状态:', url)

      const response = await fetch(url)
      if (response.ok) {
        const data = await response.json()
        return {
          available: data.code === 200,
          status: data
        }
      }
      return { available: false }
    } catch (error) {
      console.error('[BackendOCR] 服务检查失败:', error)
      return { available: false, error: error.message }
    }
  }
}

export default BackendOCRStrategy