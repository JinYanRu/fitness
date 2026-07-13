<template>
  <div class="nutrition-card" @click="handleClick">
    <!-- 头部信息 -->
    <div class="card-header">
      <div class="food-info">
        <span class="food-name">{{ data.foodName }}</span>
        <span class="food-type" v-if="data.foodType">{{ getFoodTypeLabel(data.foodType) }}</span>
      </div>
      <div class="card-actions">
        <span class="action-btn delete" @click.stop="handleDelete">删除</span>
      </div>
    </div>

    <!--份量信息 -->
    <div class="serving-info" v-if="data.servingSize">
      <span class="serving-label">份量:</span>
      <span class="serving-value">{{ data.servingSize }} {{ data.servingUnit }}</span>
    </div>

    <!-- 营养信息网格 -->
    <div class="nutrition-grid">
      <!-- 热量 -->
      <div class="nutrition-item primary">
        <span class="nutrition-value">{{ data.calories || '--' }}</span>
        <span class="nutrition-label">热量(kcal)</span>
      </div>

      <!-- 蛋白质 -->
      <div class="nutrition-item">
        <span class="nutrition-value">{{ data.protein || '--' }}</span>
        <span class="nutrition-label">蛋白质(g)</span>
      </div>

      <!-- 脂肪 -->
      <div class="nutrition-item">
        <span class="nutrition-value">{{ data.fat || '--' }}</span>
        <span class="nutrition-label">脂肪(g)</span>
      </div>

      <!-- 碳水 -->
      <div class="nutrition-item">
        <span class="nutrition-value">{{ data.carbohydrates || '--' }}</span>
        <span class="nutrition-label">碳水(g)</span>
      </div>

      <!-- 膳食纤维 -->
      <div class="nutrition-item" v-if="data.fiber">
        <span class="nutrition-value">{{ data.fiber }}</span>
        <span class="nutrition-label">纤维(g)</span>
      </div>

      <!-- 钠 -->
      <div class="nutrition-item" v-if="data.sodium">
        <span class="nutrition-value">{{ data.sodium }}</span>
        <span class="nutrition-label">钠(mg)</span>
      </div>

      <!-- 糖 -->
      <div class="nutrition-item" v-if="data.sugar">
        <span class="nutrition-value">{{ data.sugar }}</span>
        <span class="nutrition-label">糖(g)</span>
      </div>
    </div>

    <!-- 时间信息 -->
    <div class="card-footer">
      <span class="time-text">{{ formatTime(data.createdAt) }}</span>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'delete'])

const foodTypeMap = {
  staple: '主食',
  vegetable: '蔬菜',
  fruit: '水果',
  meat: '肉类',
  dairy: '乳制品',
  snack: '零食',
  drink: '饮料',
  other: '其他'
}

const getFoodTypeLabel = (type) => {
  return foodTypeMap[type] || type
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''

  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    if (hours < 1) {
      const minutes = Math.floor(diff / 60000)
      return minutes < 1 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  }

  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')

  return `${month}/${day} ${hour}:${minute}`
}

const handleClick = () => {
  emit('click', props.data)
}

const handleDelete = () => {
  emit('delete', props.data)
}
</script>

<style scoped>
.nutrition-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
}

.nutrition-card:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.food-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.food-name {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.food-type {
  font-size: 12px;
  color: #fff;
  background-color: #4CAF50;
  padding: 3px 10px;
  border-radius: 4px;
}

.card-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.action-btn.delete {
  color: #ff4444;
}

.serving-info {
  margin-bottom: 12px;
}

.serving-label {
  font-size: 14px;
  color: #999;
}

.serving-value {
  font-size: 14px;
  color: #666;
  margin-left: 5px;
}

.nutrition-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.nutrition-item {
  flex: 1;
  min-width: 100px;
  background-color: #f8f8f8;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.nutrition-item.primary {
  background-color: #E8F5E9;
}

.nutrition-value {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  display: block;
}

.nutrition-item.primary .nutrition-value {
  color: #4CAF50;
}

.nutrition-label {
  font-size: 11px;
  color: #999;
  display: block;
  margin-top: 3px;
}

.card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.time-text {
  font-size: 12px;
  color: #999;
}
</style>