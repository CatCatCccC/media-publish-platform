<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="logo">
        <el-icon><Connection /></el-icon>
        <span>自媒体文章发布系统</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        mode="horizontal"
        :router="true"
        class="nav-menu"
      >
        <el-menu-item index="/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/platforms">
          <el-icon><Setting /></el-icon>
          <span>平台配置</span>
        </el-menu-item>
        <el-menu-item index="/publish">
          <el-icon><Promotion /></el-icon>
          <span>发布文章</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <span>发布历史</span>
        </el-menu-item>
      </el-menu>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Connection, Document, Setting, Promotion, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  margin-right: 40px;
}

.nav-menu {
  border-bottom: none;
}

.app-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
