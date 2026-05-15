import type { PostQueryDTO, PostVO } from '@/types/post'
import type { Result } from '@/types/result'
import request from '@/utils/request'

export function toggleLikeApi(postId: number): Promise<Result<null>> {
  return request.post(`/post-likes/${postId}`)
}

export function cancelLikeApi(postId: number): Promise<Result<null>> {
  return request.delete(`/post-likes/${postId}`)
}

export function getMyLikesApi(params: PostQueryDTO): Promise<Result<PostVO[]>> {
  return request.get('/post-likes/my', { params })
}
