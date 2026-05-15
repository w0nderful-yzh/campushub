import type { Result } from '@/types/result'
import request from '@/utils/request'

export interface CreateReportDTO {
  targetType: number
  targetId: number
  reason: string
}

export interface ReportVO {
  id: number
  reportUserId: number
  reportNickname: string
  targetType: number
  targetId: number
  reason: string
  status: number
  handleUserId?: number
  handleNickname?: string
  handleResult?: string
  createTime: string
  updateTime?: string
}

export function createReportApi(data: CreateReportDTO): Promise<Result<null>> {
  return request.post('/reports', data)
}

export function getReportsApi(params: { status?: number; pageNum: number; pageSize: number }): Promise<Result<ReportVO[]>> {
  return request.get('/reports', { params })
}

export function handleReportApi(reportId: number, handleResult: string): Promise<Result<null>> {
  return request.put(`/reports/${reportId}/handle`, { handleResult })
}
