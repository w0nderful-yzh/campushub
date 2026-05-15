import type { Result } from '@/types/result'
import request from '@/utils/request'

export interface CreateActivityDTO {
  title: string
  description?: string
  coverImg?: string
  location?: string
  activityType?: number
  startTime: string
  endTime?: string
  maxParticipants?: number
}

export interface ActivityVO {
  id: number
  userId: number
  title: string
  description?: string
  coverImg?: string
  location?: string
  activityType?: number
  startTime: string
  endTime?: string
  maxParticipants: number
  currentCount: number
  status: number
  authorNickname: string
  authorAvatar?: string
  isSignedUp: boolean
  createTime: string
}

export interface ActivitySignupVO {
  userId: number
  nickname: string
  avatar?: string
  signupTime: string
}

export function createActivityApi(data: CreateActivityDTO): Promise<Result<number>> {
  return request.post('/activities', data)
}

export function getActivitiesApi(params: { activityType?: number; keyword?: string; pageNum: number; pageSize: number }): Promise<Result<ActivityVO[]>> {
  return request.get('/activities', { params })
}

export function getActivityDetailApi(id: number): Promise<Result<ActivityVO>> {
  return request.get(`/activities/${id}`)
}

export function updateActivityApi(id: number, data: CreateActivityDTO): Promise<Result<null>> {
  return request.put(`/activities/${id}`, data)
}

export function cancelActivityApi(id: number): Promise<Result<null>> {
  return request.delete(`/activities/${id}`)
}

export function signupActivityApi(id: number): Promise<Result<null>> {
  return request.post(`/activities/${id}/signup`)
}

export function cancelSignupApi(id: number): Promise<Result<null>> {
  return request.delete(`/activities/${id}/signup`)
}

export function getSignupsApi(id: number, params: { pageNum: number; pageSize: number }): Promise<Result<ActivitySignupVO[]>> {
  return request.get(`/activities/${id}/signups`, { params })
}

export function getMyActivitiesApi(params: { pageNum: number; pageSize: number }): Promise<Result<ActivityVO[]>> {
  return request.get('/activities/my', { params })
}

export function getMySignupsApi(params: { pageNum: number; pageSize: number }): Promise<Result<ActivityVO[]>> {
  return request.get('/activities/my-signups', { params })
}
