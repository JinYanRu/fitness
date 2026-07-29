import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('./pages/index/index.vue'),
    meta: { title: '营养记录', requiresAuth: true }
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('./pages/auth/login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('./pages/auth/register.vue'),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/food/library',
    name: 'FoodLibrary',
    component: () => import('./pages/food/library.vue'),
    meta: { title: '食物库', requiresAuth: true }
  },
  {
    path: '/food/edit/:id?',
    name: 'FoodEdit',
    component: () => import('./pages/food/food-edit.vue'),
    meta: { title: '编辑食物', requiresAuth: true }
  },
  {
    path: '/record',
    name: 'Record',
    component: () => import('./pages/record/nutrition-record.vue'),
    meta: { title: '添加记录', requiresAuth: true }
  },
  {
    path: '/record/:id',
    name: 'RecordEdit',
    component: () => import('./pages/record/nutrition-record.vue'),
    meta: { title: '编辑记录', requiresAuth: true }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('./pages/history/history.vue'),
    meta: { title: '历史记录', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('./pages/profile/profile.vue'),
    meta: { title: '我的', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：登录检查
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '健身营养记录'

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      // 未登录，跳转到登录页
      next({
        path: '/auth/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }

  // 如果已登录，不允许访问登录/注册页
  if (to.meta.guest) {
    const token = localStorage.getItem('token')
    if (token) {
      next('/')
      return
    }
  }

  next()
})

export default router