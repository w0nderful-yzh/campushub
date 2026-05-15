export interface PostQueryDTO {
  pageNum: number
  pageSize: number
  categoryId?: number
  sortType?: 'latest' | 'hottest'
  keyword?: string
}

export interface PostVO {
  id: number
  userId: number
  nickname: string
  avatar?: string
  categoryId: number
  categoryName?: string
  title: string
  content: string
  coverImg?: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  status?: number
  isTop?: number
  createTime?: string
  updateTime?: string
}

export interface PostAuthorVO {
  id: number
  nickname: string
  avatar?: string
}

export interface PostDetailVO {
  id: number
  userId?: number
  categoryId?: number
  title: string
  content: string
  coverImg?: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  createTime?: string
  updateTime?: string
  images: string[]
  author: PostAuthorVO
  isLiked: boolean
  isFavorited: boolean
}

export interface SavePostDTO {
  categoryId: number
  title: string
  content: string
  images: string[]
}
