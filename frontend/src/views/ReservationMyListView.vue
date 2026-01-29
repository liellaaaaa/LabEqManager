<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservationList, cancelReservation, type ReservationItem } from '@/api/reservation'

const loading = ref(false)
const tableData = ref<ReservationItem[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})
const filters = reactive({
  status: undefined as number | undefined,
})

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '待审批', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已使用', value: 5 },
]

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getReservationList({
      page: pagination.page,
      size: pagination.size,
      status: filters.status,
    })
    tableData.value = (resp.data?.list || []).slice().sort((a, b) => (b.id || 0) - (a.id || 0))
    pagination.total = resp.data?.total || 0
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onCancel = async (row: ReservationItem) => {
  try {
    await ElMessageBox.confirm('确认取消该预约吗？', '提示', { type: 'warning' })
    await cancelReservation(row.id, { remark: '用户取消' })
    ElMessage.success('取消成功')
    fetchList()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const formatDateTime = (date: string, time: string) => {
  if (!date || !time) return '-'
  return `${date} ${time}`
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="filters.status" placeholder="选择状态" clearable @change="fetchList">
        <el-option v-for="opt in statusOptions" :key="opt.label" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="laboratoryName" label="实验室" />
      <el-table-column prop="laboratoryCode" label="实验室编号" width="120" />
      <el-table-column label="预约时间" width="200">
        <template #default="{ row }">
          {{ formatDateTime(row.reserveDate, row.startTime) }} - {{ row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="purpose" label="预约目的" />
      <el-table-column prop="approveRemark" label="备注" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button
            v-if="[0, 1].includes(row.status)"
            type="warning"
            size="small"
            @click="onCancel(row)"
          >
            取消
          </el-button>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        :page-size="pagination.size"
        :current-page="pagination.page"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.pager {
  margin-top: 12px;
  text-align: right;
}

@media (max-width: 768px) {
  .page {
    padding: 12px;
  }
}
</style>

