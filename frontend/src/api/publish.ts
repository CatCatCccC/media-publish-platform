import axios from 'axios'
import type { PublishRecord, PublishRequest, PageResult, ApiResponse } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

export const publishApi = {
  list: (page = 1, size = 10, status?: number) => {
    let url = `/publish/records?page=${page}&size=${size}`
    if (status !== undefined) {
      url += `&status=${status}`
    }
    return request.get<ApiResponse<PageResult<PublishRecord>>>(url)
  },
  
  getByArticleId: (articleId: number) => {
    return request.get<ApiResponse<PublishRecord[]>>(`/publish/records/article/${articleId}`)
  },
  
  publish: (data: PublishRequest) => {
    return request.post<ApiResponse<PublishRecord[]>>('/publish', data)
  }
}
