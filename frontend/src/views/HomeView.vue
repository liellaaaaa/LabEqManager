<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  House,
  OfficeBuilding,
  Tools,
  DocumentAdd,
  User,
  ChatDotRound,
  Bell,
} from '@element-plus/icons-vue'
import { getReminders } from '@/api/statistics'
import AiAssistantView from './AiAssistantView.vue'

const router = useRouter()
const userStore = useUserStore()

const reminderCount = ref(0)
let reminderTimer: number | null = null
const aiDrawerVisible = ref(false)

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
            <div class="notification" @click="navigateTo('/reminders')">
              <el-badge :value="reminderCount" :hidden="reminderCount === 0">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
            <span>欢迎，{{ userStore.userInfo?.name || userStore.userInfo?.username }}</span>
            <el-button type="danger" size="small" @click="userStore.logout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="280px" class="aside">
          <el-menu :default-active="$route.path" router class="menu">
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/laboratory">
              <el-icon><OfficeBuilding /></el-icon>
              <span>实验室管理</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin()" index="/equipment">
              <el-icon><Tools /></el-icon>
              <span>设备管理</span>
            </el-menu-item>
            <el-sub-menu index="apply-manage">
              <template #title>
                <el-icon><DocumentAdd /></el-icon>
                <span>申请与管理</span>
              </template>
              <el-menu-item index="/borrow/apply">申请借用设备</el-menu-item>
              <el-menu-item index="/reservation/apply">申请预约实验室</el-menu-item>
              <el-menu-item index="/repair/apply">申请维修</el-menu-item>
              <el-menu-item index="/scrap/apply">申请报废</el-menu-item>
              <el-menu-item v-if="userStore.isAdmin()" index="/borrow/approval">
                借用审批/出借
              </el-menu-item>
              <el-menu-item v-if="userStore.isAdmin()" index="/repair/management">
                维修管理
              </el-menu-item>
              <el-menu-item v-if="userStore.isAdmin()" index="/scrap/approval">
                报废审批
              </el-menu-item>
              <el-menu-item v-if="userStore.isAdmin()" index="/reservation/approval">
                预约审批
              </el-menu-item>
              <el-menu-item v-if="userStore.isAdmin() || userStore.isTeacher()" index="/statistics">
                统计报表
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="my">
              <template #title>
                <el-badge :value="reminderCount" :hidden="reminderCount === 0" class="reminder-badge">
                  <span class="menu-title-with-badge">
                    <el-icon><User /></el-icon>
                    <span>我的</span>
                  </span>
                </el-badge>
              </template>
              <el-menu-item index="/borrow/my">我的借用</el-menu-item>
              <el-menu-item index="/reservation/my">我的预约</el-menu-item>
              <el-menu-item index="/repair/my">我的维修</el-menu-item>
              <el-menu-item index="/scrap/my">我的报废</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/ai">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI 智能助手</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    <div class="ai-float">
      <el-button type="primary" circle @click="aiDrawerVisible = true">
        <el-icon><ChatDotRound /></el-icon>
      </el-button>
    </div>
    <el-drawer
      v-model="aiDrawerVisible"
      title="AI 智能助手"
      size="50%"
      direction="rtl"
      :with-header="true"
    >
      <AiAssistantView />
    </el-drawer>
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

.notification {
  cursor: pointer;
  display: flex;
  align-items: center;
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

.menu-title-with-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.ai-float {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 3000;
}
</style>
