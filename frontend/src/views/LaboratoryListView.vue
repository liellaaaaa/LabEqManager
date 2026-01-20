<template>
  <div class="laboratory-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实验室管理</span>
          <el-button type="primary" @click="handleAdd">新增实验室</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="实验室名称">
          <el-input v-model="searchForm.name" placeholder="请输入实验室名称" clearable />
        </el-form-item>
        <el-form-item label="实验室编号">
          <el-input v-model="searchForm.code" placeholder="请输入实验室编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="不可用" :value="0" />
            <el-option label="可用" :value="1" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="实验室编号" width="120" />
        <el-table-column prop="name" label="实验室名称" width="200" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="负责人" width="120" />
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
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="实验室名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入实验室名称" />
        </el-form-item>
        <el-form-item label="实验室编号" prop="code">
          <el-input v-model="formData.code" placeholder="请输入实验室编号" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="面积(㎡)">
          <el-input-number v-model="formData.area" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="容纳人数">
          <el-input-number v-model="formData.capacity" :min="0" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="formData.type" placeholder="请输入类型" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="不可用" :value="0" />
            <el-option label="可用" :value="1" />
            <el-option label="维护中" :value="2" />
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
  getLaboratoryList,
  createLaboratory,
  updateLaboratory,
  deleteLaboratory,
  type LaboratoryItem,
  type CreateLaboratoryRequest,
  type UpdateLaboratoryRequest
} from '@/api/laboratory'

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  status: undefined as number | undefined
})

// 表格数据
const tableData = ref<LaboratoryItem[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增实验室')
const formRef = ref()
const submitting = ref(false)
const formData = reactive<CreateLaboratoryRequest & { id?: number }>({
  name: '',
  code: '',
  location: '',
  area: undefined,
  capacity: undefined,
  type: '',
  status: 1,
  managerId: undefined,
  description: ''
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入实验室名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入实验室编号', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

// 获取状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '不可用', 1: '可用', 2: '维护中' }
  return map[status] || '未知'
}

// 获取状态类型
const getStatusType = (status: number) => {
  const map: Record<number, 'danger' | 'success' | 'warning'> = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return map[status] || 'info'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await getLaboratoryList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    if (response.code === 200 && response.data) {
      tableData.value = response.data.list
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
  searchForm.code = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增实验室'
  Object.assign(formData, {
    name: '',
    code: '',
    location: '',
    area: undefined,
    capacity: undefined,
    type: '',
    status: 1,
    managerId: undefined,
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: LaboratoryItem) => {
  dialogTitle.value = '编辑实验室'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    location: row.location,
    area: row.area,
    capacity: row.capacity,
    type: row.type || '',
    status: row.status,
    managerId: row.managerId,
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
    await ElMessageBox.confirm('确定要删除该实验室吗？', '提示', {
      type: 'warning'
    })
    const response = await deleteLaboratory(id)
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
          const { id, ...updateData } = formData
          const response = await updateLaboratory(id, updateData as UpdateLaboratoryRequest)
          if (response.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          } else {
            ElMessage.error(response.message || '更新失败')
          }
        } else {
          // 新增
          const response = await createLaboratory(formData as CreateLaboratoryRequest)
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
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.laboratory-list-container {
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
</style>

