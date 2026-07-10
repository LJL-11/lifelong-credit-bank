import { request } from '@/utils/request.js'

export function getDashboardStats() { return request('/api/admin/dashboard/stats') }
export function getTableData(url, current = 1, size = 20) { return request(`${url}?current=${current}&size=${size}`) }
export function createRecord(url, data) { return request(url, { method: 'POST', body: JSON.stringify(data) }) }
export function updateRecord(url, id, data) { return request(`${url}/${id}`, { method: 'PUT', body: JSON.stringify(data) }) }
export function deleteRecord(url, id) { return request(`${url}/${id}`, { method: 'DELETE' }) }

export function reviewEnrollment(id, action, remark) { return request(`/api/admin/core/enrollments/${id}/review`, { method: 'PUT', body: JSON.stringify({ action, remark }) }) }
export function reviewAchievement(id, action, reason) { return request(`/api/admin/achievements/${id}/review`, { method: 'PUT', body: JSON.stringify({ action, reason }) }) }
export function reviewJobApplication(id, action, remark) { return request(`/api/admin/core/job-applications/${id}/review`, { method: 'PUT', body: JSON.stringify({ action, remark }) }) }
export function recomputeIntegrity(learnerId) { return request(`/api/admin/core/integrity-ratings/${learnerId}/recompute`, { method: 'POST' }) }

export function toggleLearnerStatus(id) { return request(`/api/admin/learners/${id}/toggle-status`, { method: 'PUT' }) }
export function creditTransaction(learnerId, amount, remark) { return request('/api/admin/credit-accounts/transaction', { method: 'POST', body: JSON.stringify({ learnerId, amount, remark }) }) }
export function freezeCredits(learnerId, amount) { return request('/api/admin/credit-accounts/freeze', { method: 'POST', body: JSON.stringify({ learnerId, amount }) }) }
export function unfreezeCredits(learnerId, amount) { return request('/api/admin/credit-accounts/unfreeze', { method: 'POST', body: JSON.stringify({ learnerId, amount }) }) }
