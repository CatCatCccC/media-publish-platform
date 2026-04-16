<template>
  <div class="platforms-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>平台配置</span>
          <el-button type="primary" @click="fetchPlatforms">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="platform in PLATFORM_LIST" :key="platform.code">
          <el-card class="platform-card" shadow="hover">
            <template #header>
              <div class="platform-header">
                <el-icon :size="24">
                  <component :is="platform.icon" />
                </el-icon>
                <span class="platform-name">{{ platform.name }}</span>
                <el-tag v-if="isEnabled(platform.code)" type="success" size="small">已启用</el-tag>
                <el-tag v-else type="info" size="small">未配置</el-tag>
              </div>
            </template>
            
            <div class="platform-content">
              <div class="credentials-display">
                <span class="label">凭证：</span>
                <span class="value">{{ getMaskedCredentials(platform.code) || '未配置' }}</span>
              </div>
              
              <el-divider />
              
              <div class="platform-form">
                <el-form :model="forms[platform.code]" label-position="top">
                  <el-form-item label="凭证">
                    <el-input
                      v-model="forms[platform.code].credentials"
                      type="password"
                      placeholder="请输入凭证信息"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="启用状态">
                    <el-switch v-model="forms[platform.code].enabled" />
                  </el-form-item>
                </el-form>
                
                <div class="platform-actions">
                  <el-button type="primary" size="small" @click="handleSave(platform.code)">
                    保存配置
                  </el-button>
                  <el-button size="small" @click="handleTest(platform.code)">
                    测试连接
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-alert
        type="info"
        :closable="false"
        style="margin-top: 20px;"
      >
        <template #title>
          <div class="config-tips">
            <p><strong>各平台凭证说明：</strong></p>
            <ul>
              <li><strong>CSDN：</strong>登录CSDN后，从浏览器Cookie中获取"cookie"字段的值</li>
              <li><strong>知乎：</strong>登录知乎后，从浏览器Cookie中获取"z_c0"字段的值</li>
              <li><strong>微信公众号：</strong>在微信公众平台获取AppID和AppSecret，用冒号连接</li>
            </ul>
          </div>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Monitor, Reading, ChatDotRound } from '@element-plus/icons-vue'
import { usePlatformStore } from '@/stores/platform'
import type { PlatformConfig } from '@/types'

const platformStore = usePlatformStore()

const PLATFORM_LIST = [
  { code: 'CSDN', name: 'CSDN博客', icon: Monitor },
  { code: 'ZHIHU', name: '知乎', icon: Reading },
  { code: 'WECHAT', name: '微信公众号', icon: ChatDotRound }
]

const platforms = ref<PlatformConfig[]>([])

const forms = reactive<Record<string, { credentials: string; enabled: boolean }>>({
  CSDN: { credentials: '', enabled: true },
  ZHIHU: { credentials: '', enabled: true },
  WECHAT: { credentials: '', enabled: true }
})

const fetchPlatforms = async () => {
  try {
    await platformStore.fetchPlatforms()
    platforms.value = platformStore.platforms
  } catch (error: any) {
    ElMessage.error(error.message || '获取平台配置失败')
  }
}

const isEnabled = (platform: string) => {
  const config = platforms.value.find(p => p.platform === platform)
  return config?.enabled ?? false
}

const getMaskedCredentials = (platform: string) => {
  const config = platforms.value.find(p => p.platform === platform)
  return config?.credentialsMasked ?? ''
}

const handleSave = async (platform: string) => {
  const form = forms[platform]
  if (!form.credentials) {
    ElMessage.warning('请输入凭证信息')
    return
  }
  
  try {
    await platformStore.savePlatform(platform, form.credentials, form.enabled)
    ElMessage.success('配置保存成功')
    form.credentials = ''
    await fetchPlatforms()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleTest = async (platform: string) => {
  const form = forms[platform]
  try {
    const result = await platformStore.testConnection(platform, form.credentials || undefined)
    if (result.success) {
      ElMessage.success(`${platform}: ${result.message}`)
    } else {
      ElMessage.error(`${platform}: ${result.message}`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '测试连接失败')
  }
}

onMounted(() => {
  fetchPlatforms()
})
</script>

<style scoped>
.platforms-page {
  
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.platform-card {
  margin-bottom: 20px;
}

.platform-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.platform-name {
  font-size: 16px;
  font-weight: 600;
  flex: 1;
}

.platform-content {
  padding: 10px 0;
}

.credentials-display {
  margin-bottom: 10px;
}

.credentials-display .label {
  color: #909399;
}

.credentials-display .value {
  font-family: monospace;
  color: #606266;
}

.platform-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.config-tips ul {
  margin: 10px 0;
  padding-left: 20px;
}

.config-tips li {
  margin: 5px 0;
  color: #606266;
}
</style>
