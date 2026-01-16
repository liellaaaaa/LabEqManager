import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  name: string
  email: string | null
  phone: string | null
  department: string | null
  roleCode: string
  status: number
  createTime?: string
  updateTime?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

// 登录接口
export const login = async (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
  const response = await request.post<ApiResponse<LoginResponse>>('/auth/login', data)
  return response.data
}

// 注销接口
export const logout = async (): Promise<ApiResponse<null>> => {
  const response = await request.post<ApiResponse<null>>('/auth/logout')
  return response.data
}

// 获取当前用户信息
export const getCurrentUser = async (): Promise<ApiResponse<UserInfo>> => {
  const response = await request.get<ApiResponse<UserInfo>>('/auth/me')
  return response.data
}

