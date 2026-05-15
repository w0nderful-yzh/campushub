export interface UserHomeVO {
  id: number
  username: string
  nickname: string
  avatar?: string
  gender?: number
  college?: string
  major?: string
  profile?: string
  createTime?: string
  email?: string
}

export interface UpdateProfileDTO {
  nickname: string
  gender?: number | null
  email?: string
  college?: string
  major?: string
  profile?: string
}
