import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

export interface ScrapItem {
  id: number
  equipmentId: number
  equipmentName?: string
  equipmentModel?: string
  equipmentAssetCode?: string
  applicantId: number
  applicantName?: string
  applyDate: string
  scrapReason: string
  status: number
  statusName?: string
  approverId?: number | null
  approverName?: string | null
  approveTime?: string | null
  approveRemark?: string | null
  createTime: string
  updateTime: string
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface CreateScrapRequest {
  equipmentId: number
  applyDate: string
  scrapReason: string
}

export interface ApproveScrapRequest {
  status: number // 1-通过，2-拒绝
  remark?: string
}

export interface ScrapStatsResponse {
  totalCount: number
  pendingCount: number
  approvedCount: number
  rejectedCount: number
  approvalRate: number
}

export const getScrapList = async (params?: {
  page?: number
  size?: number
  equipmentId?: number
  applicantId?: number
  status?: number
  applyDateStart?: string
  applyDateEnd?: string
  sortBy?: string
  sortOrder?: string
}): Promise<ApiResponse<PageResponse<ScrapItem>>> => {
  const response = await request.get<ApiResponse<PageResponse<ScrapItem>>>('/scrap', { params })
  return response.data
}

export const getScrapDetail = async (id: number): Promise<ApiResponse<ScrapItem>> => {
  const response = await request.get<ApiResponse<ScrapItem>>(`/scrap/${id}`)
  return response.data
}

export const createScrap = async (data: CreateScrapRequest): Promise<ApiResponse<ScrapItem>> => {
  const response = await request.post<ApiResponse<ScrapItem>>('/scrap', data)
  return response.data
}

export const approveScrap = async (id: number, data: ApproveScrapRequest): Promise<ApiResponse<ScrapItem>> => {
  const response = await request.put<ApiResponse<ScrapItem>>(`/scrap/${id}/approve`, data)
  return response.data
}

export const getScrapStats = async (params?: {
  startDate?: string
  endDate?: string
}): Promise<ApiResponse<ScrapStatsResponse>> => {
  const response = await request.get<ApiResponse<ScrapStatsResponse>>('/scrap/stats', { params })
  return response.data
}

