import { request } from './http'
import type { ResultVO } from './aqiFeedback'

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

export interface NepsRegisterPayload {
  telId: string
  password: string
}

export function registerNeps(payload: NepsRegisterPayload) {
  return request.post<ResultVO<boolean>>('auth/neps/register', payload)
}

export function logoutNeps() {
  return request.post<ResultVO<boolean>>('auth/neps/logout')
}
