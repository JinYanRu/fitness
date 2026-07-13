/**
 * 营养数据状态管理
 * 使用 Pinia 进行状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { nutritionApi } from '@/services/api/nutrition.js'

export const useNutritionStore = defineStore('nutrition', () => {
  // 所有记录
  const allRecords = ref([])

  // 加载状态
  const isLoading = ref(false)

  // 今日记录
  const todayRecords = computed(() => {
    const today = new Date().toISOString().split('T')[0]
    return allRecords.value.filter(record => {
      const recordDate = new Date(record.createdAt).toISOString().split('T')[0]
      return recordDate === today
    })
  })

  /**
   * 加载所有记录
   */
  const loadRecords = async () => {
    isLoading.value = true

    try {
      // 尝试从 API 加载
      const response = await nutritionApi.getList()
      allRecords.value = response.data || response || []
    } catch (error) {
      console.error('加载记录失败:', error)

      // 如果 API 失败，尝试从本地存储加载（作为备用）
      const localRecords = uni.getStorageSync('nutrition_records')
      if (localRecords) {
        allRecords.value = JSON.parse(localRecords)
      }
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 保存记录到本地存储（备用方案）
   */
  const saveToLocalStorage = () => {
    uni.setStorageSync('nutrition_records', JSON.stringify(allRecords.value))
  }

  /**
   * 获取单条记录
   */
  const getRecordById = (id) => {
    return allRecords.value.find(record => record.id === id)
  }

  /**
   * 添加记录
   */
  const addRecord = async (record) => {
    try {
      // 尝试通过 API 保存
      const response = await nutritionApi.save(record)
      const newRecord = response.data || response

      // 更新本地状态
      allRecords.value.unshift(newRecord)

      // 同时保存到本地存储
      saveToLocalStorage()

      return newRecord
    } catch (error) {
      console.error('保存记录失败:', error)

      // 如果 API 失败，直接保存到本地存储
      const newRecord = {
        id: Date.now(), // 使用时间戳作为临时ID
        ...record,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }

      allRecords.value.unshift(newRecord)
      saveToLocalStorage()

      return newRecord
    }
  }

  /**
   * 更新记录
   */
  const updateRecord = async (id, data) => {
    try {
      // 尝试通过 API 更新
      await nutritionApi.update(id, data)

      // 更新本地状态
      const index = allRecords.value.findIndex(r => r.id === id)
      if (index >= 0) {
        allRecords.value[index] = {
          ...allRecords.value[index],
          ...data,
          updatedAt: new Date().toISOString()
        }
      }

      saveToLocalStorage()
    } catch (error) {
      console.error('更新记录失败:', error)

      // 如果 API 失败，直接更新本地存储
      const index = allRecords.value.findIndex(r => r.id === id)
      if (index >= 0) {
        allRecords.value[index] = {
          ...allRecords.value[index],
          ...data,
          updatedAt: new Date().toISOString()
        }
        saveToLocalStorage()
      }
    }
  }

  /**
   * 删除记录
   */
  const deleteRecord = async (id) => {
    try {
      // 尝试通过 API 删除
      await nutritionApi.delete(id)

      // 更新本地状态
      allRecords.value = allRecords.value.filter(r => r.id !== id)
      saveToLocalStorage()
    } catch (error) {
      console.error('删除记录失败:', error)

      // 如果 API 失败，直接从本地存储删除
      allRecords.value = allRecords.value.filter(r => r.id !== id)
      saveToLocalStorage()
    }
  }

  /**
   * 清空所有记录
   */
  const clearAllRecords = () => {
    allRecords.value = []
    uni.removeStorageSync('nutrition_records')
  }

  return {
    allRecords,
    todayRecords,
    isLoading,
    loadRecords,
    getRecordById,
    addRecord,
    updateRecord,
    deleteRecord,
    clearAllRecords
  }
})