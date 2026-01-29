import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

// 实验室列表项
export interface LaboratoryItem {
  id: number
  name: string
  code: string
  location: string
  capacity: number | null
  type: string | null
  status: number
  managerId: number | null
  managerName: string | null
  description: string | null
  createTime: string
  updateTime: string
}

// 实验室详情
export interface LaboratoryDetail extends LaboratoryItem {
  equipmentCount: number
}

// 分页响应
export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 创建实验室请求
export interface CreateLaboratoryRequest {
  name: string
  code: string
  location: string
  capacity?: number
  type?: string
  status?: number
  managerId?: number
  description?: string
}

// 更新实验室请求
export interface UpdateLaboratoryRequest {
  name?: string
  code?: string
  location?: string
  capacity?: number
  type?: string
  managerId?: number
  description?: string
}

// 更新状态请求
export interface UpdateStatusRequest {
  status: number
}

// 批量删除请求
export interface BatchDeleteRequest {
  ids: number[]
}

// 获取实验室列表
export const getLaboratoryList = async (params?: {
  page?: number
  size?: number
  name?: string
  code?: string
  location?: string
  type?: string
  status?: number
  managerId?: number
  sortBy?: string
  sortOrder?: string
}): Promise<ApiResponse<PageResponse<LaboratoryItem>>> => {
  const response = await request.get<ApiResponse<PageResponse<LaboratoryItem>>>('/laboratory', { params })
  return response.data
}

// 获取实验室详情
export const getLaboratoryById = async (id: number): Promise<ApiResponse<LaboratoryDetail>> => {
  const response = await request.get<ApiResponse<LaboratoryDetail>>(`/laboratory/${id}`)
  return response.data
}

// 创建实验室
export const createLaboratory = async (data: CreateLaboratoryRequest): Promise<ApiResponse<LaboratoryItem>> => {
  const response = await request.post<ApiResponse<LaboratoryItem>>('/laboratory', data)
  return response.data
}

// 更新实验室
export const updateLaboratory = async (id: number, data: UpdateLaboratoryRequest): Promise<ApiResponse<LaboratoryItem>> => {
  const response = await request.put<ApiResponse<LaboratoryItem>>(`/laboratory/${id}`, data)
  return response.data
}

// 删除实验室
export const deleteLaboratory = async (id: number): Promise<ApiResponse<null>> => {
  const response = await request.delete<ApiResponse<null>>(`/laboratory/${id}`)
  return response.data
}

// 批量删除实验室
export const batchDeleteLaboratories = async (data: BatchDeleteRequest): Promise<ApiResponse<null>> => {
  const response = await request.delete<ApiResponse<null>>('/laboratory/batch', { data })
  return response.data
}

// 更新实验室状态
export const updateLaboratoryStatus = async (id: number, data: UpdateStatusRequest): Promise<ApiResponse<LaboratoryItem>> => {
  const response = await request.put<ApiResponse<LaboratoryItem>>(`/laboratory/${id}/status`, data)
  return response.data
}

// 获取实验室设备列表
export const getLaboratoryEquipment = async (id: number, params?: {
  page?: number
  size?: number
  statusCode?: string
}): Promise<ApiResponse<PageResponse<any>>> => {
  const response = await request.get<ApiResponse<PageResponse<any>>>(`/laboratory/${id}/equipment`, { params })
  return response.data
}

