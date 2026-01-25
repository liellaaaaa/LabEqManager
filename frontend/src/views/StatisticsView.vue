<template>
  <div class="statistics-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>统计与报表</span>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <!-- 设备使用统计 -->
        <el-col :span="24" v-if="userStore.isAdmin() || userStore.isTeacher()">
          <el-card class="stat-card">
            <template #header>
              <div class="stat-header">
                <el-icon><Tools /></el-icon>
                <span>设备使用次数统计</span>
              </div>
            </template>
            <div v-loading="usageLoading" class="stat-content">
              <div class="stat-summary">
                <div class="summary-item">
                  <div class="summary-label">总设备数</div>
                  <div class="summary-value">{{ usageStats?.totalEquipmentCount || 0 }}</div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">总使用次数</div>
                  <div class="summary-value">{{ usageStats?.totalUsageCount || 0 }}</div>
                </div>
              </div>
              <el-table :data="usageStats?.equipmentList || []" border stripe max-height="400">
                <el-table-column prop="equipmentName" label="设备名称" width="200" />
                <el-table-column prop="equipmentModel" label="型号" width="150" />
                <el-table-column prop="assetCode" label="资产编号" width="150" />
                <el-table-column prop="usageCount" label="使用次数" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.usageCount > 0 ? 'success' : 'info'">
                      {{ row.usageCount }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>

        <!-- 借用/逾期统计 -->
        <el-col :span="24" v-if="userStore.isAdmin() || userStore.isTeacher()">
          <el-card class="stat-card">
            <template #header>
              <div class="stat-header">
                <el-icon><DocumentAdd /></el-icon>
                <span>借用/逾期统计</span>
              </div>
            </template>
            <div v-loading="borrowStatsLoading" class="stat-content">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">总借用记录</div>
                    <div class="stat-number">{{ borrowStats?.totalBorrowCount || 0 }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">待审批</div>
                    <div class="stat-number warning">{{ borrowStats?.pendingCount || 0 }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">已通过</div>
                    <div class="stat-number success">{{ borrowStats?.approvedCount || 0 }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">已借出</div>
                    <div class="stat-number info">{{ borrowStats?.borrowedCount || 0 }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">已归还</div>
                    <div class="stat-number success">{{ borrowStats?.returnedCount || 0 }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">已逾期</div>
                    <div class="stat-number danger">{{ borrowStats?.overdueCount || 0 }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-box">
                    <div class="stat-label">已拒绝</div>
                    <div class="stat-number">{{ borrowStats?.rejectedCount || 0 }}</div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getEquipmentUsageStats, getBorrowStats } from '@/api/statistics'
import type { EquipmentUsageStatsResponse, BorrowStatsResponse } from '@/api/statistics'
import { Tools, DocumentAdd } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const usageStats = ref<EquipmentUsageStatsResponse | null>(null)
const usageLoading = ref(false)

const borrowStats = ref<BorrowStatsResponse | null>(null)
const borrowStatsLoading = ref(false)

// 加载设备使用统计
const loadUsageStats = async () => {
  if (!userStore.isAdmin() && !userStore.isTeacher()) {
    return
  }
  usageLoading.value = true
  try {
    const response = await getEquipmentUsageStats()
    if (response.code === 200 && response.data) {
      usageStats.value = response.data
    } else {
      ElMessage.error(response.message || '获取设备使用统计失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取设备使用统计失败')
  } finally {
    usageLoading.value = false
  }
}

// 加载借用统计
const loadBorrowStats = async () => {
  if (!userStore.isAdmin() && !userStore.isTeacher()) {
    return
  }
  borrowStatsLoading.value = true
  try {
    const response = await getBorrowStats()
    if (response.code === 200 && response.data) {
      borrowStats.value = response.data
    } else {
      ElMessage.error(response.message || '获取借用统计失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取借用统计失败')
  } finally {
    borrowStatsLoading.value = false
  }
}

onMounted(() => {
  loadUsageStats()
  loadBorrowStats()
})
</script>

<style scoped>
.statistics-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.stats-cards {
  margin-top: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.stat-content {
  min-height: 200px;
}

.stat-summary {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-box {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-number.success {
  color: #67c23a;
}

.stat-number.warning {
  color: #e6a23c;
}

.stat-number.danger {
  color: #f56c6c;
}

.stat-number.info {
  color: #409eff;
}
</style>

