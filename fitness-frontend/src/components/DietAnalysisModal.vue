<template>
  <div class="analysis-modal" v-if="visible" @click.self="$emit('close')">
    <div class="analysis-content">
      <!-- 头部 -->
      <div class="analysis-header">
        <span class="header-title">🤖 AI 饮食分析</span>
        <span class="header-close" @click="$emit('close')" aria-label="关闭">✕</span>
      </div>

      <!-- 加载中 -->
      <div class="analysis-loading" v-if="loading">
        <div class="loading-spinner"></div>
        <span class="loading-text">AI 正在分析你的饮食…</span>
      </div>

      <!-- 结果 -->
      <div class="analysis-body" v-else-if="result">
        <!-- 评分 -->
        <div class="score-card">
          <div class="score-circle">
            <span class="score-value">{{ result.score }}</span>
            <span class="score-label">分</span>
          </div>
          <div class="score-summary">{{ result.summary }}</div>
        </div>

        <!-- 各项分析 -->
        <div class="items-list" v-if="result.items && result.items.length">
          <div class="item-card" v-for="(item, idx) in result.items" :key="idx">
            <div class="item-top">
              <span class="item-icon">{{ item.icon }}</span>
              <span class="item-name">{{ item.name }}</span>
              <span class="item-status" :class="statusClass(item.status)">{{ item.status }}</span>
            </div>
            <div class="item-amount">
              <span class="amount-intake">{{ item.intake }}</span>
              <span class="amount-sep"> / </span>
              <span class="amount-target">{{ item.target }}</span>
              <span class="amount-unit">{{ item.unit }}</span>
            </div>
            <div class="item-comment">{{ item.comment }}</div>
          </div>
        </div>

        <!-- 改进建议 -->
        <div class="suggestions" v-if="result.suggestions && result.suggestions.length">
          <div class="suggestions-title">💡 改进建议</div>
          <div class="suggestion-item" v-for="(s, idx) in result.suggestions" :key="idx">
            <span class="suggestion-num">{{ idx + 1 }}</span>
            <span class="suggestion-text">{{ s }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  visible: Boolean,
  result: Object,
  loading: Boolean
})

defineEmits(['close'])

// 状态对应的样式类
const statusClass = (status) => {
  if (!status) return ''
  if (status === '达标' || status === '合理') return 'good'
  if (status === '偏高') return 'high'
  if (status === '偏低') return 'low'
  return ''
}
</script>

<style scoped>
.analysis-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.analysis-content {
  background: #f5f5f5;
  width: 100%;
  border-radius: 16px 16px 0 0;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.header-close {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
}

/* 加载中 */
.analysis-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e0e0e0;
  border-top-color: #4CAF50;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
  color: #666;
}

/* 内容区 */
.analysis-body {
  padding: 16px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* 评分卡片 */
.score-card {
  background: linear-gradient(135deg, #4CAF50 0%, #8BC34A 100%);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
  color: #fff;
}

.score-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}

.score-label {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 2px;
}

.score-summary {
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.95;
}

/* 各项分析 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}

.item-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-icon {
  font-size: 18px;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  flex: 1;
}

.item-status {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

.item-status.good {
  color: #4CAF50;
  background: #E8F5E9;
}

.item-status.high {
  color: #ff9800;
  background: #fff3e0;
}

.item-status.low {
  color: #2196F3;
  background: #e3f2fd;
}

.item-amount {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.amount-intake {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.amount-sep {
  color: #ccc;
}

.amount-target {
  color: #999;
}

.amount-unit {
  color: #999;
  margin-left: 2px;
}

.item-comment {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

/* 改进建议 */
.suggestions {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.suggestions-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.suggestion-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.suggestion-item:last-child {
  margin-bottom: 0;
}

.suggestion-num {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #4CAF50;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}

.suggestion-text {
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}
</style>
