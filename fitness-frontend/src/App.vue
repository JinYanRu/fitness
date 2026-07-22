<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  console.log('App mounted')
})
</script>

<template>
  <div class="app-container">
    <!-- 导航栏 -->
    <nav class="tab-bar" v-if="router.currentRoute.value.path !== '/record'">
      <div
        class="tab-item"
        :class="{ active: router.currentRoute.value.path === '/' }"
        @click="router.push('/')"
      >
        <span class="tab-icon">🏠</span>
        <span class="tab-text">首页</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: router.currentRoute.value.path === '/history' }"
        @click="router.push('/history')"
      >
        <span class="tab-icon">📋</span>
        <span class="tab-text">历史</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: router.currentRoute.value.path === '/profile' }"
        @click="router.push('/profile')"
      >
        <span class="tab-icon">👤</span>
        <span class="tab-text">我的</span>
      </div>
    </nav>

    <!-- 页面内容 -->
    <main class="page-content">
      <router-view />
    </main>
  </div>
</template>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-content {
  flex: 1;
  overflow-y: auto;
}

/* TabBar */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 5px 20px;
}

.tab-icon {
  font-size: 20px;
}

.tab-text {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.tab-item.active .tab-text {
  color: #4CAF50;
}

/* 主题色 */
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