import type { Result } from '@/types/result'
import type { UpdateProfileDTO, UserHomeVO } from '@/types/user'
import request from '@/utils/request'

export function getUserHomeApi(userId: number): Promise<Result<UserHomeVO>> {
  return request.get(`/users/${userId}`)
}

export function updateProfileApi(
  userId: number,
  data: UpdateProfileDTO
): Promise<Result<string>> {
  return request.put('/users/profile', data, {
    params: { userId }
  })
}

export function uploadAvatarApi(
  userId: number,
  file: File
): Promise<Result<string>> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', String(userId))

  return request.post('/users/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
