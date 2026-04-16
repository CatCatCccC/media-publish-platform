<template>
  <div class="publish-page">
    <el-card>
      <template #header>
        <span>发布文章</span>
      </template>
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择文章">
          <el-select
            v-model="form.articleId"
            placeholder="请选择要发布的文章"
            filterable
            style="width: 100%"
            @change="handleArticleChange"
          >
            <el-option
              v-for="article in articles"
              :key="article.id"
              :label="article.title"
              :value="article.id"
            >
              <span>{{ article.title }}</span>
              <el-tag v-if="article.status === 1" type="success" size="small" style="margin-left: 10px;">
                已发布
              </el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="文章摘要" v-if="selectedArticle">
          <div class="article-summary">{{ selectedArticle.summary || '无' }}</div>
        </el-form-item>
        
        <el-form-item label="发布平台">
          <el-checkbox-group v-model="form.platforms">
            <el-checkbox
              v-for="platform in PLATFORM_LIST"
              :key="platform.code"
              :label="platform.code"
              :disabled="!isPlatformEnabled(platform.code)"
            >
              <el-icon><component :is="platform.icon" /></el-icon>
              {{ platform.name }}
              <el-tag v-if="!isPlatformEnabled(platform.code)" type="info" size="small" style="margin-left: 5px;">
                未配置
              </el-tag>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            @click="handlePublish"
            :loading="publishing"
            :disabled="!canPublish"
          >
            <el-icon><Promotion /></el-icon>
            一键发布
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card v-if="publishResults.length > 0" style="margin-top: 20px;">
      <template #header>
        <span>发布结果</span>
      </template>
      
      <el-table :data="publishResults" stripe>
        <el-table-column prop="platform" label="平台" width="120">
          <template #default="{ row }">
            {{ getPlatformName(row.platform) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="publishUrl" label="发布链接">
          <template #default="{ row }">
            <el-link
              v-if="row.publishUrl"
              :href="row.publishUrl"
              target="_blank"
              type="primary"
            >
              {{ row.publishUrl }}
            </el-link>
            <span v-else-if="row.errorMessage" style="color: #f56c6c;">
              {{ row.errorMessage }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="publishedAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.publishedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion, Monitor, Reading, ChatDotRound } from '@element-plus/icons-vue'
import { useArticleStore } from '@/stores/article'
import { usePlatformStore } from '@/stores/platform'
import { usePublishStore } from '@/stores/publish'
import type { Article, PublishRecord } from '@/types'

const articleStore = useArticleStore()
const platformStore = usePlatformStore()
const publishStore = usePublishStore()

const PLATFORM_LIST = [
  { code: 'CSDN', name: 'CSDN', icon: Monitor },
  { code: 'ZHIHU', name: '知乎', icon: Reading },
  { code: 'WECHAT', name: '微信公众号', icon: ChatDotRound }
]

const articles = ref<Article[]>([])
const publishing = ref(false)
const publishResults = ref<PublishRecord[]>([])

const form = reactive({
  articleId: null as number | null,
  platforms: [] as string[]
})

const selectedArticle = computed(() => {
  if (!form.articleId) return null
  return articles.value.find(a => a.id === form.articleId)
})

const canPublish = computed(() => {
  return form.articleId && form.platforms.length > 0
})

const isPlatformEnabled = (platform: string) => {
  return platformStore.isPlatformEnabled(platform)
}

const getPlatformName = (code: string) => {
  const platform = PLATFORM_LIST.find(p => p.code === code)
  return platform?.name ?? code
}

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchArticles = async () => {
  try {
    await articleStore.fetchArticles(1, 100)
    articles.value = articleStore.articles.filter(a => a.status !== 1)
  } catch (error: any) {
    ElMessage.error(error.message || '获取文章列表失败')
  }
}

const fetchPlatforms = async () => {
  try {
    await platformStore.fetchPlatforms()
  } catch (error: any) {
    ElMessage.error(error.message || '获取平台配置失败')
  }
}

const handleArticleChange = () => {
  publishResults.value = []
}

const handlePublish = async () => {
  if (!form.articleId || form.platforms.length === 0) {
    ElMessage.warning('请选择文章和发布平台')
    return
  }
  
  const notConfiguredPlatforms = form.platforms.filter(p => !isPlatformEnabled(p))
  if (notConfiguredPlatforms.length > 0) {
    ElMessage.warning(`以下平台未配置凭证: ${notConfiguredPlatforms.map(p => getPlatformName(p)).join(', ')}`)
    return
  }
  
  publishing.value = true
  publishResults.value = []
  
  try {
    const results = await publishStore.publish(form.articleId, form.platforms)
    publishResults.value = results
    
    const successCount = results.filter(r => r.status === 1).length
    const failCount = results.filter(r => r.status === 2).length
    
    if (successCount > 0) {
      ElMessage.success(`发布成功 ${successCount} 个平台`)
    }
    if (failCount > 0) {
      ElMessage.warning(`${failCount} 个平台发布失败，请查看详情`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const resetForm = () => {
  form.articleId = null
  form.platforms = []
  publishResults.value = []
}

onMounted(() => {
  fetchArticles()
  fetchPlatforms()
})
</script>

<style scoped>
.publish-page {
  width: 100%;
}

.article-summary {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}

.el-checkbox {
  margin-right: 20px;
  height: auto;
}
</style>
