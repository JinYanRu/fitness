/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/services/api/auth.js'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref(null)
  const userProfile = ref(null)
  const token = ref(null)

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 初始化用户状态（从本地存储恢复）
   */
  const initUserState = () => {
    const savedToken = uni.getStorageSync('token')
    const savedUserInfo = uni.getStorageSync('userInfo')

    if (savedToken) {
      token.value = savedToken
    }
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }
  }

  /**
   * 设置 Token
   */
  const setToken = (newToken) => {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  /**
   * 设置用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = info
    uni.setStorageSync('userInfo', info)
  }

  /**
   * 登录
   */
  const login = async (credentials) => {
    const response = await authApi.login(credentials)

    if (response.code === 200 && response.data) {
      setToken(response.data.token)
      setUserInfo(response.data.user)
      return response
    }

    throw new Error(response.message || '登录失败')
  }

  /**
   * 注册
   */
  const register = async (data) => {
    const response = await authApi.register(data)

    if (response.code === 200 && response.data) {
      setToken(response.data.token)
      setUserInfo(response.data.user)
      return response
    }

    throw new Error(response.message || '注册失败')
  }

  /**
   * 登出
   */
  const logout = () => {
    token.value = null
    userInfo.value = null
    userProfile.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  /**
   * 获取用户档案
   */
  const fetchUserProfile = async () => {
    try {
      const response = await authApi.getProfileDetail()
      if (response.code === 200) {
        userProfile.value = response.data
        return response.data
      }
    } catch (error) {
      console.error('获取用户档案失败:', error)
    }
    return null
  }

  /**
   * 更新用户档案
   */
  const updateUserProfile = async (data) => {
    const response = await authApi.updateProfile(data)
    if (response.code === 200) {
      userProfile.value = response.data
      return response.data
    }
    throw new Error(response.message || '更新失败')
  }

  return {
    userInfo,
    userProfile,
    token,
    isLoggedIn,
    initUserState,
    setToken,
    setUserInfo,
    login,
    register,
    logout,
    fetchUserProfile,
    updateUserProfile
  }
})