<template>
  <div class="register-page">
    <div class="register-header">
      <h1 class="title">注册账号</h1>
      <p class="subtitle">开启健康饮食之旅</p>
    </div>

    <div class="register-form">
      <div class="form-item">
        <input
          v-model="form.username"
          type="text"
          placeholder="用户名（3-20字符）"
          class="input"
        />
      </div>

      <div class="form-item">
        <input
          v-model="form.password"
          type="password"
          placeholder="密码（至少6位）"
          class="input"
        />
      </div>

      <div class="form-item">
        <input
          v-model="form.nickname"
          type="text"
          placeholder="昵称（可选）"
          class="input"
        />
      </div>

      <button @click="handleRegister" class="btn-primary" :disabled="loading">
        {{ loading ? '注册中...' : '注册' }}
      </button>

      <button @click="goToLogin" class="btn-secondary">
        已有账号？去登录
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
  password: '',
  nickname: ''
})

const loading = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  // 验证
  if (!form.value.username) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (form.value.username.length < 3 || form.value.username.length > 20) {
    errorMsg.value = '用户名长度需要在3-20之间'
    return
  }
  if (!form.value.password) {
    errorMsg.value = '请输入密码'
    return
  }
  if (form.value.password.length < 6) {
    errorMsg.value = '密码长度至少6位'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const response = await authApi.register(form.value)

    if (response.code === 200 && response.data) {
      // 注册成功，保存 Token 并跳转
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userInfo', JSON.stringify(response.data.user))

      // 跳转到首页
      router.push('/')
    } else {
      errorMsg.value = response.message || '注册失败'
    }
  } catch (error) {
    errorMsg.value = error.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/auth/login')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #4CAF50 0%, #8BC34A 100%);
  padding: 20px;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  color: #fff;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.register-form {
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