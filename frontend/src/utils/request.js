// 统一 fetch 封装 —— 带 token 注入 + 401 拦截
import { useAuthStore } from '@/store/auth.js'

const BASE = ''

export async function request(url, options = {}) {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json',
    ...(authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {}),
    ...(options.headers || {}),
  }
  const response = await fetch(BASE + url, { ...options, headers })
  const result = await response.json()
  if (response.status === 401) {
    authStore.logout()
    throw new Error(result.message || '登录已过期')
  }
  if (!response.ok || result.code !== 200) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}
