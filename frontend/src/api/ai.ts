import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

// AI 助手请求参数
export interface AiChatRequest {
  message: string
}

// AI 助手响应数据
export interface AiChatResponse {
  answer: string
  /**
   * 回答来源：knowledge（知识库）或 api（通义千问API）
   */
  source: 'knowledge' | 'api' | string
}

// 调用后端 AI 助手接口（单轮对话）
export const chatWithAi = async (message: string): Promise<ApiResponse<AiChatResponse>> => {
  const payload: AiChatRequest = { message }
  const response = await request.post<ApiResponse<AiChatResponse>>('/ai/chat', payload)
  return response.data
}


