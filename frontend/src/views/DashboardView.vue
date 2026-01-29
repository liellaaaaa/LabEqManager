<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getEquipmentUsageStats, getBorrowStats, getReminders } from '@/api/statistics'
import type {
  EquipmentUsageStatsResponse,
  BorrowStatsResponse,
  ReminderResponse,
} from '@/api/statistics'
import { DataBoard, DocumentAdd, OfficeBuilding, Tools, Bell, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const equipmentStats = ref<EquipmentUsageStatsResponse | null>(null)
const borrowStats = ref<BorrowStatsResponse | null>(null)
const reminderData = ref<ReminderResponse | null>(null)

const loadingStats = ref(false)
const loadingReminders = ref(false)

const displayName = computed(
  () => userStore.userInfo?.name || userStore.userInfo?.username || '用户',
)

const totalEquipment = computed(() => equipmentStats.value?.totalEquipmentCount ?? 0)
const totalUsage = computed(() => equipmentStats.value?.totalUsageCount ?? 0)

const totalBorrow = computed(() => borrowStats.value?.totalBorrowCount ?? 0)
const overdueCount = computed(() => borrowStats.value?.overdueCount ?? 0)
const overdueRate = computed(() => {
  if (!totalBorrow.value) return 0
  return Number(((overdueCount.value / totalBorrow.value) * 100).toFixed(1))
})

const reminderCount = computed(() => reminderData.value?.reminderCount ?? 0)

const borrowRemindersPreview = computed(
  () => reminderData.value?.borrowReminders.slice(0, 3) ?? [],
)
const reservationRemindersPreview = computed(
  () => reminderData.value?.reservationReminders.slice(0, 3) ?? [],
)

const loadStats = async () => {
  loadingStats.value = true
  try {
    const [equipmentRes, borrowRes] = await Promise.all([
      getEquipmentUsageStats(),
      getBorrowStats(),
    ])
    if (equipmentRes.code === 200 && equipmentRes.data) {
      equipmentStats.value = equipmentRes.data
    }
    if (borrowRes.code === 200 && borrowRes.data) {
      borrowStats.value = borrowRes.data
    }
  } finally {
    loadingStats.value = false
  }
}

const loadReminders = async () => {
  loadingReminders.value = true
  try {
    const response = await getReminders()
    if (response.code === 200 && response.data) {
      reminderData.value = response.data
    }
  } finally {
    loadingReminders.value = false
  }
}

const navigateTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
  loadStats()
  loadReminders()
})
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <div>
        <h2>欢迎，{{ displayName }}！</h2>
        <p class="sub-text">
          今日待办：
          <span v-if="reminderCount > 0">
            有
            <el-tag type="danger" size="small">
              {{ reminderCount }} 条借用/预约逾期或即将到期
            </el-tag>
          </span>
          <span v-else>暂无逾期或即将到期事项</span>
        </p>
      </div>
      <el-button type="primary" plain @click="navigateTo('/ai')">
        <el-icon><ChatDotRound /></el-icon>
        <span style="margin-left: 6px">问 AI 助手</span>
      </el-button>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card v-loading="loadingStats">
          <template #header>
            <div class="card-header">
              <span>设备概览</span>
              <el-icon><Tools /></el-icon>
            </div>
          </template>
          <div class="stat-main">
            <div class="stat-number">{{ totalEquipment }}</div>
            <div class="stat-label">设备总数</div>
          </div>
          <div class="stat-extra">
            累计使用次数：<span class="strong">{{ totalUsage }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-loading="loadingStats">
          <template #header>
            <div class="card-header">
              <span>借用情况</span>
              <el-icon><DocumentAdd /></el-icon>
            </div>
          </template>
          <div class="stat-main">
            <div class="stat-number">{{ overdueRate }}%</div>
            <div class="stat-label">借用逾期率</div>
          </div>
          <div class="stat-extra">
            共 {{ totalBorrow }} 条借用，其中逾期
            <span class="strong danger">{{ overdueCount }}</span>
            条
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-loading="loadingReminders">
          <template #header>
            <div class="card-header">
              <span>到期提醒</span>
              <el-badge :value="reminderCount" :hidden="reminderCount === 0">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          <div class="stat-main">
            <div class="stat-number">{{ reminderCount }}</div>
            <div class="stat-label">待关注事项</div>
          </div>
          <div class="stat-extra">
            <el-button type="primary" link @click="navigateTo('/reminders')">
              查看详情
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="quick-actions-card">
      <template #header>
        <div class="card-header">
          <span>快速操作</span>
          <el-icon><DataBoard /></el-icon>
        </div>
      </template>
      <el-space wrap>
        <el-button type="primary" plain @click="navigateTo('/borrow/apply')">
          <el-icon><DocumentAdd /></el-icon>
          <span style="margin-left: 6px">借用设备</span>
        </el-button>
        <el-button type="primary" plain @click="navigateTo('/reservation/apply')">
          <el-icon><OfficeBuilding /></el-icon>
          <span style="margin-left: 6px">预约实验室</span>
        </el-button>
        <el-button type="warning" plain @click="navigateTo('/repair/apply')">
          <el-icon><Tools /></el-icon>
          <span style="margin-left: 6px">申请维修</span>
        </el-button>
        <el-button type="danger" plain @click="navigateTo('/scrap/apply')">
          <el-icon><Tools /></el-icon>
          <span style="margin-left: 6px">申请报废</span>
        </el-button>
        <el-button @click="navigateTo('/borrow/my')">
          <el-icon><DocumentAdd /></el-icon>
          <span style="margin-left: 6px">查看我的借用</span>
        </el-button>
      </el-space>
    </el-card>

    <el-row :gutter="20" class="reminder-preview-row">
      <el-col :span="12">
        <el-card v-loading="loadingReminders">
          <template #header>
            <div class="card-header">
              <span>借用到期预览</span>
              <el-icon><DocumentAdd /></el-icon>
            </div>
          </template>
          <el-empty v-if="borrowRemindersPreview.length === 0" description="暂无借用到期提醒" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in borrowRemindersPreview"
              :key="item.id"
              :type="item.isOverdue ? 'danger' : item.remainingDays <= 1 ? 'warning' : 'primary'"
              :timestamp="item.planReturnDate"
            >
              <div class="timeline-item">
                <div class="title">{{ item.equipmentName }}</div>
                <div class="meta">
                  <el-tag
                    :type="item.isOverdue ? 'danger' : item.remainingDays <= 1 ? 'warning' : 'success'"
                  >
                    {{ item.isOverdue ? `已逾期 ${Math.abs(item.remainingDays)} 天` : `剩余 ${item.remainingDays} 天` }}
                  </el-tag>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-loading="loadingReminders">
          <template #header>
            <div class="card-header">
              <span>预约到期预览</span>
              <el-icon><OfficeBuilding /></el-icon>
            </div>
          </template>
          <el-empty
            v-if="reservationRemindersPreview.length === 0"
            description="暂无实验室预约提醒"
          />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in reservationRemindersPreview"
              :key="item.id"
              :type="item.isExpired ? 'danger' : item.remainingDays <= 1 ? 'warning' : 'primary'"
              :timestamp="item.reserveDate"
            >
              <div class="timeline-item">
                <div class="title">{{ item.laboratoryName }}</div>
                <div class="meta">
                  {{ item.startTime }} - {{ item.endTime }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-section h2 {
  margin: 0 0 6px;
}

.sub-text {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

.stats-row {
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.stat-main {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-extra {
  font-size: 13px;
  color: #606266;
}

.strong {
  font-weight: 600;
}

.danger {
  color: #f56c6c;
}

.quick-actions-card {
  margin-top: 4px;
}

.reminder-preview-row {
  margin-bottom: 8px;
}

.timeline-item .title {
  font-weight: 600;
}

.timeline-item .meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>


