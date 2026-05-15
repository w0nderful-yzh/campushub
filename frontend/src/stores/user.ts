import { defineStore } from 'pinia'

import { getUserHomeApi } from '@/api/user'
import type { UserHomeVO } from '@/types/user'

interface UserState {
  profileMap: Record<number, UserHomeVO>
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    profileMap: {}
  }),

  actions: {
    async fetchUserProfile(userId: number, force = false) {
      if (this.profileMap[userId] && !force) {
        return this.profileMap[userId]
      }

      const res = await getUserHomeApi(userId)
      this.profileMap[userId] = res.data
      return res.data
    }
  }
})
