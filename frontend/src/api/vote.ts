import type { Result } from '@/types/result'
import request from '@/utils/request'

export interface CreateVoteDTO {
  postId?: number
  title: string
  description?: string
  maxSelect?: number
  isAnonymous?: number
  endTime?: string
  options: string[]
}

export interface VoteOptionVO {
  id: number
  content: string
  count: number
  percentage: number
  isSelected: boolean
}

export interface VoteVO {
  id: number
  userId: number
  postId?: number
  title: string
  description?: string
  maxSelect: number
  isAnonymous: number
  endTime?: string
  totalCount: number
  status: number
  authorNickname: string
  authorAvatar?: string
  isVoted: boolean
  options: VoteOptionVO[]
  createTime: string
}

export function createVoteApi(data: CreateVoteDTO): Promise<Result<number>> {
  return request.post('/votes', data)
}

export function getVotesApi(params: { pageNum: number; pageSize: number }): Promise<Result<VoteVO[]>> {
  return request.get('/votes', { params })
}

export function getVoteDetailApi(id: number): Promise<Result<VoteVO>> {
  return request.get(`/votes/${id}`)
}

export function castVoteApi(id: number, optionIds: number[]): Promise<Result<null>> {
  return request.post(`/votes/${id}/vote`, { optionIds })
}

export function deleteVoteApi(id: number): Promise<Result<null>> {
  return request.delete(`/votes/${id}`)
}

export function getVotesByPostApi(postId: number): Promise<Result<VoteVO[]>> {
  return request.get(`/votes/post/${postId}`)
}
