<template>
  <div class="page-container">
    <section class="section-card notice-card">
      <div class="notice-header">
        <h2 class="page-title">消息通知</h2>
        <n-button quaternary size="small" :disabled="!notices.length" @click="handleMarkAllRead">
          全部已读
        </n-button>
      </div>

      <n-tabs v-model:value="filterType" type="line" animated @update:value="handleFilterChange">
        <n-tab-pane name="all" tab="全部" />
        <n-tab-pane name="like" tab="点赞" />
        <n-tab-pane name="comment" tab="评论" />
        <n-tab-pane name="follow" tab="关注" />
      </n-tabs>

      <div v-if="loading" class="loading-card">
        <n-spin />
      </div>

      <template v-else-if="notices.length">
        <div class="notice-list">
          <div
            v-for="notice in notices"
            :key="notice.id"
            class="notice-item"
            :class="{ unread: notice.isRead === 0 }"
            @click="handleClickNotice(notice)"
          >
            <img
              class="notice-avatar"
              :src="resolveAvatarUrl(notice.senderAvatar, notice.senderNickname || 'U')"
              alt="avatar"
            />
            <div class="notice-body">
              <div class="notice-text">
                <span class="notice-sender">{{ notice.senderNickname }}</span>
                <span>{{ noticeText(notice) }}</span>
              </div>
              <div class="notice-time">{{ formatDateTime(notice.createTime) }}</div>
            </div>
            <div v-if="notice.isRead === 0" class="unread-dot" />
          </div>
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
        icon="🔔"
        title="暂无通知"
        description="当有人点赞、评论或关注你时，你会收到通知。"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { NButton, NSpin, NTabs, NTabPane } from 'naive-ui'
import { useRouter } from 'vue-router'

import { getNoticesApi, markAllNoticesReadApi, markNoticeReadApi, type NoticeVO } from '@/api/notice'
import EmptyState from '@/components/EmptyState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'

const router = useRouter()

const loading = ref(false)
const notices = ref<NoticeVO[]>([])
const total = ref(0)
const filterType = ref<'all' | 'like' | 'comment' | 'follow'>('all')

const query = reactive({
  pageNum: 1,
  pageSize: 15
})

const typeMap: Record<string, number | undefined> = {
  all: undefined,
  like: 1,
  comment: 2,
  follow: 3
}

function noticeText(notice: NoticeVO): string {
  if (notice.content) return ' ' + notice.content
  switch (notice.type) {
    case 1: return ' 赞了你的帖子'
    case 2: return ' 评论了你的帖子'
    case 3: return ' 关注了你'
    default: return ' 给你发了一条消息'
  }
}

async function loadNotices() {
  loading.value = true
  try {
    const res = await getNoticesApi({
      type: typeMap[filterType.value],
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    notices.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleClickNotice(notice: NoticeVO) {
  if (notice.isRead === 0) {
    await markNoticeReadApi(notice.id)
    notice.isRead = 1
  }

  if (notice.type === 3 && notice.senderUserId) {
    router.push(`/users/${notice.senderUserId}`)
  } else if (notice.postId) {
    router.push(`/posts/${notice.postId}`)
  }
}

async function handleMarkAllRead() {
  await markAllNoticesReadApi()
  notices.value.forEach(n => { n.isRead = 1 })
}

function handleFilterChange() {
  query.pageNum = 1
  loadNotices()
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
  () => loadNotices()
)

onMounted(() => loadNotices())
</script>

<style scoped lang="scss">
.notice-card {
  padding: 22px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.notice-list {
  margin-top: 12px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.notice-item:hover {
  background: #f8fafc;
}

.notice-item.unread {
  background: #eff6ff;
}

.notice-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #f1f5f9;
  flex-shrink: 0;
}

.notice-body {
  flex: 1;
  min-width: 0;
}

.notice-text {
  font-size: 14px;
  color: #334155;
  line-height: 1.5;
}

.notice-sender {
  font-weight: 600;
  color: #0f172a;
}

.notice-time {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
  flex-shrink: 0;
}

.loading-card {
  min-height: 200px;
  display: grid;
  place-items: center;
}
</style>
