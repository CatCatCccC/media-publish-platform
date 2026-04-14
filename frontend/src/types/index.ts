export interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage: string
  images: string[]
  status: number
  createdAt: string
  updatedAt: string
}

export interface ArticleCreateRequest {
  title: string
  content: string
  summary: string
  coverImage: string
  images: string
}

export interface ArticleUpdateRequest extends ArticleCreateRequest {
  id: number
  status?: number
}

export interface PlatformConfig {
  id: number
  platform: string
  credentialsMasked: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface PlatformConfigRequest {
  platform: string
  credentials: string
  enabled: boolean
}

export interface PublishRecord {
  id: number
  articleId: number
  articleTitle: string
  platform: string
  status: number
  statusText: string
  publishUrl: string
  errorMessage: string
  publishedAt: string
  createdAt: string
}

export interface PublishRequest {
  articleId: number
  platforms: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
