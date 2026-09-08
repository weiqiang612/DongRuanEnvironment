import axios from 'axios'
import type { ResultVO } from './aqiFeedback'

const request = axios.create({ baseURL: '/api', timeout: 5000 })

export interface RegionOption {
  id: number
  name: string
}

export function getProvinceOptions() {
  return request({
    url: 'region/provinces',
    method: 'get',
  }) as Promise<{ data: ResultVO<RegionOption[]> }>
}

export function getCityOptions(provinceId: number) {
  return request({
    url: 'region/cities',
    method: 'get',
    params: { provinceId },
  }) as Promise<{ data: ResultVO<RegionOption[]> }>
}
