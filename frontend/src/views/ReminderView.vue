<template>
  <div class="reminder-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>到期提醒</span>
          <el-badge :value="reminderCount" :hidden="reminderCount === 0" class="reminder-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
        </div>
      </template>

      <div v-loading="loading" class="reminder-content">
        <el-empty v-if="!hasReminder" description="暂无到期提醒" />

        <div v-else>
          <!-- 借用提醒 -->
          <div v-if="reminderData?.borrowReminders && reminderData.borrowReminders.length > 0" class="reminder-section">
            <h3 class="section-title">
              <el-icon><DocumentAdd /></el-icon>
              设备借用提醒
            </h3>
            <el-table :data="reminderData.borrowReminders" border stripe>
              <el-table-column prop="equipmentName" label="设备名称" width="200" />
              <el-table-column prop="planReturnDate" label="计划归还日期" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.planReturnDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="remainingDays" label="剩余天数" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isOverdue ? 'danger' : row.remainingDays <= 1 ? 'warning' : 'success'">
                    {{ row.isOverdue ? `已逾期 ${Math.abs(row.remainingDays)} 天` : `剩余 ${row.remainingDays} 天` }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="viewBorrow(row.id)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 预约提醒 -->
          <div v-if="reminderData?.reservationReminders && reminderData.reservationReminders.length > 0" class="reminder-section">
            <h3 class="section-title">
              <el-icon><OfficeBuilding /></el-icon>
              实验室预约提醒
            </h3>
            <el-table :data="reminderData.reservationReminders" border stripe>
              <el-table-column prop="laboratoryName" label="实验室名称" width="200" />
              <el-table-column prop="reserveDate" label="预约日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.reserveDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="startTime" label="开始时间" width="120" />
              <el-table-column prop="endTime" label="结束时间" width="120" />
              <el-table-column prop="remainingDays" label="剩余天数" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.isExpired ? 'danger' : row.remainingDays <= 1 ? 'warning' : 'success'">
                    {{ row.isExpired ? `已过期 ${Math.abs(row.remainingDays)} 天` : `剩余 ${row.remainingDays} 天` }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="viewReservation(row.id)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getReminders } from '@/api/statistics'
import type { ReminderResponse } from '@/api/statistics'
import { Bell, DocumentAdd, OfficeBuilding } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const reminderData = ref<ReminderResponse | null>(null)
const loading = ref(false)

const hasReminder = computed(() => reminderData.value?.hasReminder || false)
const reminderCount = computed(() => reminderData.value?.reminderCount || 0)

// 加载提醒数据
const loadReminders = async () => {
  loading.value = true
  try {
    const response = await getReminders()
    if (response.code === 200 && response.data) {
      reminderData.value = response.data
    } else {
      ElMessage.error(response.message || '获取提醒失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取提醒失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

// 查看借用详情
const viewBorrow = (id: number) => {
  router.push({ name: 'borrowMy', query: { highlightId: id.toString() } })
}

// 查看预约详情
const viewReservation = (id: number) => {
  router.push({ name: 'reservationMy', query: { highlightId: id.toString() } })
}

onMounted(() => {
  loadReminders()
})
</script>

<style scoped>
.reminder-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.reminder-badge {
  cursor: pointer;
}

.reminder-content {
  min-height: 300px;
}

.reminder-section {
  margin-bottom: 30px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
</style>

