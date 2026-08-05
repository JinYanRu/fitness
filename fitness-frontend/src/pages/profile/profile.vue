<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="avatar">
        <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="头像" />
        <span v-else class="avatar-placeholder">{{ userInfo?.nickname?.charAt(0) || 'U' }}</span>
      </div>
      <div class="user-info">
        <span class="nickname">{{ userInfo?.nickname || '用户' }}</span>
        <span class="username">@{{ userInfo?.username || '' }}</span>
      </div>
    </div>

    <!-- 基本信息 -->
    <div class="section-card">
      <div class="section-title">基本信息</div>

      <!-- 性别 -->
      <div class="setting-row">
        <span class="setting-label">性别</span>
        <div class="option-group">
          <div
            class="option-btn"
            :class="{ active: profile?.gender === 1 }"
            @click="selectGender(1)"
          >男</div>
          <div
            class="option-btn"
            :class="{ active: profile?.gender === 2 }"
            @click="selectGender(2)"
          >女</div>
        </div>
      </div>

      <!-- 生日 -->
      <div class="setting-row" @click="showBirthdayPicker = true">
        <span class="setting-label">生日</span>
        <div class="setting-value">
          <span v-if="profile?.birthday">{{ formatBirthday(profile.birthday) }}</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>

      <!-- 身高 -->
      <div class="setting-row" @click="showHeightPicker = true">
        <span class="setting-label">身高</span>
        <div class="setting-value">
          <span v-if="profile?.height">{{ profile.height }} cm</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>

      <!-- 当前体重 -->
      <div class="setting-row" @click="showWeightPicker = true">
        <span class="setting-label">当前体重</span>
        <div class="setting-value">
          <span v-if="profile?.weight">{{ profile.weight }} kg</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>

      <!-- 目标体重 -->
      <div class="setting-row" @click="showTargetWeightPicker = true">
        <span class="setting-label">目标体重</span>
        <div class="setting-value">
          <span v-if="profile?.targetWeight">{{ profile.targetWeight }} kg</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 健身目标 -->
    <div class="section-card">
      <div class="section-title">健身目标</div>
      <div class="goal-grid">
        <div
          class="goal-btn"
          :class="{ active: profile?.goal === 'muscle_gain' }"
          @click="selectGoal('muscle_gain')"
        >
          <span class="goal-icon">💪</span>
          <span class="goal-text">增肌</span>
        </div>
        <div
          class="goal-btn"
          :class="{ active: profile?.goal === 'fat_loss' }"
          @click="selectGoal('fat_loss')"
        >
          <span class="goal-icon">🔥</span>
          <span class="goal-text">减脂</span>
        </div>
        <div
          class="goal-btn"
          :class="{ active: profile?.goal === 'maintain' }"
          @click="selectGoal('maintain')"
        >
          <span class="goal-icon">⚖️</span>
          <span class="goal-text">维持</span>
        </div>
      </div>
    </div>

    <!-- 活动水平 -->
    <div class="section-card">
      <div class="section-title">活动水平</div>
      <div class="activity-grid">
        <div
          class="activity-btn"
          :class="{ active: profile?.activityLevel === 'sedentary' }"
          @click="selectActivity('sedentary')"
        >
          <span class="activity-text">久坐</span>
          <span class="activity-desc">几乎不运动</span>
        </div>
        <div
          class="activity-btn"
          :class="{ active: profile?.activityLevel === 'light' }"
          @click="selectActivity('light')"
        >
          <span class="activity-text">轻度</span>
          <span class="activity-desc">每周运动1-3次</span>
        </div>
        <div
          class="activity-btn"
          :class="{ active: profile?.activityLevel === 'moderate' }"
          @click="selectActivity('moderate')"
        >
          <span class="activity-text">中度</span>
          <span class="activity-desc">每周运动3-5次</span>
        </div>
        <div
          class="activity-btn"
          :class="{ active: profile?.activityLevel === 'high' }"
          @click="selectActivity('high')"
        >
          <span class="activity-text">高强度</span>
          <span class="activity-desc">每周运动6-7次</span>
        </div>
      </div>
    </div>

    <!-- 营养素倍率 -->
    <div class="section-card">
      <div class="section-title">营养素倍率 <span class="auto-calc">(体重×倍率)</span></div>

      <!-- 蛋白质倍率 -->
      <div class="setting-row" @click="showProteinPicker = true">
        <span class="setting-label">蛋白质</span>
        <div class="setting-value">
          <span v-if="profile?.proteinMultiplier">{{ profile.proteinMultiplier }} g/kg</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>

      <!-- 脂肪倍率 -->
      <div class="setting-row" @click="showFatPicker = true">
        <span class="setting-label">脂肪</span>
        <div class="setting-value">
          <span v-if="profile?.fatMultiplier">{{ profile.fatMultiplier }} g/kg</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>

      <!-- 碳水倍率 -->
      <div class="setting-row" @click="showCarbsPicker = true">
        <span class="setting-label">碳水</span>
        <div class="setting-value">
          <span v-if="profile?.carbsMultiplier">{{ profile.carbsMultiplier }} g/kg</span>
          <span v-else class="placeholder">请选择</span>
          <span class="arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 营养目标 -->
    <div class="section-card nutrition-target" v-if="profile?.targetCalories">
      <div class="section-title">营养目标参考</div>
      <div class="nutrition-summary">
        <div class="nutrition-item">
          <span class="nutrition-value">{{ profile.targetCalories }}</span>
          <span class="nutrition-unit">kcal/日</span>
        </div>
        <div class="nutrition-item">
          <span class="nutrition-value">{{ profile.targetProtein || 0 }}</span>
          <span class="nutrition-unit">蛋白质 g</span>
        </div>
        <div class="nutrition-item">
          <span class="nutrition-value">{{ profile.targetFat || 0 }}</span>
          <span class="nutrition-unit">脂肪 g</span>
        </div>
        <div class="nutrition-item">
          <span class="nutrition-value">{{ profile.targetCarbs || 0 }}</span>
          <span class="nutrition-unit">碳水 g</span>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-btn" @click="handleLogout">退出登录</div>

    <!-- 生日选择器 -->
    <div class="picker-modal" v-if="showBirthdayPicker" @click.self="showBirthdayPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showBirthdayPicker = false">取消</span>
          <span>选择生日</span>
          <span @click="confirmBirthday">确定</span>
        </div>
        <div class="picker-body">
          <div class="picker-column">
            <div
              class="picker-item"
              v-for="year in years"
              :key="year"
              :class="{ active: tempBirthday.year === year }"
              @click="tempBirthday.year = year"
            >{{ year }}年</div>
          </div>
          <div class="picker-column">
            <div
              class="picker-item"
              v-for="month in 12"
              :key="month"
              :class="{ active: tempBirthday.month === month }"
              @click="tempBirthday.month = month"
            >{{ month }}月</div>
          </div>
          <div class="picker-column">
            <div
              class="picker-item"
              v-for="day in 31"
              :key="day"
              :class="{ active: tempBirthday.day === day }"
              @click="tempBirthday.day = day"
            >{{ day }}日</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 身高选择器 -->
    <div class="picker-modal" v-if="showHeightPicker" @click.self="showHeightPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showHeightPicker = false">取消</span>
          <span>选择身高</span>
          <span @click="confirmHeight">确定</span>
        </div>
        <div class="picker-body single-column">
          <div class="picker-column scrollable">
            <div
              class="picker-item"
              v-for="h in heightRange"
              :key="h"
              :class="{ active: tempHeight === h }"
              @click="tempHeight = h"
            >{{ h }} cm</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 体重选择器 -->
    <div class="picker-modal" v-if="showWeightPicker" @click.self="showWeightPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showWeightPicker = false">取消</span>
          <span>选择体重</span>
          <span @click="confirmWeight">确定</span>
        </div>
        <div class="picker-body single-column">
          <div class="picker-column scrollable">
            <div
              class="picker-item"
              v-for="w in weightRange"
              :key="w"
              :class="{ active: Number(tempWeight).toFixed(1) === w }"
              @click="tempWeight = Number(w)"
            >{{ w }} kg</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 目标体重选择器 -->
    <div class="picker-modal" v-if="showTargetWeightPicker" @click.self="showTargetWeightPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showTargetWeightPicker = false">取消</span>
          <span>选择目标体重</span>
          <span @click="confirmTargetWeight">确定</span>
        </div>
        <div class="picker-body single-column">
          <div class="picker-column scrollable">
            <div
              class="picker-item"
              v-for="w in weightRange"
              :key="w"
              :class="{ active: Number(tempTargetWeight).toFixed(1) === w }"
              @click="tempTargetWeight = Number(w)"
            >{{ w }} kg</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 蛋白质倍率选择器 -->
    <div class="picker-modal" v-if="showProteinPicker" @click.self="showProteinPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showProteinPicker = false">取消</span>
          <span>蛋白质倍率</span>
          <span @click="confirmProteinMultiplier">确定</span>
        </div>
        <div class="picker-body single-column">
          <div class="picker-column scrollable">
            <div
              class="picker-item"
              v-for="p in proteinRange"
              :key="p"
              :class="{ active: tempProteinMultiplier === p }"
              @click="tempProteinMultiplier = p"
            >{{ p.toFixed(1) }} g/kg</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 脂肪倍率选择器 -->
    <div class="picker-modal" v-if="showFatPicker" @click.self="showFatPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showFatPicker = false">取消</span>
          <span>脂肪倍率</span>
          <span @click="confirmFatMultiplier">确定</span>
        </div>
        <div class="picker-body single-column">
          <div class="picker-column scrollable">
            <div
              class="picker-item"
              v-for="f in fatRange"
              :key="f"
              :class="{ active: tempFatMultiplier === f }"
              @click="tempFatMultiplier = f"
            >{{ f.toFixed(1) }} g/kg</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 碳水倍率选择器 -->
    <div class="picker-modal" v-if="showCarbsPicker" @click.self="showCarbsPicker = false">
      <div class="picker-content">
        <div class="picker-header">
          <span @click="showCarbsPicker = false">取消</span>
          <span>碳水倍率</span>
          <span @click="confirmCarbsMultiplier">确定</span>
        </div>
        <div class="picker-body single-column">
          <div class="picker-column scrollable">
            <div
              class="picker-item"
              v-for="c in carbsRange"
              :key="c"
              :class="{ active: tempCarbsMultiplier === c }"
              @click="tempCarbsMultiplier = c"
            >{{ c.toFixed(1) }} g/kg</div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/services/api/auth.js'

import { showToast } from '@/utils/toast.js'

const router = useRouter()

// 用户信息
const userInfo = ref(null)
const profile = ref(null)

// 选择器显示状态
const showBirthdayPicker = ref(false)
const showHeightPicker = ref(false)
const showWeightPicker = ref(false)
const showTargetWeightPicker = ref(false)
const showProteinPicker = ref(false)
const showFatPicker = ref(false)
const showCarbsPicker = ref(false)

// 选择器临时值
const tempBirthday = ref({ year: 2000, month: 1, day: 1 })
const tempHeight = ref(170)
const tempWeight = ref(65)
const tempTargetWeight = ref(60)
const tempProteinMultiplier = ref(2.0)
const tempFatMultiplier = ref(0.8)
const tempCarbsMultiplier = ref(3.0)

// 身高范围: 100-220cm
const heightRange = computed(() => {
  return Array.from({ length: 121 }, (_, i) => i + 100)
})

// 体重范围: 30-150kg，精确到0.5
const weightRange = computed(() => {
  const arr = []
  for (let w = 30; w <= 150; w += 0.5) {
    arr.push(w.toFixed(1))
  }
  return arr
})

// 年份范围: 1940-当前年份
const years = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: currentYear - 1940 + 1 }, (_, i) => 1940 + i)
})

// 蛋白质倍率范围: 0.5~4.0, 步长0.1
const proteinRange = computed(() => {
  const arr = []
  for (let p = 0.5; p <= 4.0; p = Math.round((p + 0.1) * 10) / 10) {
    arr.push(p)
  }
  return arr
})

// 脂肪倍率范围: 0.3~2.5, 步长0.1
const fatRange = computed(() => {
  const arr = []
  for (let f = 0.3; f <= 2.5; f = Math.round((f + 0.1) * 10) / 10) {
    arr.push(f)
  }
  return arr
})

// 碳水倍率范围: 0.5~6.0, 步长0.1
const carbsRange = computed(() => {
  const arr = []
  for (let c = 0.5; c <= 6.0; c = Math.round((c + 0.1) * 10) / 10) {
    arr.push(c)
  }
  return arr
})

// 格式化生日显示
const formatBirthday = (birthday) => {
  if (!birthday) return ''
  const date = new Date(birthday)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

// 加载用户数据
const loadData = async () => {
  try {
    // 获取用户基本信息
    const savedInfo = localStorage.getItem('userInfo')
    if (savedInfo) {
      userInfo.value = JSON.parse(savedInfo)
    }

    // 获取用户档案
    const res = await authApi.getProfileDetail()
    if (res.code === 200 && res.data) {
      profile.value = res.data

      // 初始化选择器临时值
      if (res.data.birthday) {
        const date = new Date(res.data.birthday)
        tempBirthday.value = {
          year: date.getFullYear(),
          month: date.getMonth() + 1,
          day: date.getDate()
        }
      }
      if (res.data.height) {
        tempHeight.value = Number(res.data.height)
      }
      if (res.data.weight) {
        tempWeight.value = Number(res.data.weight)
      }
      if (res.data.targetWeight) {
        tempTargetWeight.value = Number(res.data.targetWeight)
      }
      if (res.data.proteinMultiplier) {
        tempProteinMultiplier.value = Number(res.data.proteinMultiplier)
      }
      if (res.data.fatMultiplier) {
        tempFatMultiplier.value = Number(res.data.fatMultiplier)
      }
      if (res.data.carbsMultiplier) {
        tempCarbsMultiplier.value = Number(res.data.carbsMultiplier)
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 服务端计算、前端不直接编辑的派生字段：保存成功后从服务端同步
const DERIVED_FIELDS = ['bmr', 'tdee', 'targetCalories', 'targetProtein', 'targetFat', 'targetCarbs', 'age']

// 保存队列：串行化后台保存，避免并发响应乱序覆盖本地状态
let saveChain = Promise.resolve()

// 保存档案（乐观更新 + 后台串行保存，不阻塞 UI、不显示 loading）
const saveProfile = (data) => {
  // 记录本次改动前的旧值，用于保存失败时回滚
  const prevValues = {}
  Object.keys(data).forEach(k => {
    prevValues[k] = profile.value?.[k]
  })

  // 1. 乐观更新：立即把改动写回本地，UI 即时响应
  profile.value = { ...profile.value, ...data }

  // 2. 后台串行保存
  saveChain = saveChain.then(async () => {
    try {
      const res = await authApi.updateProfile(data)
      if (res.code === 200 && res.data) {
        // 仅同步服务端重新计算的派生字段，避免覆盖后续乐观改动
        const sync = {}
        DERIVED_FIELDS.forEach(f => {
          if (res.data[f] !== undefined && res.data[f] !== null) {
            sync[f] = res.data[f]
          }
        })
        profile.value = { ...profile.value, ...sync }
        showToast('已保存')
      } else {
        profile.value = { ...profile.value, ...prevValues }
        showToast('保存失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      profile.value = { ...profile.value, ...prevValues }
      showToast('保存失败')
    }
  })

  return saveChain
}


// 选择性别
const selectGender = async (gender) => {
  if (profile.value?.gender === gender) return
  await saveProfile({ gender })
}

// 选择健身目标
const selectGoal = async (goal) => {
  if (profile.value?.goal === goal) return
  await saveProfile({ goal })
}

// 选择活动水平
const selectActivity = async (activityLevel) => {
  if (profile.value?.activityLevel === activityLevel) return
  await saveProfile({ activityLevel })
}

// 确认生日
const confirmBirthday = async () => {
  showBirthdayPicker.value = false
  const { year, month, day } = tempBirthday.value
  const birthday = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  await saveProfile({ birthday })
}

// 确认身高
const confirmHeight = async () => {
  showHeightPicker.value = false
  await saveProfile({ height: tempHeight.value })
}

// 确认体重
const confirmWeight = async () => {
  showWeightPicker.value = false
  await saveProfile({ weight: tempWeight.value })
}

// 确认目标体重
const confirmTargetWeight = async () => {
  showTargetWeightPicker.value = false
  await saveProfile({ targetWeight: tempTargetWeight.value })
}

// 确认蛋白质倍率
const confirmProteinMultiplier = async () => {
  showProteinPicker.value = false
  await saveProfile({ proteinMultiplier: tempProteinMultiplier.value })
}

// 确认脂肪倍率
const confirmFatMultiplier = async () => {
  showFatPicker.value = false
  await saveProfile({ fatMultiplier: tempFatMultiplier.value })
}

// 确认碳水倍率
const confirmCarbsMultiplier = async () => {
  showCarbsPicker.value = false
  await saveProfile({ carbsMultiplier: tempCarbsMultiplier.value })
}

// 退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/auth/login')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e7f1e9 0%, var(--bg) 24%);
  padding: calc(var(--safe-top) + 14px) 16px calc(var(--tab-h) + var(--safe-bottom) + 28px);
}

/* 用户卡片 */
.user-card {
  position: relative;
  background: var(--primary-gradient);
  border-radius: var(--radius-xl);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-primary);
  overflow: hidden;
}
.user-card::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -40px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.avatar {
  position: relative;
  z-index: 1;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.28);
  border: 2px solid rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { font-size: 26px; color: #fff; font-weight: 700; }

.user-info { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.nickname { font-size: 20px; color: #fff; font-weight: 600; }
.username { font-size: 14px; color: rgba(255, 255, 255, 0.82); }

/* 通用卡片样式 */
.section-card {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-xs);
}

.section-title {
  font-size: 13px;
  color: var(--text-3);
  margin-bottom: 10px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* 设置行 */
.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--divider);
  cursor: pointer;
}
.setting-row:last-child { border-bottom: none; }

.setting-label { font-size: 15px; color: var(--text-1); }

.setting-value { display: flex; align-items: center; gap: 8px; color: var(--text-2); font-size: 15px; }
.placeholder { color: var(--text-4); }
.arrow { color: var(--text-4); font-size: 20px; line-height: 1; }

/* 选项组 */
.option-group { display: flex; gap: 10px; }
.option-btn {
  padding: 6px 22px;
  border-radius: 999px;
  background: var(--fill);
  color: var(--text-2);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}
.option-btn.active { background: var(--primary-gradient); color: #fff; box-shadow: 0 4px 12px rgba(67, 160, 71, 0.3); }

/* 健身目标网格 */
.goal-grid { display: flex; gap: 12px; }
.goal-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  border-radius: var(--radius);
  background: var(--fill);
  border: 2px solid transparent;
  transition: all 0.2s;
}
.goal-btn.active { background: var(--primary-50); border-color: var(--primary); }
.goal-icon { font-size: 24px; margin-bottom: 4px; }
.goal-text { font-size: 14px; color: var(--text-2); }
.goal-btn.active .goal-text { color: var(--primary-600); font-weight: 600; }

/* 活动水平网格 */
.activity-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.activity-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: var(--radius);
  background: var(--fill);
  border: 2px solid transparent;
  transition: all 0.2s;
}
.activity-btn.active { background: var(--primary-50); border-color: var(--primary); }
.activity-text { font-size: 15px; color: var(--text-2); margin-bottom: 4px; }
.activity-desc { font-size: 12px; color: var(--text-3); }
.activity-btn.active .activity-text { color: var(--primary-600); font-weight: 600; }

/* 营养目标卡片 */
.nutrition-target {
  background: var(--ai-gradient);
  box-shadow: 0 10px 26px rgba(99, 102, 241, 0.28);
}
.nutrition-target .section-title { color: rgba(255, 255, 255, 0.82); }
.auto-calc { font-size: 12px; font-weight: normal; }

.nutrition-summary { display: flex; justify-content: space-around; }
.nutrition-item { display: flex; flex-direction: column; align-items: center; }
.nutrition-value { font-size: 24px; font-weight: 800; color: #fff; }
.nutrition-unit { font-size: 12px; color: rgba(255, 255, 255, 0.82); }

/* 退出登录按钮 */
.logout-btn {
  background: var(--card);
  border-radius: var(--radius-lg);
  padding: 16px;
  text-align: center;
  color: var(--danger);
  font-size: 16px;
  font-weight: 500;
  margin-top: 16px;
  box-shadow: var(--shadow-xs);
  transition: transform 0.12s, background 0.15s;
}
.logout-btn:active { transform: scale(0.98); background: #fef2f2; }

/* 选择器弹窗 */
.picker-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.picker-content {
  background: var(--card);
  width: 100%;
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  max-height: 62vh;
  display: flex;
  flex-direction: column;
  padding-bottom: var(--safe-bottom);
  animation: sheetUp 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes sheetUp { from { transform: translateY(100%); } to { transform: translateY(0); } }

.picker-header {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--divider);
  font-size: 16px;
  flex-shrink: 0;
}
.picker-header span:first-child,
.picker-header span:last-child { color: var(--primary); cursor: pointer; font-weight: 500; }
.picker-header span:nth-child(2) { font-weight: 600; color: var(--text-1); }

.picker-body {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  max-height: 280px;
}
.picker-body.single-column { justify-content: center; }

.picker-column {
  flex: 1;
  max-height: 250px;
  overflow-y: auto;
  text-align: center;
  -webkit-overflow-scrolling: touch;
}
.picker-column.scrollable { max-width: 150px; }

.picker-item {
  padding: 10px 12px;
  font-size: 16px;
  color: var(--text-2);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.picker-item:active { background: var(--fill); }
.picker-item.active { background: var(--primary-50); color: var(--primary-600); font-weight: 600; }

</style>
