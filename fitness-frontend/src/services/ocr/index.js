/**
 * OCR 管理器
 * 使用策略模式管理多个 OCR 服务提供商
 */
import BaseOCRStrategy from './base.js'

class OCRManager {
  constructor() {
    this.strategies = new Map()
    this.currentStrategy = null
    this.defaultStrategy = null
  }

  /**
   * 注册 OCR 策略
   * @param {string} name - 策略名称
   * @param {BaseOCRStrategy} strategy - 策略实例
   * @param {boolean} isDefault - 是否设为默认
   */
  register(name, strategy, isDefault = false) {
    if (!(strategy instanceof BaseOCRStrategy)) {
      throw new Error('策略必须继承自 BaseOCRStrategy')
    }
    this.strategies.set(name, strategy)
    if (isDefault || !this.defaultStrategy) {
      this.defaultStrategy = name
      this.currentStrategy = name
    }
    console.log(`[OCRManager] 注册策略: ${name}, 默认: ${isDefault}`)
  }

  /**
   * 切换当前使用的策略
   * @param {string} name - 策略名称
   */
  use(name) {
    if (!this.strategies.has(name)) {
      throw new Error(`未找到策略: ${name}, 可用策略: ${Array.from(this.strategies.keys()).join(', ')}`)
    }
    this.currentStrategy = name
    console.log(`[OCRManager] 切换到策略: ${name}`)
  }

  /**
   * 获取当前策略
   * @returns {BaseOCRStrategy}
   */
  getCurrentStrategy() {
    if (!this.currentStrategy) {
      throw new Error('未设置当前策略')
    }
    return this.strategies.get(this.currentStrategy)
  }

  /**
   * 识别图片
   * @param {string} imagePath - 图片路径
   * @param {Object} options - 额外选项
   * @returns {Promise<{success: boolean, text: string, foodInfo: Object, nutrition: Object, error?: string}>}
   */
  async recognize(imagePath, options = {}) {
    const strategy = this.getCurrentStrategy()

    try {
      console.log(`[OCRManager] 使用策略 ${this.currentStrategy} 识别图片: ${imagePath}`)
      const result = await strategy.recognize(imagePath)

      if (result.success) {
        // 优先使用后端返回的 foodInfo，否则使用本地解析
        const foodInfo = result.foodInfo || null

        // 本地解析作为备用
        const nutrition = strategy.parseNutritionText(result.text)

        return {
          success: true,
          text: result.text,
          foodInfo,
          nutrition,
          rawText: result.text,
          strategy: this.currentStrategy
        }
      }

      return result
    } catch (error) {
      console.error(`[OCRManager] 识别失败:`, error)
      return {
        success: false,
        text: '',
        foodInfo: null,
        nutrition: null,
        error: error.message
      }
    }
  }

  /**
   * 获取所有已注册的策略
   * @returns {Array<{name: string, description: string}>}
   */
  getStrategies() {
    return Array.from(this.strategies.entries()).map(([name, strategy]) => ({
      name,
      description: strategy.description || name,
      config: strategy.config
    }))
  }

  /**
   * 检查策略是否可用
   * @param {string} name - 策略名称
   * @returns {boolean}
   */
  isAvailable(name) {
    const strategy = this.strategies.get(name)
    return strategy ? strategy.validateConfig() : false
  }
}

// 导出单例
const ocrManager = new OCRManager()
export default ocrManager