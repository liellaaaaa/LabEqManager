# 设备借用模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 获取设备借用记录列表接口 | 管理员/教师/学生 | 管理员可查看所有借用记录，教师/学生只能查看自己的借用记录 |
| 获取设备借用记录详情接口 | 管理员/教师/学生 | 管理员可查看所有借用记录详情，教师/学生只能查看自己的借用记录详情 |
| 提交设备借用申请接口 | 管理员/教师/学生 | 所有角色均可提交借用申请 |
| 审批设备借用申请接口 | 管理员 | 仅管理员可审批借用申请 |
| 确认设备借出接口 | 管理员 | 仅管理员可确认设备借出 |
| 归还设备接口 | 管理员/借用人 | 管理员和借用人可操作设备归还 |
| 申请续借设备接口 | 管理员/教师/学生 | 所有角色均可提交续借申请 |
| 审批续借申请接口 | 管理员 | 仅管理员可审批续借申请 |
| 标记逾期借用记录接口 | 管理员 | 仅管理员可标记逾期记录 |
| 获取设备可用数量接口 | 管理员/教师/学生 | 所有角色均可获取设备可用数量 |

## 1. 获取设备借用记录列表接口

### 1.1 接口说明
获取设备借用记录列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/borrow
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| equipmentId | Long | 否 | 设备ID |
| userId | Long | 否 | 借用用户ID（管理员可查询所有，教师/学生只能查询自己的） |
| status | Integer | 否 | 状态（0-待审批，1-已通过，2-已拒绝，3-已借出，4-已归还，5-已逾期） |
| borrowDateStart | Date | 否 | 借用日期起始（格式：yyyy-MM-dd） |
| borrowDateEnd | Date | 否 | 借用日期结束（格式：yyyy-MM-dd） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/borrow?page=1&size=10&equipmentId=1&status=3
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 借用记录列表 |
| list[].id | Long | 借用记录ID |
| list[].equipmentId | Long | 设备ID |
| list[].equipmentName | String | 设备名称 |
| list[].equipmentModel | String | 设备型号 |
| list[].equipmentAssetCode | String | 设备资产编号 |
| list[].userId | Long | 借用用户ID |
| list[].userName | String | 借用用户姓名 |
| list[].userDepartment | String | 借用用户院系 |
| list[].borrowDate | DateTime | 借用日期 |
| list[].planReturnDate | DateTime | 计划归还日期 |
| list[].actualReturnDate | DateTime | 实际归还日期 |
| list[].purpose | String | 借用用途 |
| list[].quantity | Integer | 借用数量 |
| list[].status | Integer | 状态 |
| list[].statusName | String | 状态名称 |
| list[].approverId | Long | 审批人ID |
| list[].approverName | String | 审批人姓名 |
| list[].approveTime | DateTime | 审批时间 |
| list[].approveRemark | String | 审批备注 |
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
        "equipmentId": 1,
        "equipmentName": "联想台式电脑",
        "equipmentModel": "ThinkCentre M720t",
        "equipmentAssetCode": "EQ20230001",
        "userId": 3,
        "userName": "李同学",
        "userDepartment": "计算机学院",
        "borrowDate": "2023-10-01 10:00:00",
        "planReturnDate": "2023-10-07 17:00:00",
        "actualReturnDate": null,
        "purpose": "课程设计",
        "quantity": 1,
        "status": 3,
        "statusName": "已借出",
        "approverId": 2,
        "approverName": "张老师",
        "approveTime": "2023-09-30 14:30:00",
        "approveRemark": "审批通过",
        "createTime": "2023-09-30 14:00:00",
        "updateTime": "2023-10-01 10:00:00"
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
  "message": "无权限查看该借用记录列表",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取设备借用记录详情接口

### 2.1 接口说明
根据借用记录ID获取借用记录详细信息。

### 2.2 接口URL
```
GET /api/v1/borrow/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 借用记录ID |

### 2.4 请求示例
```
GET /api/v1/borrow/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| equipmentId | Long | 设备ID |
| equipmentName | String | 设备名称 |
| equipmentModel | String | 设备型号 |
| equipmentAssetCode | String | 设备资产编号 |
| userId | Long | 借用用户ID |
| userName | String | 借用用户姓名 |
| userDepartment | String | 借用用户院系 |
| borrowDate | DateTime | 借用日期 |
| planReturnDate | DateTime | 计划归还日期 |
| actualReturnDate | DateTime | 实际归还日期 |
| purpose | String | 借用用途 |
| quantity | Integer | 借用数量 |
| status | Integer | 状态 |
| statusName | String | 状态名称 |
| approverId | Long | 审批人ID |
| approverName | String | 审批人姓名 |
| approveTime | DateTime | 审批时间 |
| approveRemark | String | 审批备注 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "equipmentId": 1,
    "equipmentName": "联想台式电脑",
    "equipmentModel": "ThinkCentre M720t",
    "equipmentAssetCode": "EQ20230001",
    "userId": 3,
    "userName": "李同学",
    "userDepartment": "计算机学院",
    "borrowDate": "2023-10-01 10:00:00",
    "planReturnDate": "2023-10-07 17:00:00",
    "actualReturnDate": null,
    "purpose": "课程设计",
    "quantity": 1,
    "status": 3,
    "statusName": "已借出",
    "approverId": 2,
    "approverName": "张老师",
    "approveTime": "2023-09-30 14:30:00",
    "approveRemark": "审批通过",
    "createTime": "2023-09-30 14:00:00",
    "updateTime": "2023-10-01 10:00:00"
  }
}
```

### 2.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：借用记录ID不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看该借用记录详情",
  "data": null
}

{
  "code": 404,
  "message": "借用记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 3. 提交设备借用申请接口

### 3.1 接口说明
用户提交设备借用申请。

### 3.2 接口URL
```
POST /api/v1/borrow
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| equipmentId | Long | 是 | 设备ID |
| borrowDate | DateTime | 是 | 借用日期（格式：yyyy-MM-dd HH:mm:ss） |
| planReturnDate | DateTime | 是 | 计划归还日期（格式：yyyy-MM-dd HH:mm:ss） |
| purpose | String | 是 | 借用用途 |
| quantity | Integer | 否 | 借用数量，默认1 |

### 3.4 请求示例
```json
{
  "equipmentId": 2,
  "borrowDate": "2023-11-01 09:00:00",
  "planReturnDate": "2023-11-07 17:00:00",
  "purpose": "课程设计",
  "quantity": 1
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| equipmentId | Long | 设备ID |
| borrowDate | DateTime | 借用日期 |
| planReturnDate | DateTime | 计划归还日期 |
| purpose | String | 借用用途 |
| quantity | Integer | 借用数量 |
| status | Integer | 状态（0-待审批） |
| userId | Long | 借用用户ID |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "借用申请提交成功",
  "data": {
    "id": 2,
    "equipmentId": 2,
    "borrowDate": "2023-11-01 09:00:00",
    "planReturnDate": "2023-11-07 17:00:00",
    "purpose": "课程设计",
    "quantity": 1,
    "status": 0,
    "userId": 3,
    "createTime": "2023-10-28 16:00:00"
  }
}
```

### 3.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：设备ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：借用日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：计划归还日期不能早于借用日期",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：借用数量不能超过设备可用数量",
  "data": null
}

{
  "code": 400,
  "message": "设备不可用：该设备当前无法借用",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 404,
  "message": "设备不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 4. 审批设备借用申请接口

### 4.1 接口说明
审批设备借用申请（仅管理员可操作）。

### 4.2 接口URL
```
PUT /api/v1/borrow/{id}/approve
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 借用记录ID |
| status | Integer | 是 | 审批结果（1-已通过，2-已拒绝） |
| remark | String | 否 | 审批备注 |

### 4.4 请求示例
```json
{
  "status": 1,
  "remark": "审批通过"
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| status | Integer | 审批结果 |
| approverId | Long | 审批人ID |
| approveTime | DateTime | 审批时间 |
| approveRemark | String | 审批备注 |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "id": 2,
    "status": 1,
    "approverId": 2,
    "approveTime": "2023-10-28 16:30:00",
    "approveRemark": "审批通过",
    "updateTime": "2023-10-28 16:30:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：借用记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：审批状态无效（必须是1-通过或2-拒绝）",
  "data": null
}

{
  "code": 400,
  "message": "该借用记录已审批过，无法重复审批",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限审批借用申请（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "借用记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 5. 确认设备借出接口

### 5.1 接口说明
确认设备借出（仅管理员可操作）。

### 5.2 接口URL
```
PUT /api/v1/borrow/{id}/borrow
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 借用记录ID |
| borrowDate | DateTime | 否 | 实际借出时间（格式：yyyy-MM-dd HH:mm:ss） |

### 5.4 请求示例
```json
{
  "borrowDate": "2023-11-01 09:30:00"
}
```

### 5.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| status | Integer | 状态（3-已借出） |
| borrowDate | DateTime | 实际借出时间 |
| updateTime | DateTime | 更新时间 |

### 5.6 成功响应示例
```json
{
  "code": 200,
  "message": "设备借出确认成功",
  "data": {
    "id": 2,
    "status": 3,
    "borrowDate": "2023-11-01 09:30:00",
    "updateTime": "2023-11-01 09:30:00"
  }
}
```

### 5.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：借用记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：实际借出时间格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "该借用记录状态不符合要求，无法确认借出",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限确认设备借出（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "借用记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 6. 归还设备接口

### 6.1 接口说明
归还设备。

### 6.2 接口URL
```
PUT /api/v1/borrow/{id}/return
```

### 6.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 借用记录ID |
| actualReturnDate | DateTime | 否 | 实际归还时间（格式：yyyy-MM-dd HH:mm:ss） |
| remark | String | 否 | 归还备注 |

### 6.4 请求示例
```json
{
  "actualReturnDate": "2023-11-07 16:30:00",
  "remark": "设备完好无损"
}
```

### 6.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| status | Integer | 状态（4-已归还） |
| actualReturnDate | DateTime | 实际归还时间 |
| updateTime | DateTime | 更新时间 |

### 6.6 成功响应示例
```json
{
  "code": 200,
  "message": "设备归还成功",
  "data": {
    "id": 2,
    "status": 4,
    "actualReturnDate": "2023-11-07 16:30:00",
    "updateTime": "2023-11-07 16:30:00"
  }
}
```

### 6.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：借用记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：实际归还时间格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "该借用记录状态不符合要求，无法归还",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限归还该设备",
  "data": null
}

{
  "code": 404,
  "message": "借用记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 7. 申请续借设备接口

### 7.1 接口说明
申请续借设备。

### 7.2 接口URL
```
POST /api/v1/borrow/{id}/renew
```

### 7.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 借用记录ID |
| newPlanReturnDate | DateTime | 是 | 新的计划归还日期（格式：yyyy-MM-dd HH:mm:ss） |
| reason | String | 是 | 续借原因 |

### 7.4 请求示例
```json
{
  "newPlanReturnDate": "2023-11-14 17:00:00",
  "reason": "课程设计需要更多时间"
}
```

### 7.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| newPlanReturnDate | DateTime | 新的计划归还日期 |
| status | Integer | 状态（0-待审批） |
| updateTime | DateTime | 更新时间 |

### 7.6 成功响应示例
```json
{
  "code": 200,
  "message": "续借申请提交成功",
  "data": {
    "id": 2,
    "newPlanReturnDate": "2023-11-14 17:00:00",
    "status": 0,
    "updateTime": "2023-11-06 15:00:00"
  }
}
```

### 7.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：借用记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：新的计划归还日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：新的计划归还日期不能早于原计划归还日期",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：续借原因不能为空",
  "data": null
}

{
  "code": 400,
  "message": "该借用记录状态不符合要求，无法申请续借",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限申请续借该设备",
  "data": null
}

{
  "code": 404,
  "message": "借用记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 8. 审批续借申请接口

### 8.1 接口说明
审批设备续借申请（仅管理员可操作）。

### 8.2 接口URL
```
PUT /api/v1/borrow/{id}/approve-renew
```

### 8.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 借用记录ID |
| status | Integer | 是 | 审批结果（1-已通过，2-已拒绝） |
| remark | String | 否 | 审批备注 |

### 8.4 请求示例
```json
{
  "status": 1,
  "remark": "续借审批通过"
}
```

### 8.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 借用记录ID |
| planReturnDate | DateTime | 更新后的计划归还日期 |
| status | Integer | 状态 |
| approverId | Long | 审批人ID |
| approveTime | DateTime | 审批时间 |
| approveRemark | String | 审批备注 |
| updateTime | DateTime | 更新时间 |

### 8.6 成功响应示例
```json
{
  "code": 200,
  "message": "续借审批成功",
  "data": {
    "id": 2,
    "planReturnDate": "2023-11-14 17:00:00",
    "status": 3,
    "approverId": 2,
    "approveTime": "2023-11-06 15:30:00",
    "approveRemark": "续借审批通过",
    "updateTime": "2023-11-06 15:30:00"
  }
}
```

### 8.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：借用记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：审批状态无效（必须是1-通过或2-拒绝）",
  "data": null
}

{
  "code": 400,
  "message": "该借用记录没有续借申请，无法审批",
  "data": null
}

{
  "code": 400,
  "message": "该续借申请已审批过，无法重复审批",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限审批续借申请（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "借用记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 9. 标记逾期借用记录接口

### 9.1 接口说明
标记逾期的借用记录（系统自动调用或管理员手动调用）。

### 9.2 接口URL
```
PUT /api/v1/borrow/mark-overdue
```

### 9.3 请求参数
无

### 9.4 请求示例
```
PUT /api/v1/borrow/mark-overdue
Authorization: Bearer {token}
```

### 9.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| overdueCount | Integer | 标记为逾期的记录数量 |

### 9.6 成功响应示例
```json
{
  "code": 200,
  "message": "逾期标记完成",
  "data": {
    "overdueCount": 5
  }
}
```

### 9.7 错误响应示例
```json
{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限标记逾期记录（仅管理员或系统可操作）",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 10. 获取设备可用数量接口

### 10.1 接口说明
获取设备当前可用数量（总数量减去已借出数量）。

### 10.2 接口URL
```
GET /api/v1/borrow/available-quantity/{equipmentId}
```

### 10.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| equipmentId | Long | 是 | 设备ID |

### 10.4 请求示例
```
GET /api/v1/borrow/available-quantity/1
Authorization: Bearer {token}
```

### 10.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| equipmentId | Long | 设备ID |
| totalQuantity | Integer | 总数量 |
| borrowedQuantity | Integer | 已借出数量 |
| availableQuantity | Integer | 可用数量 |

### 10.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "equipmentId": 1,
    "totalQuantity": 55,
    "borrowedQuantity": 5,
    "availableQuantity": 50
  }
}
```

### 10.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：设备ID不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限获取设备可用数量",
  "data": null
}

{
  "code": 404,
  "message": "设备不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}