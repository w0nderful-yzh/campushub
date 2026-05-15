import type { CommentVO, CreateCommentDTO } from '@/types/comment'
import type { Result } from '@/types/result'
import request from '@/utils/request'

export function createCommentApi(data: CreateCommentDTO): Promise<Result<null>> {
  return request.post('/comments', data)
}

export function getPostCommentsApi(postId: number): Promise<Result<CommentVO[]>> {
  return request.get(`/comments/post/${postId}`)
}

export function deleteCommentApi(commentId: number): Promise<Result<null>> {
  return request.delete(`/comments/${commentId}`)
}
