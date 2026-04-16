<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="browser-container">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
          v-model="currentUrl"
          placeholder="输入网址"
          style="flex: 1"
          @keyup.enter="navigate"
        >
          <template #prepend>URL</template>
        </el-input>
        <el-button @click="navigate" :loading="loading">跳转</el-button>
        <el-button @click="refreshScreenshot" :loading="loading" :icon="Refresh">刷新</el-button>
      </div>

      <!-- 浏览器画面 -->
      <div class="screen-container" v-loading="status === 'CREATING'">
        <img
          v-if="screenshot"
          :src="'data:image/png;base64,' + screenshot"
          class="browser-screen"
          @click="handleClick"
          @contextmenu.prevent
        />
        <div v-else class="empty-screen">
          <el-icon :size="48"><Monitor /></el-icon>
          <p>等待浏览器启动...</p>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputText"
          placeholder="输入文本后按回车发送"
          @keyup.enter="sendText"
        >
          <template #append>
            <el-button @click="sendText">发送</el-button>
          </template>
        </el-input>
      </div>

      <!-- 状态栏 -->
      <div class="status-bar">
        <el-tag :type="statusTagType">{{ statusText }}</el-tag>
        <span class="session-id" v-if="sessionId">会话: {{ sessionId }}</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        @click="extractCookies"
        :disabled="status !== 'READY'"
        :loading="extracting"
      >
        提取凭证
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Monitor } from '@element-plus/icons-vue'
import axios from 'axios'

const props = defineProps<{
  modelValue: boolean
  platform: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success', credentials: string): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = computed(() => `获取 ${platformName.value} 凭证`)

const platformName = computed(() => {
  const names: Record<string, string> = {
    csdn: 'CSDN',
    zhihu: '知乎',
    wechat: '微信公众号'
  }
  return names[props.platform.toLowerCase()] || props.platform
})

const sessionId = ref('')
const status = ref('IDLE')
const screenshot = ref('')
const currentUrl = ref('')
const inputText = ref('')
const loading = ref(false)
const extracting = ref(false)

const statusText = computed(() => {
  switch (status.value) {
    case 'CREATING': return '正在启动浏览器...'
    case 'READY': return '浏览器已就绪'
    case 'SUCCESS': return '凭证已提取'
    case 'FAILED': return '启动失败'
    default: return '空闲'
  }
})

const statusTagType = computed(() => {
  switch (status.value) {
    case 'READY': return 'success'
    case 'SUCCESS': return 'success'
    case 'FAILED': return 'danger'
    case 'CREATING': return 'warning'
    default: return 'info'
  }
})

watch(visible, async (val) => {
  if (val && props.platform) {
    await startSession()
  } else if (!val) {
    cleanup()
  }
})

const startSession = async () => {
  status.value = 'CREATING'
  screenshot.value = ''
  
  try {
    const { data } = await axios.post(`/api/browser/session/${props.platform}`)
    sessionId.value = data.data.sessionId
    
    // 轮询等待就绪
    await pollStatus()
    
    if (status.value === 'READY') {
      await refreshScreenshot()
    }
  } catch (error: any) {
    console.error('启动会话失败:', error)
    status.value = 'FAILED'
    ElMessage.error('启动浏览器失败')
  }
}

const pollStatus = async () => {
  for (let i = 0; i < 30; i++) {
    await new Promise(r => setTimeout(r, 1000))
    
    try {
      const { data } = await axios.get(`/api/browser/session/${sessionId.value}`)
      if (data.data.status === 'READY') {
        status.value = 'READY'
        currentUrl.value = data.data.url || ''
        return
      } else if (data.data.status === 'FAILED') {
        status.value = 'FAILED'
        return
      }
    } catch (e) {
      console.error('查询状态失败:', e)
    }
  }
  
  status.value = 'FAILED'
}

const refreshScreenshot = async () => {
  if (!sessionId.value) return
  loading.value = true
  
  try {
    const { data } = await axios.get(`/api/browser/session/${sessionId.value}/screenshot`)
    if (data.data.success) {
      screenshot.value = data.data.screenshot
      currentUrl.value = data.data.url || currentUrl.value
    }
  } catch (error) {
    console.error('获取截图失败:', error)
  } finally {
    loading.value = false
  }
}

const handleClick = async (event: MouseEvent) => {
  if (!sessionId.value || status.value !== 'READY') return
  
  const rect = (event.target as HTMLImageElement).getBoundingClientRect()
  const x = Math.round(event.clientX - rect.left)
  const y = Math.round(event.clientY - rect.top)
  
  loading.value = true
  try {
    const { data } = await axios.post(`/api/browser/session/${sessionId.value}/click`, { x, y })
    if (data.data.success) {
      screenshot.value = data.data.screenshot
      currentUrl.value = data.data.url || currentUrl.value
    }
  } catch (error) {
    console.error('点击失败:', error)
  } finally {
    loading.value = false
  }
}

const sendText = async () => {
  if (!inputText.value || !sessionId.value) return
  
  loading.value = true
  try {
    await axios.post(`/api/browser/session/${sessionId.value}/type`, { text: inputText.value })
    inputText.value = ''
    await refreshScreenshot()
  } catch (error) {
    console.error('输入失败:', error)
  } finally {
    loading.value = false
  }
}

const navigate = async () => {
  if (!currentUrl.value || !sessionId.value) return
  
  loading.value = true
  try {
    const { data } = await axios.post(`/api/browser/session/${sessionId.value}/navigate`, { url: currentUrl.value })
    if (data.data.success) {
      screenshot.value = data.data.screenshot
    }
  } catch (error) {
    console.error('导航失败:', error)
  } finally {
    loading.value = false
  }
}

const extractCredentials = async () => {
  extracting.value = true
  
  try {
    const { data } = await axios.post(`/api/browser/session/${sessionId.value}/cookies`)
    
    if (data.data.success) {
      ElMessage.success('凭证提取成功')
      emit('success', data.data.cookieString)
      handleClose()
    } else {
      ElMessage.error(data.data.error || '提取失败，请确保已登录')
    }
  } catch (error: any) {
    ElMessage.error('提取凭证失败')
  } finally {
    extracting.value = false
  }
}

const handleClose = () => {
  visible.value = false
}

const cleanup = async () => {
  if (sessionId.value) {
    try {
      await axios.delete(`/api/browser/session/${sessionId.value}`)
    } catch (e) {}
    sessionId.value = ''
  }
  
  status.value = 'IDLE'
  screenshot.value = ''
}

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.browser-container {
  min-height: 600px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.screen-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #1a1a1a;
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.browser-screen {
  max-width: 100%;
  cursor: pointer;
}

.empty-screen {
  text-align: center;
  color: #909399;
}

.empty-screen p {
  margin-top: 10px;
}

.input-area {
  margin-top: 10px;
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.session-id {
  color: #909399;
  font-size: 12px;
}
</style>
