<template>
  <div class="page-container">
    <div v-if="loading" class="section-card loading-card"><n-spin size="large" /></div>

    <template v-else-if="activity">
      <section class="section-card" style="padding: 24px;">
        <div class="detail-header">
          <div>
            <n-tag :type="activity.status === 1 ? 'success' : 'default'" size="small">
              {{ activity.status === 1 ? '报名中' : activity.status === 0 ? '已取消' : '已结束' }}
            </n-tag>
            <span class="act-type">{{ typeText(activity.activityType) }}</span>
          </div>
          <n-space v-if="isOwner">
            <n-popconfirm @positive-click="handleCancel">
              <template #trigger><n-button size="small" type="error">取消活动</n-button></template>
              确定取消此活动？
            </n-popconfirm>
          </n-space>
        </div>

        <h1 class="detail-title">{{ activity.title }}</h1>

        <div class="detail-meta">
          <div>📍 地点：{{ activity.location || '待定' }}</div>
          <div>🕐 开始：{{ formatDateTime(activity.startTime) }}</div>
          <div v-if="activity.endTime">🕐 结束：{{ formatDateTime(activity.endTime) }}</div>
          <div>👥 报名：{{ activity.currentCount }}{{ activity.maxParticipants > 0 ? ' / ' + activity.maxParticipants : '' }} 人</div>
        </div>

        <div v-if="activity.description" class="detail-desc">{{ activity.description }}</div>

        <div class="detail-author">
          <img :src="resolveAvatarUrl(activity.authorAvatar, activity.authorNickname || 'U')" class="author-avatar" />
          <span>发布者：{{ activity.authorNickname }}</span>
        </div>

        <div class="detail-actions">
          <n-button v-if="!isOwner && activity.status === 1 && !activity.isSignedUp"
            type="primary" size="large" :loading="signupLoading" @click="handleSignup">
            立即报名
          </n-button>
          <n-button v-if="!isOwner && activity.isSignedUp"
            size="large" :loading="signupLoading" @click="handleCancelSignup">
            取消报名
          </n-button>
        </div>
      </section>

      <section class="section-card" style="padding: 22px; margin-top: 16px;">
        <h3 style="margin-bottom: 12px;">报名人员 ({{ signups.length }})</h3>
        <div v-if="signups.length" class="signup-list">
          <div v-for="s in signups" :key="s.userId" class="signup-item">
            <img :src="resolveAvatarUrl(s.avatar, s.nickname || 'U')" class="signup-avatar" />
            <span>{{ s.nickname }}</span>
          </div>
        </div>
        <div v-else class="muted">暂无人报名</div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NButton, NPopconfirm, NSpace, NSpin, NTag } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'

import { getActivityDetailApi, signupActivityApi, cancelSignupApi, cancelActivityApi, getSignupsApi, type ActivityVO, type ActivitySignupVO } from '@/api/activity'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'
import { message } from '@/utils/message'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const signupLoading = ref(false)
const activity = ref<ActivityVO | null>(null)
const signups = ref<ActivitySignupVO[]>([])

const activityId = computed(() => Number(route.params.id))
const isOwner = computed(() => authStore.user?.id === activity.value?.userId)

function typeText(t?: number) {
  const map: Record<number, string> = { 1: '讲座', 2: '聚会', 3: '运动', 4: '其他' }
  return t ? map[t] || '其他' : '其他'
}

async function loadDetail() {
  loading.value = true
  try {
    const [detailRes, signupsRes] = await Promise.all([
      getActivityDetailApi(activityId.value),
      getSignupsApi(activityId.value, { pageNum: 1, pageSize: 100 })
    ])
    activity.value = detailRes.data
    signups.value = signupsRes.data || []
  } finally {
    loading.value = false
  }
}

async function handleSignup() {
  if (!authStore.isLoggedIn) { message.warning('请先登录'); return }
  signupLoading.value = true
  try {
    await signupActivityApi(activityId.value)
    message.success('报名成功')
    await loadDetail()
  } catch { /* */ } finally { signupLoading.value = false }
}

async function handleCancelSignup() {
  signupLoading.value = true
  try {
    await cancelSignupApi(activityId.value)
    message.success('已取消报名')
    await loadDetail()
  } catch { /* */ } finally { signupLoading.value = false }
}

async function handleCancel() {
  await cancelActivityApi(activityId.value)
  message.success('活动已取消')
  await loadDetail()
}

onMounted(() => loadDetail())
</script>

<style scoped lang="scss">
.loading-card { min-height: 200px; display: grid; place-items: center; }
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.act-type { font-size: 12px; color: #64748b; margin-left: 8px; }
.detail-title { font-size: 24px; font-weight: 800; color: #0f172a; margin-bottom: 16px; }
.detail-meta { font-size: 14px; color: #475569; display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.detail-desc { font-size: 14px; color: #334155; line-height: 1.7; margin-bottom: 16px; padding: 16px; background: #f8fafc; border-radius: 12px; }
.detail-author { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #334155; margin-bottom: 20px; }
.author-avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.detail-actions { display: flex; gap: 12px; }
.signup-list { display: flex; flex-wrap: wrap; gap: 12px; }
.signup-item { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.signup-avatar { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
</style>
