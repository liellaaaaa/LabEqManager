import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

export interface BorrowItem {
  id: number
  equipmentId: number
  equipmentName?: string
  equipmentModel?: string
  equipmentAssetCode?: string
  userId: number
  userName?: string
  userDepartment?: string
  borrowDate: string
  planReturnDate: string
  actualReturnDate?: string | null
  purpose?: string | null
  quantity: number
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

export interface CreateBorrowRequest {
  equipmentId: number
  borrowDate: string
  planReturnDate: string
  purpose?: string
  quantity?: number
}

export interface ApproveBorrowRequest {
  status: number // 1-通过，2-拒绝
  remark?: string
}

export interface ConfirmBorrowRequest {
  borrowDate?: string
}

export interface ReturnBorrowRequest {
  actualReturnDate?: string
  remark?: string
}

export interface MarkOverdueResponse {
  overdueCount: number
}

export interface AvailableQuantityResponse {
  equipmentId: number
  totalQuantity: number
  borrowedQuantity: number
  availableQuantity: number
}

export const getBorrowList = async (params?: {
  page?: number
  size?: number
  equipmentId?: number
  userId?: number
  status?: number
  borrowDateStart?: string
  borrowDateEnd?: string
  sortBy?: string
  sortOrder?: string
}): Promise<ApiResponse<PageResponse<BorrowItem>>> => {
  const response = await request.get<ApiResponse<PageResponse<BorrowItem>>>('/borrow', { params })
  return response.data
}

export const createBorrow = async (data: CreateBorrowRequest): Promise<ApiResponse<BorrowItem>> => {
  const response = await request.post<ApiResponse<BorrowItem>>('/borrow', data)
  return response.data
}

export const approveBorrow = async (id: number, data: ApproveBorrowRequest): Promise<ApiResponse<BorrowItem>> => {
  const response = await request.put<ApiResponse<BorrowItem>>(`/borrow/${id}/approve`, data)
  return response.data
}

export const confirmBorrow = async (id: number, data?: ConfirmBorrowRequest): Promise<ApiResponse<BorrowItem>> => {
  const response = await request.put<ApiResponse<BorrowItem>>(`/borrow/${id}/borrow`, data || {})
  return response.data
}

export const returnBorrow = async (id: number, data?: ReturnBorrowRequest): Promise<ApiResponse<BorrowItem>> => {
  const response = await request.put<ApiResponse<BorrowItem>>(`/borrow/${id}/return`, data || {})
  return response.data
}

export const markOverdue = async (): Promise<ApiResponse<MarkOverdueResponse>> => {
  const response = await request.put<ApiResponse<MarkOverdueResponse>>('/borrow/mark-overdue')
  return response.data
}

export const getAvailableQuantity = async (equipmentId: number): Promise<ApiResponse<AvailableQuantityResponse>> => {
  const response = await request.get<ApiResponse<AvailableQuantityResponse>>(`/borrow/available-quantity/${equipmentId}`)
  return response.data
}


