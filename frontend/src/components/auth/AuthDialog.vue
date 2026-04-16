<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="auth-container">
      <!-- 状态提示 -->
      <el-alert
        v-if="status === 'STARTING'"
        type="info"
        title="正在启动浏览器..."
        :closable="false"
        show-icon
      />
      <el-alert
        v-else-if="status === 'READY'"
        type="success"
        title="浏览器已就绪，请在下方窗口中登录"
        :closable="false"
        show-icon
      />
      <el-alert
        v-else-if="status === 'FAILED'"
        type="error"
        title="启动失败，请重试"
        :closable="false"
        show-icon
      />

      <!-- noVNC 容器 -->
      <div class="vnc-container" v-loading="status === 'STARTING'">
        <div ref="vncContainer" class="vnc-screen"></div>
      </div>

      <!-- 操作提示 -->
      <div class="auth-tips" v-if="status === 'READY'">
        <p>1. 在上方浏览器窗口中登录 {{ platformName }}</p>
        <p>2. 登录成功后，点击下方「提取凭证」按钮</p>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        @click="extractCredentials"
        :disabled="status !== 'READY'"
        :loading="extracting"
      >
        提取凭证
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import RFB from '@novnc/novnc/core/rfb'

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

const status = ref('IDLE')
const sessionId = ref('')
const wsPort = ref(0)
const extracting = ref(false)
const vncContainer = ref<HTMLDivElement>()
let rfb: RFB | null = null

watch(visible, async (val) => {
  if (val && props.platform) {
    await startSession()
  } else if (!val) {
    cleanup()
  }
})

const startSession = async () => {
  status.value = 'STARTING'
  
  try {
    const { data } = await axios.post(`/api/auth/session/${props.platform}`)
    sessionId.value = data.data.sessionId
    wsPort.value = data.data.websockifyPort
    
    // 等待会话就绪
    await pollStatus()
    
    // 连接 noVNC
    if (status.value === 'READY') {
      connectVNC()
    }
  } catch (error: any) {
    console.error('创建会话失败:', error)
    status.value = 'FAILED'
    ElMessage.error('启动浏览器失败')
  }
}

const pollStatus = async () => {
  for (let i = 0; i < 30; i++) {
    await new Promise(r => setTimeout(r, 1000))
    
    try {
      const { data } = await axios.get(`/api/auth/session/${sessionId.value}`)
      if (data.data.status === 'READY') {
        status.value = 'READY'
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

const connectVNC = () => {
  if (!vncContainer.value) return
  
  const wsUrl = `ws://${location.hostname}:${wsPort.value}`
  
  try {
    rfb = new RFB(vncContainer.value, wsUrl, {
      credentials: { password: '' }
    })
    rfb.scaleViewport = true
    rfb.resizeSession = true
  } catch (error) {
    console.error('连接 VNC 失败:', error)
    ElMessage.error('连接远程浏览器失败')
  }
}

const extractCredentials = async () => {
  extracting.value = true
  
  try {
    const { data } = await axios.post(`/api/auth/session/${sessionId.value}/extract`)
    
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
  if (rfb) {
    rfb.disconnect()
    rfb = null
  }
  
  if (sessionId.value) {
    try {
      await axios.delete(`/api/auth/session/${sessionId.value}`)
    } catch (e) {}
    sessionId.value = ''
  }
  
  status.value = 'IDLE'
}

onBeforeUnmount(() => {
  cleanup()
})
</script>

<style scoped>
.auth-container {
  min-height: 500px;
}

.vnc-container {
  margin-top: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.vnc-screen {
  width: 100%;
  height: 500px;
  background: #1a1a1a;
}

.auth-tips {
  margin-top: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.auth-tips p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}
</style>
