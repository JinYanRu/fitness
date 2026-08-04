/**
 * AI 服务 API
 */
import request from '@/utils/request.js'

export const aiApi = {
  /**
   * 解析食谱文本
   * @param {string} content - 食谱文本内容
   * @returns {Promise} 解析结果
   */
  parseRecipe(content) {
    return request.post('/ai/parse-recipe', { content })
  },

  /**
   * 解析食谱图片
   * @param {string} imageBase64 - Base64 编码的图片数据
   * @param {string} supplement - 补充说明文本（可选）
   * @returns {Promise} 解析结果
   */
  parseRecipeImage(imageBase64, supplement = '') {
    return request.post('/ai/parse-recipe-image', { imageBase64, supplement })
  },

  /**
   * 根据食物名称填充营养成分
   * @param {string} foodName - 食物名称
   * @returns {Promise} 营养成分信息
   */
  fillNutrition(foodName) {
    return request.post('/ai/fill-nutrition', { foodName })
  },

  /**
   * AI 分析每日饮食
   * 综合身体数据和当日饮食记录，给出评分、各项营养分析和改进建议
   * @param {string} date - 日期 (yyyy-MM-dd)，可选，默认今天
   * @returns {Promise} 分析结果
   */
  analyzeDiet(date) {
    return request.post('/ai/analyze-diet', { date })
  }
}

export default aiApi
