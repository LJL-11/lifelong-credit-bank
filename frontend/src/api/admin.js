import { request } from '@/utils/request.js'

export function getDashboardStats() { return request('/api/admin/dashboard/stats') }
export function getRevenueTrend() { return request('/api/admin/dashboard/revenue') }
export function getIntegrityDist() { return request('/api/admin/dashboard/integrity') }
export function getCourseRanking() { return request('/api/admin/dashboard/course-ranking') }
export function getTableData(url, current = 1, size = 20) { return request(`${url}?current=${current}&size=${size}`) }
export function createRecord(url, data) { return request(url, { method: 'POST', body: JSON.stringify(data) }) }
export function updateRecord(url, id, data) { return request(`${url}/${id}`, { method: 'PUT', body: JSON.stringify(data) }) }
export function deleteRecord(url, id) { return request(`${url}/${id}`, { method: 'DELETE' }) }

export function reviewEnrollment(id, status, remark) { return request(`/api/admin/core/enrollments/${id}/review`, { method: 'PUT', body: JSON.stringify({ status, remark }) }) }
export function reviewAchievement(id, status, rejectReason) { return request(`/api/admin/core/achievements/${id}/review`, { method: 'PUT', body: JSON.stringify({ status, rejectReason }) }) }
export function reviewJobApplication(id, status, remark) { return request(`/api/admin/core/job-applications/${id}/review`, { method: 'PUT', body: JSON.stringify({ status, remark }) }) }
export function recomputeIntegrity(learnerId) { return request(`/api/admin/core/integrity-ratings/${learnerId}/recompute`, { method: 'POST' }) }

export function toggleLearnerStatus(rowId, newStatus) { return request(`/api/admin/learners/${rowId}/status`, { method: 'PUT', body: JSON.stringify({ status: newStatus }) }) }
export function creditOperation(type, payload) { return request(`/api/admin/credit-accounts/${type}`, { method: 'POST', body: JSON.stringify(payload) }) }
export function openAccount(learnerId) { return request(`/api/admin/credit-accounts/open/${learnerId}`, { method: 'POST' }) }
export function freezeOperation(type, body) { return request(`/api/admin/credit-accounts/${type}`, { method: 'POST', body: JSON.stringify(body) }) }
