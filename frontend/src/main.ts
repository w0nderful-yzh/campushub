import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './style.scss'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const authStore = useAuthStore()

router.beforeEach(async (to) => {
  if (!authStore.bootstrapped) {
    await authStore.bootstrap()
  }

  if (to.meta.requiresGuest && authStore.isLoggedIn) {
    return { path: '/' }
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { path: '/' }
  }

  return true
})

app.use(router)
app.mount('#app')
