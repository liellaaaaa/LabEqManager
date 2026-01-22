import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

export interface ReservationItem {
  id: number
  laboratoryId: number
  laboratoryName?: string
  laboratoryCode?: string
  userId: number
  userName?: string
  reserveDate: string
  startTime: string
  endTime: string
  purpose?: string | null
  status: number
  statusName?: string
  approverId?: number | null
  approverName?: string | null
  approveTime?: string | null
  approveRemark?: string | null
  actualStartTime?: string | null
  actualEndTime?: string | null
  usageRemark?: string | null
  createTime: string
  updateTime: string
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface CreateReservationRequest {
  laboratoryId: number
  reserveDate: string // YYYY-MM-DD
  startTime: string // HH:mm:ss
  endTime: string // HH:mm:ss
  purpose?: string
}

export interface ApproveReservationRequest {
  status: number // 1-通过，2-拒绝
  remark?: string
}

export interface CancelReservationRequest {
  remark?: string
}

export interface CompleteReservationRequest {
  actualStartTime?: string // HH:mm:ss
  actualEndTime?: string // HH:mm:ss
  usageRemark?: string
}

export interface CheckConflictRequest {
  laboratoryId: number
  reserveDate: string // YYYY-MM-DD
  startTime: string // HH:mm:ss
  endTime: string // HH:mm:ss
  excludeId?: number
}

export interface ConflictInfo {
  id: number
  startTime: string
  endTime: string
  status: number
}

export interface CheckConflictResponse {
  hasConflict: boolean
  conflictList?: ConflictInfo[]
}

export interface AvailableTimeSlot {
  startTime: string
  endTime: string
}

export interface AvailableTimeResponse {
  availableTimeSlots: AvailableTimeSlot[]
}

export const getReservationList = async (params?: {
  page?: number
  size?: number
  laboratoryId?: number
  userId?: number
  reserveDate?: string
  status?: number
  sortBy?: string
  sortOrder?: string
}): Promise<ApiResponse<PageResponse<ReservationItem>>> => {
  const response = await request.get<ApiResponse<PageResponse<ReservationItem>>>('/reservation', { params })
  return response.data
}

export const getReservationDetail = async (id: number): Promise<ApiResponse<ReservationItem>> => {
  const response = await request.get<ApiResponse<ReservationItem>>(`/reservation/${id}`)
  return response.data
}

export const createReservation = async (data: CreateReservationRequest): Promise<ApiResponse<ReservationItem>> => {
  const response = await request.post<ApiResponse<ReservationItem>>('/reservation', data)
  return response.data
}

export const cancelReservation = async (id: number, data?: CancelReservationRequest): Promise<ApiResponse<ReservationItem>> => {
  const response = await request.put<ApiResponse<ReservationItem>>(`/reservation/${id}/cancel`, data || {})
  return response.data
}

export const approveReservation = async (id: number, data: ApproveReservationRequest): Promise<ApiResponse<ReservationItem>> => {
  const response = await request.put<ApiResponse<ReservationItem>>(`/reservation/${id}/approve`, data)
  return response.data
}

export const completeReservation = async (id: number, data?: CompleteReservationRequest): Promise<ApiResponse<ReservationItem>> => {
  const response = await request.put<ApiResponse<ReservationItem>>(`/reservation/${id}/complete`, data || {})
  return response.data
}

export const checkConflict = async (data: CheckConflictRequest): Promise<ApiResponse<CheckConflictResponse>> => {
  const response = await request.post<ApiResponse<CheckConflictResponse>>('/reservation/check-conflict', data)
  return response.data
}

export const getAvailableTime = async (laboratoryId: number, reserveDate: string): Promise<ApiResponse<AvailableTimeResponse>> => {
  const response = await request.get<ApiResponse<AvailableTimeResponse>>('/reservation/available-time', {
    params: { laboratoryId, reserveDate }
  })
  return response.data
}

