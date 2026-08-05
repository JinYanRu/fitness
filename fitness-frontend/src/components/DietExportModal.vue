<template>
  <div class="export-modal" v-if="visible" @click.self="$emit('close')">
    <div class="export-content">
      <!-- 头部 -->
      <div class="export-header">
        <span class="header-title">📋 饮食报告</span>
        <span class="header-close" @click="$emit('close')" aria-label="关闭">✕</span>
      </div>

      <!-- 报告预览 -->
      <div class="export-body">
        <pre class="report-text">{{ reportText }}</pre>
        <p class="report-tip">已自动复制，可直接粘贴；也可点下方按钮再次复制</p>
      </div>

      <!-- 底部操作 -->
      <div class="export-footer">
        <button class="copy-btn" :class="{ copied }" @click="doCopy">
          <span v-if="copied">✓ 已复制到剪切板</span>
          <span v-else>📋 复制到剪切板</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { copyText } from '@/utils/clipboard.js'

import { showToast } from '@/utils/toast.js'

const props = defineProps({
  visible: Boolean,
  reportText: String,
  copied: Boolean
})

defineEmits(['close'])

const copied = ref(false)
let timer = null

// 弹窗打开时同步初始复制状态
watch(
  () => props.visible,
  (v) => {
    if (v) {
      copied.value = !!props.copied
      if (copied.value) {
        clearTimeout(timer)
        timer = setTimeout(() => (copied.value = false), 2500)
      }
    }
  }
)

const doCopy = async () => {
  const ok = await copyText(props.reportText || '')
  copied.value = ok
  clearTimeout(timer)
  timer = setTimeout(() => (copied.value = false), 2500)
  if (!ok) showToast('复制失败，请长按上方文本手动复制')
}
</script>

<style scoped>
.export-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}
.export-content {
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

.export-header {
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

.export-body {
  padding: 16px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  flex: 1;
}
.report-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'SF Mono', 'Menlo', 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-1);
  background: var(--card);
  border-radius: var(--radius);
  padding: 16px;
  box-shadow: var(--shadow-xs);
}
.report-tip {
  margin: 10px 2px 0;
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.5;
}

.export-footer {
  padding: 12px 16px calc(12px + var(--safe-bottom));
  background: var(--card);
  border-top: 1px solid var(--divider);
  flex-shrink: 0;
}
.copy-btn {
  width: 100%;
  padding: 14px;
  border-radius: var(--radius-lg);
  background: var(--primary-gradient);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  box-shadow: var(--shadow-primary);
  transition: transform 0.12s, background 0.2s;
}
.copy-btn:active { transform: scale(0.98); }
.copy-btn.copied { background: var(--primary-600); box-shadow: none; }
</style>
