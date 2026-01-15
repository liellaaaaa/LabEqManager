# 设备维修模块接口文档

## 权限说明

| 接口模块 | 接口名称 | 权限要求 | 描述 |
|---------|---------|---------|------|
| 设备维修 | 获取设备维修记录列表 | 管理员/教师/学生 | 管理员可查看所有记录，教师/学生只能查看自己的记录 |
| 设备维修 | 获取设备维修记录详情 | 管理员/教师/学生 | 管理员可查看所有记录，教师/学生只能查看自己的记录 |
| 设备维修 | 提交设备维修申请 | 管理员/教师/学生 | 所有角色均可提交维修申请 |
| 设备维修 | 更新维修记录状态 | 管理员 | 仅管理员可更新维修记录状态 |
| 设备维修 | 获取设备维修统计 | 管理员 | 仅管理员可查看维修统计数据 |
| 设备报废 | 获取设备报废记录列表 | 管理员/教师/学生 | 管理员可查看所有记录，教师/学生只能查看自己的记录 |
| 设备报废 | 获取设备报废记录详情 | 管理员/教师/学生 | 管理员可查看所有记录，教师/学生只能查看自己的记录 |
| 设备报废 | 提交设备报废申请 | 管理员/教师/学生 | 所有角色均可提交报废申请 |
| 设备报废 | 审批设备报废申请 | 管理员 | 仅管理员可审批报废申请 |
| 设备报废 | 获取设备报废统计 | 管理员 | 仅管理员可查看报废统计数据 |

## 1. 获取设备维修记录列表接口

### 1.1 接口说明
获取设备维修记录列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/repair
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| equipmentId | Long | 否 | 设备ID |
| reporterId | Long | 否 | 报修人ID（管理员可查询所有，教师/学生只能查询自己的） |
| status | Integer | 否 | 状态（0-待维修，1-维修中，2-已修好，3-无法修复） |
| reportDateStart | Date | 否 | 报修日期起始（格式：yyyy-MM-dd） |
| reportDateEnd | Date | 否 | 报修日期结束（格式：yyyy-MM-dd） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/repair?page=1&size=10&equipmentId=1&status=1
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 维修记录列表 |
| list[].id | Long | 维修记录ID |
| list[].equipmentId | Long | 设备ID |
| list[].equipmentName | String | 设备名称 |
| list[].equipmentModel | String | 设备型号 |
| list[].equipmentAssetCode | String | 设备资产编号 |
| list[].reporterId | Long | 报修人ID |
| list[].reporterName | String | 报修人姓名 |
| list[].reportDate | DateTime | 报修日期 |
| list[].faultDescription | String | 故障描述 |
| list[].repairResult | String | 维修结果 |
| list[].repairDate | DateTime | 维修日期 |
| list[].status | Integer | 状态 |
| list[].statusName | String | 状态名称 |
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
        "reporterId": 3,
        "reporterName": "李同学",
        "reportDate": "2023-10-15 14:30:00",
        "faultDescription": "电脑无法开机",
        "repairResult": null,
        "repairDate": null,
        "status": 1,
        "statusName": "维修中",
        "createTime": "2023-10-15 14:30:00",
        "updateTime": "2023-10-15 15:00:00"
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
  "code": 400,
  "message": "参数错误：页码必须大于0",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：每页条数必须大于0且不超过100",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看维修记录",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取设备维修记录详情接口

### 2.1 接口说明
根据维修记录ID获取维修记录详细信息。

### 2.2 接口URL
```
GET /api/v1/repair/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 维修记录ID |

### 2.4 请求示例
```
GET /api/v1/repair/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 维修记录ID |
| equipmentId | Long | 设备ID |
| equipmentName | String | 设备名称 |
| equipmentModel | String | 设备型号 |
| equipmentAssetCode | String | 设备资产编号 |
| reporterId | Long | 报修人ID |
| reporterName | String | 报修人姓名 |
| reportDate | DateTime | 报修日期 |
| faultDescription | String | 故障描述 |
| repairResult | String | 维修结果 |
| repairDate | DateTime | 维修日期 |
| status | Integer | 状态 |
| statusName | String | 状态名称 |
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
    "reporterId": 3,
    "reporterName": "李同学",
    "reportDate": "2023-10-15 14:30:00",
    "faultDescription": "电脑无法开机",
    "repairResult": null,
    "repairDate": null,
    "status": 1,
    "statusName": "维修中",
    "createTime": "2023-10-15 14:30:00",
    "updateTime": "2023-10-15 15:00:00"
  }
}
```

### 2.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：维修记录ID不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看该维修记录",
  "data": null
}

{
  "code": 404,
  "message": "维修记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 3. 提交设备维修申请接口

### 3.1 接口说明
用户提交设备维修申请。

### 3.2 接口URL
```
POST /api/v1/repair
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| equipmentId | Long | 是 | 设备ID |
| reportDate | DateTime | 是 | 报修日期（格式：yyyy-MM-dd HH:mm:ss） |
| faultDescription | String | 是 | 故障描述 |

### 3.4 请求示例
```json
{
  "equipmentId": 2,
  "reportDate": "2023-11-01 10:00:00",
  "faultDescription": "屏幕显示异常，有闪烁"
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 维修记录ID |
| equipmentId | Long | 设备ID |
| reportDate | DateTime | 报修日期 |
| faultDescription | String | 故障描述 |
| status | Integer | 状态（0-待维修） |
| reporterId | Long | 报修人ID |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "维修申请提交成功",
  "data": {
    "id": 2,
    "equipmentId": 2,
    "reportDate": "2023-11-01 10:00:00",
    "faultDescription": "屏幕显示异常，有闪烁",
    "status": 0,
    "reporterId": 3,
    "createTime": "2023-11-01 10:00:00"
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
  "message": "参数错误：报修日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：故障描述不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限提交维修申请",
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

## 4. 更新维修记录状态接口

### 4.1 接口说明
更新维修记录状态（仅管理员可操作）。

### 4.2 接口URL
```
PUT /api/v1/repair/{id}/status
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 维修记录ID |
| status | Integer | 是 | 新的状态（0-待维修，1-维修中，2-已修好，3-无法修复） |
| repairResult | String | 否 | 维修结果（当状态为2或3时必填） |
| repairDate | DateTime | 否 | 维修日期（当状态为2或3时必填，格式：yyyy-MM-dd HH:mm:ss） |

### 4.4 请求示例
```json
{
  "status": 2,
  "repairResult": "更换了屏幕，设备恢复正常使用",
  "repairDate": "2023-11-05 15:00:00"
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 维修记录ID |
| status | Integer | 维修状态 |
| repairResult | String | 维修结果 |
| repairDate | DateTime | 维修日期 |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "维修记录状态更新成功",
  "data": {
    "id": 1,
    "status": 2,
    "repairResult": "更换了电源，设备恢复正常使用",
    "repairDate": "2023-10-18 15:00:00",
    "updateTime": "2023-10-18 15:00:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：维修记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：维修状态无效（必须是0-4之间的整数）",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：当状态为2或3时，维修结果不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：当状态为2或3时，维修日期不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限更新维修记录状态（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "维修记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 5. 获取设备维修统计接口

### 5.1 接口说明
获取设备维修统计数据。

### 5.2 接口URL
```
GET /api/v1/repair/stats
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| equipmentId | Long | 否 | 设备ID（为空时统计所有设备） |
| startDate | Date | 否 | 统计起始日期（格式：yyyy-MM-dd） |
| endDate | Date | 否 | 统计结束日期（格式：yyyy-MM-dd） |

### 5.4 请求示例
```
GET /api/v1/repair/stats?startDate=2023-01-01&endDate=2023-12-31
Authorization: Bearer {token}
```

### 5.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| totalCount | Integer | 总维修次数 |
| pendingCount | Integer | 待维修数量 |
| repairingCount | Integer | 维修中数量 |
| fixedCount | Integer | 已修好数量 |
| unrepairableCount | Integer | 无法修复数量 |
| repairRate | Double | 维修成功率（已修好数量/总维修次数） |

### 5.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalCount": 25,
    "pendingCount": 3,
    "repairingCount": 7,
    "fixedCount": 13,
    "unrepairableCount": 2,
    "repairRate": 0.84
  }
}
```

### 5.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：统计起始日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：统计结束日期格式不正确",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看维修统计数据（仅管理员可操作）",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

# 设备报废模块接口文档

## 1. 获取设备报废记录列表接口

### 1.1 接口说明
获取设备报废记录列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/scrap
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| equipmentId | Long | 否 | 设备ID |
| applicantId | Long | 否 | 申请人ID（管理员可查询所有，教师/学生只能查询自己的） |
| status | Integer | 否 | 状态（0-待审批，1-已通过，2-已拒绝） |
| applyDateStart | Date | 否 | 申请日期起始（格式：yyyy-MM-dd） |
| applyDateEnd | Date | 否 | 申请日期结束（格式：yyyy-MM-dd） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/scrap?page=1&size=10&equipmentId=1&status=0
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 报废记录列表 |
| list[].id | Long | 报废记录ID |
| list[].equipmentId | Long | 设备ID |
| list[].equipmentName | String | 设备名称 |
| list[].equipmentModel | String | 设备型号 |
| list[].equipmentAssetCode | String | 设备资产编号 |
| list[].applicantId | Long | 申请人ID |
| list[].applicantName | String | 申请人姓名 |
| list[].applyDate | DateTime | 申请日期 |
| list[].scrapReason | String | 报废原因 |
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
        "equipmentId": 3,
        "equipmentName": "老旧台式电脑",
        "equipmentModel": "ThinkCentre M58",
        "equipmentAssetCode": "EQ20150001",
        "applicantId": 2,
        "applicantName": "张老师",
        "applyDate": "2023-10-20 14:00:00",
        "scrapReason": "设备老化，性能严重下降，无法满足教学需求",
        "status": 0,
        "statusName": "待审批",
        "approverId": null,
        "approverName": null,
        "approveTime": null,
        "approveRemark": null,
        "createTime": "2023-10-20 14:00:00",
        "updateTime": "2023-10-20 14:00:00"
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
  "code": 400,
  "message": "参数错误：页码必须大于0",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：每页条数必须大于0且不超过100",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看报废记录",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取设备报废记录详情接口

### 2.1 接口说明
根据报废记录ID获取报废记录详细信息。

### 2.2 接口URL
```
GET /api/v1/scrap/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 报废记录ID |

### 2.4 请求示例
```
GET /api/v1/scrap/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 报废记录ID |
| equipmentId | Long | 设备ID |
| equipmentName | String | 设备名称 |
| equipmentModel | String | 设备型号 |
| equipmentAssetCode | String | 设备资产编号 |
| applicantId | Long | 申请人ID |
| applicantName | String | 申请人姓名 |
| applyDate | DateTime | 申请日期 |
| scrapReason | String | 报废原因 |
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
    "equipmentId": 3,
    "equipmentName": "老旧台式电脑",
    "equipmentModel": "ThinkCentre M58",
    "equipmentAssetCode": "EQ20150001",
    "applicantId": 2,
    "applicantName": "张老师",
    "applyDate": "2023-10-20 14:00:00",
    "scrapReason": "设备老化，性能严重下降，无法满足教学需求",
    "status": 0,
    "statusName": "待审批",
    "approverId": null,
    "approverName": null,
    "approveTime": null,
    "approveRemark": null,
    "createTime": "2023-10-20 14:00:00",
    "updateTime": "2023-10-20 14:00:00"
  }
}
```

### 2.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：报废记录ID不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看该报废记录",
  "data": null
}

{
  "code": 404,
  "message": "报废记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 3. 提交设备报废申请接口

### 3.1 接口说明
用户提交设备报废申请。

### 3.2 接口URL
```
POST /api/v1/scrap
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| equipmentId | Long | 是 | 设备ID |
| applyDate | DateTime | 是 | 申请日期（格式：yyyy-MM-dd HH:mm:ss） |
| scrapReason | String | 是 | 报废原因 |

### 3.4 请求示例
```json
{
  "equipmentId": 4,
  "applyDate": "2023-11-01 14:00:00",
  "scrapReason": "设备损坏严重，无法维修，维修成本过高"
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 报废记录ID |
| equipmentId | Long | 设备ID |
| applyDate | DateTime | 申请日期 |
| scrapReason | String | 报废原因 |
| status | Integer | 状态（0-待审批） |
| applicantId | Long | 申请人ID |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "报废申请提交成功",
  "data": {
    "id": 2,
    "equipmentId": 4,
    "applyDate": "2023-11-01 14:00:00",
    "scrapReason": "设备损坏严重，无法维修，维修成本过高",
    "status": 0,
    "applicantId": 2,
    "createTime": "2023-11-01 14:00:00"
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
  "message": "参数错误：申请日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：报废原因不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限提交报废申请",
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

## 4. 审批设备报废申请接口

### 4.1 接口说明
审批设备报废申请（仅管理员可操作）。

### 4.2 接口URL
```
PUT /api/v1/scrap/{id}/approve
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 报废记录ID |
| status | Integer | 是 | 审批结果（1-已通过，2-已拒绝） |
| remark | String | 否 | 审批备注 |

### 4.4 请求示例
```json
{
  "status": 1,
  "remark": "审批通过，设备可进行报废处理"
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 报废记录ID |
| status | Integer | 审批结果 |
| approverId | Long | 审批人ID |
| approverName | String | 审批人姓名 |
| approveTime | DateTime | 审批时间 |
| approveRemark | String | 审批备注 |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "id": 1,
    "status": 1,
    "approverId": 1,
    "approverName": "系统管理员",
    "approveTime": "2023-10-25 10:30:00",
    "approveRemark": "审批通过，设备可进行报废处理",
    "updateTime": "2023-10-25 10:30:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：报废记录ID不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：审批状态无效（必须是1-通过或2-拒绝）",
  "data": null
}

{
  "code": 400,
  "message": "该报废申请已审批过，无法重复审批",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限审批报废申请（仅管理员可操作）",
  "data": null
}

{
  "code": 404,
  "message": "报废记录不存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 5. 获取设备报废统计接口

### 5.1 接口说明
获取设备报废统计数据。

### 5.2 接口URL
```
GET /api/v1/scrap/stats
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| startDate | Date | 否 | 统计起始日期（格式：yyyy-MM-dd） |
| endDate | Date | 否 | 统计结束日期（格式：yyyy-MM-dd） |

### 5.4 请求示例
```
GET /api/v1/scrap/stats?startDate=2023-01-01&endDate=2023-12-31
Authorization: Bearer {token}
```

### 5.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| totalCount | Integer | 总报废申请数 |
| pendingCount | Integer | 待审批数量 |
| approvedCount | Integer | 已通过数量 |
| rejectedCount | Integer | 已拒绝数量 |
| approvalRate | Double | 审批通过率（已通过数量/总报废申请数） |

### 5.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalCount": 15,
    "pendingCount": 2,
    "approvedCount": 11,
    "rejectedCount": 2,
    "approvalRate": 0.87
  }
}
```

### 5.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：统计起始日期格式不正确",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：统计结束日期格式不正确",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看报废统计数据（仅管理员可操作）",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}