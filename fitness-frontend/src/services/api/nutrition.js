/**
 * 营养数据 API 服务
 */
import request from '@/utils/request.js'

export const nutritionApi = {
  /**
   * 保存营养记录
   * @param {Object} data - 营养数据
   */
  save(data) {
    return request.post('/nutrition', data)
  },

  /**
   * 更新营养记录
   * @param {number|string} id - 记录ID
   * @param {Object} data - 更新的数据
   */
  update(id, data) {
    return request.put(`/nutrition/${id}`, data)
  },

  /**
   * 获取营养记录列表
   * @param {Object} params - 查询参数
   */
  getList(params = {}) {
    return request.get('/nutrition', params)
  },

  /**
   * 获取单条记录
   * @param {number|string} id - 记录ID
   */
  getById(id) {
    return request.get(`/nutrition/${id}`)
  },

  /**
   * 删除记录
   * @param {number|string} id - 记录ID
   */
  delete(id) {
    return request.delete(`/nutrition/${id}`)
  },

  /**
   * 导入指定日期的记录到今天（导入的记录默认未吃）
   * @param {string} sourceDate - 源日期 yyyy-MM-dd（通常为昨天）
   */
  importFrom(sourceDate) {
    return request.post('/nutrition/import', { sourceDate })
  },

  /**
   * 标记记录是否已吃
   * @param {number|string} id - 记录ID
   * @param {boolean} eaten - 是否已吃
   */
  markEaten(id, eaten) {
    return request.put(`/nutrition/${id}/eaten`, { eaten })
  },

  /**
   * 获取今日统计
   */
  getTodayStats() {
    return request.get('/nutrition/stats/today')
  },

  /**
   * 获取指定日期范围内的记录
   * @param {string} startDate - 开始日期
   * @param {string} endDate - 结束日期
   */
  getByDateRange(startDate, endDate) {
    return request.get('/nutrition/range', { startDate, endDate })
  },

  /**
   * 获取指定日期的记录
   * @param {string} date - 日期 (yyyy-MM-dd)
   */
  getByDate(date) {
    return request.get(`/nutrition/daily/${date}`)
  },

  /**
   * 获取指定日期的营养统计
   * @param {string} date - 日期 (yyyy-MM-dd)
   */
  getStatsByDate(date) {
    return request.get(`/nutrition/stats/${date}`)
  }
}

export default nutritionApi