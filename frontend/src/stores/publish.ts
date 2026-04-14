import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PublishRecord, PageResult } from '@/types'
import { publishApi } from '@/api/publish'

export const usePublishStore = defineStore('publish', () => {
  const records = ref<PublishRecord[]>([])
  const total = ref(0)
  const loading = ref(false)

  const fetchRecords = async (page = 1, size = 10, status?: number) => {
    loading.value = true
    try {
      const res = await publishApi.list(page, size, status)
      if (res.data.code === 200) {
        records.value = res.data.data.records
        total.value = res.data.data.total
      }
    } finally {
      loading.value = false
    }
  }

  const publish = async (articleId: number, platforms: string[]) => {
    const res = await publishApi.publish({ articleId, platforms })
    if (res.data.code === 200) {
      return res.data.data
    }
    throw new Error(res.data.message)
  }

  return {
    records,
    total,
    loading,
    fetchRecords,
    publish
  }
})
