# 用户管理模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 获取用户列表接口 | 管理员 | 仅管理员可查看所有用户列表 |
| 获取用户详情接口 | 管理员/当前用户 | 管理员可查看所有用户详情，用户可查看自己的详情 |
| 创建用户接口 | 管理员 | 仅管理员可创建用户 |
| 更新用户接口 | 管理员 | 仅管理员可更新用户信息 |
| 删除用户接口 | 管理员 | 仅管理员可删除用户 |
| 更新用户密码接口 | 管理员/当前用户 | 管理员可重置任何用户密码，用户可修改自己的密码 |
| 批量删除用户接口 | 管理员 | 仅管理员可批量删除用户 |
| 获取角色列表接口 | 管理员/教师 | 管理员和教师可查看角色列表 |

## 1. 获取用户列表接口

### 1.1 接口说明
获取用户列表，支持分页、筛选和排序。

### 1.2 接口URL
```
GET /api/v1/users
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| username | String | 否 | 用户名/学号/工号（模糊查询） |
| name | String | 否 | 姓名（模糊查询） |
| department | String | 否 | 所属院系 |
| roleCode | String | 否 | 角色代码 |
| status | Integer | 否 | 用户状态（0-禁用，1-启用） |
| sortBy | String | 否 | 排序字段，默认createTime |
| sortOrder | String | 否 | 排序方式，asc（升序）或desc（降序），默认desc |

### 1.4 请求示例
```
GET /api/v1/users?page=1&size=10&department=信息中心&status=1
Authorization: Bearer {token}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 用户列表 |
| list[].id | Long | 用户ID |
| list[].username | String | 用户名/学号/工号 |
| list[].name | String | 姓名 |
| list[].email | String | 邮箱 |
| list[].phone | String | 联系电话 |
| list[].department | String | 所属院系 |
| list[].roleCode | String | 角色代码 |
| list[].status | Integer | 用户状态 |
| list[].createTime | DateTime | 创建时间 |
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
        "username": "admin",
        "name": "系统管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "department": "信息中心",
        "roleCode": "admin",
        "status": 1,
        "createTime": "2026-01-14 10:00:00"
      },
      {
        "id": 2,
        "username": "teacher01",
        "name": "张老师",
        "email": "teacher01@example.com",
        "phone": "13800138001",
        "department": "计算机学院",
        "roleCode": "teacher",
        "status": 1,
        "createTime": "2026-01-14 10:05:00"
      }
    ],
    "total": 2,
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
  "message": "无权限查看用户列表",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 2. 获取用户详情接口

### 2.1 接口说明
根据用户ID获取用户详细信息。

### 2.2 接口URL
```
GET /api/v1/users/{id}
```

### 2.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

### 2.4 请求示例
```
GET /api/v1/users/1
Authorization: Bearer {token}
```

### 2.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名/学号/工号 |
| name | String | 姓名 |
| email | String | 邮箱 |
| phone | String | 联系电话 |
| department | String | 所属院系 |
| roleCode | String | 角色代码 |
| status | Integer | 用户状态 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "department": "信息中心",
    "roleCode": "admin",
    "status": 1,
    "createTime": "2026-01-14 10:00:00",
    "updateTime": "2026-01-14 10:00:00"
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
  "message": "无权限查看该用户详情",
  "data": null
}

{
  "code": 404,
  "message": "用户不存在",
  "data": null
}

## 3. 创建用户接口

### 3.1 接口说明
创建新用户。

### 3.2 接口URL
```
POST /api/v1/users
```

### 3.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| username | String | 是 | 用户名/学号/工号 |
| password | String | 是 | 密码 |
| name | String | 是 | 姓名 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 联系电话 |
| department | String | 否 | 所属院系 |
| roleCode | String | 是 | 角色代码（admin/teacher/student） |
| status | Integer | 否 | 用户状态（0-禁用，1-启用），默认1 |

### 3.4 请求示例
```json
{
  "username": "student01",
  "password": "123456",
  "name": "李同学",
  "email": "student01@example.com",
  "phone": "13800138002",
  "department": "计算机学院",
  "roleCode": "student",
  "status": 1
}
```

### 3.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名/学号/工号 |
| name | String | 姓名 |
| email | String | 邮箱 |
| phone | String | 联系电话 |
| department | String | 所属院系 |
| roleCode | String | 角色代码 |
| status | Integer | 用户状态 |
| createTime | DateTime | 创建时间 |

### 3.6 成功响应示例
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "username": "student01",
    "name": "李同学",
    "email": "student01@example.com",
    "phone": "13800138002",
    "department": "计算机学院",
    "roleCode": "student",
    "status": 1,
    "createTime": "2026-01-14 10:10:00"
  }
}
```

### 3.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：用户名、密码、姓名和角色代码不能为空",
  "data": null
}

{
  "code": 403,
  "message": "无权限创建用户",
  "data": null
}

{
  "code": 409,
  "message": "用户名已存在",
  "data": null
}

## 4. 更新用户接口

### 4.1 接口说明
更新用户信息。

### 4.2 接口URL
```
PUT /api/v1/users/{id}
```

### 4.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |
| name | String | 否 | 姓名 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 联系电话 |
| department | String | 否 | 所属院系 |
| roleCode | String | 否 | 角色代码（admin/teacher/student） |
| status | Integer | 否 | 用户状态（0-禁用，1-启用） |

### 4.4 请求示例
```json
{
  "name": "张老师（更新）",
  "email": "teacher01_update@example.com",
  "phone": "13800138010",
  "department": "计算机学院（更新）",
  "roleCode": "teacher",
  "status": 1
}
```

### 4.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名/学号/工号 |
| name | String | 姓名 |
| email | String | 邮箱 |
| phone | String | 联系电话 |
| department | String | 所属院系 |
| roleCode | String | 角色代码 |
| status | Integer | 用户状态 |
| updateTime | DateTime | 更新时间 |

### 4.6 成功响应示例
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 2,
    "username": "teacher01",
    "name": "张老师（更新）",
    "email": "teacher01_update@example.com",
    "phone": "13800138010",
    "department": "计算机学院（更新）",
    "roleCode": "teacher",
    "status": 1,
    "updateTime": "2026-01-14 10:15:00"
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
  "message": "无权限更新用户信息",
  "data": null
}

{
  "code": 404,
  "message": "用户不存在",
  "data": null
}

## 5. 删除用户接口

### 5.1 接口说明
删除用户。

### 5.2 接口URL
```
DELETE /api/v1/users/{id}
```

### 5.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

### 5.4 请求示例
```
DELETE /api/v1/users/3
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
  "message": "无权限删除用户",
  "data": null
}

{
  "code": 404,
  "message": "用户不存在",
  "data": null
}

## 6. 更新用户密码接口

### 6.1 接口说明
更新用户密码。

### 6.2 接口URL
```
PUT /api/v1/users/{id}/password
```

### 6.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |
| oldPassword | String | 否 | 旧密码（当前用户更新自己密码时必填） |
| newPassword | String | 是 | 新密码 |

### 6.4 请求示例
```json
{
  "oldPassword": "admin123",
  "newPassword": "admin456"
}
```

### 6.5 响应参数
无

### 6.6 成功响应示例
```json
{
  "code": 200,
  "message": "密码更新成功",
  "data": null
}
```

### 6.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：新密码不能为空",
  "data": null
}

{
  "code": 400,
  "message": "参数错误：修改自己密码时需要提供旧密码",
  "data": null
}

{
  "code": 401,
  "message": "旧密码错误",
  "data": null
}

{
  "code": 403,
  "message": "无权限修改该用户密码",
  "data": null
}

{
  "code": 404,
  "message": "用户不存在",
  "data": null
}

## 7. 批量删除用户接口

### 7.1 接口说明
批量删除用户。

### 7.2 接口URL
```
DELETE /api/v1/users/batch
```

### 7.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| ids | Array<Long> | 是 | 用户ID列表 |

### 7.4 请求示例
```json
{
  "ids": [3, 4, 5]
}
```

### 7.5 响应参数
无

### 7.6 成功响应示例
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": null
}
```

### 7.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：用户ID列表不能为空",
  "data": null
}

{
  "code": 403,
  "message": "无权限批量删除用户",
  "data": null
}

{
  "code": 500,
  "message": "批量删除失败，请检查用户状态",
  "data": null
}

## 8. 获取角色列表接口

### 8.1 接口说明
获取系统中的角色列表。

### 8.2 接口URL
```
GET /api/v1/roles
```

### 8.3 请求参数
无

### 8.4 请求示例
```
GET /api/v1/roles
Authorization: Bearer {token}
```

### 8.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | Array | 角色列表 |
| list[].id | Long | 角色ID |
| list[].name | String | 角色名称 |
| list[].code | String | 角色代码 |
| list[].description | String | 角色描述 |

### 8.6 成功响应示例
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "系统管理员",
        "code": "admin",
        "description": "系统最高权限角色"
      },
      {
        "id": 2,
        "name": "教师",
        "code": "teacher",
        "description": "教师角色"
      },
      {
        "id": 3,
        "name": "学生",
        "code": "student",
        "description": "学生角色"
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
  "code": 403,
  "message": "无权限查看角色列表",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}
```