<template>
  <div class="page-container">
    <div v-if="loading" class="section-card loading-card">
      <n-spin size="large" />
    </div>

    <n-alert v-else-if="errorText" type="error" :show-icon="false">
      {{ errorText }}
    </n-alert>

    <section v-else-if="profile" class="section-card profile-card">
      <div class="profile-main">
        <img
          class="profile-avatar"
          :src="resolveAvatarUrl(profile.avatar, profile.nickname || profile.username || 'U')"
          alt="avatar"
        />

        <div class="profile-info">
          <div class="page-header" style="margin-bottom: 10px;">
            <div>
              <h1 class="page-title" style="margin-bottom: 6px;">
                {{ profile.nickname || profile.username }}
              </h1>
              <div class="muted">
                用户名：{{ profile.username }} · 注册于 {{ formatDateTime(profile.createTime) }}
              </div>
            </div>

            <router-link v-if="isSelf" to="/me">
              <n-button quaternary>进入我的主页</n-button>
            </router-link>
          </div>

          <div class="profile-grid">
            <div class="info-item">
              <span class="label">学院</span>
              <span>{{ profile.college || '未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="label">专业</span>
              <span>{{ profile.major || '未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span>{{ genderText(profile.gender) }}</span>
            </div>
            <div class="info-item">
              <span class="label">简介</span>
              <span>{{ profile.profile || '这个人还没有留下简介。' }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NAlert, NButton, NSpin } from 'naive-ui'
import { useRoute } from 'vue-router'

import { getUserHomeApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { UserHomeVO } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'

const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const errorText = ref('')
const profile = ref<UserHomeVO | null>(null)

const userId = computed(() => Number(route.params.id))
const isSelf = computed(() => authStore.user?.id === userId.value)

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

async function loadProfile() {
  loading.value = true
  errorText.value = ''

  try {
    const res = await getUserHomeApi(userId.value)
    profile.value = res.data
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '用户信息加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.profile-card {
  padding: 24px;
}

.profile-main {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 20px;
  align-items: start;
}

.profile-avatar {
  width: 120px;
  height: 120px;
  border-radius: 28px;
  object-fit: cover;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
}

.label {
  color: #64748b;
  font-size: 13px;
}

.loading-card {
  min-height: 220px;
  display: grid;
  place-items: center;
}

@media (max-width: 768px) {
  .profile-main {
    grid-template-columns: 1fr;
  }

  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
