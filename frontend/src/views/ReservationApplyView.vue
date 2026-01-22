<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createReservation, checkConflict, getAvailableTime, type CreateReservationRequest } from '@/api/reservation'
import { getLaboratoryList, type LaboratoryItem } from '@/api/laboratory'

const form = ref<CreateReservationRequest>({
  laboratoryId: undefined as number | undefined,
  reserveDate: new Date().toISOString().split('T')[0],
  startTime: '09:00:00',
  endTime: '11:00:00',
  purpose: '课程实验',
})

const submitting = ref(false)
const laboratories = ref<LaboratoryItem[]>([])
const availableTimeSlots = ref<Array<{ startTime: string; endTime: string }>>([])
const conflictInfo = ref<string>('')

const fetchLaboratories = async () => {
  try {
    const resp = await getLaboratoryList({ status: 1, size: 100 }) // 只获取可用实验室
    laboratories.value = resp.data?.list || []
  } catch (e: any) {
    console.error(e)
  }
}

const fetchAvailableTime = async () => {
  if (!form.value.laboratoryId || !form.value.reserveDate) {
    availableTimeSlots.value = []
    return
  }
  try {
    const resp = await getAvailableTime(form.value.laboratoryId, form.value.reserveDate)
    availableTimeSlots.value = resp.data?.availableTimeSlots || []
  } catch (e: any) {
    console.error(e)
    availableTimeSlots.value = []
  }
}

const checkTimeConflict = async () => {
  if (!form.value.laboratoryId || !form.value.reserveDate || !form.value.startTime || !form.value.endTime) {
    conflictInfo.value = ''
    return
  }
  try {
    const resp = await checkConflict({
      laboratoryId: form.value.laboratoryId,
      reserveDate: form.value.reserveDate,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
    })
    if (resp.data?.hasConflict) {
      conflictInfo.value = '该时间段已被预约，请选择其他时间'
    } else {
      conflictInfo.value = ''
    }
  } catch (e: any) {
    conflictInfo.value = ''
  }
}

watch(
  () => [form.value.laboratoryId, form.value.reserveDate],
  () => {
    fetchAvailableTime()
    checkTimeConflict()
  }
)

watch(
  () => [form.value.startTime, form.value.endTime],
  () => {
    checkTimeConflict()
  }
)

const onSubmit = async () => {
  if (!form.value.laboratoryId) {
    ElMessage.warning('请选择实验室')
    return
  }
  if (!form.value.reserveDate) {
    ElMessage.warning('请选择预约日期')
    return
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择时间段')
    return
  }
  if (form.value.endTime <= form.value.startTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  if (conflictInfo.value) {
    ElMessage.warning(conflictInfo.value)
    return
  }

  submitting.value = true
  try {
    await createReservation(form.value)
    ElMessage.success('预约申请提交成功，等待审批')
    // 重置表单
    form.value = {
      laboratoryId: undefined,
      reserveDate: new Date().toISOString().split('T')[0],
      startTime: '09:00:00',
      endTime: '11:00:00',
      purpose: '课程实验',
    }
    conflictInfo.value = ''
    availableTimeSlots.value = []
  } catch (e: any) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

fetchLaboratories()
</script>

<template>
  <div class="page">
    <h2>申请预约</h2>
    <el-form label-width="120px" :model="form" class="form">
      <el-form-item label="实验室" required>
        <el-select v-model="form.laboratoryId" placeholder="请选择实验室" style="width: 100%">
          <el-option
            v-for="lab in laboratories"
            :key="lab.id"
            :label="`${lab.name} (${lab.code})`"
            :value="lab.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="预约日期" required>
        <el-date-picker
          v-model="form.reserveDate"
          type="date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          placeholder="选择预约日期"
          :disabled-date="(date: Date) => date < new Date(new Date().setHours(0, 0, 0, 0))"
        />
      </el-form-item>
      <el-form-item label="开始时间" required>
        <el-time-picker
          v-model="form.startTime"
          format="HH:mm:ss"
          value-format="HH:mm:ss"
          placeholder="选择开始时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间" required>
        <el-time-picker
          v-model="form.endTime"
          format="HH:mm:ss"
          value-format="HH:mm:ss"
          placeholder="选择结束时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item v-if="conflictInfo" label="提示">
        <span style="color: #f56c6c">{{ conflictInfo }}</span>
      </el-form-item>
      <el-form-item v-if="availableTimeSlots.length > 0" label="可用时间段">
        <div class="time-slots">
          <el-tag
            v-for="(slot, index) in availableTimeSlots"
            :key="index"
            type="success"
            style="margin-right: 8px; margin-bottom: 8px"
          >
            {{ slot.startTime }} - {{ slot.endTime }}
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item label="预约目的" required>
        <el-input v-model="form.purpose" type="textarea" :rows="3" placeholder="请输入预约目的" />
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
  max-width: 600px;
}
.tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}
.time-slots {
  display: flex;
  flex-wrap: wrap;
}
</style>

