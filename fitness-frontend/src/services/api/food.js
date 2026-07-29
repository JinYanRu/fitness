/**
 * 食物 API 服务
 */
import request from '@/utils/request.js'

// 用户食物库 API
export const userFoodApi = {
  getList() {
    return request.get('/food/user')
  },

  search(keyword) {
    return request.get('/food/user/search', { keyword })
  },

  getById(id) {
    return request.get(`/food/user/${id}`)
  },

  create(data) {
    return request.post('/food/user', data)
  },

  // 从 OCR 结果创建食物
  createFromOcr(data) {
    return request.post('/food/user/from-ocr', data)
  },

  update(id, data) {
    return request.put(`/food/user/${id}`, data)
  },

  delete(id) {
    return request.delete(`/food/user/${id}`)
  }
}

// 菜谱 API
export const recipeApi = {
  getList() {
    return request.get('/recipe')
  },

  search(keyword) {
    return request.get('/recipe/search', { keyword })
  },

  getById(id) {
    return request.get(`/recipe/${id}`)
  },

  create(data) {
    return request.post('/recipe', data)
  },

  update(id, data) {
    return request.put(`/recipe/${id}`, data)
  },

  delete(id) {
    return request.delete(`/recipe/${id}`)
  }
}

export default {
  userFoodApi,
  recipeApi
}