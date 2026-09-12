import axios from 'axios'
import type { ResultVO } from './aqiFeedback'

export type PortalRole = 'NEPS_SUPERVISOR' | 'NEPG_GRID_MEMBER' | 'NEPM_ADMIN' | 'NEPV_DECISION_MAKER'
export interface SessionStatus {
  portal: PortalRole
  displayName?: string
  accountCode?: string
  telId?: string
}

const sessionRequest = axios.create({ baseURL: '/api', timeout: 5000 })
export const getSessionStatus = () => sessionRequest.get<ResultVO<SessionStatus>>('auth/session')
export const logoutSession = () => sessionRequest.post<ResultVO<boolean>>('auth/logout')
