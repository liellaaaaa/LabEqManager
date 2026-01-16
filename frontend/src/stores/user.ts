import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/api/auth'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import type { LoginRequest } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const stored = localStorage.getItem('userInfo')
  const userInfo = ref<UserInfo | null>(stored ? (JSON.parse(stored) as UserInfo) : null)

  // 登录
  const login = async (loginData: LoginRequest) => {
    try {
      const response = await loginApi(loginData)
      if (response.data) {
        token.value = response.data.token
        userInfo.value = response.data.userInfo

        // 保存到localStorage
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))

        return { success: true }
      }
      return { success: false, message: '登录失败' }
    } catch (error: any) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  // 注销
  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 无论成功失败都清除本地数据
      token.value = null
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  // 获取当前用户信息
  const fetchUserInfo = async () => {
    try {
      const response = await getCurrentUser()
      if (response.data) {
        userInfo.value = response.data
        localStorage.setItem('userInfo', JSON.stringify(response.data))
        return { success: true }
      }
      return { success: false }
    } catch (error) {
      return { success: false }
    }
  }

  // 检查是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  // 检查是否是管理员
  const isAdmin = () => {
    return userInfo.value?.roleCode === 'admin'
  }

  // 检查是否是教师
  const isTeacher = () => {
    return userInfo.value?.roleCode === 'teacher'
  }

  // 检查是否是学生
  const isStudent = () => {
    return userInfo.value?.roleCode === 'student'
  }

  return {
    token,
    userInfo,
    login,
    logout,
    fetchUserInfo,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
  }
})

