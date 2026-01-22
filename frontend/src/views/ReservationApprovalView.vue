<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveReservation, completeReservation, getReservationList, type ReservationItem } from '@/api/reservation'

const loading = ref(false)
const tableData = ref<ReservationItem[]>([])
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
  { label: '已取消', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已使用', value: 5 },
  { label: '全部', value: undefined },
]

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getReservationList({
      page: pagination.page,
      size: pagination.size,
      status: filters.status,
      sortBy: 'createTime',
      sortOrder: 'desc',
    })
    tableData.value = resp.data?.list || []
    pagination.total = resp.data?.total || 0
  } catch (e: any) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const doApprove = async (row: ReservationItem, pass: boolean) => {
  try {
    await approveReservation(row.id, {
      status: pass ? 1 : 2,
      remark: pass ? '审批通过' : '审批拒绝',
    })
    ElMessage.success(pass ? '已通过' : '已拒绝')
    fetchList()
  } catch (e: any) {
    console.error(e)
  }
}

const doComplete = async (row: ReservationItem) => {
  try {
    await ElMessageBox.prompt('请输入使用备注（可选）', '标记完成', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '使用备注',
    })
      .then(async ({ value }) => {
        await completeReservation(row.id, {
          usageRemark: value || undefined,
        })
        ElMessage.success('已标记为完成')
        fetchList()
      })
      .catch(() => {
        // 用户取消
      })
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

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="laboratoryName" label="实验室" />
      <el-table-column prop="laboratoryCode" label="实验室编号" width="120" />
      <el-table-column prop="userName" label="预约人" width="120" />
      <el-table-column label="预约时间" width="200">
        <template #default="{ row }">
          {{ formatDateTime(row.reserveDate, row.startTime) }} - {{ row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="purpose" label="预约目的" />
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
            @click="doComplete(row)"
          >
            标记完成
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
}
.pager {
  margin-top: 12px;
  text-align: right;
}
</style>

