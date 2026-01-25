<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createRepair, type CreateRepairRequest } from '@/api/repair'

const toInputValue = (date: Date) => date.toISOString().slice(0, 16)

const form = ref({
  equipmentId: undefined as number | undefined,
  reportDate: toInputValue(new Date()),
  faultDescription: '',
})

const submitting = ref(false)

const onSubmit = async () => {
  if (!form.value.equipmentId) {
    ElMessage.warning('请填写设备ID')
    return
  }
  if (!form.value.faultDescription || form.value.faultDescription.trim() === '') {
    ElMessage.warning('请填写故障描述')
    return
  }
  submitting.value = true
  try {
    const payload: CreateRepairRequest = {
      equipmentId: form.value.equipmentId!,
      reportDate: new Date(form.value.reportDate).toISOString(),
      faultDescription: form.value.faultDescription.trim(),
    }
    await createRepair(payload)
    ElMessage.success('维修申请提交成功')
    // 重置表单
    form.value = {
      equipmentId: undefined,
      reportDate: toInputValue(new Date()),
      faultDescription: '',
    }
  } catch (e: any) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <h2>设备维修申请</h2>
    <el-form label-width="120px" :model="form" class="form">
      <el-form-item label="设备ID">
        <el-input-number v-model="form.equipmentId" :min="1" />
        <span class="tip">请先在设备列表查询设备 ID</span>
      </el-form-item>
      <el-form-item label="报修日期">
        <el-date-picker
          v-model="form.reportDate"
          type="datetime"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm"
          placeholder="选择报修日期"
        />
      </el-form-item>
      <el-form-item label="故障描述">
        <el-input
          v-model="form.faultDescription"
          type="textarea"
          :rows="5"
          placeholder="请详细描述设备故障情况"
        />
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

