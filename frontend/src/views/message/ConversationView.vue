<template>
  <div class="page-container">
    <section class="section-card conversation-card">
      <div v-if="loading" class="loading-card"><n-spin /></div>

      <template v-else>
        <div class="conv-header">
          <n-button quaternary @click="router.push('/messages')">← 返回</n-button>
          <h3 class="conv-title">{{ otherUser?.nickname || '私信' }}</h3>
        </div>

        <div ref="messageListRef" class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{ mine: msg.senderId === currentUserId }"
          >
            <img
              v-if="msg.senderId !== currentUserId"
              class="msg-avatar"
              :src="resolveAvatarUrl(msg.senderAvatar, msg.senderNickname || 'U')"
              alt="avatar"
            />
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
        </div>

        <div class="send-bar">
          <n-input v-model="newMessage" placeholder="输入消息..." @keyup.enter="handleSend" />
          <n-button type="primary" :disabled="!newMessage.trim()" @click="handleSend">发送</n-button>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { NButton, NInput, NSpin } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'

import { getMessagesApi, sendMessageApi, type MessageVO } from '@/api/message'
import { useAuthStore } from '@/stores/auth'
import { resolveAvatarUrl } from '@/utils/url'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const messages = ref<MessageVO[]>([])
const newMessage = ref('')
const messageListRef = ref<HTMLElement | null>(null)
const otherUser = ref<{ nickname: string } | null>(null)

const conversationId = computed(() => Number(route.params.id))
const currentUserId = computed(() => authStore.user?.id || 0)

async function loadMessages() {
  loading.value = true
  try {
    const res = await getMessagesApi(conversationId.value, { pageNum: 1, pageSize: 100 })
    messages.value = (res.data || []).reverse()
    if (messages.value.length > 0) {
      const other = messages.value.find(m => m.senderId !== currentUserId.value)
      if (other) otherUser.value = { nickname: other.senderNickname }
    }
    await nextTick()
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSend() {
  if (!newMessage.value.trim()) return
  // We need the other user's ID. Find it from messages.
  const otherMsg = messages.value.find(m => m.senderId !== currentUserId.value)
  if (!otherMsg) return

  await sendMessageApi({ receiverId: otherMsg.senderId, content: newMessage.value })
  newMessage.value = ''
  await loadMessages()
}

onMounted(() => loadMessages())
</script>

<style scoped lang="scss">
.conversation-card { padding: 22px; display: flex; flex-direction: column; height: calc(100vh - 180px); }
.loading-card { flex: 1; display: grid; place-items: center; }
.conv-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.conv-title { font-weight: 700; color: #0f172a; }
.message-list { flex: 1; overflow-y: auto; padding: 8px 0; display: flex; flex-direction: column; gap: 12px; }
.message-item { display: flex; gap: 8px; align-items: flex-start; }
.message-item.mine { flex-direction: row-reverse; }
.msg-avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; background: #eff6ff; flex-shrink: 0; }
.msg-bubble {
  max-width: 60%; padding: 10px 14px; border-radius: 16px;
  background: #f1f5f9; color: #0f172a; font-size: 14px; line-height: 1.5;
  word-break: break-word;
}
.message-item.mine .msg-bubble { background: #3b82f6; color: #fff; }
.send-bar { display: flex; gap: 8px; margin-top: 12px; }
</style>
