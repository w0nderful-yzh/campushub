<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link class="brand" to="/">
        <span class="brand-mark">C</span>
        <div>
          <div class="brand-title">CampusHub</div>
        </div>
      </router-link>

      <nav class="nav">
        <router-link class="nav-link" to="/">首页</router-link>
        <router-link class="nav-link" to="/posts/create">发帖</router-link>
        <router-link class="nav-link" to="/activities">活动</router-link>
        <router-link v-if="authStore.isLoggedIn" class="nav-link" to="/me">
          我的主页
        </router-link>
      </nav>

      <div class="actions">
        <template v-if="authStore.isLoggedIn && authStore.user">
          <router-link class="icon-btn" to="/notices" title="通知">
            <n-badge :value="unreadNoticeCount" :max="99" :show="unreadNoticeCount > 0">
              <span class="icon-text">🔔</span>
            </n-badge>
          </router-link>
          <router-link class="icon-btn" to="/messages" title="私信">
            <n-badge :value="unreadMessageCount" :max="99" :show="unreadMessageCount > 0">
              <span class="icon-text">✉</span>
            </n-badge>
          </router-link>
          <router-link class="user-chip" to="/me">
            <img
              class="avatar"
              :src="resolveAvatarUrl(authStore.user.avatar, authStore.user.nickname || authStore.user.username || 'U')"
              alt="avatar"
            />
            <span>{{ authStore.user.nickname || authStore.user.username }}</span>
          </router-link>
          <n-button quaternary @click="handleLogout">退出</n-button>
        </template>

        <template v-else>
          <router-link to="/login">
            <n-button quaternary>登录</n-button>
          </router-link>
          <router-link to="/register">
            <n-button type="primary">注册</n-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NBadge, NButton } from 'naive-ui'
import { useRouter } from 'vue-router'

import { getUnreadNoticeCountApi } from '@/api/notice'
import { getUnreadMessageCountApi } from '@/api/message'
import { useAuthStore } from '@/stores/auth'
import { resolveAvatarUrl } from '@/utils/url'
import { message } from '@/utils/message'

const router = useRouter()
const authStore = useAuthStore()

const unreadNoticeCount = ref(0)
const unreadMessageCount = ref(0)

function handleLogout() {
  authStore.logout()
  message.success('已退出登录')
  router.push('/login')
}

async function loadUnreadCounts() {
  if (!authStore.isLoggedIn) return
  try {
    const [noticeRes, msgRes] = await Promise.all([
      getUnreadNoticeCountApi(),
      getUnreadMessageCountApi()
    ])
    unreadNoticeCount.value = (noticeRes.data as number) || 0
    unreadMessageCount.value = (msgRes.data as number) || 0
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadUnreadCounts()
})
</script>

<style scoped lang="scss">
.app-header {
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(14px);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.86));
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.05);
}

.header-inner {
  width: min(1120px, calc(100vw - 32px));
  height: 74px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  align-items: center;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  transition: transform 0.2s ease;
}

.brand:hover {
  transform: translateY(-1px);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #3b82f6, #2563eb 55%, #1d4ed8);
  color: #ffffff;
  font-size: 22px;
  font-weight: 800;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.28);
}

.brand-title {
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 0.2px;
  background: linear-gradient(135deg, #0f172a 10%, #1e3a8a 90%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  color: #334155;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: #0f172a;
  background: rgba(148, 163, 184, 0.12);
}

.nav-link.router-link-active {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.12);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transition: background 0.2s;
  text-decoration: none;
}

.icon-btn:hover {
  background: rgba(148, 163, 184, 0.12);
}

.icon-text {
  font-size: 18px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 6px 12px 6px 6px;
  background: #ffffff;
  border: 1px solid #e5edf6;
  border-radius: 999px;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
  transition: all 0.2s ease;
}

.user-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.1);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background: #eff6ff;
}

@media (max-width: 768px) {
  .header-inner {
    width: calc(100vw - 20px);
    grid-template-columns: 1fr auto;
    height: auto;
    padding: 14px 0;
  }

  .nav {
    display: none;
  }

  .brand-title {
    font-size: 26px;
  }
}
</style>
