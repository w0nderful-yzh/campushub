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
              <div class="follow-stats">
                <span class="stat-item">
                  <strong>{{ followCount.followingCount }}</strong> 关注
                </span>
                <span class="stat-item">
                  <strong>{{ followCount.followerCount }}</strong> 粉丝
                </span>
              </div>
            </div>

            <n-space v-if="isSelf">
              <router-link to="/me">
                <n-button quaternary>进入我的主页</n-button>
              </router-link>
            </n-space>
            <n-space v-else>
              <n-button
                :type="isFollowed ? 'default' : 'primary'"
                :loading="followLoading"
                @click="handleToggleFollow"
              >
                {{ isFollowed ? '已关注' : '关注' }}
              </n-button>
              <router-link to="/messages">
                <n-button quaternary>发私信</n-button>
              </router-link>
            </n-space>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { NAlert, NButton, NSpace, NSpin } from 'naive-ui'
import { useRoute } from 'vue-router'

import { getFollowCountApi, toggleFollowApi, getFollowingApi } from '@/api/follow'
import { getUserHomeApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { UserHomeVO } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'
import { message } from '@/utils/message'

const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const errorText = ref('')
const profile = ref<UserHomeVO | null>(null)
const isFollowed = ref(false)
const followLoading = ref(false)
const followCount = reactive({ followingCount: 0, followerCount: 0 })

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

async function loadFollowCount() {
  try {
    const res = await getFollowCountApi(userId.value)
    if (res.data) {
      followCount.followingCount = res.data.followingCount
      followCount.followerCount = res.data.followerCount
    }
  } catch {
    // ignore
  }
}

async function checkFollowStatus() {
  if (!authStore.isLoggedIn || isSelf.value) return
  try {
    const res = await getFollowingApi({ pageNum: 1, pageSize: 100 })
    if (res.data) {
      isFollowed.value = res.data.some(u => u.id === userId.value)
    }
  } catch {
    // ignore
  }
}

async function handleToggleFollow() {
  if (!authStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  followLoading.value = true
  try {
    const res = await toggleFollowApi(userId.value)
    isFollowed.value = res.data === true
    followCount.followerCount += isFollowed.value ? 1 : -1
    message.success(isFollowed.value ? '关注成功' : '已取消关注')
  } catch {
    message.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadFollowCount()
  checkFollowStatus()
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

.follow-stats {
  display: flex;
  gap: 20px;
  margin-top: 8px;
  font-size: 14px;
  color: #64748b;
}

.stat-item strong {
  color: #0f172a;
  font-size: 16px;
  margin-right: 4px;
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
