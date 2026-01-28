<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chatWithAi, type AiChatResponse } from '@/api/ai'

const userInput = ref('')
const loading = ref(false)
const answer = ref<string | null>(null)
const source = ref<'knowledge' | 'api' | string | null>(null)

// 单轮对话：每次只根据当前输入请求一次，不保留上下文
const handleAsk = async () => {
  const content = userInput.value.trim()
  if (!content) {
    ElMessage.warning('请输入要咨询的问题')
    return
  }

  loading.value = true
  answer.value = null
  source.value = null

  try {
    const res = await chatWithAi(content)
    const data = res.data as AiChatResponse | null
    if (data) {
      answer.value = data.answer
      source.value = data.source
    } else {
      answer.value = '未获取到有效回答，请稍后重试。'
      source.value = null
    }
  } catch (error) {
    console.error('调用 AI 助手失败', error)
    answer.value = '调用 AI 助手失败，请稍后重试。'
    source.value = null
  } finally {
    loading.value = false
  }
}

const clearAll = () => {
  userInput.value = ''
  answer.value = null
  source.value = null
}
</script>

<template>
  <div class="ai-assistant-container">
    <el-card class="ai-card">
      <template #header>
        <div class="ai-header">
          <div>
            <span class="title">AI 智能助手（实验室设备管理）</span>
            <el-tag type="info" size="small" class="tag">单轮对话 · 不保留上下文</el-tag>
          </div>
          <div class="tips">
            <span>只回答与“高校实验室设备管理系统”相关的咨询、解释和指引类问题。</span>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="panel">
            <div class="panel-title">请输入你的问题</div>
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="10"
              maxlength="500"
              show-word-limit
              placeholder="例如：如何借用设备？&#10;例如：实验室预约的流程是怎样的？"
            />
            <div class="actions">
              <el-button @click="clearAll">清空</el-button>
              <el-button type="primary" :loading="loading" @click="handleAsk">发送</el-button>
            </div>

            <el-alert
              class="rules"
              type="info"
              show-icon
              :closable="false"
              title="使用说明"
            >
              <ul>
                <li>不执行任何真实操作：不会修改设备、借用、预约或用户数据。</li>
                <li>不会生成 SQL 或调用内部接口，仅提供文字说明和流程指引。</li>
                <li>对于与系统无关的内容（如政治、娱乐、生活问答等）将拒绝回答。</li>
              </ul>
            </el-alert>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="panel">
            <div class="panel-title">
              AI 回答
              <el-tag v-if="source" size="small" :type="source === 'knowledge' ? 'success' : 'warning'">
                {{ source === 'knowledge' ? '知识库' : source === 'api' ? '通义千问' : source }}
              </el-tag>
            </div>
            <div class="answer" v-loading="loading">
              <template v-if="answer">
                <pre>{{ answer }}</pre>
              </template>
              <template v-else>
                <div class="placeholder">
                  请在左侧输入问题并点击“发送”，AI 助手会在此处给出解答。
                </div>
              </template>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.ai-assistant-container {
  padding: 0;
}

.ai-card {
  width: 100%;
}

.ai-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  margin-right: 8px;
}

.tag {
  vertical-align: middle;
}

.tips {
  font-size: 12px;
  color: #909399;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-title {
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.answer {
  min-height: 220px;
  max-height: 360px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.answer pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.placeholder {
  color: #909399;
}

.rules {
  margin-top: 8px;
}

.rules ul {
  margin: 0;
  padding-left: 18px;
}

.rules li {
  font-size: 12px;
  color: #606266;
}
</style>


