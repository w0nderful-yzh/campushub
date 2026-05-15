<template>
  <div class="page-container">
    <div v-if="loading" class="section-card loading-card"><n-spin size="large" /></div>

    <template v-else-if="vote">
      <section class="section-card" style="padding: 24px;">
        <div class="vote-header">
          <n-tag :type="vote.status === 1 ? 'success' : 'default'" size="small">
            {{ vote.status === 1 ? '进行中' : '已关闭' }}
          </n-tag>
          <span class="vote-info">{{ vote.maxSelect > 1 ? `多选（最多${vote.maxSelect}项）` : '单选' }} · {{ vote.totalCount }} 人参与</span>
        </div>

        <h1 class="vote-title">{{ vote.title }}</h1>
        <div v-if="vote.description" class="vote-desc">{{ vote.description }}</div>
        <div v-if="vote.endTime" class="vote-endtime">截止时间：{{ formatDateTime(vote.endTime) }}</div>

        <div class="options-list">
          <div v-for="opt in vote.options" :key="opt.id" class="option-item"
            :class="{ selected: selectedIds.includes(opt.id), voted: vote.isVoted }"
            @click="toggleOption(opt.id)">
            <div class="option-top">
              <span class="option-content">
                <span v-if="vote.isVoted" class="check-mark">{{ opt.isSelected ? '✓' : '' }}</span>
                {{ opt.content }}
              </span>
              <span v-if="vote.isVoted" class="option-count">{{ opt.count }}票 ({{ opt.percentage }}%)</span>
            </div>
            <div v-if="vote.isVoted" class="progress-bar">
              <div class="progress-fill" :style="{ width: opt.percentage + '%' }" />
            </div>
          </div>
        </div>

        <div v-if="!vote.isVoted" class="vote-actions">
          <n-button type="primary" size="large" :loading="voting" :disabled="selectedIds.length === 0" @click="handleVote">
            投票
          </n-button>
        </div>

        <div class="vote-author">
          <img :src="resolveAvatarUrl(vote.authorAvatar, vote.authorNickname || 'U')" class="author-avatar" />
          <span>发起者：{{ vote.authorNickname }} · {{ formatDateTime(vote.createTime) }}</span>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NButton, NSpin, NTag } from 'naive-ui'
import { useRoute } from 'vue-router'

import { getVoteDetailApi, castVoteApi, type VoteVO } from '@/api/vote'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'
import { message } from '@/utils/message'

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const voting = ref(false)
const vote = ref<VoteVO | null>(null)
const selectedIds = ref<number[]>([])

const voteId = computed(() => Number(route.params.id))

function toggleOption(id: number) {
  if (vote.value?.isVoted) return
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    if (vote.value && selectedIds.value.length >= vote.value.maxSelect) {
      message.warning(`最多选择 ${vote.value.maxSelect} 项`)
      return
    }
    selectedIds.value.push(id)
  }
}

async function loadVote() {
  loading.value = true
  try {
    const res = await getVoteDetailApi(voteId.value)
    vote.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleVote() {
  if (!authStore.isLoggedIn) { message.warning('请先登录'); return }
  if (selectedIds.value.length === 0) { message.warning('请选择选项'); return }
  voting.value = true
  try {
    await castVoteApi(voteId.value, selectedIds.value)
    message.success('投票成功')
    await loadVote()
  } catch { /* */ } finally { voting.value = false }
}

onMounted(() => loadVote())
</script>

<style scoped lang="scss">
.loading-card { min-height: 200px; display: grid; place-items: center; }
.vote-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.vote-info { font-size: 13px; color: #64748b; }
.vote-title { font-size: 24px; font-weight: 800; color: #0f172a; margin-bottom: 10px; }
.vote-desc { font-size: 14px; color: #475569; margin-bottom: 10px; }
.vote-endtime { font-size: 13px; color: #94a3b8; margin-bottom: 20px; }
.options-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.option-item {
  padding: 14px 16px; border: 2px solid #e2e8f0; border-radius: 12px;
  cursor: pointer; transition: all 0.2s;
}
.option-item:hover:not(.voted) { border-color: #93c5fd; }
.option-item.selected { border-color: #3b82f6; background: #eff6ff; }
.option-item.voted { cursor: default; }
.option-top { display: flex; justify-content: space-between; align-items: center; }
.option-content { font-weight: 600; color: #0f172a; }
.check-mark { color: #3b82f6; margin-right: 6px; }
.option-count { font-size: 13px; color: #64748b; font-weight: 600; }
.progress-bar { height: 6px; background: #f1f5f9; border-radius: 3px; margin-top: 10px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #3b82f6, #60a5fa); border-radius: 3px; transition: width 0.5s ease; }
.vote-actions { margin-bottom: 20px; }
.vote-author { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #64748b; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.author-avatar { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
</style>
