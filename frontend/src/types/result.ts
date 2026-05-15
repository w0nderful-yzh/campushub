export interface Result<T = unknown> {
  code: number
  message: string
  data: T
  total?: number
  success: boolean
}
