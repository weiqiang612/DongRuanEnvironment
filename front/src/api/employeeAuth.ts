import axios from 'axios'
import type { ResultVO } from './aqiFeedback'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

export type EmployeePortal = 'nepg' | 'nepm' | 'nepv'

export interface EmployeeLoginPayload {
  accountCode: string
  password: string
}

export interface EmployeeLoginUser {
  accountCode: string
  displayName: string
  role: 'NEPG_GRID_MEMBER' | 'NEPM_ADMIN' | 'NEPV_DECISION_MAKER'
}

export function loginEmployee(portal: EmployeePortal, payload: EmployeeLoginPayload) {
  return request.post<ResultVO<EmployeeLoginUser>>(`auth/${portal}/login`, payload)
}
