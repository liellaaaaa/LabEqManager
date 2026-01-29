<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveBorrow, confirmBorrow, getBorrowList, markOverdue, type BorrowItem } from '@/api/borrow'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(false)
const tableData = ref<BorrowItem[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})
const filters = reactive({
  status: 0 as number | undefined, // 默认看待审批
})

const statusOptions = [
  { label: '待审批', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 },
  { label: '已借出', value: 3 },
  { label: '已归还', value: 4 },
  { label: '已逾期', value: 5 },
  { label: '全部', value: undefined },
]

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getBorrowList({
      page: pagination.page,
      size: pagination.size,
      status: filters.status,
      sortBy: 'createTime',
      sortOrder: 'desc',
    })
    tableData.value = (resp.data?.list || []).slice().sort((a, b) => (b.id || 0) - (a.id || 0))
    pagination.total = resp.data?.total || 0
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const doApprove = async (row: BorrowItem, pass: boolean) => {
  await approveBorrow(row.id, { status: pass ? 1 : 2, remark: pass ? '审批通过' : '审批拒绝' })
  ElMessage.success(pass ? '已通过' : '已拒绝')
  fetchList()
}

const doConfirmBorrow = async (row: BorrowItem) => {
  await confirmBorrow(row.id, { borrowDate: new Date().toISOString() })
  ElMessage.success('已确认借出')
  fetchList()
}

const onMarkOverdue = async () => {
  await ElMessageBox.confirm('将标记所有“已借出且超过计划归还时间”的记录为逾期，确定执行？', '提示', { type: 'warning' })
  const resp = await markOverdue()
  ElMessage.success(`已标记逾期 ${resp.data?.overdueCount || 0} 条`)
  fetchList()
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

onMounted(() => {
  // 如果通过菜单 anchor 进入逾期标记，保持列表刷新
  if (route.hash === '#overdue') {
    filters.status = 3
  }
  fetchList()
})
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="filters.status" placeholder="选择状态" clearable @change="fetchList">
        <el-option v-for="opt in statusOptions" :key="opt.label" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" @click="fetchList">刷新</el-button>
      <el-button type="warning" plain @click="onMarkOverdue">标记逾期</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="equipmentName" label="设备" />
      <el-table-column prop="userName" label="借用人" width="120" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="borrowDate" label="借用时间" width="170" />
      <el-table-column prop="planReturnDate" label="计划归还" width="170" />
      <el-table-column prop="approveRemark" label="备注" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="success"
            size="small"
            @click="doApprove(row, true)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="danger"
            size="small"
            @click="doApprove(row, false)"
          >
            拒绝
          </el-button>
          <el-button
            v-if="row.status === 1"
            type="primary"
            size="small"
            @click="doConfirmBorrow(row)"
          >
            确认借出
          </el-button>
          <span v-if="![0, 1].includes(row.status)" style="color: #999">-</span>
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

