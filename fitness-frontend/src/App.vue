<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentPath = computed(() => router.currentRoute.value.path)
const isRecordRoute = computed(() => currentPath.value.startsWith('/record'))

onMounted(() => {
  console.log('App mounted')
})
</script>

<template>
  <div class="app-container">
    <!-- 页面内容 -->
    <main class="page-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部导航栏 -->
    <nav class="tab-bar" v-if="!isRecordRoute">
      <div
        class="tab-item"
        :class="{ active: currentPath === '/' }"
        @click="router.push('/')"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9.5 12 3l9 6.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V9.5Z" />
        </svg>
        <span class="tab-text">首页</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: currentPath === '/history' }"
        @click="router.push('/history')"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="4.5" width="18" height="16.5" rx="2.5" />
          <path d="M16 2.5v4M8 2.5v4M3 10h18M8.5 14.5h.01M12 14.5h.01M15.5 14.5h.01M8.5 18h.01M12 18h.01" />
        </svg>
        <span class="tab-text">历史</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: currentPath === '/profile' }"
        @click="router.push('/profile')"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="8.5" r="4" />
          <path d="M5 21c0-3.9 3.1-6 7-6s7 2.1 7 6" />
        </svg>
        <span class="tab-text">我的</span>
      </div>
    </nav>
  </div>
</template>

<style>
/* ===== 设计令牌 ===== */
:root {
  /* 品牌色 - 活力绿 */
  --primary: #4CAF50;
  --primary-600: #43a047;
  --primary-700: #388e3c;
  --primary-300: #81c784;
  --primary-100: #e8f5e9;
  --primary-50: #f1f8f3;
  --primary-gradient: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
  --primary-gradient-soft: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%);

  /* 功能色 */
  --protein: #3b82f6;
  --fat: #f59e0b;
  --carbs: #22c55e;
  --accent: #ff9800;
  --danger: #ef4444;
  --ai-gradient: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);

  /* 中性色阶 */
  --text-1: #1f2a24;
  --text-2: #4b5563;
  --text-3: #9aa3a0;
  --text-4: #c7ccc9;
  --bg: #f3f6f3;
  --bg-soft: #f7f9f7;
  --card: #ffffff;
  --fill: #f4f6f4;
  --border: #eceeec;
  --divider: #f0f2f0;

  /* 圆角 */
  --radius-sm: 10px;
  --radius: 14px;
  --radius-lg: 18px;
  --radius-xl: 24px;

  /* 阴影 */
  --shadow-xs: 0 1px 2px rgba(16, 30, 24, 0.04);
  --shadow-sm: 0 2px 8px rgba(16, 30, 24, 0.06);
  --shadow: 0 6px 22px rgba(16, 30, 24, 0.08);
  --shadow-lg: 0 14px 38px rgba(16, 30, 24, 0.14);
  --shadow-primary: 0 10px 26px rgba(67, 160, 71, 0.30);

  /* 安全区与尺寸 */
  --safe-bottom: env(safe-area-inset-bottom, 0px);
  --safe-top: env(safe-area-inset-top, 0px);
  --tab-h: 60px;
}

/* ===== 基础重置 ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
}

html, body {
  height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background: var(--bg);
  color: var(--text-1);
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

button {
  font-family: inherit;
  border: none;
  background: none;
  cursor: pointer;
}

input, select, textarea {
  font-family: inherit;
}

::selection {
  background: var(--primary-100);
}

/* 滚动条美化 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.14); border-radius: 6px; }
::-webkit-scrollbar-track { background: transparent; }

/* ===== 应用骨架 ===== */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* 页面切换过渡 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.18s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* ===== 底部导航栏 ===== */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(var(--tab-h) + var(--safe-bottom));
  padding-bottom: var(--safe-bottom);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-around;
  align-items: stretch;
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding-top: 8px;
  color: var(--text-3);
  cursor: pointer;
  transition: color 0.2s ease;
  position: relative;
}

.tab-icon {
  width: 24px;
  height: 24px;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.tab-text {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.2px;
}

.tab-item.active {
  color: var(--primary);
}

.tab-item.active .tab-icon {
  transform: translateY(-1px) scale(1.08);
}

.tab-item.active::before {
  content: '';
  position: absolute;
  top: 2px;
  width: 22px;
  height: 3px;
  border-radius: 3px;
  background: var(--primary);
  opacity: 0.85;
}

/* 兼容旧令牌（部分页面仍引用） */
:root {
  --primary-color: #4CAF50;
  --primary-light: #81C784;
  --primary-dark: #388E3C;
  --accent-color: #FF9800;
  --text-primary: #333333;
  --text-secondary: #666666;
  --text-hint: #999999;
  --divider-color: #E0E0E0;
  --background-color: #F5F5F5;
  --card-background: #FFFFFF;
}
</style>