import { defineStore } from 'pinia'

import { getCategoriesApi } from '@/api/category'
import type { CategoryVO } from '@/types/category'

interface AppState {
  categories: CategoryVO[]
  loaded: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    categories: [],
    loaded: false
  }),

  actions: {
    async fetchCategories(force = false) {
      if (this.loaded && !force) return

      const res = await getCategoriesApi()
      this.categories = res.data || []
      this.loaded = true
    }
  }
})
