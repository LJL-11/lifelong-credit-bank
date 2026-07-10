import { ref } from 'vue'

export function useApi(tokenRef, onLogout) {
  const toast = ref({ text: '', type: 'ok' })
  let toastTimer = 0

  function showToast(text, type = 'ok') {
    toast.value = { text, type }
    clearTimeout(toastTimer)
    toastTimer = setTimeout(() => { toast.value = { text: '', type: 'ok' } }, 2600)
  }

  async function request(url, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...(tokenRef.value ? { Authorization: `Bearer ${tokenRef.value}` } : {}),
      ...(options.headers || {}),
    }
    const response = await fetch(url, { ...options, headers })
    const result = await response.json()
    if (response.status === 401) {
      showToast(result.message || '登录已过期', 'error')
      if (onLogout) onLogout()
      throw new Error(result.message || '未登录')
    }
    if (!response.ok || result.code !== 200) {
      throw new Error(result.message || '请求失败')
    }
    return result.data
  }

  return { request, showToast, toast }
}
