import axios from 'axios'
import type { PlatformConfig, PlatformConfigRequest, ApiResponse } from '@/types'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000
})

export const platformApi = {
  list: () => {
    return request.get<ApiResponse<PlatformConfig[]>>('/platforms')
  },
  
  getByPlatform: (platform: string) => {
    return request.get<ApiResponse<PlatformConfig>>(`/platforms/${platform}`)
  },
  
  save: (data: PlatformConfigRequest) => {
    return request.post<ApiResponse<PlatformConfig>>('/platforms', data)
  },
  
  delete: (platform: string) => {
    return request.delete<ApiResponse<void>>(`/platforms/${platform}`)
  },
  
  test: (data: { platform: string; credentials?: string }) => {
    return request.post<ApiResponse<{ success: boolean; message: string }>>('/platforms/test', data)
  }
}
