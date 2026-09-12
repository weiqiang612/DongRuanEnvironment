import { request } from './http'

import type { ResultVO } from './aqiFeedback'

export interface NepgTask {
  afId: number
  provinceName: string
  cityName: string
  address: string
  information: string
  estimatedGrade: number
  state: 1 | 2
  timeoutFlag: boolean
  assignedAt: string | null
  completedAt: string | null
}

export interface NepgTaskDetail extends NepgTask {
  finalGrade: number | null
  finalGradeName: string | null
  so2Value: number | null
  so2Level: number | null
  coValue: number | null
  coLevel: number | null
  spmValue: number | null
  spmLevel: number | null
  detectedAt: string | null
  alertGenerated: boolean
}

export interface MeasurementPayload { so2Value: number; coValue: number; spmValue: number }

export const getNepgTasks = () => request.get<ResultVO<NepgTask[]>>('nepg/tasks')
export const getNepgTask = (afId: string | number) => request.get<ResultVO<NepgTaskDetail>>(`nepg/tasks/${afId}`)
export const submitMeasurement = (afId: string | number, payload: MeasurementPayload) =>
  request.post<ResultVO<NepgTaskDetail>>(`nepg/tasks/${afId}/measurements`, payload)
