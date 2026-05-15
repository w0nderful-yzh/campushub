import type { PostQueryDTO, PostVO } from '@/types/post'
import type { Result } from '@/types/result'
import request from '@/utils/request'

export function toggleFavoriteApi(postId: number): Promise<Result<null>> {
  return request.post(`/post-favorites/${postId}`)
}

export function cancelFavoriteApi(postId: number): Promise<Result<null>> {
  return request.delete(`/post-favorites/${postId}`)
}

export function getMyFavoritesApi(params: PostQueryDTO): Promise<Result<PostVO[]>> {
  return request.get('/post-favorites/my', { params })
}
