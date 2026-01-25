<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getScrapList, type ScrapItem } from '@/api/scrap'

const loading = ref(false)
const tableData = ref<ScrapItem[]>([])
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
]

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getScrapList({
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
    <h2>我的报废记录</h2>
    <div class="toolbar">
      <el-select v-model="filters.status" placeholder="选择状态" clearable @change="fetchList">
        <el-option v-for="opt in statusOptions" :key="opt.label" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" @click="fetchList">刷新</el-button>
    </div>

    <el-table :data="tableData" style="width: 100%" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="equipmentName" label="设备名称" />
      <el-table-column prop="equipmentModel" label="设备型号" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="applyDate" label="申请日期" width="170" />
      <el-table-column prop="scrapReason" label="报废原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="approverName" label="审批人" width="120" />
      <el-table-column prop="approveTime" label="审批时间" width="170" />
      <el-table-column prop="approveRemark" label="审批备注" min-width="200" show-overflow-tooltip />
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

