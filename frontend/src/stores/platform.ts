import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PlatformConfig } from '@/types'
import { platformApi } from '@/api/platform'

export const usePlatformStore = defineStore('platform', () => {
  const platforms = ref<PlatformConfig[]>([])
  const loading = ref(false)

  const PLATFORM_LIST = [
    { code: 'CSDN', name: 'CSDN', icon: 'Monitor' },
    { code: 'ZHIHU', name: '知乎', icon: 'Reading' },
    { code: 'WECHAT', name: '微信公众号', icon: 'ChatDotRound' }
  ]

  const fetchPlatforms = async () => {
    loading.value = true
    try {
      const res = await platformApi.list()
      if (res.data.code === 200) {
        platforms.value = res.data.data
      }
    } finally {
      loading.value = false
    }
  }

  const savePlatform = async (platform: string, credentials: string, enabled: boolean) => {
    const res = await platformApi.save({ platform, credentials, enabled })
    if (res.data.code === 200) {
      await fetchPlatforms()
      return res.data.data
    }
    throw new Error(res.data.message)
  }

  const testConnection = async (platform: string, credentials?: string) => {
    const res = await platformApi.test({ platform, credentials })
    if (res.data.code === 200) {
      return res.data.data
    }
    throw new Error(res.data.message)
  }

  const getPlatformConfig = (platform: string) => {
    return platforms.value.find(p => p.platform === platform)
  }

  const isPlatformEnabled = (platform: string) => {
    const config = getPlatformConfig(platform)
    return config?.enabled ?? false
  }

  return {
    platforms,
    loading,
    PLATFORM_LIST,
    fetchPlatforms,
    savePlatform,
    testConnection,
    getPlatformConfig,
    isPlatformEnabled
  }
})
