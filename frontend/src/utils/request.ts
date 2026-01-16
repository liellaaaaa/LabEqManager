import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// API响应格式
export interface ApiResponse<T> {
  code: number
  message: string
  data: T | null
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<any>>) => {
    const res = response.data

    // 如果code不是200，说明有错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 如果是401未授权，清除token并跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // 如果不在登录页，则跳转
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 返回完整的response对象，但将data替换为ApiResponse
    return {
      ...response,
      data: res,
    } as AxiosResponse<ApiResponse<any>>
  },
  (error) => {
    console.error('Response error:', error)
    let message = '请求失败'

    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const data = error.response.data

      if (status === 401) {
        message = '未授权，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      } else if (status === 403) {
        message = '拒绝访问'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 500) {
        message = '服务器内部错误'
      } else if (data && data.message) {
        message = data.message
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      message = '网络错误，请检查网络连接'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service

