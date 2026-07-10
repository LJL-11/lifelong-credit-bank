import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

import LoginVue from '@/views/Login.vue'
import LayoutVue from '@/views/Layout.vue'

const routes = [
  { path: '/login', component: LoginVue },
  { path: '/', component: LayoutVue },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.path === '/login') {
    next()
    return
  }
  if (authStore.token) {
    next()
  } else {
    next('/login')
  }
})

export default router
