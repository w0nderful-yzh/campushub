<template>
  <div class="page-container">
    <section class="section-card message-card">
      <h2 class="page-title" style="margin-bottom: 16px;">私信</h2>

      <div v-if="loading" class="loading-card"><n-spin /></div>

      <template v-else-if="conversations.length">
        <div class="conversation-list">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conversation-item"
            @click="router.push(`/messages/${conv.id}`)"
          >
            <img
              class="conv-avatar"
              :src="resolveAvatarUrl(conv.otherAvatar, conv.otherNickname || 'U')"
              alt="avatar"
            />
            <div class="conv-body">
              <div class="conv-top">
                <span class="conv-name">{{ conv.otherNickname }}</span>
                <span class="conv-time">{{ formatDateTime(conv.lastMessageTime) }}</span>
              </div>
              <div class="conv-preview">{{ conv.lastMessage }}</div>
            </div>
            <div v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount }}</div>
          </div>
        </div>
      </template>

      <EmptyState v-else icon="✉" title="暂无私信" description="与其他用户聊天会显示在这里。" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NSpin } from 'naive-ui'
import { useRouter } from 'vue-router'

import { getConversationsApi, type ConversationVO } from '@/api/message'
import EmptyState from '@/components/EmptyState.vue'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'

const router = useRouter()
const loading = ref(false)
const conversations = ref<ConversationVO[]>([])

async function loadConversations() {
  loading.value = true
  try {
    const res = await getConversationsApi()
    conversations.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => loadConversations())
</script>

<style scoped lang="scss">
.message-card { padding: 22px; }
.loading-card { min-height: 200px; display: grid; place-items: center; }
.conversation-list { margin-top: 8px; }
.conversation-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 12px; border-radius: 12px; cursor: pointer; transition: background 0.2s;
}
.conversation-item:hover { background: #f8fafc; }
.conv-avatar { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; background: #eff6ff; flex-shrink: 0; }
.conv-body { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-weight: 600; color: #0f172a; }
.conv-time { font-size: 12px; color: #94a3b8; }
.conv-preview { font-size: 13px; color: #64748b; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-badge {
  background: #ef4444; color: #fff; font-size: 12px; font-weight: 600;
  min-width: 20px; height: 20px; border-radius: 10px; display: grid; place-items: center;
  padding: 0 6px; flex-shrink: 0;
}
</style>
