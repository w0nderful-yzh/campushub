import { defineStore } from 'pinia'

import { getMeApi, loginApi } from '@/api/auth'
import type { LoginDTO, LoginVO, UserInfoVO } from '@/types/auth'
import { clearAuthCache, getCachedUser, getToken, setCachedUser, setToken } from '@/utils/auth'

type LoginPayload = LoginVO

interface AuthState {
  token: string
  user: UserInfoVO | null
  bootstrapped: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: getToken(),
    user: getCachedUser(),
    bootstrapped: false
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },

  actions: {
    applyAuth(payload: LoginPayload) {
      this.token = payload.token
      this.user = payload.userInfo
      setToken(payload.token)
      setCachedUser(payload.userInfo)
    },

    updateUser(user: UserInfoVO | null) {
      this.user = user
      setCachedUser(user)
    },

    async bootstrap() {
      if (this.bootstrapped) return

      if (!this.token) {
        this.bootstrapped = true
        return
      }

      if (this.user) {
        this.bootstrapped = true
        return
      }

      try {
        const res = await getMeApi()
        this.updateUser(res.data || null)
      } catch {
        // 保持静默，避免启动时弹错
      } finally {
        this.bootstrapped = true
      }
    },

    async loginAction(data: LoginDTO) {
      const res = await loginApi(data)
      if (res.data) {
        this.applyAuth(res.data)
      }

      try {
        const me = await getMeApi()
        if (me.data) {
          this.updateUser({
            ...this.user,
            ...me.data
          } as UserInfoVO)
        }
      } catch {
        // 某些后端环境下 /me 依赖拦截器，失败时保留登录响应中的 userInfo
      }

      return res
    },

    logout() {
      this.token = ''
      this.user = null
      this.bootstrapped = true
      clearAuthCache()
    }
  }
})
