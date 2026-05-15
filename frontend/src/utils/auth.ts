import type { UserInfoVO } from '@/types/auth'

const TOKEN_KEY = 'campushub_token'
const USER_KEY = 'campushub_user'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getCachedUser(): UserInfoVO | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as UserInfoVO
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export function setCachedUser(user: UserInfoVO | null): void {
  if (!user) {
    localStorage.removeItem(USER_KEY)
    return
  }
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuthCache(): void {
  clearToken()
  localStorage.removeItem(USER_KEY)
}
