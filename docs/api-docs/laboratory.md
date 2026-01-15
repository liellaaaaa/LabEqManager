# 实验室管理模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 获取实验室列表接口 | 管理员/教师/学生 | 所有角色均可查看实验室列表 |
| 获取实验室详情接口 | 管理员/教师/学生 | 所有角色均可查看实验室详情 |
| 创建实验室接口 | 管理员 | 仅管理员可创建实验室 |
| 更新实验室接口 | 管理员/实验室负责人 | 管理员可更新所有实验室，实验室负责人可更新自己负责的实验室 |
| 删除实验室接口 | 管理员 | 仅管理员可删除实验室 |
| 批量删除实验室接口 | 管理员 | 仅管理员可批量删除实验室 |
| 更新实验室状态接口 | 管理员/实验室负责人 | 管理员可更新所有实验室状态，实验室负责人可更新自己负责的实验室状态 |
| 获取实验室设备列表接口 | 管理员/教师 | 管理员和教师可查看实验室设备列表 |

## 1. 获取实验室列表接口

### 1.1 接口说明
获取实验室列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/laboratory
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| name | String | 否 | 实验室名称（模糊查询） |
| code | String | 否 | 实验室编号（模糊查询） |
| location | String | 否 | 实验室位置（模糊查询） |
| type | String | 否 | 实验室类型 |
| status | Integer | 否 | 状态（0-不可用，1-可用，2-维护中） |
| managerId | Long | 否 | 负责人ID |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/laboratory?page=1&size=10&type=计算机实验室&status=1
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 实验室列表 |
| list[].id | Long | 实验室ID |
| list[].name | String | 实验室名称 |
| list[].code | String | 实验室编号 |
| list[].location | String | 实验室位置 |
| list[].area | Double | 实验室面积 |
| list[].capacity | Integer | 容纳人数 |
| list[].type | String | 实验室类型 |
| list[].status | Integer | 状态（0-不可用，1-可用，2-维护中） |
| list[].managerId | Long | 负责人ID |
| list[].managerName | String | 负责人姓名 |
| list[].description | String | 实验室描述 |
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
        "name": "计算机基础实验室",
        "code": "LAB001",
        "location": "科技楼301",
        "area": 120.5,
        "capacity": 50,
        "type": "计算机实验室",
        "status": 1,
        "managerId": 2,
        "managerName": "张老师",
        "description": "用于计算机基础课程教学",
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

### 1.7 错误响应示例
```json
{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看实验室列表",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取实验室详情接口

### 2.1 接口说明
根据实验室ID获取实验室详细信息。

### 2.2 接口URL
```
GET /api/v1/laboratory/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 实验室ID |

### 2.4 请求示例
```
GET /api/v1/laboratory/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 实验室ID |
| name | String | 实验室名称 |
| code | String | 实验室编号 |
| location | String | 实验室位置 |
| area | Double | 实验室面积 |
| capacity | Integer | 容纳人数 |
| type | String | 实验室类型 |
| status | Integer | 状态（0-不可用，1-可用，2-维护中） |
| managerId | Long | 负责人ID |
| managerName | String | 负责人姓名 |
| description | String | 实验室描述 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |
| equipmentCount | Integer | 设备数量 |

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "计算机基础实验室",
    "code": "LAB001",
    "location": "科技楼301",
    "area": 120.5,
    "capacity": 50,
    "type": "计算机实验室",
    "status": 1,
    "managerId": 2,
    "managerName": "张老师",
    "description": "用于计算机基础课程教学",
    "createTime": "2023-09-10 10:00:00",
    "updateTime": "2023-09-10 10:00:00",
    "equipmentCount": 50
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
  "message": "无权限查看该实验室详情",
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

## 3. 创建实验室接口

### 3.1 接口说明
创建新实验室。

### 3.2 接口URL
```
POST /api/v1/laboratory
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| name | String | 是 | 实验室名称 |
| code | String | 是 | 实验室编号 |
| location | String | 是 | 实验室位置 |
| area | Double | 否 | 实验室面积 |
| capacity | Integer | 否 | 容纳人数 |
| type | String | 否 | 实验室类型 |
| status | Integer | 否 | 状态（0-不可用，1-可用，2-维护中），默认1 |
| managerId | Long | 否 | 负责人ID |
| description | String | 否 | 实验室描述 |

### 3.4 请求示例
```json
{
  "name": "计算机网络实验室",
  "code": "LAB002",
  "location": "科技楼402",
  "area": 150.0,
  "capacity": 40,
  "type": "计算机实验室",
  "status": 1,
  "managerId": 2,
  "description": "用于计算机网络课程教学"
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 实验室ID |
| name | String | 实验室名称 |
| code | String | 实验室编号 |
| location | String | 实验室位置 |
| area | Double | 实验室面积 |
| capacity | Integer | 容纳人数 |
| type | String | 实验室类型 |
| status | Integer | 状态 |
| managerId | Long | 负责人ID |
| description | String | 实验室描述 |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "name": "计算机网络实验室",
    "code": "LAB002",
    "location": "科技楼402",
    "area": 150.0,
    "capacity": 40,
    "type": "计算机实验室",
    "status": 1,
    "managerId": 2,
    "description": "用于计算机网络课程教学",
    "createTime": "2023-10-15 14:30:00"
  }
}
```

### 3.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室名称不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：实验室编号已存在",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限创建实验室",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 4. 更新实验室接口

### 4.1 接口说明
更新实验室信息。

### 4.2 接口URL
```
PUT /api/v1/laboratory/{id}
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 实验室ID |
| name | String | 否 | 实验室名称 |
| code | String | 否 | 实验室编号 |
| location | String | 否 | 实验室位置 |
| area | Double | 否 | 实验室面积 |
| capacity | Integer | 否 | 容纳人数 |
| type | String | 否 | 实验室类型 |
| managerId | Long | 否 | 负责人ID |
| description | String | 否 | 实验室描述 |

### 4.4 请求示例
```json
{
  "name": "计算机基础实验室（更新）",
  "location": "科技楼301（更新）",
  "area": 130.5,
  "capacity": 55,
  "description": "用于计算机基础课程教学（更新）"
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 实验室ID |
| name | String | 实验室名称 |
| code | String | 实验室编号 |
| location | String | 实验室位置 |
| area | Double | 实验室面积 |
| capacity | Integer | 容纳人数 |
| type | String | 实验室类型 |
| status | Integer | 状态 |
| managerId | Long | 负责人ID |
| description | String | 实验室描述 |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "计算机基础实验室（更新）",
    "code": "LAB001",
    "location": "科技楼301（更新）",
    "area": 130.5,
    "capacity": 55,
    "type": "计算机实验室",
    "status": 1,
    "managerId": 2,
    "description": "用于计算机基础课程教学（更新）",
    "updateTime": "2023-10-15 15:00:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室编号已存在",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限更新该实验室",
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

## 5. 删除实验室接口

### 5.1 接口说明
删除实验室。

### 5.2 接口URL
```
DELETE /api/v1/laboratory/{id}
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 实验室ID |

### 5.4 请求示例
```
DELETE /api/v1/laboratory/2
Authorization: Bearer {token}
```

### 5.5 响应参数
无

### 5.6 成功响应示例
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 5.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室ID不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限删除该实验室",
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

## 6. 批量删除实验室接口

### 6.1 接口说明
批量删除实验室。

### 6.2 接口URL
```
DELETE /api/v1/laboratory/batch
```

### 6.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| ids | Array<Long> | 是 | 实验室ID列表 |

### 6.4 请求示例
```json
{
  "ids": [2, 3]
}
```

### 6.5 响应参数
无

### 6.6 成功响应示例
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": null
}
```

### 6.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：实验室ID列表不能为空",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限批量删除实验室",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 7. 更新实验室状态接口

### 7.1 接口说明
更新实验室状态。

### 7.2 接口URL
```
PUT /api/v1/laboratory/{id}/status
```

### 7.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 实验室ID |
| status | Integer | 是 | 新的状态（0-不可用，1-可用，2-维护中） |

### 7.4 请求示例
```json
{
  "status": 2
}
```

### 7.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 实验室ID |
| status | Integer | 实验室状态 |
| updateTime | DateTime | 更新时间 |

### 7.6 成功响应示例
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": {
    "id": 1,
    "status": 2,
    "updateTime": "2023-10-15 15:30:00"
  }
}
```

### 7.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：状态值无效",
  "data": null
}

{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限更新该实验室状态",
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

## 8. 获取实验室设备列表接口

### 8.1 接口说明
获取指定实验室的设备列表。

### 8.2 接口URL
```
GET /api/v1/laboratory/{id}/equipment
```

### 8.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 实验室ID |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| statusCode | String | 否 | 设备状态代码（pending/instored/inuse/repairing/scrapped） |

### 8.4 请求示例
```
GET /api/v1/laboratory/1/equipment?page=1&size=10&statusCode=instored
Authorization: Bearer {token}
```

### 8.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 设备列表 |
| list[].id | Long | 设备ID |
| list[].name | String | 设备名称 |
| list[].model | String | 设备型号 |
| list[].assetCode | String | 资产编号 |
| list[].statusName | String | 设备状态名称 |
| list[].statusCode | String | 设备状态代码 |
| list[].quantity | Integer | 数量 |
| total | Long | 总记录数 |
| page | Integer | 当前页码 |
| size | Integer | 每页条数 |

### 8.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "联想台式电脑",
        "model": "ThinkCentre M720t",
        "assetCode": "EQ20230001",
        "statusName": "已入库",
        "statusCode": "instored",
        "quantity": 55
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
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
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限查看实验室设备列表",
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

## 9. 实验室预约模块

详细接口见：[实验室预约模块接口文档](./reservation.md)