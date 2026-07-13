/**
 * 认证 API 服务
 */
import request from '@/utils/request.js'

export const authApi = {
  /**
   * 用户登录
   * @param {Object} data - { username, password }
   */
  login(data) {
    return request.post('/auth/login', data)
  },

  /**
   * 用户注册
   * @param {Object} data - { username, password, nickname }
   */
  register(data) {
    return request.post('/auth/register', data)
  },

  /**
   * 获取当前用户信息
   */
  getProfile() {
    return request.get('/auth/profile')
  },

  /**
   * 获取用户档案详情
   */
  getProfileDetail() {
    return request.get('/auth/profile/detail')
  },

  /**
   * 更新用户档案
   * @param {Object} data - 档案数据
   */
  updateProfile(data) {
    return request.put('/auth/profile', data)
  }
}

export default authApi