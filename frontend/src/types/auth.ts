export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  confirmPassword: string
  nickname: string
}

export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  avatar?: string
  role?: string | number
}

export interface LoginVO {
  token: string
  userInfo: UserInfoVO
}
