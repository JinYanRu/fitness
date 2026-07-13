/**
 * uTools MCP OCR 策略实现
 * 通过 uTools MCP 服务进行 OCR 识别
 */
import BaseOCRStrategy from './base.js'

class UToolsOCRStrategy extends BaseOCRStrategy {
  constructor(config = {}) {
    super(config)
    this.name = 'utools'
    this.description = 'uTools MCP OCR 服务'
  }

  /**
   * 识别图片中的文字
   * @param {string} imagePath - 图片路径
   * @returns {Promise<{success: boolean, text: string, error?: string}>}
   */
  async recognize(imagePath) {
    try {
      console.log('[UToolsOCR] 开始识别图片:', imagePath)

      // 在 Web 环境下，通过 HTTP 调用 MCP OCR 服务
      // 实际使用时需要根据 MCP 服务的配置进行调整
      const result = await this.recognizeViaHTTP(imagePath)

      return result
    } catch (error) {
      console.error('[UToolsOCR] 识别失败:', error)
      return {
        success: false,
        text: '',
        error: error.message
      }
    }
  }

  /**
   * 通过 HTTP 调用 OCR 服务
   */
  async recognizeViaHTTP(imagePath) {
    // 将图片转为 base64
    const base64 = await this.imageToBase64(imagePath)

    // 调用配置的 OCR API
    const apiUrl = this.config.apiUrl || 'http://localhost:3721/ocr'

    try {
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          image: base64,
          extractType: 'text'
        })
      })

      if (response.ok) {
        const data = await response.json()
        return {
          success: true,
          text: data.text || data.result || ''
        }
      }

      throw new Error('OCR 服务请求失败')
    } catch (error) {
      console.error('[UToolsOCR] HTTP 调用失败:', error)
      throw error
    }
  }

  /**
   * 图片转 Base64
   */
  imageToBase64(imagePath) {
    return new Promise((resolve, reject) => {
      // 如果已经是 base64 格式，直接返回
      if (imagePath.startsWith('data:image')) {
        resolve(imagePath.split(',')[1])
        return
      }

      // Web 环境使用 fetch获取图片
      fetch(imagePath)
        .then(res => res.blob())
        .then(blob => {
          const reader = new FileReader()
          reader.onload = () => {
            const base64 = reader.result.split(',')[1]
            resolve(base64)
          }
          reader.onerror = reject
          reader.readAsDataURL(blob)
        })
        .catch(reject)
    })
  }

  validateConfig() {
    return true
  }
}

export default UToolsOCRStrategy