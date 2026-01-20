import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

// 设备列表项
export interface EquipmentItem {
  id: number
  name: string
  model: string
  specification: string | null
  assetCode: string | null
  unitPrice: number
  quantity: number
  supplier: string | null
  purchaseDate: string
  warrantyPeriod: number | null
  statusId: number
  statusName: string
  statusCode: string
  laboratoryId: number
  laboratoryName: string
  description: string | null
  createTime: string
  updateTime: string
}

// 设备详情
export interface EquipmentDetail extends EquipmentItem {
}

// 分页响应
export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 设备状态
export interface EquipmentStatus {
  id: number
  name: string
  code: string
  description: string | null
}

// 创建设备请求
export interface CreateEquipmentRequest {
  name: string
  model: string
  specification?: string
  assetCode?: string
  unitPrice: number
  quantity?: number
  supplier?: string
  purchaseDate: string
  warrantyPeriod?: number
  statusId: number
  laboratoryId: number
  description?: string
}

// 更新设备请求
export interface UpdateEquipmentRequest {
  name?: string
  model?: string
  specification?: string
  assetCode?: string
  unitPrice?: number
  quantity?: number
  supplier?: string
  purchaseDate?: string
  warrantyPeriod?: number
  laboratoryId?: number
  description?: string
}

// 更新设备状态请求
export interface UpdateEquipmentStatusRequest {
  statusId: number
}

// 批量删除请求
export interface BatchDeleteRequest {
  ids: number[]
}

// 获取设备列表
export const getEquipmentList = async (params?: {
  page?: number
  size?: number
  name?: string
  model?: string
  specification?: string
  assetCode?: string
  supplier?: string
  statusCode?: string
  laboratoryId?: number
  purchaseDateStart?: string
  purchaseDateEnd?: string
  sortBy?: string
  sortOrder?: string
}): Promise<ApiResponse<PageResponse<EquipmentItem>>> => {
  const response = await request.get<ApiResponse<PageResponse<EquipmentItem>>>('/equipment', { params })
  return response.data
}

// 获取设备详情
export const getEquipmentById = async (id: number): Promise<ApiResponse<EquipmentDetail>> => {
  const response = await request.get<ApiResponse<EquipmentDetail>>(`/equipment/${id}`)
  return response.data
}

// 创建设备
export const createEquipment = async (data: CreateEquipmentRequest): Promise<ApiResponse<EquipmentItem>> => {
  const response = await request.post<ApiResponse<EquipmentItem>>('/equipment', data)
  return response.data
}

// 更新设备
export const updateEquipment = async (id: number, data: UpdateEquipmentRequest): Promise<ApiResponse<EquipmentItem>> => {
  const response = await request.put<ApiResponse<EquipmentItem>>(`/equipment/${id}`, data)
  return response.data
}

// 删除设备
export const deleteEquipment = async (id: number): Promise<ApiResponse<null>> => {
  const response = await request.delete<ApiResponse<null>>(`/equipment/${id}`)
  return response.data
}

// 批量删除设备
export const batchDeleteEquipment = async (data: BatchDeleteRequest): Promise<ApiResponse<null>> => {
  const response = await request.delete<ApiResponse<null>>('/equipment/batch', { data })
  return response.data
}

// 更新设备状态
export const updateEquipmentStatus = async (id: number, data: UpdateEquipmentStatusRequest): Promise<ApiResponse<EquipmentItem>> => {
  const response = await request.put<ApiResponse<EquipmentItem>>(`/equipment/${id}/status`, data)
  return response.data
}

// 获取设备状态列表
export const getEquipmentStatusList = async (): Promise<ApiResponse<EquipmentStatus[]>> => {
  const response = await request.get<ApiResponse<EquipmentStatus[]>>('/equipment/status')
  return response.data
}

