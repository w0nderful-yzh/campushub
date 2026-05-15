import type { Result } from '@/types/result'
import request from '@/utils/request'

export interface NoticeVO {
  id: number
  type: number
  content: string
  senderUserId: number
  senderNickname: string
  senderAvatar?: string
  postId?: number
  commentId?: number
  isRead: number
  createTime: string
}

export function getNoticesApi(params: { type?: number; pageNum: number; pageSize: number }): Promise<Result<NoticeVO[]>> {
  return request.get('/notices', { params })
}

export function getUnreadNoticeCountApi(): Promise<Result<number>> {
  return request.get('/notices/unread-count')
}

export function markNoticeReadApi(noticeId: number): Promise<Result<null>> {
  return request.put(`/notices/${noticeId}/read`)
}

export function markAllNoticesReadApi(): Promise<Result<null>> {
  return request.put('/notices/read-all')
}
