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
  }
}

export default aiApi
