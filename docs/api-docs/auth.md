# 认证模块接口文档

## 权限说明

| 接口名称 | 权限要求 | 描述 |
|--------|---------|------|
| 登录接口 | 所有角色 | 公开访问，无需登录 |
| 注销接口 | 已登录用户 | 所有已登录用户均可访问 |
| 获取当前用户信息接口 | 已登录用户 | 所有已登录用户均可访问 |

## 1. 登录接口

### 1.1 接口说明
用户登录接口，验证用户名和密码，返回JWT Token。

### 1.2 接口URL
```
POST /api/v1/auth/login
```

### 1.3 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| username | String | 是 | 用户名/学号/工号 |
| password | String | 是 | 密码 |

### 1.4 请求示例
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### 1.5 响应参数
| 参数名 | 类型 | 描述 |
|--------|------|------|
| token | String | JWT Token |
| userInfo | Object | 用户基本信息 |
| userInfo.id | Long | 用户ID |
| userInfo.username | String | 用户名 |
| userInfo.name | String | 姓名 |
| userInfo.email | String | 邮箱 |
| userInfo.phone | String | 联系电话 |
| userInfo.department | String | 所属院系 |
| userInfo.roleCode | String | 角色代码 |
| userInfo.status | Integer | 用户状态 |

### 1.6 成功响应示例
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "department": "信息中心",
      "roleCode": "admin",
      "status": 1
    }
  }
}
```

### 1.7 错误响应示例
```json
{
  "code": 400,
  "message": "参数错误：用户名和密码不能为空",
  "data": null
}

{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}

{
  "code": 401,
  "message": "用户已被禁用",
  "data": null
}

## 2. 注销接口

### 2.1 接口说明
用户注销接口，清除用户登录状态。

### 2.2 接口URL
```
POST /api/v1/auth/logout
```

### 2.3 请求参数
无

### 2.4 请求示例
```
POST /api/v1/auth/logout
Authorization: Bearer {token}
```

### 2.5 响应参数
无

### 2.6 成功响应示例
```json
{
  "code": 200,
  "message": "注销成功",
  "data": null
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
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}

## 3. 获取当前用户信息接口

### 3.1 接口说明
获取当前登录用户的详细信息。

### 3.2 接口URL
```
GET /api/v1/auth/me
```

### 3.3 请求参数
无

### 3.4 请求示例
```
GET /api/v1/auth/me
Authorization: Bearer {token}
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
| updateTime | DateTime | 更新时间 |

### 3.6 成功响应示例
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

### 3.7 错误响应示例
```json
{
  "code": 401,
  "message": "未授权访问，请先登录",
  "data": null
}

{
  "code": 401,
  "message": "Token已过期，请重新登录",
  "data": null
}

{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}
```