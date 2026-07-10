import { request } from '@/utils/request.js'

export function login(username, password) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
}

export function logoutRequest() {
  return request('/api/auth/logout', { method: 'POST' })
}

export function getMe() {
  return request('/api/auth/me')
}
