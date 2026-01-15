# AI智能助手模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 智能问答接口 | 所有角色 | 所有用户均可使用 |
| 知识库列表查询接口 | 管理员 | 仅管理员可查看 |
| 知识库详情查询接口 | 管理员 | 仅管理员可查看 |
| 创建知识库接口 | 管理员 | 仅管理员可创建 |
| 更新知识库接口 | 管理员 | 仅管理员可更新 |
| 删除知识库接口 | 管理员 | 仅管理员可删除 |
| 批量删除知识库接口 | 管理员（扩展） | 仅管理员可操作，扩展功能 |
| 导入知识库接口 | 管理员（扩展） | 仅管理员可操作，扩展功能 |
| 导出知识库接口 | 管理员（扩展） | 仅管理员可操作，扩展功能 |
| 获取对话历史接口 | 所有角色（扩展） | 所有用户均可查看自己的历史，扩展功能 |
| 清除对话历史接口 | 所有角色（扩展） | 所有用户均可清除自己的历史，扩展功能 |
| 获取知识库分类接口 | 所有角色（扩展） | 所有用户均可使用，扩展功能 |

## 核心接口

## 1. 智能问答接口

### 1.1 接口说明
接收用户的自然语言输入，返回智能回答。系统会优先从本地知识库查询答案，若未匹配到则调用外部AI API。

### 1.2 接口URL
```
POST /api/v1/ai/chat
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| question | String | 是 | 用户输入的问题 |
| source | String | 否 | 来源（可选，用于统计） |

### 1.4 请求示例
```json
{
  "question": "如何申请借用设备？",
  "source": "设备管理页面"
}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| answer | String | AI回答内容 |
| source | String | 回答来源（knowledge-知识库，api-外部API） |
| knowledgeId | Long | 匹配的知识库ID（当来源为knowledge时返回） |

### 1.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "answer": "申请借用设备的流程如下：1. 登录系统；2. 进入设备管理页面；3. 选择要借用的设备；4. 点击借用按钮；5. 填写借用信息并提交；6. 等待管理员审批；7. 审批通过后，在规定时间内领取设备。",
    "source": "knowledge",
    "knowledgeId": 1
  }
}
```

### 1.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：问题不能为空",
  "data": null
}

{
  "code": 500,
  "message": "AI服务异常，请稍后重试",
  "data": null
}
```

## 2. 获取知识库列表接口

### 2.1 接口说明
获取自定义知识库列表（仅管理员可操作）。

### 2.2 接口URL
```
GET /api/v1/ai/knowledge
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| question | String | 否 | 问题（模糊查询） |
| category | String | 否 | 分类 |
| status | Integer | 否 | 状态（0-禁用，1-启用） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 2.4 请求示例
```
GET /api/v1/ai/knowledge?page=1&size=10&category=设备借用&status=1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 知识库列表 |
| list[].id | Long | 知识ID |
| list[].question | String | 问题 |
| list[].answer | String | 答案 |
| list[].category | String | 分类 |
| list[].keywords | String | 关键词 |
| list[].status | Integer | 状态（0-禁用，1-启用） |
| list[].createTime | DateTime | 创建时间 |
| list[].updateTime | DateTime | 更新时间 |
| total | Long | 总记录数 |
| page | Integer | 当前页码 |
| size | Integer | 每页条数 |

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "question": "如何申请借用设备？",
        "answer": "申请借用设备的流程如下：1. 登录系统；2. 进入设备管理页面；3. 选择要借用的设备；4. 点击借用按钮；5. 填写借用信息并提交；6. 等待管理员审批；7. 审批通过后，在规定时间内领取设备。",
        "category": "设备借用",
        "keywords": "设备借用,申请流程",
        "status": 1,
        "createTime": "2023-09-10 10:00:00",
        "updateTime": "2023-09-10 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 2.7 错误响应示例
```json
{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限操作此接口",
  "data": null
}
```

## 3. 获取知识库详情接口

### 3.1 接口说明
获取知识库详情（仅管理员可操作）。

### 3.2 接口URL
```
GET /api/v1/ai/knowledge/{id}
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 知识ID |

### 3.4 请求示例
```
GET /api/v1/ai/knowledge/1
Authorization: Bearer {token}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 知识ID |
| question | String | 问题 |
| answer | String | 答案 |
| category | String | 分类 |
| keywords | String | 关键词 |
| status | Integer | 状态（0-禁用，1-启用） |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "question": "如何申请借用设备？",
    "answer": "申请借用设备的流程如下：1. 登录系统；2. 进入设备管理页面；3. 选择要借用的设备；4. 点击借用按钮；5. 填写借用信息并提交；6. 等待管理员审批；7. 审批通过后，在规定时间内领取设备。",
    "category": "设备借用",
    "keywords": "设备借用,申请流程",
    "status": 1,
    "createTime": "2023-09-10 10:00:00",
    "updateTime": "2023-09-10 10:00:00"
  }
}
```

### 3.7 错误响应示例
```json
{
  "code": 404,
  "message": "知识库不存在",
  "data": null
}

{
  "code": 403,
  "message": "无权限操作此接口",
  "data": null
}
```

## 4. 创建知识库接口

### 4.1 接口说明
创建新的知识库（仅管理员可操作）。

### 4.2 接口URL
```
POST /api/v1/ai/knowledge
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| question | String | 是 | 问题 |
| answer | String | 是 | 答案 |
| category | String | 否 | 分类 |
| keywords | String | 否 | 关键词（多个关键词用逗号分隔） |
| status | Integer | 否 | 状态（0-禁用，1-启用），默认1 |

### 4.4 请求示例
```json
{
  "question": "如何预约实验室？",
  "answer": "预约实验室的流程如下：1. 登录系统；2. 进入实验室管理页面；3. 选择要预约的实验室；4. 点击预约按钮；5. 选择预约日期和时间；6. 填写预约目的并提交；7. 等待管理员审批；8. 审批通过后，按时使用实验室。",
  "category": "实验室预约",
  "keywords": "实验室预约,申请流程",
  "status": 1
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 知识ID |
| question | String | 问题 |
| answer | String | 答案 |
| category | String | 分类 |
| keywords | String | 关键词 |
| status | Integer | 状态 |
| createTime | DateTime | 创建时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "question": "如何预约实验室？",
    "answer": "预约实验室的流程如下：1. 登录系统；2. 进入实验室管理页面；3. 选择要预约的实验室；4. 点击预约按钮；5. 选择预约日期和时间；6. 填写预约目的并提交；7. 等待管理员审批；8. 审批通过后，按时使用实验室。",
    "category": "实验室预约",
    "keywords": "实验室预约,申请流程",
    "status": 1,
    "createTime": "2023-11-01 15:00:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：问题和答案不能为空",
  "data": null
}

{
  "code": 403,
  "message": "无权限操作此接口",
  "data": null
}
```

## 5. 更新知识库接口

### 5.1 接口说明
更新知识库（仅管理员可操作）。

### 5.2 接口URL
```
PUT /api/v1/ai/knowledge/{id}
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 知识ID |
| question | String | 否 | 问题 |
| answer | String | 否 | 答案 |
| category | String | 否 | 分类 |
| keywords | String | 否 | 关键词（多个关键词用逗号分隔） |
| status | Integer | 否 | 状态（0-禁用，1-启用） |

### 5.4 请求示例
```json
{
  "question": "如何申请借用设备？（更新）",
  "answer": "申请借用设备的流程如下：1. 登录系统；2. 进入设备管理页面；3. 选择要借用的设备；4. 点击借用按钮；5. 填写借用信息并提交；6. 等待管理员审批（一般1-2个工作日）；7. 审批通过后，在规定时间内领取设备。",
  "keywords": "设备借用,申请流程,借用设备",
  "status": 1
}
```

### 5.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 知识ID |
| question | String | 问题 |
| answer | String | 答案 |
| category | String | 分类 |
| keywords | String | 关键词 |
| status | Integer | 状态 |
| updateTime | DateTime | 更新时间 |

### 5.6 成功响应示例
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "question": "如何申请借用设备？（更新）",
    "answer": "申请借用设备的流程如下：1. 登录系统；2. 进入设备管理页面；3. 选择要借用的设备；4. 点击借用按钮；5. 填写借用信息并提交；6. 等待管理员审批（一般1-2个工作日）；7. 审批通过后，在规定时间内领取设备。",
    "category": "设备借用",
    "keywords": "设备借用,申请流程,借用设备",
    "status": 1,
    "updateTime": "2023-11-01 15:30:00"
  }
}
```

### 5.7 错误响应示例
```json
{
  "code": 404,
  "message": "知识库不存在",
  "data": null
}

{
  "code": 403,
  "message": "无权限操作此接口",
  "data": null
}
```

## 6. 删除知识库接口

### 6.1 接口说明
删除知识库（仅管理员可操作）。

### 6.2 接口URL
```
DELETE /api/v1/ai/knowledge/{id}
```

### 6.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 知识ID |

### 6.4 请求示例
```
DELETE /api/v1/ai/knowledge/2
Authorization: Bearer {token}
```

### 6.5 响应参数
无

### 6.6 成功响应示例
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 6.7 错误响应示例
```json
{
  "code": 404,
  "message": "知识库不存在",
  "data": null
}

{
  "code": 403,
  "message": "无权限操作此接口",
  "data": null
}
```

## 扩展接口（时间允许可实现）

### 7. 批量删除知识库接口

### 8. 导入知识库接口

### 9. 导出知识库接口

### 10. 获取对话历史接口

### 11. 清除对话历史接口

### 12. 获取知识库分类接口
```