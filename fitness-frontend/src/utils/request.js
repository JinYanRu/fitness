/**
 * 网络请求封装
 * 支持 Token 自动注入和 401 跳转
 */

import { useRouter } from 'vue-router'

// API 基础地址配置 - 使用 Vite 代理，指向 Spring Boot 后端
const API_BASE_URL = '/api'

// 全局 router 实例
let routerInstance = null

/**
 * 设置 router 实例（在 main.js 中调用）
 */
export const setupRequestRouter = (router) => {
  routerInstance = router
}

/**
 * 请求拦截器 - 自动添加 Token
 */
const requestInterceptor = (config) => {
  // 从本地存储获取 token
  const token = localStorage.getItem('token')
  if (token) {
    config.header = config.header || {}
    config.header.Authorization = `Bearer ${token}`
  }
  return config
}

/**
 * 处理 401 未授权
 */
const handle401 = () => {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')

  // 跳转到登录页
  if (routerInstance) {
    routerInstance.push('/auth/login')
  } else {
    // 如果 router 还没初始化，使用 window.location
    window.location.href = '/#/auth/login'
  }
}

/**
 * 响应拦截器
 */
const responseInterceptor = (response) => {
  // 检查 HTTP 状态码
  if (response.statusCode === 200) {
    const data = response.data

    // 检查业务状态码
    if (data && data.code === 401) {
      handle401()
      throw new Error('登录已过期，请重新登录')
    }

    return data
  }

  // 处理 HTTP 401 未授权
  if (response.statusCode === 401) {
    handle401()
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

      fetch(config.url, {
        method: 'GET',
        headers: config.header
      })
        .then(res => res.json())
        .then(data => {
          // 检查业务状态码
          if (data && data.code === 401) {
            handle401()
            reject(new Error('登录已过期，请重新登录'))
            return
          }
          resolve(data)
        })
        .catch(err => {
          reject(new Error(err.message || '网络请求失败'))
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

      fetch(config.url, {
        method: 'POST',
        headers: config.header,
        body: JSON.stringify(config.data)
      })
        .then(res => res.json())
        .then(data => {
          // 检查业务状态码
          if (data && data.code === 401) {
            handle401()
            reject(new Error('登录已过期，请重新登录'))
            return
          }
          resolve(data)
        })
        .catch(err => {
          reject(new Error(err.message || '网络请求失败'))
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

      fetch(config.url, {
        method: 'PUT',
        headers: config.header,
        body: JSON.stringify(config.data)
      })
        .then(res => res.json())
        .then(data => {
          // 检查业务状态码
          if (data && data.code === 401) {
            handle401()
            reject(new Error('登录已过期，请重新登录'))
            return
          }
          resolve(data)
        })
        .catch(err => {
          reject(new Error(err.message || '网络请求失败'))
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

      fetch(config.url, {
        method: 'DELETE',
        headers: config.header
      })
        .then(res => res.json())
        .then(data => {
          // 检查业务状态码
          if (data && data.code === 401) {
            handle401()
            reject(new Error('登录已过期，请重新登录'))
            return
          }
          resolve(data)
        })
        .catch(err => {
          reject(new Error(err.message || '网络请求失败'))
        })
    })
  }
}

export default request
export { API_BASE_URL }