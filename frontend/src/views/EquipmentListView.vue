<template>
  <div class="equipment-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-button type="primary" @click="handleAdd">新增设备</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.name" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="searchForm.model" placeholder="请输入设备型号" clearable />
        </el-form-item>
        <el-form-item label="所属实验室">
          <el-select v-model="searchForm.laboratoryId" placeholder="请选择实验室" clearable filterable>
            <el-option
              v-for="lab in laboratoryList"
              :key="lab.id"
              :label="lab.name"
              :value="lab.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="searchForm.statusCode" placeholder="请选择状态" clearable>
            <el-option
              v-for="status in statusList"
              :key="status.code"
              :label="status.name"
              :value="status.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="assetCode" label="资产编号" width="120" />
        <el-table-column prop="name" label="设备名称" width="200" />
        <el-table-column prop="model" label="型号" width="150" />
        <el-table-column prop="specification" label="规格" width="200" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.unitPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="laboratoryName" label="所属实验室" width="150" />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.statusCode)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row.id)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备型号" prop="model">
          <el-input v-model="formData.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="设备规格">
          <el-input v-model="formData.specification" placeholder="请输入设备规格" />
        </el-form-item>
        <el-form-item label="资产编号">
          <el-input v-model="formData.assetCode" placeholder="请输入资产编号" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="formData.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="formData.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="购置日期" prop="purchaseDate">
          <el-date-picker
            v-model="formData.purchaseDate"
            type="date"
            placeholder="请选择购置日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="保修期(月)">
          <el-input-number v-model="formData.warrantyPeriod" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属实验室" prop="laboratoryId">
          <el-select v-model="formData.laboratoryId" placeholder="请选择实验室" filterable style="width: 100%">
            <el-option
              v-for="lab in laboratoryList"
              :key="lab.id"
              :label="lab.name"
              :value="lab.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态" prop="statusId">
          <el-select v-model="formData.statusId" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="status in statusList"
              :key="status.id"
              :label="status.name"
              :value="status.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEquipmentList,
  createEquipment,
  updateEquipment,
  deleteEquipment,
  getEquipmentStatusList,
  type EquipmentItem,
  type CreateEquipmentRequest,
  type UpdateEquipmentRequest,
  type EquipmentStatus
} from '@/api/equipment'
import {
  getLaboratoryList,
  type LaboratoryItem
} from '@/api/laboratory'

// 搜索表单
const searchForm = reactive({
  name: '',
  model: '',
  laboratoryId: undefined as number | undefined,
  statusCode: ''
})

// 表格数据
const tableData = ref<EquipmentItem[]>([])
const loading = ref(false)

// 实验室列表
const laboratoryList = ref<LaboratoryItem[]>([])

// 状态列表
const statusList = ref<EquipmentStatus[]>([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const formRef = ref()
const submitting = ref(false)
type EquipmentFormModel = Omit<CreateEquipmentRequest, 'laboratoryId' | 'statusId'> & {
  id?: number
  laboratoryId?: number
  statusId?: number
}

const formData = reactive<EquipmentFormModel>({
  name: '',
  model: '',
  specification: '',
  assetCode: '',
  unitPrice: 0,
  quantity: 1,
  supplier: '',
  purchaseDate: '',
  warrantyPeriod: undefined,
  statusId: undefined,
  laboratoryId: undefined,
  description: ''
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  purchaseDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  statusId: [{ required: true, message: '请选择设备状态', trigger: 'change' }],
  laboratoryId: [{ required: true, message: '请选择所属实验室', trigger: 'change' }]
}

// 获取状态类型
const getStatusType = (code: string) => {
  const map: Record<string, 'danger' | 'success' | 'warning' | 'info'> = {
    pending: 'info',
    instored: 'info',
    inuse: 'success',
    repairing: 'warning',
    scrapped: 'danger'
  }
  return map[code] || 'info'
}

// 加载实验室列表
const loadLaboratoryList = async () => {
  try {
    const response = await getLaboratoryList({ page: 1, size: 1000 })
    if (response.code === 200 && response.data) {
      laboratoryList.value = (response.data.list || [])
        .slice()
        .sort((a, b) => (a.id || 0) - (b.id || 0))
    }
  } catch (error) {
    console.error('加载实验室列表失败', error)
  }
}

// 加载状态列表
const loadStatusList = async () => {
  try {
    const response = await getEquipmentStatusList()
    if (response.code === 200 && response.data) {
      statusList.value = response.data
      // 设置默认状态
      if (statusList.value.length > 0 && !formData.statusId) {
        const instoredStatus = statusList.value.find(s => s.code === 'instored')
        if (instoredStatus) {
          formData.statusId = instoredStatus.id
        }
      }
    }
  } catch (error) {
    console.error('加载状态列表失败', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await getEquipmentList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    if (response.code === 200 && response.data) {
      tableData.value = (response.data.list || []).slice().sort((a, b) => (b.id || 0) - (a.id || 0))
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.model = ''
  searchForm.laboratoryId = undefined
  searchForm.statusCode = ''
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增设备'
  Object.assign(formData, {
    id: undefined,
    name: '',
    model: '',
    specification: '',
    assetCode: '',
    unitPrice: 0,
    quantity: 1,
    supplier: '',
    purchaseDate: '',
    warrantyPeriod: undefined,
    statusId: statusList.value.find(s => s.code === 'instored')?.id,
    laboratoryId: undefined,
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: EquipmentItem) => {
  dialogTitle.value = '编辑设备'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    model: row.model,
    specification: row.specification || '',
    assetCode: row.assetCode || '',
    unitPrice: row.unitPrice,
    quantity: row.quantity,
    supplier: row.supplier || '',
    purchaseDate: row.purchaseDate,
    warrantyPeriod: row.warrantyPeriod,
    statusId: row.statusId,
    laboratoryId: row.laboratoryId,
    description: row.description || ''
  })
  dialogVisible.value = true
}

// 查看
const handleView = (id: number) => {
  // TODO: 跳转到详情页
  ElMessage.info('查看功能待实现')
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', {
      type: 'warning'
    })
    const response = await deleteEquipment(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        if (formData.id) {
          // 更新
          const { id, statusId, ...updateData } = formData
          const response = await updateEquipment(id, updateData as UpdateEquipmentRequest)
          if (response.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          } else {
            ElMessage.error(response.message || '更新失败')
          }
        } else {
          // 新增
          const response = await createEquipment(formData as CreateEquipmentRequest)
          if (response.code === 200) {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            loadData()
          } else {
            ElMessage.error(response.message || '创建失败')
          }
        }
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 分页变化
const handlePageChange = () => {
  loadData()
}

const handleSizeChange = () => {
  pagination.page = 1
  loadData()
}

// 初始化
onMounted(async () => {
  await loadLaboratoryList()
  await loadStatusList()
  loadData()
})
</script>

<style scoped>
.equipment-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .equipment-list-container {
    padding: 12px;
  }
}
</style>

