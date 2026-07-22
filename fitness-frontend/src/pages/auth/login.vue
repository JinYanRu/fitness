<template>
  <div class="login-page">
    <div class="login-header">
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
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #4CAF50 0%, #8BC34A 100%);
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 36px;
  color: #fff;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.login-form {
  width: 100%;
  max-width: 300px;
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.form-item {
  margin-bottom: 20px;
}

.input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.input:focus {
  border-color: #4CAF50;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  margin-bottom: 10px;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  width: 100%;
  padding: 12px;
  background: transparent;
  color: #4CAF50;
  border: 1px solid #4CAF50;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.error-msg {
  color: #f44336;
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}
</style>