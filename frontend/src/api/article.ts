import axios from 'axios'
import type { Article, ArticleCreateRequest, ArticleUpdateRequest, PageResult, ApiResponse } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

export const articleApi = {
  list: (page = 1, size = 10) => {
    return request.get<ApiResponse<PageResult<Article>>>(`/articles?page=${page}&size=${size}`)
  },
  
  getById: (id: number) => {
    return request.get<ApiResponse<Article>>(`/articles/${id}`)
  },
  
  create: (data: ArticleCreateRequest) => {
    return request.post<ApiResponse<Article>>('/articles', data)
  },
  
  update: (id: number, data: ArticleUpdateRequest) => {
    return request.put<ApiResponse<Article>>(`/articles/${id}`, data)
  },
  
  delete: (id: number) => {
    return request.delete<ApiResponse<void>>(`/articles/${id}`)
  },
  
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<ApiResponse<{ url: string }>>('/articles/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
