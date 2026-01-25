<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getRepairList, type RepairItem } from '@/api/repair'

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

const fetchList = async () => {
  loading.value = true
  try {
    const resp = await getRepairList({
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
    <h2>我的维修记录</h2>
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
      <el-table-column prop="reportDate" label="报修日期" width="170" />
      <el-table-column prop="faultDescription" label="故障描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="repairResult" label="维修结果" min-width="200" show-overflow-tooltip />
      <el-table-column prop="repairDate" label="维修日期" width="170" />
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

