import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

export interface ResultVO<T> {
  code: number
  message: string
  data: T
}

export interface AqiFeedbackPayload {
  afId?: number
  telId: string
  provinceId: number | null
  cityId: number | null
  address: string
  information: string
  estimatedGrade: number | null
  afDate: string
  afTime: string
}

export interface AqiFeedbackRow extends AqiFeedbackPayload {
  afId: number
  provinceName?: string
  cityName?: string
  state?: number
  timeoutFlag?: boolean
}

export function getAqiFeedbackList() {
  return request({
    url: 'aqiFeedback/list',
    method: 'get',
  }) as Promise<{ data: ResultVO<AqiFeedbackRow[]> }>
}

export function deleteByAfid(afId: number) {
  return request({
    url: `aqiFeedback/delete/${afId}`,
    method: 'get',
  }) as Promise<{ data: ResultVO<boolean> }>
}

export function saveAqiFeedback(data: AqiFeedbackPayload) {
  return request({
    url: 'aqiFeedback/save',
    method: 'post',
    data,
  }) as Promise<{ data: ResultVO<boolean> }>
}

export function updateAqiFeedback(data: AqiFeedbackPayload) {
  return request({
    url: 'aqiFeedback/update',
    method: 'post',
    data,
  }) as Promise<{ data: ResultVO<boolean> }>
}
