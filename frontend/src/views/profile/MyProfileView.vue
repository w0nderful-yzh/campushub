<template>
  <div class="page-container">
    <div v-if="loadingProfile" class="section-card loading-card">
      <n-spin size="large" />
    </div>

    <template v-else-if="profile">
      <section class="section-card profile-card">
        <div class="profile-main">
          <img
            class="profile-avatar"
            :src="resolveAvatarUrl(profile.avatar, profile.nickname || profile.username || 'U')"
            alt="avatar"
          />

          <div class="profile-info">
            <div class="profile-top">
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

              <n-space>
                <router-link to="/profile/edit">
                  <n-button quaternary>编辑资料</n-button>
                </router-link>
                <router-link to="/posts/create">
                  <n-button type="primary">发布帖子</n-button>
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
                <span>{{ profile.profile || '这个人很神秘，还没有留下简介。' }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="section-card tab-card">
        <n-tabs
          v-model:value="activeTab"
          type="line"
          animated
          @update:value="handleTabChange"
        >
          <n-tab-pane name="posts" tab="我的帖子" />
          <n-tab-pane name="likes" tab="我的点赞" />
          <n-tab-pane name="favorites" tab="我的收藏" />
          <n-tab-pane name="following" tab="我的关注" />
          <n-tab-pane name="followers" tab="我的粉丝" />
        </n-tabs>

        <div v-if="listLoading" class="loading-card">
          <n-spin />
        </div>

        <template v-else-if="isFollowTab">
          <div v-if="followList.length" class="follow-list">
            <div v-for="user in followList" :key="user.id" class="follow-item">
              <router-link :to="`/users/${user.id}`" class="follow-user">
                <img
                  class="follow-avatar"
                  :src="resolveAvatarUrl(user.avatar, user.nickname || 'U')"
                  alt="avatar"
                />
                <div class="follow-info">
                  <span class="follow-name">{{ user.nickname }}</span>
                  <span class="follow-college">{{ user.college || '' }}</span>
                </div>
              </router-link>
            </div>

            <PaginationBar
              :page="query.pageNum"
              :page-size="query.pageSize"
              :total="total"
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
            />
          </div>

          <EmptyState
            v-else
            icon="◍"
            :title="activeTab === 'following' ? '你还没有关注任何人' : '暂时没有粉丝'"
            description="去发现更多有趣的人吧。"
          />
        </template>

        <template v-else-if="listData.length">
          <div class="stack">
            <PostCard
              v-for="item in listData"
              :key="item.id"
              :post="item"
            />
          </div>

          <PaginationBar
            :page="query.pageNum"
            :page-size="query.pageSize"
            :total="total"
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </template>

        <EmptyState
          v-else
          icon="◍"
          :title="emptyTitle"
          description="这里暂时还没有数据。"
        />
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NSpace, NSpin, NTabs, NTabPane } from 'naive-ui'
import { useRouter } from 'vue-router'

import { getMyFavoritesApi } from '@/api/favorite'
import { getFollowCountApi, getFollowingApi, getFollowersApi, type FollowUserVO } from '@/api/follow'
import { getMyLikesApi } from '@/api/like'
import { getMyPostsApi } from '@/api/post'
import { getUserHomeApi } from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import PostCard from '@/components/PostCard.vue'
import { useAuthStore } from '@/stores/auth'
import type { PostVO } from '@/types/post'
import type { UserHomeVO } from '@/types/user'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'

const router = useRouter()
const authStore = useAuthStore()

const loadingProfile = ref(false)
const listLoading = ref(false)
const profile = ref<UserHomeVO | null>(null)
const listData = ref<PostVO[]>([])
const followList = ref<FollowUserVO[]>([])
const total = ref(0)
const activeTab = ref<'posts' | 'likes' | 'favorites' | 'following' | 'followers'>('posts')
const followCount = reactive({ followingCount: 0, followerCount: 0 })

const query = reactive({
  pageNum: 1,
  pageSize: 10
})

const isFollowTab = computed(() => activeTab.value === 'following' || activeTab.value === 'followers')

const emptyTitle = computed(() => {
  if (activeTab.value === 'posts') return '你还没有发布帖子'
  if (activeTab.value === 'likes') return '你还没有点赞任何帖子'
  return '你还没有收藏任何帖子'
})

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

async function loadProfile() {
  if (!authStore.user?.id) {
    router.push('/login')
    return
  }

  loadingProfile.value = true
  try {
    const res = await getUserHomeApi(authStore.user.id)
    profile.value = res.data
  } finally {
    loadingProfile.value = false
  }
}

async function loadFollowCount() {
  if (!authStore.user?.id) return
  try {
    const res = await getFollowCountApi(authStore.user.id)
    if (res.data) {
      followCount.followingCount = res.data.followingCount
      followCount.followerCount = res.data.followerCount
    }
  } catch {
    // ignore
  }
}

async function loadList() {
  listLoading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      sortType: 'latest' as const
    }

    if (isFollowTab.value) {
      const res = activeTab.value === 'following'
        ? await getFollowingApi(params)
        : await getFollowersApi(params)
      followList.value = res.data || []
      total.value = res.total || 0
      listData.value = []
    } else {
      let res
      if (activeTab.value === 'posts') {
        res = await getMyPostsApi(params)
      } else if (activeTab.value === 'likes') {
        res = await getMyLikesApi(params)
      } else {
        res = await getMyFavoritesApi(params)
      }
      listData.value = res.data || []
      total.value = res.total || 0
      followList.value = []
    }
  } finally {
    listLoading.value = false
  }
}

function handleTabChange() {
  query.pageNum = 1
  loadList()
}

function handlePageChange(value: number) {
  query.pageNum = value
}

function handlePageSizeChange(value: number) {
  query.pageSize = value
  query.pageNum = 1
}

watch(
  () => [query.pageNum, query.pageSize],
  () => {
    loadList()
  }
)

onMounted(async () => {
  await loadProfile()
  await loadFollowCount()
  await loadList()
})
</script>

<style scoped lang="scss">
.profile-card,
.tab-card {
  padding: 22px;
  margin-bottom: 18px;
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

.profile-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: start;
  flex-wrap: wrap;
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
  margin-top: 18px;
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
  min-height: 200px;
  display: grid;
  place-items: center;
}

.follow-list {
  margin-top: 12px;
}

.follow-item {
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.follow-item:last-of-type {
  border-bottom: none;
}

.follow-user {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: inherit;
}

.follow-user:hover .follow-name {
  color: #2563eb;
}

.follow-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  background: #eff6ff;
}

.follow-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.follow-name {
  font-weight: 600;
  color: #0f172a;
  transition: color 0.2s;
}

.follow-college {
  font-size: 13px;
  color: #94a3b8;
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
