import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

export interface RepairItem {
  id: number
  equipmentId: number
  equipmentName?: string
  equipmentModel?: string
  equipmentAssetCode?: string
  reporterId: number
  reporterName?: string
  reportDate: string
  faultDescription: string
  repairResult?: string | null
  repairDate?: string | null
  status: number
  statusName?: string
  createTime: string
  updateTime: string
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface CreateRepairRequest {
  equipmentId: number
  reportDate: string
  faultDescription: string
}

export interface UpdateRepairStatusRequest {
  status: number // 0-待维修，1-维修中，2-已修好，3-无法修复
  repairResult?: string
  repairDate?: string
}

export interface RepairStatsResponse {
  totalCount: number
  pendingCount: number
  repairingCount: number
  fixedCount: number
  unrepairableCount: number
  repairRate: number
}

export const getRepairList = async (params?: {
  page?: number
  size?: number
  equipmentId?: number
  reporterId?: number
  status?: number
  reportDateStart?: string
  reportDateEnd?: string
  sortBy?: string
  sortOrder?: string
}): Promise<ApiResponse<PageResponse<RepairItem>>> => {
  const response = await request.get<ApiResponse<PageResponse<RepairItem>>>('/repair', { params })
  return response.data
}

export const getRepairDetail = async (id: number): Promise<ApiResponse<RepairItem>> => {
  const response = await request.get<ApiResponse<RepairItem>>(`/repair/${id}`)
  return response.data
}

export const createRepair = async (data: CreateRepairRequest): Promise<ApiResponse<RepairItem>> => {
  const response = await request.post<ApiResponse<RepairItem>>('/repair', data)
  return response.data
}

export const updateRepairStatus = async (id: number, data: UpdateRepairStatusRequest): Promise<ApiResponse<RepairItem>> => {
  const response = await request.put<ApiResponse<RepairItem>>(`/repair/${id}/status`, data)
  return response.data
}

export const getRepairStats = async (params?: {
  equipmentId?: number
  startDate?: string
  endDate?: string
}): Promise<ApiResponse<RepairStatsResponse>> => {
  const response = await request.get<ApiResponse<RepairStatsResponse>>('/repair/stats', { params })
  return response.data
}

