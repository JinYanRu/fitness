/**
 * 腾讯云 OCR 策略实现
 * 使用腾讯云文字识别服务
 */
import BaseOCRStrategy from './base.js'

class TencentOCRStrategy extends BaseOCRStrategy {
  constructor(config = {}) {
    super(config)
    this.name = 'tencent'
    this.description = '腾讯云 OCR 文字识别服务'

    this.secretId = config.secretId || ''
    this.secretKey = config.secretKey || ''
    this.proxyUrl = config.proxyUrl || ''
  }

  /**
   * 识别图片中的文字
   */
  async recognize(imagePath) {
    try {
      console.log('[TencentOCR] 开始识别图片:', imagePath)

      // 实际生产环境建议通过后端代理调用腾讯云 API
      // 前端直接调用会暴露 secretKey，存在安全风险

      if (this.proxyUrl) {
        const base64 = await this.imageToBase64(imagePath)

        const response = await fetch(this.proxyUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            image: base64,
            action: 'GeneralAccurateOCR'
          })
        })

        const data = await response.json()

        if (data.TextDetections) {
          const text = data.TextDetections.map(item => item.DetectedText).join('\n')
          return {
            success: true,
            text
          }
        }

        throw new Error('OCR 服务返回数据异常')
      }

      throw new Error('请配置后端代理地址 proxyUrl')
    } catch (error) {
      console.error('[TencentOCR] 识别失败:', error)
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
    return !!(this.secretId && this.secretKey)
  }
}

export default TencentOCRStrategy