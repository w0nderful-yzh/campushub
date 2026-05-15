import type { Result } from '@/types/result'
import request from '@/utils/request'

export interface ConversationVO {
  id: number
  otherUserId: number
  otherNickname: string
  otherAvatar?: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

export interface MessageVO {
  id: number
  senderId: number
  senderNickname: string
  senderAvatar?: string
  content: string
  isRead: number
  createTime: string
}

export function sendMessageApi(data: { receiverId: number; content: string }): Promise<Result<null>> {
  return request.post('/messages', data)
}

export function getConversationsApi(): Promise<Result<ConversationVO[]>> {
  return request.get('/messages/conversations')
}

export function getMessagesApi(conversationId: number, params: { pageNum: number; pageSize: number }): Promise<Result<MessageVO[]>> {
  return request.get(`/messages/conversations/${conversationId}`, { params })
}

export function getUnreadMessageCountApi(): Promise<Result<number>> {
  return request.get('/messages/unread-count')
}
