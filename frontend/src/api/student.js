import { request } from '@/utils/request.js'

export function getProfile() { return request('/api/student/profile') }
export function updateProfile(data) { return request('/api/student/profile', { method: 'PUT', body: JSON.stringify(data) }) }
export function getAccount() { return request('/api/student/account') }
export function openAccount() { return request('/api/student/account/open', { method: 'POST' }) }
export function getProducts(current = 1, size = 10) { return request(`/api/student/products?current=${current}&size=${size}`) }
export function placeOrder(data) { return request('/api/student/orders/place', { method: 'POST', body: JSON.stringify(data) }) }
export function getOrders(current = 1, size = 10) { return request(`/api/student/orders?current=${current}&size=${size}`) }
export function payOrder(id) { return request(`/api/student/orders/${id}/pay`, { method: 'POST' }) }
export function cancelOrder(id) { return request(`/api/student/orders/${id}/cancel`, { method: 'POST' }) }
export function getCourses(current = 1, size = 10) { return request(`/api/student/courses?current=${current}&size=${size}`) }
export function getMyCourses(current = 1, size = 10) { return request(`/api/student/courses/my?current=${current}&size=${size}`) }
export function learnCourse(courseId) { return request(`/api/student/courses/${courseId}/learn`, { method: 'POST' }) }
export function enrollCourse(courseId) { return request(`/api/student/courses/${courseId}/enroll`, { method: 'POST' }) }
export function getEnrollments(current = 1, size = 10) { return request(`/api/student/enrollments?current=${current}&size=${size}`) }
export function signToday() { return request('/api/student/sign-ins/today', { method: 'POST' }) }
export function getSignIns(current = 1, size = 20) { return request(`/api/student/sign-ins?current=${current}&size=${size}`) }
export function getCreditSources(current = 1, size = 20) { return request(`/api/student/credit-transactions?current=${current}&size=${size}`) }
export function submitAchievement(data) { return request('/api/student/achievements', { method: 'POST', body: JSON.stringify(data) }) }
export function getAchievements(current = 1, size = 10) { return request(`/api/student/achievements?current=${current}&size=${size}`) }
export function getJobs(current = 1, size = 10) { return request(`/api/student/jobs?current=${current}&size=${size}`) }
export function applyJob(jobId, data) { return request(`/api/student/jobs/${jobId}/apply`, { method: 'POST', body: JSON.stringify(data) }) }
export function getJobApplications(current = 1, size = 10) { return request(`/api/student/job-applications?current=${current}&size=${size}`) }
export function getIntegrity() { return request('/api/student/integrity') }
export function getLearningRecords(current = 1, size = 100) { return request(`/api/student/learning-records?current=${current}&size=${size}`) }
export function getStats() { return request('/api/student/stats') }
