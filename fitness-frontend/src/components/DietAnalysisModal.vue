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
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}
.analysis-content {
  background: var(--bg);
  width: 100%;
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding-bottom: var(--safe-bottom);
  animation: sheetUp 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes sheetUp { from { transform: translateY(100%); } to { transform: translateY(0); } }

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  background: var(--card);
  border-bottom: 1px solid var(--divider);
  flex-shrink: 0;
}
.header-title { font-size: 16px; font-weight: 600; color: var(--text-1); }
.header-close {
  font-size: 18px;
  color: var(--text-3);
  cursor: pointer;
  padding: 4px 10px;
  background: var(--fill);
  border-radius: 999px;
  line-height: 1;
}

.analysis-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 14px;
}
.loading-spinner {
  width: 38px;
  height: 38px;
  border: 3px solid var(--primary-100);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.loading-text { font-size: 14px; color: var(--text-2); }

.analysis-body { padding: 16px; overflow-y: auto; -webkit-overflow-scrolling: touch; }

.score-card {
  background: var(--primary-gradient);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
  color: #fff;
  box-shadow: var(--shadow-primary);
}
.score-circle {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.score-value { font-size: 32px; font-weight: 800; line-height: 1; }
.score-label { font-size: 12px; opacity: 0.9; margin-top: 2px; }
.score-summary { font-size: 14px; line-height: 1.5; opacity: 0.96; }

.items-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
.item-card { background: var(--card); border-radius: var(--radius); padding: 14px; box-shadow: var(--shadow-xs); }
.item-top { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.item-icon { font-size: 18px; }
.item-name { font-size: 15px; font-weight: 600; color: var(--text-1); flex: 1; }
.item-status { font-size: 12px; padding: 2px 10px; border-radius: 999px; font-weight: 500; }
.item-status.good { color: var(--primary-600); background: var(--primary-100); }
.item-status.high { color: var(--accent); background: #fff6e6; }
.item-status.low { color: #3b82f6; background: #e7f0fe; }
.item-amount { font-size: 13px; color: var(--text-3); margin-bottom: 6px; }
.amount-intake { font-size: 18px; font-weight: 700; color: var(--text-1); }
.amount-sep { color: var(--text-4); }
.amount-target { color: var(--text-3); }
.amount-unit { color: var(--text-3); margin-left: 2px; }
.item-comment { font-size: 13px; color: var(--text-2); line-height: 1.5; }

.suggestions { background: var(--card); border-radius: var(--radius); padding: 16px; box-shadow: var(--shadow-xs); }
.suggestions-title { font-size: 15px; font-weight: 600; color: var(--text-1); margin-bottom: 12px; }
.suggestion-item { display: flex; gap: 10px; margin-bottom: 10px; }
.suggestion-item:last-child { margin-bottom: 0; }
.suggestion-num {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--primary-gradient);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}
.suggestion-text { font-size: 13px; color: var(--text-2); line-height: 1.5; }
</style>
