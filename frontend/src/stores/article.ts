import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Article, PageResult } from '@/types'
import { articleApi } from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articles = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const total = ref(0)
  const loading = ref(false)

  const fetchArticles = async (page = 1, size = 10) => {
    loading.value = true
    try {
      const res = await articleApi.list(page, size)
      if (res.data.code === 200) {
        articles.value = res.data.data.records
        total.value = res.data.data.total
      }
    } finally {
      loading.value = false
    }
  }

  const fetchArticle = async (id: number) => {
    loading.value = true
    try {
      const res = await articleApi.getById(id)
      if (res.data.code === 200) {
        currentArticle.value = res.data.data
      }
    } finally {
      loading.value = false
    }
  }

  const createArticle = async (data: {
    title: string
    content: string
    summary: string
    coverImage: string
    images: string
  }) => {
    const res = await articleApi.create(data)
    if (res.data.code === 200) {
      await fetchArticles()
      return res.data.data
    }
    throw new Error(res.data.message)
  }

  const updateArticle = async (id: number, data: any) => {
    const res = await articleApi.update(id, data)
    if (res.data.code === 200) {
      await fetchArticles()
      return res.data.data
    }
    throw new Error(res.data.message)
  }

  const deleteArticle = async (id: number) => {
    const res = await articleApi.delete(id)
    if (res.data.code === 200) {
      await fetchArticles()
    } else {
      throw new Error(res.data.message)
    }
  }

  return {
    articles,
    currentArticle,
    total,
    loading,
    fetchArticles,
    fetchArticle,
    createArticle,
    updateArticle,
    deleteArticle
  }
})
