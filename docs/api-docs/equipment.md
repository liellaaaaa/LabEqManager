# 设备管理模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 获取设备列表接口 | 管理员/教师/学生 | 管理员可查看所有设备，教师可查看所属实验室设备，学生可查看可借用设备 |
| 获取设备详情接口 | 管理员/教师/学生 | 管理员可查看所有设备详情，教师可查看所属实验室设备详情，学生可查看可借用设备详情 |
| 创建设备接口 | 管理员 | 仅管理员可创建设备 |
| 更新设备接口 | 管理员 | 仅管理员可更新设备信息 |
| 删除设备接口 | 管理员 | 仅管理员可删除设备 |
| 批量删除设备接口 | 管理员 | 仅管理员可批量删除设备 |
| 更新设备状态接口 | 管理员/教师 | 管理员可更新所有设备状态，教师可更新所属实验室设备状态 |
| 获取设备状态列表接口 | 管理员/教师/学生 | 所有角色均可查看设备状态列表 |
| 导入设备数据接口 | 管理员 | 仅管理员可导入设备数据 |
| 导出设备数据接口 | 管理员/教师 | 管理员可导出所有设备数据，教师可导出所属实验室设备数据 |

## 1. 获取设备列表接口

### 1.1 接口说明
获取设备列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/equipment
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| name | String | 否 | 设备名称（模糊查询） |
| model | String | 否 | 设备型号（模糊查询） |
| specification | String | 否 | 设备规格（模糊查询） |
| assetCode | String | 否 | 资产编号 |
| supplier | String | 否 | 供应商 |
| statusCode | String | 否 | 设备状态代码（pending/instored/inuse/repairing/scrapped） |
| laboratoryId | Long | 否 | 所属实验室ID |
| purchaseDateStart | Date | 否 | 购置日期起始（格式：yyyy-MM-dd） |
| purchaseDateEnd | Date | 否 | 购置日期结束（格式：yyyy-MM-dd） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/equipment?page=1&size=10&name=电脑&statusCode=instored&laboratoryId=1
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 设备列表 |
| list[].id | Long | 设备ID |
| list[].name | String | 设备名称 |
| list[].model | String | 设备型号 |
| list[].specification | String | 设备规格 |
| list[].assetCode | String | 资产编号 |
| list[].unitPrice | Double | 单价 |
| list[].quantity | Integer | 数量 |
| list[].supplier | String | 供应商 |
| list[].purchaseDate | Date | 购置日期 |
| list[].warrantyPeriod | Integer | 保修期（月） |
| list[].statusId | Long | 设备状态ID |
| list[].statusName | String | 设备状态名称 |
| list[].statusCode | String | 设备状态代码 |
| list[].laboratoryId | Long | 所属实验室ID |
| list[].laboratoryName | String | 所属实验室名称 |
| list[].description | String | 设备描述 |
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
        "name": "联想台式电脑",
        "model": "ThinkCentre M720t",
        "specification": "i5-9500/8G/256G SSD/1T HDD",
        "assetCode": "EQ20230001",
        "unitPrice": 4500.00,
        "quantity": 50,
        "supplier": "联想科技有限公司",
        "purchaseDate": "2023-09-10",
        "warrantyPeriod": 36,
        "statusId": 2,
        "statusName": "已入库",
        "statusCode": "instored",
        "laboratoryId": 1,
        "laboratoryName": "计算机基础实验室",
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
  "message": "无权限查看设备列表",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取设备详情接口

### 2.1 接口说明
根据设备ID获取设备详细信息。

### 2.2 接口URL
```
GET /api/v1/equipment/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 设备ID |

### 2.4 请求示例
```
GET /api/v1/equipment/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 设备ID |
| name | String | 设备名称 |
| model | String | 设备型号 |
| specification | String | 设备规格 |
| assetCode | String | 资产编号 |
| unitPrice | Double | 单价 |
| quantity | Integer | 数量 |
| supplier | String | 供应商 |
| purchaseDate | Date | 购置日期 |
| warrantyPeriod | Integer | 保修期（月） |
| statusId | Long | 设备状态ID |
| statusName | String | 设备状态名称 |
| statusCode | String | 设备状态代码 |
| laboratoryId | Long | 所属实验室ID |
| laboratoryName | String | 所属实验室名称 |
| description | String | 设备描述 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "联想台式电脑",
    "model": "ThinkCentre M720t",
    "specification": "i5-9500/8G/256G SSD/1T HDD",
    "assetCode": "EQ20230001",
    "unitPrice": 4500.00,
    "quantity": 50,
    "supplier": "联想科技有限公司",
    "purchaseDate": "2023-09-10",
    "warrantyPeriod": 36,
    "statusId": 2,
    "statusName": "已入库",
    "statusCode": "instored",
    "laboratoryId": 1,
    "laboratoryName": "计算机基础实验室",
    "description": "用于计算机基础课程教学",
    "createTime": "2023-09-10 10:00:00",
    "updateTime": "2023-09-10 10:00:00"
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
  "message": "无权限查看该设备详情",
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

## 3. 创建设备接口

### 3.1 接口说明
创建新设备。

### 3.2 接口URL
```
POST /api/v1/equipment
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| name | String | 是 | 设备名称 |
| model | String | 是 | 设备型号 |
| specification | String | 否 | 设备规格 |
| assetCode | String | 否 | 资产编号 |
| unitPrice | Double | 是 | 单价 |
| quantity | Integer | 否 | 数量，默认1 |
| supplier | String | 否 | 供应商 |
| purchaseDate | Date | 是 | 购置日期（格式：yyyy-MM-dd） |
| warrantyPeriod | Integer | 否 | 保修期（月） |
| statusId | Long | 是 | 设备状态ID |
| laboratoryId | Long | 是 | 所属实验室ID |
| description | String | 否 | 设备描述 |

### 3.4 请求示例
```json
{
  "name": "戴尔笔记本电脑",
  "model": "Latitude 5420",
  "specification": "i7-1165G7/16G/512G SSD",
  "assetCode": "EQ20230002",
  "unitPrice": 7800.00,
  "quantity": 20,
  "supplier": "戴尔（中国）有限公司",
  "purchaseDate": "2023-10-15",
  "warrantyPeriod": 24,
  "statusId": 2,
  "laboratoryId": 1,
  "description": "用于高级程序设计课程教学"
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 设备ID |
| name | String | 设备名称 |
| model | String | 设备型号 |
| specification | String | 设备规格 |
| assetCode | String | 资产编号 |
| unitPrice | Double | 单价 |
| quantity | Integer | 数量 |
| supplier | String | 供应商 |
| purchaseDate | Date | 购置日期 |
| warrantyPeriod | Integer | 保修期（月） |
| statusId | Long | 设备状态ID |
| laboratoryId | Long | 所属实验室ID |
| description | String | 设备描述 |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "name": "戴尔笔记本电脑",
    "model": "Latitude 5420",
    "specification": "i7-1165G7/16G/512G SSD",
    "assetCode": "EQ20230002",
    "unitPrice": 7800.00,
    "quantity": 20,
    "supplier": "戴尔（中国）有限公司",
    "purchaseDate": "2023-10-15",
    "warrantyPeriod": 24,
    "statusId": 2,
    "laboratoryId": 1,
    "description": "用于高级程序设计课程教学",
    "createTime": "2023-10-15 14:30:00"
  }
}
```

### 3.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：设备名称、型号、单价、购置日期、状态ID和实验室ID不能为空",
  "data": null
}

{
  "code": 403,
  "message": "无权限创建设备",
  "data": null
}

{
  "code": 409,
  "message": "资产编号已存在",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 4. 更新设备接口

### 4.1 接口说明
更新设备信息。

### 4.2 接口URL
```
PUT /api/v1/equipment/{id}
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 设备ID |
| name | String | 否 | 设备名称 |
| model | String | 否 | 设备型号 |
| specification | String | 否 | 设备规格 |
| assetCode | String | 否 | 资产编号 |
| unitPrice | Double | 否 | 单价 |
| quantity | Integer | 否 | 数量 |
| supplier | String | 否 | 供应商 |
| purchaseDate | Date | 否 | 购置日期（格式：yyyy-MM-dd） |
| warrantyPeriod | Integer | 否 | 保修期（月） |
| laboratoryId | Long | 否 | 所属实验室ID |
| description | String | 否 | 设备描述 |

### 4.4 请求示例
```json
{
  "name": "联想台式电脑（更新）",
  "model": "ThinkCentre M720t",
  "specification": "i5-9500/16G/256G SSD/1T HDD",
  "unitPrice": 4800.00,
  "quantity": 55,
  "laboratoryId": 1,
  "description": "用于计算机基础课程教学（更新）"
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 设备ID |
| name | String | 设备名称 |
| model | String | 设备型号 |
| specification | String | 设备规格 |
| assetCode | String | 资产编号 |
| unitPrice | Double | 单价 |
| quantity | Integer | 数量 |
| supplier | String | 供应商 |
| purchaseDate | Date | 购置日期 |
| warrantyPeriod | Integer | 保修期（月） |
| statusId | Long | 设备状态ID |
| laboratoryId | Long | 所属实验室ID |
| description | String | 设备描述 |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "联想台式电脑（更新）",
    "model": "ThinkCentre M720t",
    "specification": "i5-9500/16G/256G SSD/1T HDD",
    "assetCode": "EQ20230001",
    "unitPrice": 4800.00,
    "quantity": 55,
    "supplier": "联想科技有限公司",
    "purchaseDate": "2023-09-10",
    "warrantyPeriod": 36,
    "statusId": 2,
    "laboratoryId": 1,
    "description": "用于计算机基础课程教学（更新）",
    "updateTime": "2023-10-15 15:00:00"
  }
}
```

### 4.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：至少需要更新一个字段",
  "data": null
}

{
  "code": 403,
  "message": "无权限更新设备信息",
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

## 5. 删除设备接口

### 5.1 接口说明
删除设备。

### 5.2 接口URL
```
DELETE /api/v1/equipment/{id}
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 设备ID |

### 5.4 请求示例
```
DELETE /api/v1/equipment/2
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
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 403,
  "message": "无权限删除设备",
  "data": null
}

{
  "code": 404,
  "message": "设备不存在",
  "data": null
}

{
  "code": 409,
  "message": "设备正在被使用，无法删除",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 6. 批量删除设备接口

### 6.1 接口说明
批量删除设备。

### 6.2 接口URL
```
DELETE /api/v1/equipment/batch
```

### 6.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| ids | Array<Long> | 是 | 设备ID列表 |

### 6.4 请求示例
```json
{
  "ids": [2, 3, 4]
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
  "message": "参数错误：设备ID列表不能为空",
  "data": null
}

{
  "code": 403,
  "message": "无权限批量删除设备",
  "data": null
}

{
  "code": 409,
  "message": "部分设备正在被使用，无法删除",
  "data": {
    "failedIds": [2, 3],
    "message": "设备ID 2和3正在被使用，无法删除"
  }
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 7. 更新设备状态接口

### 7.1 接口说明
更新设备状态。

### 7.2 接口URL
```
PUT /api/v1/equipment/{id}/status
```

### 7.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 设备ID |
| statusId | Long | 是 | 新的设备状态ID |

### 7.4 请求示例
```json
{
  "statusId": 3
}
```

### 7.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 设备ID |
| statusId | Long | 设备状态ID |
| statusName | String | 设备状态名称 |
| statusCode | String | 设备状态代码 |
| updateTime | DateTime | 更新时间 |

### 7.6 成功响应示例
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": {
    "id": 1,
    "statusId": 3,
    "statusName": "使用中",
    "statusCode": "inuse",
    "updateTime": "2023-10-15 15:30:00"
  }
}
```

### 7.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：状态ID不能为空",
  "data": null
}

{
  "code": 403,
  "message": "无权限更新设备状态",
  "data": null
}

{
  "code": 404,
  "message": "设备不存在或状态ID不存在",
  "data": null
}

{
  "code": 409,
  "message": "设备状态流转不合法",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 8. 获取设备状态列表接口

### 8.1 接口说明
获取设备状态列表。

### 8.2 接口URL
```
GET /api/v1/equipment/status
```

### 8.3 请求参数
无

### 8.4 请求示例
```
GET /api/v1/equipment/status
Authorization: Bearer {token}
```

### 8.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 设备状态列表 |
| list[].id | Long | 状态ID |
| list[].name | String | 状态名称 |
| list[].code | String | 状态代码 |
| list[].description | String | 状态描述 |

### 8.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "待入库",
        "code": "pending",
        "description": "设备已采购但尚未入库"
      },
      {
        "id": 2,
        "name": "已入库",
        "code": "instored",
        "description": "设备已入库且可使用"
      },
      {
        "id": 3,
        "name": "使用中",
        "code": "inuse",
        "description": "设备正在使用中"
      },
      {
        "id": 4,
        "name": "维修中",
        "code": "repairing",
        "description": "设备正在维修中"
      },
      {
        "id": 5,
        "name": "报废",
        "code": "scrapped",
        "description": "设备已报废"
      }
    ]
  }
}
```

### 8.7 错误响应示例
```json
{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 9. 导入设备数据接口

### 9.1 接口说明
批量导入设备数据，支持Excel和CSV格式。

### 9.2 接口URL
```
POST /api/v1/equipment/import
```

### 9.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| file | File | 是 | 设备数据文件 |
| fileType | String | 是 | 文件类型（excel/csv） |

### 9.4 请求示例
```
POST /api/v1/equipment/import
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: (文件)
fileType: excel
```

### 9.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| successCount | Integer | 成功导入数量 |
| failCount | Integer | 失败导入数量 |
| failList | Array | 失败导入的设备信息列表 |

### 9.6 成功响应示例
```json
{
  "code": 200,
  "message": "导入成功",
  "data": {
    "successCount": 18,
    "failCount": 2,
    "failList": [
      {
        "row": 5,
        "name": "测试设备",
        "errorMsg": "资产编号已存在"
      },
      {
        "row": 12,
        "name": "测试设备2",
        "errorMsg": "实验室不存在"
      }
    ]
  }
}
```

### 9.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：文件不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：文件类型不支持，仅支持excel和csv格式",
  "data": null
}

{
  "code": 403,
  "message": "无权限导入设备数据",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误：导入失败",
  "data": null
}

## 10. 导出设备数据接口

### 10.1 接口说明
导出设备数据，支持Excel和CSV格式。

### 10.2 接口URL
```
GET /api/v1/equipment/export
```

### 10.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| fileType | String | 是 | 文件类型（excel/csv） |
| ids | Array<Long> | 否 | 设备ID列表（为空时导出全部） |

### 10.4 请求示例
```
GET /api/v1/equipment/export?fileType=excel&ids=1,2,3
Authorization: Bearer {token}
```

### 10.5 响应参数
文件流

### 10.6 成功响应示例
```
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=equipment_data_20231015.xlsx

(文件流)
```

### 10.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：文件类型不支持，仅支持excel和csv格式",
  "data": null
}

{
  "code": 403,
  "message": "无权限导出设备数据",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误：导出失败",
  "data": null
}