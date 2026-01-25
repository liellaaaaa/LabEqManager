import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

// 设备使用统计项
export interface EquipmentUsageItem {
  equipmentId: number
  equipmentName: string
  equipmentModel: string
  assetCode: string | null
  usageCount: number
}

// 设备使用统计响应
export interface EquipmentUsageStatsResponse {
  equipmentList: EquipmentUsageItem[]
  totalEquipmentCount: number
  totalUsageCount: number
}

// 借用/逾期统计响应
export interface BorrowStatsResponse {
  totalBorrowCount: number
  pendingCount: number
  approvedCount: number
  borrowedCount: number
  returnedCount: number
  overdueCount: number
  rejectedCount: number
}

// 借用提醒项
export interface BorrowReminderItem {
  id: number
  equipmentName: string
  planReturnDate: string
  remainingDays: number
  isOverdue: boolean
}

// 预约提醒项
export interface ReservationReminderItem {
  id: number
  laboratoryName: string
  reserveDate: string
  startTime: string
  endTime: string
  remainingDays: number
  isExpired: boolean
}

// 到期提醒响应
export interface ReminderResponse {
  hasReminder: boolean
  reminderCount: number
  borrowReminders: BorrowReminderItem[]
  reservationReminders: ReservationReminderItem[]
}

// 获取设备使用次数统计
export const getEquipmentUsageStats = async (): Promise<ApiResponse<EquipmentUsageStatsResponse>> => {
  const response = await request.get<ApiResponse<EquipmentUsageStatsResponse>>('/statistics/equipment-usage')
  return response.data
}

// 获取借用/逾期统计
export const getBorrowStats = async (): Promise<ApiResponse<BorrowStatsResponse>> => {
  const response = await request.get<ApiResponse<BorrowStatsResponse>>('/statistics/borrow-stats')
  return response.data
}

// 获取到期提醒
export const getReminders = async (): Promise<ApiResponse<ReminderResponse>> => {
  const response = await request.get<ApiResponse<ReminderResponse>>('/statistics/reminders')
  return response.data
}

