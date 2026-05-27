<template>
  <article class="post-card section-card" @click="goDetail">
    <div class="post-main">
      <div class="post-meta">
        <span class="tag">{{ post.categoryName || '未分类' }}</span>
        <span class="muted">{{ formatDateTime(post.createTime) }}</span>
      </div>

      <h3
        v-if="post.highlightedTitle"
        class="post-title"
        v-html="post.highlightedTitle"
      />
      <h3 v-else class="post-title">{{ post.title }}</h3>
      <p
        v-if="post.highlightedContent"
        class="post-content text-truncate-3"
        v-html="post.highlightedContent"
      />
      <p v-else class="post-content text-truncate-3">{{ post.content }}</p>

      <div class="post-footer">
        <router-link
          class="author"
          :to="`/users/${post.userId}`"
          @click.stop
        >
          <img
            class="author-avatar"
            :src="resolveAvatarUrl(post.avatar, post.nickname || 'U')"
            alt="avatar"
          />
          <span>{{ post.nickname }}</span>
        </router-link>

        <div class="stats">
          <span>浏览 {{ formatCount(post.viewCount) }}</span>
          <span>赞 {{ formatCount(post.likeCount) }}</span>
          <span>评 {{ formatCount(post.commentCount) }}</span>
          <span>藏 {{ formatCount(post.favoriteCount) }}</span>
        </div>
      </div>
    </div>

    <img
      v-if="post.coverImg"
      class="cover"
      :src="resolveCoverUrl(post.coverImg, post.title)"
      alt="cover"
    />
  </article>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

import type { PostSearchVO, PostVO } from '@/types/post'
import { formatCount, formatDateTime } from '@/utils/format'
import { resolveAvatarUrl, resolveCoverUrl } from '@/utils/url'

const props = defineProps<{
  post: PostVO | PostSearchVO
}>()

const router = useRouter()

function goDetail() {
  router.push(`/posts/${props.post.id}`)
}
</script>

<style scoped lang="scss">
.post-card {
  display: grid;
  grid-template-columns: 1fr 180px;
  gap: 18px;
  padding: 18px;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.07);
}

.post-main {
  min-width: 0;
}

.post-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
}

.post-title {
  margin: 0 0 10px;
  font-size: 22px;
  line-height: 1.3;
}

.post-content {
  margin: 0;
  color: #475569;
  line-height: 1.75;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-top: 18px;
}

.author {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  min-width: 0;
}

.author-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}

.cover {
  width: 180px;
  height: 132px;
  object-fit: cover;
  border-radius: 8px;
  background: #eef2ff;
}

@media (max-width: 768px) {
  .post-card {
    grid-template-columns: 1fr;
  }

  .cover {
    width: 100%;
    height: 180px;
  }

  .post-title {
    font-size: 20px;
  }

  .post-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
