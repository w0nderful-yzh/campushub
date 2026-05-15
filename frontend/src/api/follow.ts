import type { Result } from '@/types/result'
import request from '@/utils/request'

export interface FollowUserVO {
  id: number
  nickname: string
  avatar?: string
  college?: string
  isFollowed: boolean
}

export interface FollowCountVO {
  followingCount: number
  followerCount: number
}

export function toggleFollowApi(userId: number): Promise<Result<boolean>> {
  return request.post(`/follows/${userId}`)
}

export function getFollowingApi(params: { pageNum: number; pageSize: number }): Promise<Result<FollowUserVO[]>> {
  return request.get('/follows/following', { params })
}

export function getFollowersApi(params: { pageNum: number; pageSize: number }): Promise<Result<FollowUserVO[]>> {
  return request.get('/follows/followers', { params })
}

export function getFollowCountApi(userId: number): Promise<Result<FollowCountVO>> {
  return request.get(`/follows/${userId}/count`)
}
