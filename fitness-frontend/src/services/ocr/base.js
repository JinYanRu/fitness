/**
 * OCR 基础策略类
 * 所有 OCR 实现都需要继承此类
 */
class BaseOCRStrategy {
  constructor(config = {}) {
    this.config = config
    this.name = 'base'
  }

  /**
   * 识别图片中的文字
   * @param {string} imagePath - 图片路径
   * @returns {Promise<{success: boolean, text: string, error?: string}>}
   */
  async recognize(imagePath) {
    throw new Error('子类必须实现 recognize 方法')
  }

  /**
   * 解析营养标签文本为结构化数据
   * @param {string} text - OCR 识别的原始文本
   * @returns {Object} 结构化的营养数据
   */
  parseNutritionText(text) {
    const result = {
      calories: null,
      protein: null,
      fat: null,
      carbohydrates: null,
      fiber: null,
      sodium: null,
      sugar: null,
      servingSize: null,
      servingUnit: 'g'
    }

    if (!text) return result

    // 常见营养标签的正则匹配模式
    const patterns = {
      calories: [
        /能量[：:]\s*(\d+(?:\.\d+)?)\s*(?:kcal|千卡|大卡)/i,
        /热量[：:]\s*(\d+(?:\.\d+)?)\s*(?:kcal|千卡|大卡)/i,
        /(\d+(?:\.\d+)?)\s*(?:kcal|千卡|大卡)/i
      ],
      protein: [
        /蛋白质[：:]\s*(\d+(?:\.\d+)?)\s*g/i,
        /蛋白[：:]\s*(\d+(?:\.\d+)?)\s*g/i
      ],
      fat: [
        /脂肪[：:]\s*(\d+(?:\.\d+)?)\s*g/i,
        /总脂肪[：:]\s*(\d+(?:\.\d+)?)\s*g/i
      ],
      carbohydrates: [
        /碳水化合物[：:]\s*(\d+(?:\.\d+)?)\s*g/i,
        /碳水[：:]\s*(\d+(?:\.\d+)?)\s*g/i
      ],
      fiber: [
        /膳食纤维[：:]\s*(\d+(?:\.\d+)?)\s*g/i,
        /纤维[：:]\s*(\d+(?:\.\d+)?)\s*g/i
      ],
      sodium: [
        /钠[：:]\s*(\d+(?:\.\d+)?)\s*mg/i,
        /钠含量[：:]\s*(\d+(?:\.\d+)?)\s*mg/i
      ],
      sugar: [
        /糖[：:]\s*(\d+(?:\.\d+)?)\s*g/i,
        /糖含量[：:]\s*(\d+(?:\.\d+)?)\s*g/i
      ],
      servingSize: [
        /净含量[：:]\s*(\d+(?:\.\d+)?)\s*(g|ml|kg|l)/i,
        /份量[：:]\s*(\d+(?:\.\d+)?)\s*(g|ml|kg|l)/i,
        /每份[：:]\s*(\d+(?:\.\d+)?)\s*(g|ml|kg|l)/i
      ]
    }

    // 遍历每个营养素，尝试匹配
    for (const [key, regexList] of Object.entries(patterns)) {
      for (const regex of regexList) {
        const match = text.match(regex)
        if (match) {
          result[key] = parseFloat(match[1])
          // 如果是份量，还捕获单位
          if (key === 'servingSize' && match[2]) {
            result.servingUnit = match[2].toLowerCase()
          }
          break
        }
      }
    }

    return result
  }

  /**
   * 验证配置是否有效
   * @returns {boolean}
   */
  validateConfig() {
    return true
  }
}

export default BaseOCRStrategy
