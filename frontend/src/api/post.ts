import type { PostDetailVO, PostQueryDTO, PostVO, SavePostDTO } from '@/types/post'
import type { Result } from '@/types/result'
import request from '@/utils/request'

export function getPostsApi(params: PostQueryDTO): Promise<Result<PostVO[]>> {
  return request.get('/posts', { params })
}

export function getMyPostsApi(params: PostQueryDTO): Promise<Result<PostVO[]>> {
  return request.get('/posts/my', { params })
}

export function createPostApi(data: SavePostDTO): Promise<Result<null>> {
  return request.post('/posts', data)
}

export function getPostDetailApi(postId: number): Promise<Result<PostDetailVO>> {
  return request.get(`/posts/${postId}`)
}

export function updatePostApi(
  postId: number,
  data: SavePostDTO
): Promise<Result<null>> {
  return request.put(`/posts/${postId}`, data)
}

export function deletePostApi(postId: number): Promise<Result<null>> {
  return request.delete(`/posts/${postId}`)
}
