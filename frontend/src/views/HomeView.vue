<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { OfficeBuilding, Tools, DocumentAdd, List, Finished, AlarmClock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>实验室设备管理系统</h1>
          <div class="user-info">
            <span>欢迎，{{ userStore.userInfo?.name || userStore.userInfo?.username }}</span>
            <el-button type="danger" size="small" @click="userStore.logout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="$route.path"
            router
            class="menu"
          >
            <el-menu-item index="/laboratory">
              <el-icon><OfficeBuilding /></el-icon>
              <span>实验室管理</span>
            </el-menu-item>
            <el-menu-item index="/equipment">
              <el-icon><Tools /></el-icon>
              <span>设备管理</span>
            </el-menu-item>
            <el-menu-item index="/borrow/apply">
              <el-icon><DocumentAdd /></el-icon>
              <span>申请借用</span>
            </el-menu-item>
            <el-menu-item index="/borrow/my">
              <el-icon><List /></el-icon>
              <span>我的借用</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/borrow/approval">
              <el-icon><Finished /></el-icon>
              <span>借用审批/出借</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/borrow/approval#overdue">
              <el-icon><AlarmClock /></el-icon>
              <span>逾期标记</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.home-container {
  height: 100vh;
}

.header {
  background-color: #409eff;
  color: white;
  padding: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.aside {
  background-color: #f5f5f5;
}

.menu {
  border-right: none;
  height: 100%;
}

.main {
  padding: 20px;
  background-color: #f0f2f5;
}
</style>
