import axios, { AxiosError } from 'axios'

import type { Result } from '@/types/result'
import { clearAuthCache, getToken } from './auth'
import { message } from './message'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>

    if (result && typeof result.code === 'number') {
      if (result.code === 200 || result.success) {
        return result
      }

      if (result.code === 401) {
        clearAuthCache()
      }

      message.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message || '请求失败'))
    }

    return response.data
  },
  (error: AxiosError<{ message?: string }>) => {
    const status = error.response?.status
    const serverMessage = error.response?.data?.message

    if (status === 401) {
      clearAuthCache()
      message.error(serverMessage || '登录已失效，请重新登录')
    } else if (status === 403) {
      message.error(serverMessage || '无权限执行该操作')
    } else if (status === 404) {
      message.error(serverMessage || '请求资源不存在')
    } else if (status === 500) {
      message.error(serverMessage || '服务器错误，请稍后重试')
    } else {
      message.error(serverMessage || error.message || '网络请求失败')
    }

    return Promise.reject(error)
  }
)

export default request
