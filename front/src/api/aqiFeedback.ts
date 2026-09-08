import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

export function getAqiFeedbackList() {
  return request({
    url: 'aqiFeedback/list',
    method: 'get',
  })
}

export function deleteByAfid(afId: number) {
  return request({
    url: `aqiFeedback/delete/${afId}`,
    method: 'get',
  })
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
  state?: number | null
}

export function saveAqiFeedback(data: AqiFeedbackPayload) {
  return request({
    url: 'aqiFeedback/save',
    method: 'post',
    data,
  })
}

export function updateAqiFeedback(data: AqiFeedbackPayload) {
  return request({
    url: 'aqiFeedback/update',
    method: 'post',
    data,
  })
}
