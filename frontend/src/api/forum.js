import { request } from '@/utils/request.js'

export function getPosts(section, keyword, current = 1, size = 20) {
  let url = `/api/forum/posts?current=${current}&size=${size}`
  if (section) url += `&section=${encodeURIComponent(section)}`
  if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
  return request(url)
}
export function createPost(data) { return request('/api/forum/posts', { method: 'POST', body: JSON.stringify(data) }) }
export function replyPost(postId, content) { return request(`/api/forum/posts/${postId}/replies`, { method: 'POST', body: JSON.stringify({ content }) }) }
export function likePost(postId) { return request(`/api/forum/posts/${postId}/like`, { method: 'POST' }) }
export function unlikePost(postId) { return request(`/api/forum/posts/${postId}/unlike`, { method: 'POST' }) }
