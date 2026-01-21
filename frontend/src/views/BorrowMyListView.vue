<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBorrowList, returnBorrow, type BorrowItem } from '@/api/borrow'

const loading = ref(false)
const tableData = ref<BorrowItem[]>([])
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
  { label: '已借出', value: 3 },
  { label: '已归还', value: 4 },
  { label: '已逾期', value: 5 },
]

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getBorrowList({
      page: pagination.page,
      size: pagination.size,
      status: filters.status,
    })
    tableData.value = resp.data?.list || []
    pagination.total = resp.data?.total || 0
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onReturn = async (row: BorrowItem) => {
  try {
    await ElMessageBox.confirm('确认归还该设备吗？', '提示', { type: 'warning' })
    await returnBorrow(row.id, { actualReturnDate: new Date().toISOString(), remark: '归还确认' })
    ElMessage.success('归还成功')
    fetchList()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
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
      <el-table-column prop="equipmentName" label="设备" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="borrowDate" label="借用时间" width="170" />
      <el-table-column prop="planReturnDate" label="计划归还" width="170" />
      <el-table-column prop="actualReturnDate" label="实际归还" width="170" />
      <el-table-column prop="purpose" label="用途" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button
            v-if="[3, 5].includes(row.status)"
            type="primary"
            size="small"
            @click="onReturn(row)"
          >
            归还
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
}
.pager {
  margin-top: 12px;
  text-align: right;
}
</style>

