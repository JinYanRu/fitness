/**
 * 百度 OCR 策略实现
 * 使用百度 AI 开放平台的文字识别服务
 */
import BaseOCRStrategy from './base.js'

class BaiduOCRStrategy extends BaseOCRStrategy {
  constructor(config = {}) {
    super(config)
    this.name = 'baidu'
    this.description = '百度 OCR 文字识别服务'
    this.accessToken = null
    this.tokenExpireTime = 0

    this.apiKey = config.apiKey || ''
    this.secretKey = config.secretKey || ''
    this.tokenUrl = 'https://aip.baidubce.com/oauth/2.0/token'
    this.ocrUrl = 'https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic'
  }

  /**
   * 获取 Access Token
   */
  async getAccessToken() {
    if (this.accessToken && Date.now() < this.tokenExpireTime) {
      return this.accessToken
    }

    try {
      const response = await fetch(
        `${this.tokenUrl}?grant_type=client_credentials&client_id=${this.apiKey}&client_secret=${this.secretKey}`,
        { method: 'POST' }
      )

      const data = await response.json()

      if (data.access_token) {
        this.accessToken = data.access_token
        this.tokenExpireTime = Date.now() + (data.expires_in - 86400) * 1000
        return this.accessToken
      }

      throw new Error('获取 Access Token 失败')
    } catch (error) {
      console.error('[BaiduOCR] 获取 Token 失败:', error)
      throw error
    }
  }

  /**
   * 识别图片中的文字
   */
  async recognize(imagePath) {
    try {
      console.log('[BaiduOCR] 开始识别图片:', imagePath)

      const token = await this.getAccessToken()
      const base64 = await this.imageToBase64(imagePath)

      const response = await fetch(
        `${this.ocrUrl}?access_token=${token}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: `image=${encodeURIComponent(base64)}`
        }
      )

      const data = await response.json()

      if (data.words_result) {
        const text = data.words_result.map(item => item.words).join('\n')
        return {
          success: true,
          text,
          wordsCount: data.words_result_num
        }
      }

      if (data.error_code) {
        throw new Error(`百度 OCR 错误: ${data.error_msg}`)
      }

      throw new Error('OCR 识别失败')
    } catch (error) {
      console.error('[BaiduOCR] 识别失败:', error)
      return {
        success: false,
        text: '',
        error: error.message
      }
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
    return !!(this.apiKey && this.secretKey)
  }
}

export default BaiduOCRStrategy