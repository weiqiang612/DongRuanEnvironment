import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

export interface RegionOption {
  id: number
  name: string
}

export function getProvinceOptions() {
  return request({
    url: 'region/provinces',
    method: 'get',
  })
}

export function getCityOptions(provinceId: number) {
  return request({
    url: 'region/cities',
    method: 'get',
    params: { provinceId },
  })
}
