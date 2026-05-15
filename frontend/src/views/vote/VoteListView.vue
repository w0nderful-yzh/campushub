<template>
  <div class="page-container">
    <section class="section-card" style="padding: 22px;">
      <div class="page-header" style="margin-bottom: 16px;">
        <h2 class="page-title">投票</h2>
        <router-link to="/votes/create">
          <n-button type="primary">创建投票</n-button>
        </router-link>
      </div>

      <div v-if="loading" class="loading-card"><n-spin /></div>

      <template v-else-if="votes.length">
        <div class="vote-list">
          <div v-for="vote in votes" :key="vote.id" class="vote-card" @click="router.push(`/votes/${vote.id}`)">
            <h3 class="vote-title">{{ vote.title }}</h3>
            <div v-if="vote.description" class="vote-desc">{{ vote.description }}</div>
            <div class="vote-meta">
              <span>{{ vote.totalCount }} 人参与</span>
              <span>{{ vote.maxSelect > 1 ? '多选' : '单选' }}</span>
              <span v-if="vote.endTime">截止：{{ formatDateTime(vote.endTime) }}</span>
            </div>
            <div class="vote-author">
              <img :src="resolveAvatarUrl(vote.authorAvatar, vote.authorNickname || 'U')" class="vote-avatar" />
              <span>{{ vote.authorNickname }}</span>
            </div>
          </div>
        </div>

        <PaginationBar :page="query.pageNum" :page-size="query.pageSize" :total="total"
          @update:page="v => { query.pageNum = v }" @update:page-size="v => { query.pageSize = v; query.pageNum = 1 }" />
      </template>

      <EmptyState v-else icon="📊" title="暂无投票" description="还没有人创建投票。" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { NButton, NSpin } from 'naive-ui'
import { useRouter } from 'vue-router'

import { getVotesApi, type VoteVO } from '@/api/vote'
import EmptyState from '@/components/EmptyState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'

const router = useRouter()
const loading = ref(false)
const votes = ref<VoteVO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

async function loadVotes() {
  loading.value = true
  try {
    const res = await getVotesApi(query)
    votes.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

watch(() => [query.pageNum, query.pageSize], () => loadVotes())
onMounted(() => loadVotes())
</script>

<style scoped lang="scss">
.loading-card { min-height: 200px; display: grid; place-items: center; }
.vote-list { display: flex; flex-direction: column; gap: 14px; }
.vote-card {
  padding: 18px; border: 1px solid #f1f5f9; border-radius: 16px;
  cursor: pointer; transition: all 0.2s;
}
.vote-card:hover { border-color: #bfdbfe; box-shadow: 0 4px 16px rgba(59, 130, 246, 0.1); }
.vote-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }
.vote-desc { font-size: 13px; color: #64748b; margin-bottom: 10px; }
.vote-meta { font-size: 12px; color: #94a3b8; display: flex; gap: 16px; margin-bottom: 10px; }
.vote-author { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #334155; }
.vote-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
</style>
