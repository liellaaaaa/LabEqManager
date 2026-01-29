<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveScrap, getScrapList, type ScrapItem, type ApproveScrapRequest } from '@/api/scrap'

const loading = ref(false)
const tableData = ref<ScrapItem[]>([])
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
  { label: '全部', value: undefined },
]

const dialogVisible = ref(false)
const currentScrap = ref<ScrapItem | null>(null)
const approvalForm = ref<ApproveScrapRequest>({
  status: 1,
  remark: '',
})

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getScrapList({
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

const openApprovalDialog = (row: ScrapItem, pass: boolean) => {
  currentScrap.value = row
  approvalForm.value = {
    status: pass ? 1 : 2,
    remark: '',
  }
  dialogVisible.value = true
}

const handleApproval = async () => {
  if (!currentScrap.value) return

  try {
    await approveScrap(currentScrap.value.id, approvalForm.value)
    ElMessage.success(approvalForm.value.status === 1 ? '审批通过' : '审批拒绝')
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
    <h2>报废审批</h2>
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
      <el-table-column prop="applicantName" label="申请人" width="120" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="applyDate" label="申请日期" width="170" />
      <el-table-column prop="scrapReason" label="报废原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="approverName" label="审批人" width="120" />
      <el-table-column prop="approveTime" label="审批时间" width="170" />
      <el-table-column prop="approveRemark" label="审批备注" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="success"
            size="small"
            @click="openApprovalDialog(row, true)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="danger"
            size="small"
            @click="openApprovalDialog(row, false)"
          >
            拒绝
          </el-button>
          <span v-if="row.status !== 0" style="color: #999">-</span>
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

    <!-- 审批对话框 -->
    <el-dialog v-model="dialogVisible" :title="approvalForm.status === 1 ? '审批通过' : '审批拒绝'" width="600px">
      <el-form label-width="120px">
        <el-form-item label="审批结果">
          <el-tag :type="approvalForm.status === 1 ? 'success' : 'danger'">
            {{ approvalForm.status === 1 ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审批备注">
          <el-input
            v-model="approvalForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审批备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApproval">确定</el-button>
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

