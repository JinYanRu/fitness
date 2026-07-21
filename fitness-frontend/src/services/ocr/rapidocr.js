/**
 * RapidOCR 本地服务策略实现
 * 对接部署在 localhost:8000 的 RapidOCR 服务
 */
import BaseOCRStrategy from './base.js'

class RapidOCRStrategy extends BaseOCRStrategy {
  constructor(config = {}) {
    super(config)
    this.name = 'rapidocr'
    this.description = 'RapidOCR 本地服务'
    this.baseUrl = config.baseUrl || 'http://111.228.49.250:10265'
    // 支持通过 Vite 代理访问，避免 CORS 问题
    this.proxyUrl = config.proxyUrl || '/ocr-proxy'
  }

  /**
   * 获取实际请求地址
   */
  getRequestUrl(path) {
    // 如果配置了代理，使用代理地址
    if (this.proxyUrl && typeof window !== 'undefined') {
      return `${this.proxyUrl}${path}`
    }
    return `${this.baseUrl}${path}`
  }

  /**
   * 识别图片中的文字
   * @param {string} imagePath - 图片路径（可以是 base64 或 URL）
   * @returns {Promise<{success: boolean, text: string, error?: string}>}
   */
  async recognize(imagePath) {
    try {
      console.log('[RapidOCR] 开始识别图片:', imagePath.substring(0, 50))

      // 如果是 base64 格式，直接使用 base64 接口
      if (imagePath.startsWith('data:image')) {
        const result = await this.recognizeViaBase64(imagePath)
        return result
      }

      // 其他情况，转换为 base64 后识别
      const base64 = await this.imageToBase64(imagePath)
      const result = await this.recognizeViaBase64(`data:image/jpeg;base64,${base64}`)

      return result
    } catch (error) {
      console.error('[RapidOCR] 识别失败:', error)
      return {
        success: false,
        text: '',
        error: error.message
      }
    }
  }

  /**
   * 通过 Base64 接口识别
   * API: POST /ocr/base64
   * 参数: image_base64 (form-data)
   */
  async recognizeViaBase64(base64Image) {
    const url = this.getRequestUrl('/ocr/base64')

    try {
      // 移除 data:image/xxx;base64, 前缀
      const base64Data = base64Image.includes(',')
        ? base64Image.split(',')[1]
        : base64Image

      console.log('[RapidOCR] 调用接口:', url)

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `image_base64=${encodeURIComponent(base64Data)}`
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('[RapidOCR] HTTP错误:', response.status, errorText)
        throw new Error(`HTTP错误: ${response.status}`)
      }

      const data = await response.json()
      console.log('[RapidOCR] 返回数据:', data)

      // 解析返回结果 - RapidOCR 返回格式
      // 格式: { success: true, texts: [...], full_text: "...", elapse_ms: ... }
      if (data && data.success) {
        // 使用 texts 数组进行更精确的营养成分解析
        const nutritionData = parseNutritionFromTexts(data.texts || [])

        return {
          success: true,
          text: data.full_text || '',
          texts: data.texts || null,
          nutrition: nutritionData,
          elapseMs: data.elapse_ms || null
        }
      }

      // 兼容旧格式 { text: "...", boxes: [...], scores: [...] }
      if (data && data.text) {
        return {
          success: true,
          text: data.text,
          boxes: data.boxes || null,
          scores: data.scores || null
        }
      }

      // 如果返回的是数组格式 [{text: "...", box: [...], score: ...}]
      if (data && Array.isArray(data)) {
        const text = data.map(item => {
          if (typeof item === 'string') return item
          return item.text || ''
        }).filter(Boolean).join('\n')
        return {
          success: true,
          text
        }
      }

      // 如果返回的是纯文本
      if (typeof data === 'string') {
        return {
          success: true,
          text: data
        }
      }

      console.warn('[RapidOCR] 未知返回格式:', data)
      throw new Error('OCR返回数据格式异常')
    } catch (error) {
      console.error('[RapidOCR] Base64识别失败:', error)
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
      const url = this.getRequestUrl('/')
      console.log('[RapidOCR] 检查服务状态:', url)

      const response = await fetch(url)
      if (response.ok) {
        const data = await response.json()
        return {
          available: true,
          status: data
        }
      }
      return { available: false }
    } catch (error) {
      console.error('[RapidOCR] 服务检查失败:', error)
      return { available: false, error: error.message }
    }
  }
}

/**
 * 从 texts 数组解析营养成分
 * 利用 box 坐标判断同一行的元素
 */
function parseNutritionFromTexts(texts) {
  const result = {
    calories: null,
    protein: null,
    fat: null,
    carbohydrates: null,
    fiber: null,
    sodium: null,
    sugar: null,
    calcium: null
  }

  if (!texts || !Array.isArray(texts)) {
    return result
  }

  // 营养名称关键词
  const nutritionKeywords = {
    calories: ['能量', '热量'],
    protein: ['蛋白质'],
    fat: ['脂肪'],
    saturatedFat: ['饱和脂肪'],  // 新增饱和脂肪
    carbohydrates: ['碳水化合物', '碳水'],
    fiber: ['膳食纤维', '纤维'],
    sodium: ['钠'],
    sugar: ['糖'],
    calcium: ['钙']
  }

  // 数值正则：提取数字
  const numberRegex = /(\d+(?:\.\d+)?)\s*(千焦|千卡|kcal|克|g|毫克|mg)/i

  // 按位置分组（同一行的元素 y 坐标相近）
  const lines = groupTextsByLine(texts)

  console.log('[RapidOCR] 按行分组:', lines)

  // 遍历每一行，查找营养名称和对应数值
  for (const line of lines) {
    const lineText = line.map(t => t.text).join(' ')

    // 检查是否包含营养名称
    for (const [key, keywords] of Object.entries(nutritionKeywords)) {
      for (const keyword of keywords) {
        // 特殊处理：排除干扰项
        if (keyword === '脂肪' && lineText.includes('饱和脂肪')) {
          continue
        }
        if (keyword === '钠' && lineText.includes('钠钙')) {
          continue
        }

        if (lineText.includes(keyword)) {
          // 尝试从同一行提取数值
          const match = lineText.match(numberRegex)
          if (match) {
            result[key] = match[1]
            break
          }
        }
      }
    }
  }

  // 特殊处理：钠钙连在一起的情况
  // 查找 "钠钙" 后面的数值
  for (let i = 0; i < texts.length; i++) {
    const item = texts[i]
    if (item.text === '钠钙' || item.text.includes('钠')) {
      // 找到钠钙后，查找同一行的数值
      const sodiumLine = findSameLineTexts(texts, item)
      const sodiumText = sodiumLine.map(t => t.text).join(' ')
      const sodiumMatch = sodiumText.match(/(\d+(?:\.\d+)?)\s*毫克/i)
      if (sodiumMatch) {
        result.sodium = sodiumMatch[1]
      }

      // 查找下一行的数值（可能是钙）
      if (i + 1 < texts.length) {
        const nextItem = texts[i + 1]
        if (nextItem.text.includes('毫克')) {
          const calciumMatch = nextItem.text.match(/(\d+(?:\.\d+)?)\s*毫克/i)
          if (calciumMatch) {
            result.calcium = calciumMatch[1]
          }
        }
      }
    }
  }

  // 特殊处理：一糖（OCR 把"—糖"识别成"一糖"）
  for (let i = 0; i < texts.length; i++) {
    const item = texts[i]
    if (item.text === '一糖' || item.text === '—糖' || item.text === '糖') {
      // 查找同一行或下一行的数值
      const sugarLine = findSameLineTexts(texts, item)
      const sugarText = sugarLine.map(t => t.text).join(' ')
      const sugarMatch = sugarText.match(/(\d+(?:\.\d+)?)\s*克/i)
      if (sugarMatch) {
        result.sugar = sugarMatch[1]
        break
      }

      // 检查下一项
      if (i + 1 < texts.length) {
        const nextItem = texts[i + 1]
        const nextMatch = nextItem.text.match(/(\d+(?:\.\d+)?)\s*克/i)
        if (nextMatch) {
          result.sugar = nextMatch[1]
          break
        }
      }
    }
  }

  console.log('[RapidOCR] 解析的营养数据:', result)
  return result
}

/**
 * 按 Y 坐标将 texts 分组（同一行的元素）
 */
function groupTextsByLine(texts) {
  if (!texts || texts.length === 0) return []

  // 按第一个 box 点的 Y 坐标排序
  const sorted = [...texts].sort((a, b) => {
    const yA = a.box?.[0]?.[1] || 0
    const yB = b.box?.[0]?.[1] || 0
    return yA - yB
  })

  const lines = []
  let currentLine = []
  let lastY = null
  const tolerance = 30 // Y坐标差小于30认为同一行

  for (const item of sorted) {
    const y = item.box?.[0]?.[1] || 0

    if (lastY === null || Math.abs(y - lastY) <= tolerance) {
      currentLine.push(item)
    } else {
      if (currentLine.length > 0) {
        lines.push(currentLine)
      }
      currentLine = [item]
    }
    lastY = y
  }

  if (currentLine.length > 0) {
    lines.push(currentLine)
  }

  return lines
}

/**
 * 查找与指定元素同一行的其他元素
 */
function findSameLineTexts(texts, targetItem) {
  const targetY = targetItem.box?.[0]?.[1] || 0
  const tolerance = 30

  return texts.filter(item => {
    const y = item.box?.[0]?.[1] || 0
    return Math.abs(y - targetY) <= tolerance
  })
}

export default RapidOCRStrategy