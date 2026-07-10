import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const loggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const learnerId = computed(() => userInfo.value?.learnerId || 0)

  async function login(username, password) {
    const data = await authApi.login(username, password)
    token.value = data.token
    userInfo.value = { learnerId: data.learnerId, username: data.username, realName: data.realName, role: data.role, institutionName: data.institutionName }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return data
  }

  function logout() {
    authApi.logoutRequest().catch(() => {})
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, loggedIn, isAdmin, learnerId, login, logout }
})
