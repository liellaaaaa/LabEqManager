<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairList, updateRepairStatus, getRepairStats, type RepairItem, type UpdateRepairStatusRequest } from '@/api/repair'

const loading = ref(false)
const tableData = ref<RepairItem[]>([])
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
  { label: '待维修', value: 0 },
  { label: '维修中', value: 1 },
  { label: '已修好', value: 2 },
  { label: '无法修复', value: 3 },
]

const dialogVisible = ref(false)
const currentRepair = ref<RepairItem | null>(null)
const statusForm = ref<UpdateRepairStatusRequest>({
  status: 0,
  repairResult: '',
  repairDate: '',
})

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getRepairList({
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

const openStatusDialog = (row: RepairItem) => {
  currentRepair.value = row
  statusForm.value = {
    status: row.status,
    repairResult: row.repairResult || '',
    repairDate: row.repairDate ? new Date(row.repairDate).toISOString().slice(0, 16) : '',
  }
  dialogVisible.value = true
}

const handleStatusUpdate = async () => {
  if (!currentRepair.value) return

  // 验证：当状态为2或3时，维修结果和维修日期必填
  if ((statusForm.value.status === 2 || statusForm.value.status === 3)) {
    if (!statusForm.value.repairResult || statusForm.value.repairResult.trim() === '') {
      ElMessage.warning('当状态为"已修好"或"无法修复"时，维修结果不能为空')
      return
    }
    if (!statusForm.value.repairDate) {
      ElMessage.warning('当状态为"已修好"或"无法修复"时，维修日期不能为空')
      return
    }
  }

  try {
    const payload: UpdateRepairStatusRequest = {
      status: statusForm.value.status,
    }
    if (statusForm.value.repairResult) {
      payload.repairResult = statusForm.value.repairResult.trim()
    }
    if (statusForm.value.repairDate) {
      payload.repairDate = new Date(statusForm.value.repairDate).toISOString()
    }
    await updateRepairStatus(currentRepair.value.id, payload)
    ElMessage.success('状态更新成功')
    dialogVisible.value = false
    fetchList()
  } catch (e: any) {
    console.error(e)
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
    <h2>维修管理</h2>
    <div class="toolbar">
      <el-select v-model="filters.status" placeholder="选择状态" clearable @change="fetchList">
        <el-option v-for="opt in statusOptions" :key="opt.label" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="equipmentName" label="设备名称" />
      <el-table-column prop="equipmentModel" label="设备型号" />
      <el-table-column prop="reporterName" label="报修人" width="120" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="reportDate" label="报修日期" width="170" />
      <el-table-column prop="faultDescription" label="故障描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="repairResult" label="维修结果" min-width="200" show-overflow-tooltip />
      <el-table-column prop="repairDate" label="维修日期" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openStatusDialog(row)">更新状态</el-button>
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

    <!-- 状态更新对话框 -->
    <el-dialog v-model="dialogVisible" title="更新维修状态" width="600px">
      <el-form label-width="120px">
        <el-form-item label="维修状态">
          <el-select v-model="statusForm.status" style="width: 100%">
            <el-option label="待维修" :value="0" />
            <el-option label="维修中" :value="1" />
            <el-option label="已修好" :value="2" />
            <el-option label="无法修复" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修结果" :required="statusForm.status === 2 || statusForm.status === 3">
          <el-input
            v-model="statusForm.repairResult"
            type="textarea"
            :rows="4"
            placeholder="请输入维修结果"
          />
        </el-form-item>
        <el-form-item label="维修日期" :required="statusForm.status === 2 || statusForm.status === 3">
          <el-date-picker
            v-model="statusForm.repairDate"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm"
            placeholder="选择维修日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusUpdate">确定</el-button>
      </template>
    </el-dialog>
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

