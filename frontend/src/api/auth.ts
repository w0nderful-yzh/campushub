import type { LoginDTO, LoginVO, RegisterDTO, UserInfoVO } from '@/types/auth'
import type { Result } from '@/types/result'
import request from '@/utils/request'

export function registerApi(data: RegisterDTO): Promise<Result<string>> {
  return request.post('/auth/register', data)
}

export function loginApi(data: LoginDTO): Promise<Result<LoginVO>> {
  return request.post('/auth/login', data)
}

export function getMeApi(): Promise<Result<UserInfoVO>> {
  return request.get('/auth/me')
}
