<template>
  <div class="login-page">
    <div class="login-header">
      <div class="brand-logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 20A7 7 0 0 1 4 13C4 8 8 4 13 4c2 0 4 .5 5 1.5C17 12 14 18 8 19" />
          <path d="M11 20c0-4 1.5-7 4-9.5" />
        </svg>
      </div>
      <h1 class="title">营养记录</h1>
      <p class="subtitle">科学饮食，健康生活</p>
    </div>

    <div class="login-form">
      <div class="form-item">
        <input
          v-model="form.username"
          type="text"
          placeholder="请输入用户名"
          class="input"
        />
      </div>

      <div class="form-item">
        <input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          class="input"
        />
      </div>

      <button @click="handleLogin" class="btn-primary" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <button @click="goToRegister" class="btn-secondary">
        没有账号？去注册
      </button>
    </div>

    <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/services/api/auth.js'

const router = useRouter()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const response = await authApi.login(form.value)

    if (response.code === 200 && response.data) {
      // 保存 Token 和用户信息
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userInfo', JSON.stringify(response.data.user))

      // 跳转到首页
      router.push('/')
    } else {
      errorMsg.value = response.message || '登录失败'
    }
  } catch (error) {
    errorMsg.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/auth/register')
}
</script>

<style scoped>
.login-page,
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(160deg, #2e7d32 0%, #43a047 48%, #7cb342 100%);
  padding: 24px 20px calc(var(--safe-bottom) + 24px);
  position: relative;
  overflow: hidden;
}
.login-page::before,
.register-page::before {
  content: '';
  position: absolute;
  top: -80px;
  right: -60px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.10);
  filter: blur(2px);
}
.login-page::after,
.register-page::after {
  content: '';
  position: absolute;
  bottom: -90px;
  left: -70px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  filter: blur(2px);
}

.login-header,
.register-header {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-bottom: 32px;
}

.brand-logo {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.18);
}
.brand-logo svg { width: 38px; height: 38px; }

.title {
  font-size: 30px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}
.subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
}

.login-form,
.register-form {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 320px;
  background: rgba(255, 255, 255, 0.97);
  padding: 28px 24px;
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.22);
  backdrop-filter: blur(10px);
}

.form-item { margin-bottom: 18px; }

.input {
  width: 100%;
  padding: 13px 16px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  font-size: 16px;
  color: var(--text-1);
  background: var(--bg-soft);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.input::placeholder { color: var(--text-4); }
.input:focus {
  border-color: var(--primary);
  background: #fff;
  box-shadow: 0 0 0 4px var(--primary-100);
}

.btn-primary {
  width: 100%;
  padding: 13px;
  background: var(--primary-gradient);
  color: #fff;
  border-radius: var(--radius);
  font-size: 16px;
  font-weight: 600;
  box-shadow: var(--shadow-primary);
  transition: transform 0.12s, box-shadow 0.2s;
  margin-bottom: 12px;
}
.btn-primary:active { transform: translateY(1px) scale(0.99); }
.btn-primary:disabled { background: #c8c8c8; box-shadow: none; cursor: not-allowed; }

.btn-secondary {
  width: 100%;
  padding: 12px;
  background: transparent;
  color: var(--primary);
  border: 1.5px solid var(--primary);
  border-radius: var(--radius);
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s;
}
.btn-secondary:active { background: var(--primary-50); }

.error-msg {
  position: relative;
  z-index: 1;
  color: #fff;
  background: rgba(244, 67, 54, 0.92);
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  padding: 10px 16px;
  border-radius: var(--radius);
  max-width: 320px;
  box-shadow: 0 8px 20px rgba(244, 67, 54, 0.35);
}
</style>