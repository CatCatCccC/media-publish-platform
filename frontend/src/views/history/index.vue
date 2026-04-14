<template>
  <div class="history-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布历史</span>
          <div class="header-actions">
            <el-select
              v-model="filterStatus"
              placeholder="状态筛选"
              clearable
              style="width: 150px; margin-right: 10px;"
              @change="handleFilterChange"
            >
              <el-option label="全部" :value="undefined" />
              <el-option label="发布中" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="发布失败" :value="2" />
            </el-select>
            <el-button @click="fetchRecords">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="articleTitle" label="文章标题" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="goToEdit(row.articleId)">
              {{ row.articleTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="platform" label="平台" width="120">
          <template #default="{ row }">
            {{ getPlatformName(row.platform) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishUrl" label="发布链接" min-width="200">
          <template #default="{ row }">
            <el-link
              v-if="row.publishUrl"
              :href="row.publishUrl"
              target="_blank"
              type="primary"
            >
              {{ row.publishUrl }}
            </el-link>
            <span v-else-if="row.errorMessage" style="color: #f56c6c;" class="error-message">
              {{ row.errorMessage }}
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.publishedAt) }}
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { usePublishStore } from '@/stores/publish'
import type { PublishRecord } from '@/types'

const router = useRouter()
const publishStore = usePublishStore()

const records = ref<PublishRecord[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref<number | undefined>(undefined)

const PLATFORM_LIST = [
  { code: 'CSDN', name: 'CSDN' },
  { code: 'ZHIHU', name: '知乎' },
  { code: 'WECHAT', name: '微信公众号' }
]

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

const fetchRecords = async () => {
  loading.value = true
  try {
    await publishStore.fetchRecords(currentPage.value, pageSize.value, filterStatus.value)
    records.value = publishStore.records
    total.value = publishStore.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取发布记录失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchRecords()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchRecords()
}

const handlePageChange = () => {
  fetchRecords()
}

const goToEdit = (articleId: number) => {
  router.push(`/articles/${articleId}/edit`)
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.history-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.error-message {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
