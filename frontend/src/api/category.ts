import type { CategoryVO } from '@/types/category'
import type { Result } from '@/types/result'
import request from '@/utils/request'

export function getCategoriesApi(): Promise<Result<CategoryVO[]>> {
  return request.get('/categories')
}
