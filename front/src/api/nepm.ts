import { request } from './http'
import type { AqiFeedbackRow, ResultVO } from './aqiFeedback'

export interface DashboardSummary { pending: number; assigned: number; completed: number; timeout: number }
export interface GridMember { gmId: string; gmName: string; provinceId: number; cityId: number; cityName: string; sourceLevel: 'SAME_CITY' | 'SAME_PROVINCE' }
export interface AssignLog { id: number; actionType: string; fromGmId?: string; toGmId?: string; createdAt?: string }
export interface NepmPage<T> { items: T[]; total: number; page: number; pageSize: number }
export type TaskStatus = '待指派' | '处理中' | '已完成' | '已超时'
export type AqiGrade = '优' | '良' | '轻度污染' | '中度污染' | '重度污染' | '严重污染'

export interface FeedbackItem {
  id: number
  sn: string
  address: string
  submitTime: string
  status: TaskStatus
  estimatedAqi: AqiGrade
  finalAqi?: AqiGrade | '-'
  handler?: string
}

export interface DispatchItem extends FeedbackItem {
  handler: string
}
export interface FeedbackQuery {
  provinceId?: number
  cityId?: number
  states?: number[]
  timeoutOnly?: boolean
  estimatedGrade?: number
  submittedFrom?: string
  submittedTo?: string
  keyword?: string
  page?: number
  pageSize?: number
}

export const getDashboard = () => request.get<ResultVO<DashboardSummary>>('nepm/dashboard')
export const getOverview = (params: Pick<FeedbackQuery, 'provinceId' | 'cityId' | 'submittedFrom' | 'submittedTo'>) =>
  request.get<ResultVO<DashboardSummary & { total: number }>>('nepm/analytics/overview', { params })
export const getFeedbacks = (params: FeedbackQuery) => request.get<ResultVO<NepmPage<AqiFeedbackRow>>>('nepm/feedbacks', { params })
export const getTimeoutAlerts = (params: Omit<FeedbackQuery, 'timeoutOnly' | 'estimatedGrade'>) =>
  request.get<ResultVO<NepmPage<AqiFeedbackRow>>>('nepm/timeout-alerts', { params })
export const getFeedback = (id: number) => request.get<ResultVO<AqiFeedbackRow>>(`nepm/feedbacks/${id}`)
export const getCandidates = (id: number) => request.get<ResultVO<GridMember[]>>(`nepm/feedbacks/${id}/candidates`)
export const getLogs = (id: number) => request.get<ResultVO<AssignLog[]>>(`nepm/feedbacks/${id}/logs`)
export const dispatchFeedback = (id: number, gridMemberId: string) => request.post<ResultVO<AqiFeedbackRow>>(`nepm/feedbacks/${id}/dispatch`, { gridMemberId })
