import { request } from './http'
import type { AqiFeedbackRow, ResultVO } from './aqiFeedback'

export interface DashboardSummary {
  pending: number
  pendingChange?: number
  assigned: number
  assignedChange?: number
  completed: number
  completedChange?: number
  timeout: number
  timeoutChange?: number
  todayCompleted?: number
}
export interface GridMember { gmId: string; gmName: string; provinceId: number; cityId: number; cityName: string; sourceLevel: 'SAME_CITY' | 'SAME_PROVINCE' }
export interface AssignLog { id: number; actionType: string; fromGmId?: string; toGmId?: string; createdAt?: string }
export interface NepmPage<T> { items: T[]; total: number; page: number; pageSize: number }
export type TaskStatus = '待指派' | '处理中' | '已完成' | '已超时'
export type AqiGrade = '优' | '良' | '轻度污染' | '中度污染' | '重度污染' | '严重污染'

export interface RecentTrendData {
  dates: string[]
  newFeedbacks: number[]
  completedFeedbacks: number[]
  pendingFeedbacks: number[]
}

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
export const getRecentTrend = () => request.get<ResultVO<RecentTrendData>>('nepm/analytics/recent-trend')
export const getOverview = (params: Pick<FeedbackQuery, 'provinceId' | 'cityId' | 'submittedFrom' | 'submittedTo'>) =>
  request.get<ResultVO<DashboardSummary & { total: number }>>('nepm/analytics/overview', { params })
export const getFeedbacks = (params: FeedbackQuery) => request.get<ResultVO<NepmPage<AqiFeedbackRow>>>('nepm/feedbacks', { params })
export const getTimeoutAlerts = (params: Omit<FeedbackQuery, 'timeoutOnly' | 'estimatedGrade'>) =>
  request.get<ResultVO<NepmPage<AqiFeedbackRow>>>('nepm/timeout-alerts', { params })
export const getFeedback = (id: number) => request.get<ResultVO<AqiFeedbackRow>>(`nepm/feedbacks/${id}`)
export const getCandidates = (id: number) => request.get<ResultVO<GridMember[]>>(`nepm/feedbacks/${id}/candidates`)
export const getLogs = (id: number) => request.get<ResultVO<AssignLog[]>>(`nepm/feedbacks/${id}/logs`)
export const dispatchFeedback = (id: number, gridMemberId: string) => request.post<ResultVO<AqiFeedbackRow>>(`nepm/feedbacks/${id}/dispatch`, { gridMemberId })

// ==================== TASK-009: 检测结果、AQI 预警与统计分析 ====================

export interface DetectionResultItem {
  id: number
  feedbackId: number
  provinceId: number
  provinceName: string
  cityId: number
  cityName: string
  address: string
  information: string
  estimatedGrade: number
  gmId: string
  gmName: string
  so2Value: number
  so2Level: number
  coValue: number
  coLevel: number
  spmValue: number
  spmLevel: number
  aqiId: number
  detectedAt: string
  submittedAt: string
}

export interface DetectionResultQuery {
  provinceId?: number
  cityId?: number
  aqiId?: number
  submittedFrom?: string
  submittedTo?: string
  keyword?: string
  page?: number
  pageSize?: number
}

export const getDetectionResults = (params: DetectionResultQuery) =>
  request.get<ResultVO<NepmPage<DetectionResultItem>>>('nepm/detection-results', { params })

export const getDetectionResult = (id: number) =>
  request.get<ResultVO<DetectionResultItem>>(`nepm/detection-results/${id}`)

export interface AqiAlertItem {
  id: number
  feedbackId: number
  resultId: number
  alertLevel: number
  alertStatus: 'PENDING' | 'HANDLED'
  createdAt: string
  handledAt: string | null
  provinceId: number
  provinceName: string
  cityId: number
  cityName: string
  address: string
  aqiId: number
  gmName: string
}

export interface AqiAlertQuery {
  status?: 'PENDING' | 'HANDLED'
  provinceId?: number
  cityId?: number
  page?: number
  pageSize?: number
}

export const getAqiAlerts = (params: AqiAlertQuery) =>
  request.get<ResultVO<NepmPage<AqiAlertItem>>>('nepm/aqi-alerts', { params })

export const handleAqiAlert = (alertId: number) =>
  request.post<ResultVO<AqiAlertItem>>(`nepm/aqi-alerts/${alertId}/handle`)

export interface AqiDistributionItem {
  aqiId: number
  aqiName: string
  count: number
}

export interface MonthlyTrendItem {
  month: string
  detectionCount: number
  alertCount: number
}

export interface RegionRiskItem {
  regionName: string
  feedbackCount: number
  highPollutionRate: number
}

export interface EfficiencyTrendItem {
  month: string
  feedbackCount: number
  completionRate: number
}

export interface PollutionTrendItem {
  month: string
  highPollutionRate: number
}

export interface KeyRegionItem {
  rank: number
  regionName: string
  feedbackCount: number
  highPollutionRate: number
  changeRate: string
  priorityLevel: string
  advice: string
}

export interface AnalyticsStats {
  totalDetections: number
  aqiDistribution: AqiDistributionItem[]
  monthlyTrends: MonthlyTrendItem[]
  highAlertCount: number
  pendingAlertCount: number
  handledAlertCount: number
  totalFeedbacks?: number
  completionRate?: number
  completionRateChange?: number
  timeoutRate?: number
  timeoutRateChange?: number
  highPollutionRate?: number
  highPollutionRateChange?: number
  periodInsight?: string
  regionRisks?: RegionRiskItem[]
  efficiencyTrends?: EfficiencyTrendItem[]
  pollutionTrends?: PollutionTrendItem[]
  keyRegions?: KeyRegionItem[]
}

export interface AnalyticsQuery {
  provinceId?: number
  cityId?: number
  submittedFrom?: string
  submittedTo?: string
}

export const getAnalyticsStats = (params: AnalyticsQuery) =>
  request.get<ResultVO<AnalyticsStats>>('nepm/analytics/stats', { params })
