# 实验室预约模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 获取实验室预约列表接口 | 管理员/教师/学生 | 管理员可查看所有预约记录，教师/学生只能查看自己的预约记录 |
| 获取实验室预约详情接口 | 管理员/教师/学生 | 管理员可查看所有预约详情，教师/学生只能查看自己的预约详情 |
| 创建实验室预约接口 | 管理员/教师/学生 | 所有角色均可创建预约 |
| 取消实验室预约接口 | 管理员/预约人 | 管理员和预约人可取消预约 |
| 审批实验室预约接口 | 管理员 | 仅管理员可审批预约 |
| 标记实验室预约完成接口 | 管理员 | 仅管理员可标记预约完成 |
| 检查实验室预约冲突接口 | 管理员/教师/学生 | 所有角色均可检查预约冲突 |
| 获取实验室可用时间段接口 | 管理员/教师/学生 | 所有角色均可获取可用时间段 |

## 1. 获取实验室预约列表接口

### 1.1 接口说明
获取实验室预约列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/reservation
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| laboratoryId | Long | 否 | 实验室ID |
| reserveDate | Date | 否 | 预约日期（格式：yyyy-MM-dd） |
| userId | Long | 否 | 预约人ID（管理员可查询所有，教师/学生只能查询自己的） |
| status | Integer | 否 | 状态（0-待审批，1-已通过，2-已拒绝，3-已取消，4-已完成，5-已使用） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/reservation?page=1&size=10&laboratoryId=1&reserveDate=2023-11-01&status=1
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 预约列表 |
| list[].id | Long | 预约记录ID |
| list[].laboratoryId | Long | 实验室ID |
| list[].laboratoryName | String | 实验室名称 |
| list[].laboratoryCode | String | 实验室编号 |
| list[].userId | Long | 预约人ID |
| list[].userName | String | 预约人姓名 |
| list[].reserveDate | Date | 预约日期 |
| list[].startTime | Time | 开始时间 |
| list[].endTime | Time | 结束时间 |
| list[].purpose | String | 预约目的 |
| list[].status | Integer | 状态 |
| list[].approverId | Long | 审批人ID |
| list[].approverName | String | 审批人姓名 |
| list[].approveTime | DateTime | 审批时间 |
| list[].approveRemark | String | 审批备注 |
| list[].actualStartTime | Time | 实际开始时间 |
| list[].actualEndTime | Time | 实际结束时间 |
| list[].usageRemark | String | 使用备注 |
| list[].createTime | DateTime | 创建时间 |
| list[].updateTime | DateTime | 更新时间 |
| total | Long | 总记录数 |
| page | Integer | 当前页码 |
| size | Integer | 每页条数 |

### 1.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "laboratoryId": 1,
        "laboratoryName": "计算机基础实验室",
        "laboratoryCode": "LAB001",
        "userId": 3,
        "userName": "李同学",
        "reserveDate": "2023-11-01",
        "startTime": "09:00:00",
        "endTime": "11:00:00",
        "purpose": "计算机基础课程实验",
        "status": 1,
        "approverId": 2,
        "approverName": "张老师",
        "approveTime": "2023-10-28 14:30:00",
        "approveRemark": "审批通过",
        "createTime": "2023-10-28 14:00:00",
        "updateTime": "2023-10-28 14:30:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 1.7 错误响应示例
```json
{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看该预约列表",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取实验室预约详情接口

### 2.1 接口说明
根据预约记录ID获取预约详细信息。

### 2.2 接口URL
```
GET /api/v1/reservation/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 预约记录ID |

### 2.4 请求示例
```
GET /api/v1/reservation/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 预约记录ID |
| laboratoryId | Long | 实验室ID |
| laboratoryName | String | 实验室名称 |
| laboratoryCode | String | 实验室编号 |
| userId | Long | 预约人ID |
| userName | String | 预约人姓名 |
| reserveDate | Date | 预约日期 |
| startTime | Time | 开始时间 |
| endTime | Time | 结束时间 |
| purpose | String | 预约目的 |
| status | Integer | 状态 |
| approverId | Long | 审批人ID |
| approverName | String | 审批人姓名 |
| approveTime | DateTime | 审批时间 |
| approveRemark | String | 审批备注 |
| actualStartTime | Time | 实际开始时间 |
| actualEndTime | Time | 实际结束时间 |
| usageRemark | String | 使用备注 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "laboratoryId": 1,
    "laboratoryName": "计算机基础实验室",
    "laboratoryCode": "LAB001",
    "userId": 3,
    "userName": "李同学",
    "reserveDate": "2023-11-01",
    "startTime": "09:00:00",
    "endTime": "11:00:00",
    "purpose": "计算机基础课程实验",
    "status": 1,
    "approverId": 2,
    "approverName": "张老师",
    "approveTime": "2023-10-28 14:30:00",
    "approveRemark": "审批通过",
    "createTime": "2023-10-28 14:00:00",
    "updateTime": "2023-10-28 14:30:00"
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
  "message": "无权限查看该预约详情",
  "data": null
}

{
  "code": 404,
  "message": "预约记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 3. 创建实验室预约接口

### 3.1 接口说明
创建实验室预约申请。

### 3.2 接口URL
```
POST /api/v1/reservation
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| laboratoryId | Long | 是 | 实验室ID |
| reserveDate | Date | 是 | 预约日期（格式：yyyy-MM-dd） |
| startTime | Time | 是 | 开始时间（格式：HH:mm:ss） |
| endTime | Time | 是 | 结束时间（格式：HH:mm:ss） |
| purpose | String | 是 | 预约目的 |

### 3.4 请求示例
```json
{
  "laboratoryId": 1,
  "reserveDate": "2023-11-01",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "purpose": "计算机程序设计实验"
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 预约记录ID |
| laboratoryId | Long | 实验室ID |
| reserveDate | Date | 预约日期 |
| startTime | Time | 开始时间 |
| endTime | Time | 结束时间 |
| purpose | String | 预约目的 |
| status | Integer | 状态（默认0-待审批） |
| userId | Long | 预约人ID |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "预约申请提交成功",
  "data": {
    "id": 2,
    "laboratoryId": 1,
    "reserveDate": "2023-11-01",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "purpose": "计算机程序设计实验",
    "status": 0,
    "userId": 3,
    "createTime": "2023-10-28 15:00:00"
  }
}
```

### 3.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：预约日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：结束时间不能早于开始时间",
  "data": null
}

{
  "code": 400,
  "message": "预约冲突：该时间段实验室已被预约",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限创建预约",
  "data": null
}

{
  "code": 404,
  "message": "实验室不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 4. 取消实验室预约接口

### 4.1 接口说明
取消实验室预约。

### 4.2 接口URL
```
PUT /api/v1/reservation/{id}/cancel
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 预约记录ID |
| remark | String | 否 | 取消备注 |

### 4.4 请求示例
```json
{
  "remark": "因课程调整取消预约"
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 预约记录ID |
| status | Integer | 状态（3-已取消） |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "预约取消成功",
  "data": {
    "id": 2,
    "status": 3,
    "updateTime": "2023-10-28 15:30:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：预约记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "该预约已无法取消",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限取消该预约",
  "data": null
}

{
  "code": 404,
  "message": "预约记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 5. 审批实验室预约接口

### 5.1 接口说明
审批实验室预约申请（仅管理员可操作）。

### 5.2 接口URL
```
PUT /api/v1/reservation/{id}/approve
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 预约记录ID |
| status | Integer | 是 | 审批结果（1-已通过，2-已拒绝） |
| remark | String | 否 | 审批备注 |

### 5.4 请求示例
```json
{
  "status": 1,
  "remark": "审批通过"
}
```

### 5.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 预约记录ID |
| status | Integer | 审批结果 |
| approverId | Long | 审批人ID |
| approveTime | DateTime | 审批时间 |
| approveRemark | String | 审批备注 |
| updateTime | DateTime | 更新时间 |

### 5.6 成功响应示例
```json
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "id": 2,
    "status": 1,
    "approverId": 2,
    "approveTime": "2023-10-28 16:00:00",
    "approveRemark": "审批通过",
    "updateTime": "2023-10-28 16:00:00"
  }
}
```

### 5.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：预约记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：审批状态无效（必须是1-通过或2-拒绝）",
  "data": null
}

{
  "code": 400,
  "message": "该预约已审批过，无法重复审批",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限审批预约（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "预约记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 6. 标记实验室预约完成接口

### 6.1 接口说明
标记实验室预约已完成（仅管理员可操作）。

### 6.2 接口URL
```
PUT /api/v1/reservation/{id}/complete
```

### 6.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 预约记录ID |
| actualStartTime | Time | 否 | 实际开始时间（格式：HH:mm:ss） |
| actualEndTime | Time | 否 | 实际结束时间（格式：HH:mm:ss） |
| usageRemark | String | 否 | 使用备注 |

### 6.4 请求示例
```json
{
  "actualStartTime": "09:10:00",
  "actualEndTime": "11:05:00",
  "usageRemark": "实验顺利完成"
}
```

### 6.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 预约记录ID |
| status | Integer | 状态（4-已完成） |
| actualStartTime | Time | 实际开始时间 |
| actualEndTime | Time | 实际结束时间 |
| usageRemark | String | 使用备注 |
| updateTime | DateTime | 更新时间 |

### 6.6 成功响应示例
```json
{
  "code": 200,
  "message": "预约已标记为完成",
  "data": {
    "id": 1,
    "status": 4,
    "actualStartTime": "09:10:00",
    "actualEndTime": "11:05:00",
    "usageRemark": "实验顺利完成",
    "updateTime": "2023-11-01 11:10:00"
  }
}
```

### 6.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：预约记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：实际结束时间不能早于实际开始时间",
  "data": null
}

{
  "code": 400,
  "message": "该预约状态不符合要求，无法标记为完成",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限标记预约完成（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "预约记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 7. 检查实验室预约冲突接口

### 7.1 接口说明
检查指定时间段内实验室是否有预约冲突。

### 7.2 接口URL
```
POST /api/v1/reservation/check-conflict
```

### 7.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| laboratoryId | Long | 是 | 实验室ID |
| reserveDate | Date | 是 | 预约日期（格式：yyyy-MM-dd） |
| startTime | Time | 是 | 开始时间（格式：HH:mm:ss） |
| endTime | Time | 是 | 结束时间（格式：HH:mm:ss） |
| excludeId | Long | 否 | 排除的预约ID（用于更新预约时） |

### 7.4 请求示例
```json
{
  "laboratoryId": 1,
  "reserveDate": "2023-11-01",
  "startTime": "14:00:00",
  "endTime": "16:00:00"
}
```

### 7.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| hasConflict | Boolean | 是否有冲突（true-有冲突，false-无冲突） |
| conflictList | Array | 冲突的预约列表 |
| conflictList[].id | Long | 预约记录ID |
| conflictList[].startTime | Time | 开始时间 |
| conflictList[].endTime | Time | 结束时间 |
| conflictList[].status | Integer | 状态 |

### 7.6 成功响应示例
```json
{
  "code": 200,
  "message": "检查成功",
  "data": {
    "hasConflict": true,
    "conflictList": [
      {
        "id": 1,
        "startTime": "15:00:00",
        "endTime": "17:00:00",
        "status": 1
      }
    ]
  }
}
```

### 7.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：预约日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：结束时间不能早于开始时间",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 404,
  "message": "实验室不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 8. 获取实验室可用时间段接口

### 8.1 接口说明
获取指定日期实验室的可用时间段。

### 8.2 接口URL
```
GET /api/v1/reservation/available-time
```

### 8.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| laboratoryId | Long | 是 | 实验室ID |
| reserveDate | Date | 是 | 预约日期（格式：yyyy-MM-dd） |

### 8.4 请求示例
```
GET /api/v1/reservation/available-time?laboratoryId=1&reserveDate=2023-11-01
Authorization: Bearer {token}
```

### 8.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| availableTimeSlots | Array | 可用时间段列表 |
| availableTimeSlots[].startTime | Time | 开始时间 |
| availableTimeSlots[].endTime | Time | 结束时间 |

### 8.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "availableTimeSlots": [
      {
        "startTime": "08:00:00",
        "endTime": "10:00:00"
      },
      {
        "startTime": "16:00:00",
        "endTime": "18:00:00"
      }
    ]
  }
}
```

### 8.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：预约日期格式不正确",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 404,
  "message": "实验室不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}