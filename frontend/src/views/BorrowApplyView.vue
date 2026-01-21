<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createBorrow, getAvailableQuantity, type AvailableQuantityResponse } from '@/api/borrow'

const toInputValue = (date: Date) => date.toISOString().slice(0, 16)

const form = ref({
  equipmentId: undefined as number | undefined,
  borrowDate: toInputValue(new Date(Date.now() + 60 * 60 * 1000)),
  planReturnDate: toInputValue(new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)),
  quantity: 1,
  purpose: '教学/实验使用',
})

const submitting = ref(false)
const availableInfo = ref<AvailableQuantityResponse | null>(null)

const fetchAvailable = async () => {
  if (!form.value.equipmentId) {
    availableInfo.value = null
    return
  }
  try {
    const resp = await getAvailableQuantity(form.value.equipmentId)
    availableInfo.value = resp.data
  } catch {
    availableInfo.value = null
  }
}

watch(
  () => form.value.equipmentId,
  () => fetchAvailable()
)

const onSubmit = async () => {
  if (!form.value.equipmentId) {
    ElMessage.warning('请填写设备ID')
    return
  }
  submitting.value = true
  try {
    const payload = {
      equipmentId: form.value.equipmentId,
      borrowDate: new Date(form.value.borrowDate).toISOString(),
      planReturnDate: new Date(form.value.planReturnDate).toISOString(),
      purpose: form.value.purpose,
      quantity: form.value.quantity || 1,
    }
    await createBorrow(payload)
    ElMessage.success('提交成功，等待审批')
    await fetchAvailable()
  } catch (e: any) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <h2>申请借用</h2>
    <el-form label-width="120px" :model="form" class="form">
      <el-form-item label="设备ID">
        <el-input-number v-model="form.equipmentId" :min="1" />
        <span class="tip">请先在设备列表查询设备 ID</span>
      </el-form-item>
      <el-form-item label="借用时间">
        <el-date-picker
          v-model="form.borrowDate"
          type="datetime"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm"
          placeholder="选择借用时间"
        />
      </el-form-item>
      <el-form-item label="计划归还时间">
        <el-date-picker
          v-model="form.planReturnDate"
          type="datetime"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm"
          placeholder="选择计划归还时间"
        />
      </el-form-item>
      <el-form-item label="数量">
        <el-input-number v-model="form.quantity" :min="1" />
        <span v-if="availableInfo" class="tip">
          总量 {{ availableInfo.totalQuantity }}，已借出 {{ availableInfo.borrowedQuantity }}，可用 {{ availableInfo.availableQuantity }}
        </span>
      </el-form-item>
      <el-form-item label="用途">
        <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="请输入用途" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
.form {
  max-width: 520px;
}
.tip {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}
</style>

