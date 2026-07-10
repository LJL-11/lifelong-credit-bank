import { request } from '@/utils/request.js'

export function getActiveSales() { return request('/api/flash/active') }
export function seckill(flashSaleId) { return request(`/api/flash/seckill/${flashSaleId}`, { method: 'POST' }) }
