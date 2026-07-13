/**
 * Mock OCR 策略实现
 * 用于开发测试，无需真实 OCR 服务
 */
import BaseOCRStrategy from './base.js'

class MockOCRStrategy extends BaseOCRStrategy {
  constructor(config = {}) {
    super(config)
    this.name = 'mock'
    this.description = '模拟 OCR 服务（用于开发测试）'

    // 模拟的营养标签文本
    this.mockTexts = [
      `营养成分表
每100克
能量 456千卡
蛋白质 12.5克
脂肪 18.3克
碳水化合物 62.8克
膳食纤维 3.2克
钠 385毫克
糖 8.5克`,
      `营养信息
份量：每份30克
热量 120 kcal
蛋白质 4.2g
脂肪 2.5g
碳水 20.1g
膳食纤维 1.8g
钠 150mg`,
      `Nutrition Facts
Serving Size 28g
Calories 150
Protein 6g
Total Fat 8g
Carbohydrates 16g
Dietary Fiber 2g
Sodium 200mg
Sugars 3g`
    ]
  }

  /**
   * 模拟识别
   */
  async recognize(imagePath) {
    console.log('[MockOCR] 模拟识别图片:', imagePath)

    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 随机返回一个模拟文本
    const randomIndex = Math.floor(Math.random() * this.mockTexts.length)
    const text = this.mockTexts[randomIndex]

    return {
      success: true,
      text
    }
  }

  validateConfig() {
    return true
  }
}

export default MockOCRStrategy