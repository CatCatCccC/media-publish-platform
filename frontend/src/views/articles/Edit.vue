<template>
  <div class="article-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑文章' : '新建文章' }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
        </el-form-item>
        
        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            placeholder="请输入文章摘要"
            :rows="2"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="封面图片">
          <div class="cover-upload">
            <el-input v-model="form.coverImage" placeholder="请输入封面图片URL或上传" />
            <el-upload
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :http-request="uploadCover"
            >
              <el-button type="primary" :loading="coverUploading">上传封面</el-button>
            </el-upload>
          </div>
          <div v-if="form.coverImage" class="cover-preview">
            <el-image :src="form.coverImage" fit="cover" />
          </div>
        </el-form-item>
        
        <el-form-item label="正文内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入文章内容，支持富文本"
            :rows="15"
          />
        </el-form-item>
        
        <el-form-item label="配图">
          <div class="images-upload">
            <el-upload
              :file-list="imageList"
              :before-upload="beforeImageUpload"
              :http-request="uploadImage"
              list-type="picture-card"
              multiple
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">
            {{ isEdit ? '保存修改' : '创建文章' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-dialog v-model="previewVisible" title="文章预览" width="80%">
      <div class="preview-content">
        <h1>{{ form.title }}</h1>
        <div v-if="form.summary" class="preview-summary">{{ form.summary }}</div>
        <div v-if="form.coverImage" class="preview-cover">
          <img :src="form.coverImage" alt="封面" />
        </div>
        <div class="preview-body" v-html="form.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useArticleStore } from '@/stores/article'
import { articleApi } from '@/api/article'

const router = useRouter()
const route = useRoute()
const articleStore = useArticleStore()

const formRef = ref()
const saving = ref(false)
const coverUploading = ref(false)
const previewVisible = ref(false)
const imageList = ref<any[]>([])

const isEdit = computed(() => !!route.params.id)
const articleId = computed(() => Number(route.params.id))

const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  images: [] as string[]
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const fetchArticle = async () => {
  if (!articleId.value) return
  try {
    await articleStore.fetchArticle(articleId.value)
    const article = articleStore.currentArticle
    if (article) {
      form.title = article.title
      form.content = article.content
      form.summary = article.summary || ''
      form.coverImage = article.coverImage || ''
      form.images = article.images || []
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取文章失败')
  }
}

const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const uploadCover = async ({ file }: { file: File }) => {
  coverUploading.value = true
  try {
    const res = await articleApi.upload(file)
    if (res.data.code === 200) {
      form.coverImage = 'http://localhost:8080' + res.data.data.url
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(res.data.message || '上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    coverUploading.value = false
  }
}

const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const uploadImage = async ({ file }: { file: File }) => {
  try {
    const res = await articleApi.upload(file)
    if (res.data.code === 200) {
      const url = 'http://localhost:8080' + res.data.data.url
      form.images.push(url)
      imageList.value.push({ url })
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.data.message || '上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    const data = {
      title: form.title,
      content: form.content,
      summary: form.summary,
      coverImage: form.coverImage,
      images: JSON.stringify(form.images)
    }
    
    if (isEdit.value) {
      await articleStore.updateArticle(articleId.value, data)
      ElMessage.success('保存成功')
    } else {
      await articleStore.createArticle(data)
      ElMessage.success('创建成功')
    }
    
    goBack()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/articles')
}

onMounted(() => {
  if (isEdit.value) {
    fetchArticle()
  }
})
</script>

<style scoped>
.article-edit {
  max-width: 1000px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cover-upload {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cover-upload .el-input {
  flex: 1;
}

.cover-preview {
  margin-top: 10px;
}

.cover-preview .el-image {
  width: 200px;
  height: 120px;
  border-radius: 4px;
}

.images-upload {
  width: 100%;
}

.preview-content {
  padding: 20px;
}

.preview-content h1 {
  text-align: center;
  margin-bottom: 20px;
}

.preview-summary {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.preview-cover {
  margin-bottom: 20px;
  text-align: center;
}

.preview-cover img {
  max-width: 100%;
  border-radius: 8px;
}

.preview-body {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
