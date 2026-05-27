<template>
  <div class="page-container">
    <n-breadcrumb style="margin-bottom: 16px;">
      <n-breadcrumb-item @click="router.push('/')">首页</n-breadcrumb-item>
      <n-breadcrumb-item>帖子详情</n-breadcrumb-item>
    </n-breadcrumb>

    <div v-if="loading" class="section-card detail-loading">
      <n-spin size="large" />
    </div>

    <n-alert v-else-if="errorText" type="error" :show-icon="false">
      {{ errorText }}
    </n-alert>

    <template v-else-if="post">
      <section class="section-card detail-card">
        <div class="detail-meta">
          <router-link
            class="author"
            :to="`/users/${post.author.id}`"
          >
            <img
              class="author-avatar"
              :src="resolveAvatarUrl(post.author.avatar, post.author.nickname || 'U')"
              alt="avatar"
            />
            <div>
              <div class="author-name">{{ post.author.nickname }}</div>
              <div class="muted">{{ formatDateTime(post.createTime) }}</div>
            </div>
          </router-link>

          <n-space>
            <router-link
              v-if="canManagePost"
              :to="`/posts/${post.id}/edit`"
            >
              <n-button quaternary>编辑</n-button>
            </router-link>

            <n-popconfirm
              v-if="canManagePost"
              @positive-click="handleDeletePost"
            >
              <template #trigger>
                <n-button type="error" quaternary>删除</n-button>
              </template>
              删除后不可恢复，确认删除这条帖子？
            </n-popconfirm>
          </n-space>
        </div>

        <h1 class="detail-title">{{ post.title }}</h1>
        <p class="detail-content">{{ post.content }}</p>

        <div v-if="post.images?.length" class="images">
          <n-image
            v-for="(image, index) in post.images"
            :key="`${image}-${index}`"
            :src="resolveCoverUrl(image, post.title)"
            width="220"
            object-fit="cover"
            class="detail-image"
          />
        </div>

        <div class="detail-stats">
          <span>浏览 {{ formatCount(post.viewCount) }}</span>
          <span>点赞 {{ formatCount(post.likeCount) }}</span>
          <span>评论 {{ formatCount(post.commentCount) }}</span>
          <span>收藏 {{ formatCount(post.favoriteCount) }}</span>
          <span>{{ formatDateTime(post.updateTime || post.createTime) }}</span>
        </div>

        <div class="toolbar-wrap">
          <n-button
            :type="post.isLiked ? 'primary' : 'default'"
            :loading="likeLoading"
            @click="handleLike"
          >
            {{ post.isLiked ? '取消点赞' : '点赞' }}
          </n-button>
          <n-button
            :type="post.isFavorited ? 'primary' : 'default'"
            :loading="favoriteLoading"
            @click="handleFavorite"
          >
            {{ post.isFavorited ? '取消收藏' : '收藏' }}
          </n-button>
        </div>
      </section>

      <section class="section-card composer-card">
        <div class="composer-header">
          <div>
            <h3 style="margin: 0 0 6px;">发表评论</h3>
            <div class="muted">
              {{ replyHint || '支持一级评论与回复评论。' }}
            </div>
          </div>

          <n-button
            v-if="replyParentId"
            quaternary
            @click="clearReply"
          >
            取消回复
          </n-button>
        </div>

        <n-input
          v-model:value="commentContent"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 8 }"
          placeholder="请输入评论内容"
        />

        <div class="composer-actions">
          <n-button
            type="primary"
            :loading="submittingComment"
            @click="handleSubmitComment"
          >
            提交评论
          </n-button>
        </div>
      </section>

      <section class="section-card comments-card">
        <div class="comments-header">
          <h2 style="margin: 0;">评论区</h2>
          <span class="muted">{{ comments.length }} 条一级评论</span>
        </div>

        <template v-if="comments.length">
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-main">
              <img
                class="comment-avatar"
                :src="resolveAvatarUrl(comment.avatar, comment.nickname || 'U')"
                alt="avatar"
              />

              <div class="comment-body">
                <div class="comment-top">
                  <router-link
                    class="comment-name"
                    :to="`/users/${comment.userId}`"
                  >
                    {{ comment.nickname }}
                  </router-link>
                  <span class="muted">{{ formatDateTime(comment.createTime) }}</span>
                </div>

                <p class="comment-text">{{ comment.content }}</p>

                <div class="comment-actions">
                  <n-button
                    size="small"
                    quaternary
                    @click="prepareReply(comment.id, comment.userId, comment.nickname)"
                  >
                    回复
                  </n-button>

                  <n-popconfirm
                    v-if="canDeleteComment(comment.userId)"
                    @positive-click="handleDeleteComment(comment.id)"
                  >
                    <template #trigger>
                      <n-button size="small" quaternary type="error">删除</n-button>
                    </template>
                    确认删除这条评论？
                  </n-popconfirm>
                </div>

                <div
                  v-if="comment.children?.length"
                  class="child-list"
                >
                  <div
                    v-for="child in comment.children"
                    :key="child.id"
                    class="child-item"
                  >
                    <div class="comment-top">
                      <router-link
                        class="comment-name"
                        :to="`/users/${child.userId}`"
                      >
                        {{ child.nickname }}
                      </router-link>
                      <span class="muted">{{ formatDateTime(child.createTime) }}</span>
                    </div>
                    <p class="comment-text">
                      <template v-if="child.replyNickname">
                        <span class="reply-prefix">回复 {{ child.replyNickname }}：</span>
                      </template>
                      {{ child.content }}
                    </p>
                    <div class="comment-actions">
                      <n-button
                        size="small"
                        quaternary
                        @click="prepareReply(comment.id, child.userId, child.nickname)"
                      >
                        回复
                      </n-button>

                      <n-popconfirm
                        v-if="canDeleteComment(child.userId)"
                        @positive-click="handleDeleteComment(child.id)"
                      >
                        <template #trigger>
                          <n-button size="small" quaternary type="error">删除</n-button>
                        </template>
                        确认删除这条回复？
                      </n-popconfirm>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <EmptyState
          v-else
          icon="✎"
          title="还没有评论"
          description="成为第一个发表评论的人吧。"
        />
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import {
  NBreadcrumb,
  NBreadcrumbItem,
  NAlert,
  NButton,
  NImage,
  NInput,
  NPopconfirm,
  NSpace,
  NSpin
} from 'naive-ui'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { createCommentApi, deleteCommentApi, getPostCommentsApi } from '@/api/comment'
import { cancelFavoriteApi, toggleFavoriteApi } from '@/api/favorite'
import { cancelLikeApi, toggleLikeApi } from '@/api/like'
import { deletePostApi, getPostDetailApi } from '@/api/post'
import EmptyState from '@/components/EmptyState.vue'
import { useAuthStore } from '@/stores/auth'
import type { CommentVO } from '@/types/comment'
import type { PostDetailVO } from '@/types/post'
import { formatCount, formatDateTime } from '@/utils/format'
import { message } from '@/utils/message'
import { resolveAvatarUrl, resolveCoverUrl } from '@/utils/url'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const likeLoading = ref(false)
const favoriteLoading = ref(false)
const submittingComment = ref(false)
const errorText = ref('')
const post = ref<PostDetailVO | null>(null)
const comments = ref<CommentVO[]>([])
const commentContent = ref('')
const replyParentId = ref<number>(0)
const replyUserId = ref<number | undefined>(undefined)
const replyNickname = ref('')

const postId = computed(() => Number(route.params.id))
const currentUserId = computed(() => authStore.user?.id || 0)
const canManagePost = computed(() => {
  if (!post.value || !currentUserId.value) return false
  return post.value.author?.id === currentUserId.value || post.value.userId === currentUserId.value
})
const replyHint = computed(() => {
  if (!replyParentId.value || !replyNickname.value) return ''
  return `当前正在回复：${replyNickname.value}`
})

function canDeleteComment(userId: number) {
  return Boolean(currentUserId.value && userId === currentUserId.value)
}

function ensureLoggedIn() {
  if (authStore.isLoggedIn) return true

  message.warning('请先登录')
  router.push({
    path: '/login',
    query: {
      redirect: route.fullPath
    }
  })
  return false
}

async function loadPostDetail() {
  const res = await getPostDetailApi(postId.value)
  post.value = res.data
}

async function loadComments() {
  const res = await getPostCommentsApi(postId.value)
  comments.value = res.data || []
}

async function loadAll() {
  loading.value = true
  errorText.value = ''

  try {
    await Promise.all([loadPostDetail(), loadComments()])
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '详情加载失败'
  } finally {
    loading.value = false
  }
}

function prepareReply(parentId: number, userId: number, nickname: string) {
  if (!ensureLoggedIn()) return

  replyParentId.value = parentId
  replyUserId.value = userId
  replyNickname.value = nickname
}

function clearReply() {
  replyParentId.value = 0
  replyUserId.value = undefined
  replyNickname.value = ''
}

async function handleLike() {
  if (!post.value) return
  if (!ensureLoggedIn()) return

  likeLoading.value = true
  try {
    if (post.value.isLiked) {
      await cancelLikeApi(post.value.id)
      post.value.isLiked = false
      post.value.likeCount = Math.max(0, post.value.likeCount - 1)
      message.success('已取消点赞')
    } else {
      await toggleLikeApi(post.value.id)
      post.value.isLiked = true
      post.value.likeCount += 1
      message.success('点赞成功')
    }
  } finally {
    likeLoading.value = false
  }
}

async function handleFavorite() {
  if (!post.value) return
  if (!ensureLoggedIn()) return

  favoriteLoading.value = true
  try {
    if (post.value.isFavorited) {
      await cancelFavoriteApi(post.value.id)
      post.value.isFavorited = false
      post.value.favoriteCount = Math.max(0, post.value.favoriteCount - 1)
      message.success('已取消收藏')
    } else {
      await toggleFavoriteApi(post.value.id)
      post.value.isFavorited = true
      post.value.favoriteCount += 1
      message.success('收藏成功')
    }
  } finally {
    favoriteLoading.value = false
  }
}

async function handleSubmitComment() {
  if (!ensureLoggedIn()) return

  if (!commentContent.value.trim()) {
    message.warning('请输入评论内容')
    return
  }

  submittingComment.value = true
  try {
    await createCommentApi({
      postId: postId.value,
      parentId: replyParentId.value || 0,
      replyUserId: replyUserId.value,
      content: commentContent.value.trim()
    })

    message.success('评论成功')
    commentContent.value = ''
    clearReply()
    await Promise.all([loadComments(), loadPostDetail()])
  } finally {
    submittingComment.value = false
  }
}

async function handleDeleteComment(commentId: number) {
  await deleteCommentApi(commentId)
  message.success('评论已删除')
  await Promise.all([loadComments(), loadPostDetail()])
}

async function handleDeletePost() {
  if (!post.value) return
  await deletePostApi(post.value.id)
  message.success('帖子已删除')
  router.push('/')
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped lang="scss">
.detail-loading {
  min-height: 320px;
  display: grid;
  place-items: center;
}

.detail-card,
.composer-card,
.comments-card {
  padding: 22px;
  margin-bottom: 18px;
}

.detail-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.author {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: 700;
  font-size: 16px;
}

.detail-title {
  margin: 0 0 18px;
  font-size: 32px;
  line-height: 1.28;
}

.detail-content {
  margin: 0;
  line-height: 1.9;
  white-space: pre-wrap;
  color: #334155;
}

.images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 22px 0 18px;
}

:deep(.detail-image img) {
  border-radius: 18px;
}

.detail-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #64748b;
  margin-bottom: 18px;
}

.composer-header,
.comments-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.comment-item + .comment-item {
  padding-top: 18px;
  margin-top: 18px;
  border-top: 1px solid #eef2f7;
}

.comment-main {
  display: flex;
  gap: 14px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-top {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.comment-name {
  font-weight: 700;
}

.comment-text {
  margin: 10px 0;
  line-height: 1.75;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.child-list {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}

.child-item + .child-item {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #dbe4ef;
}

.reply-prefix {
  color: #2563eb;
  font-weight: 600;
}

@media (max-width: 768px) {
  .detail-title {
    font-size: 26px;
  }

  .comment-main {
    gap: 10px;
  }
}
</style>
