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
  }
}

export default aiApi
