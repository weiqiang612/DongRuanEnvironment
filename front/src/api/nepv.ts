import { request } from './http'
import type { ResultVO } from './aqiFeedback'

export interface NepvQuery {
  provinceId?: number
  cityId?: number
  submittedFrom?: string
  submittedTo?: string
}

export interface NepvDashboard {
  totalDetections: number
  highPollutionDetections: number
  totalAlerts: number
  pendingAlerts: number
  gridCoverage: { totalCities: number; coveredCities: number; coverageRate: number }
  aqiDistribution: Array<{ aqiId: number; label: string; count: number }>
  monthlyTrends: Array<{ month: string; detectionCount: number; alertCount: number }>
  provinceRisks: Array<{
    provinceId: number
    provinceName: string
    detectionCount: number
    highPollutionCount: number
    pendingAlertCount: number
  }>
  cityRisks: Array<{
    cityId: number
    cityName: string
    detectionCount: number
    highPollutionCount: number
    pendingAlertCount: number
    dominantAqiId: number | null
  }>
  recentAlerts: Array<{
    id: number
    alertLevel: number
    alertStatus: 'PENDING' | 'HANDLED'
    provinceName: string
    cityName: string
    address: string
    createdAt: string
  }>
}

export function getNepvDashboard(params: NepvQuery) {
  return request({ url: 'nepv/dashboard', method: 'get', params }) as Promise<{ data: ResultVO<NepvDashboard> }>
}
