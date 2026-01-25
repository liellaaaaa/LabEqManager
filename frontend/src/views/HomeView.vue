<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { OfficeBuilding, Tools, DocumentAdd, List, Finished, AlarmClock, Setting, Delete, DataAnalysis, Bell } from '@element-plus/icons-vue'
import { getReminders } from '@/api/statistics'

const router = useRouter()
const userStore = useUserStore()

const reminderCount = ref(0)
let reminderTimer: number | null = null

const navigateTo = (path: string) => {
  router.push(path)
}

// 加载提醒数量
const loadReminderCount = async () => {
  try {
    const response = await getReminders()
    if (response.code === 200 && response.data) {
      reminderCount.value = response.data.reminderCount || 0
    }
  } catch (error) {
    // 静默失败，不影响页面加载
    console.error('加载提醒数量失败', error)
  }
}

onMounted(() => {
  loadReminderCount()
  // 每30秒刷新一次提醒数量
  reminderTimer = window.setInterval(loadReminderCount, 30000)
})

onUnmounted(() => {
  if (reminderTimer) {
    clearInterval(reminderTimer)
  }
})
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
            <el-menu-item index="/repair/apply">
              <el-icon><Setting /></el-icon>
              <span>申请维修</span>
            </el-menu-item>
            <el-menu-item index="/repair/my">
              <el-icon><List /></el-icon>
              <span>我的维修</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/repair/management">
              <el-icon><Setting /></el-icon>
              <span>维修管理</span>
            </el-menu-item>
            <el-menu-item index="/scrap/apply">
              <el-icon><Delete /></el-icon>
              <span>申请报废</span>
            </el-menu-item>
            <el-menu-item index="/scrap/my">
              <el-icon><List /></el-icon>
              <span>我的报废</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/scrap/approval">
              <el-icon><Finished /></el-icon>
              <span>报废审批</span>
            </el-menu-item>
            <el-menu-item index="/reservation/apply">
              <el-icon><OfficeBuilding /></el-icon>
              <span>申请预约</span>
            </el-menu-item>
            <el-menu-item index="/reservation/my">
              <el-icon><List /></el-icon>
              <span>我的预约</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/reservation/approval">
              <el-icon><Finished /></el-icon>
              <span>预约审批</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin() || userStore.isTeacher()" index="/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>统计报表</span>
            </el-menu-item>
            <el-menu-item index="/reminders">
              <el-icon><Bell /></el-icon>
              <span>到期提醒</span>
              <el-badge v-if="reminderCount > 0" :value="reminderCount" class="reminder-badge" />
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

.reminder-badge {
  margin-left: 8px;
}
</style>
