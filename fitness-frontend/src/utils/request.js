/**
 * 网络请求封装
 * 支持 Token 自动注入和 401 跳转
 */

// API 基础地址配置 - 使用 Vite 代理，指向 Spring Boot 后端
const API_BASE_URL = '/api'

/**
 * 请求拦截器 - 自动添加 Token
 */
const requestInterceptor = (config) => {
  // 从本地存储获取 token
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = config.header || {}
    config.header.Authorization = `Bearer ${token}`
  }
  return config
}

/**
 * 响应拦截器
 */
const responseInterceptor = (response) => {
  if (response.statusCode === 200) {
    return response.data
  }

  // 处理 401 未授权
  if (response.statusCode === 401) {
    // 清除本地存储的 token
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')

    // 跳转到登录页
    uni.navigateTo({ url: '/auth/login' })
    throw new Error('登录已过期，请重新登录')
  }

  // 处理错误状态码
  const errorMap = {
    400: '请求参数错误',
    403: '拒绝访问',
    404: '资源不存在',
    500: '服务器内部错误'
  }

  const errorMsg = errorMap[response.statusCode] || `请求失败: ${response.statusCode}`
  throw new Error(errorMsg)
}

/**
 * 封装请求方法
 */
const request = {
  get(url, params = {}, options = {}) {
    return new Promise((resolve, reject) => {
      const config = requestInterceptor({
        url: API_BASE_URL + url,
        method: 'GET',
        data: params,
        header: {
          'Content-Type': 'application/json'
        },
        ...options
      })

      uni.request({
        ...config,
        success: (res) => {
          try {
            resolve(responseInterceptor(res))
          } catch (error) {
            reject(error)
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '网络请求失败'))
        }
      })
    })
  },

  post(url, data = {}, options = {}) {
    return new Promise((resolve, reject) => {
      const config = requestInterceptor({
        url: API_BASE_URL + url,
        method: 'POST',
        data,
        header: {
          'Content-Type': 'application/json'
        },
        ...options
      })

      uni.request({
        ...config,
        success: (res) => {
          try {
            resolve(responseInterceptor(res))
          } catch (error) {
            reject(error)
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '网络请求失败'))
        }
      })
    })
  },

  put(url, data = {}, options = {}) {
    return new Promise((resolve, reject) => {
      const config = requestInterceptor({
        url: API_BASE_URL + url,
        method: 'PUT',
        data,
        header: {
          'Content-Type': 'application/json'
        },
        ...options
      })

      uni.request({
        ...config,
        success: (res) => {
          try {
            resolve(responseInterceptor(res))
          } catch (error) {
            reject(error)
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '网络请求失败'))
        }
      })
    })
  },

  delete(url, options = {}) {
    return new Promise((resolve, reject) => {
      const config = requestInterceptor({
        url: API_BASE_URL + url,
        method: 'DELETE',
        header: {
          'Content-Type': 'application/json'
        },
        ...options
      })

      uni.request({
        ...config,
        success: (res) => {
          try {
            resolve(responseInterceptor(res))
          } catch (error) {
            reject(error)
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '网络请求失败'))
        }
      })
    })
  }
}

export default request
export { API_BASE_URL }