export interface CreateCommentDTO {
  postId: number
  parentId?: number
  replyUserId?: number
  content: string
}

export interface CommentVO {
  id: number
  postId: number
  userId: number
  nickname: string
  avatar?: string
  parentId: number
  replyUserId?: number
  replyNickname?: string
  content: string
  likeCount: number
  createTime?: string
  children?: CommentVO[]
}
