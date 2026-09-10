import axios from 'axios'
import type { ResultVO } from './aqiFeedback'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

export interface NepsLoginPayload {
  telId: string
  password: string
}

export interface NepsLoginUser {
  telId: string
  realName: string
}

export function loginNeps(payload: NepsLoginPayload) {
  return request.post<ResultVO<NepsLoginUser>>('auth/neps/login', payload)
}

export function logoutNeps() {
  return request.post<ResultVO<boolean>>('auth/neps/logout')
}
