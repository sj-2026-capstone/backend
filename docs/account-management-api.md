# Account Management API

계정 관리 및 기준정보 API 연동 문서. `auth`, `admin`, `line`, `shift` 도메인을 다룬다.

---

## 공통

### Base URL

```
http://localhost:8080
```

> ngrok 연결 시 ngrok URL로 교체한다.

### 인증 헤더

로그인 API를 제외한 모든 API는 JWT 토큰이 필요하다.

```http
Authorization: Bearer {accessToken}
```

### 응답 형식

**성공**

```json
{
  "success": true,
  "message": "처리 메시지",
  "data": {}
}
```

**실패**

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

### 에러 코드 목록

| code | HTTP | 발생 상황 |
|---|---|---|
| `AUTH_NOT_FOUND` | 404 | loginId가 존재하지 않음 |
| `INVALID_PASSWORD` | 400 | 비밀번호 불일치 |
| `PASSWORD_CONFIRM_MISMATCH` | 400 | 새 비밀번호와 확인 비밀번호 불일치 |
| `DUPLICATE_LOGIN_ID` | 409 | 이미 사용 중인 loginId |
| `DUPLICATE_EMAIL` | 409 | 이미 사용 중인 이메일 |
| `USER_NOT_FOUND` | 404 | 존재하지 않는 사용자 |
| `SHIFT_NOT_FOUND` | 404 | 존재하지 않는 교대조 |
| `LINE_NOT_FOUND` | 404 | 존재하지 않는 라인 |
| `SHIFT_REQUIRED_FOR_WORKER` | 400 | WORKER 계정 생성/수정 시 shiftId 누락 |
| `LINE_REQUIRED_FOR_WORKER` | 400 | WORKER 계정 생성/수정 시 lineId 누락 |
| `UNAUTHORIZED` | 401 | 토큰 없음 또는 만료 |
| `FORBIDDEN` | 403 | 권한 없음 (ADMIN 전용 API를 WORKER가 호출) |

### Enum 값

| 타입 | 값 |
|---|---|
| `role` | `ADMIN`, `WORKER` |
| `status` | `ACTIVE`, `INACTIVE`, `PENDING` |
| `shiftType` | `DAY`, `EVENING`, `NIGHT` |
| `lineCode` | `A`, `B`, `C` |

---

## Auth API

### 로그인

로그인 성공 시 발급된 `accessToken`을 이후 모든 API 요청에 사용한다.  
`passwordChangeRequired: true`이면 비밀번호 변경 화면으로 이동시킨다.

```
POST /api/auth/login
```

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `loginId` | string | Y | 최대 50자 |
| `password` | string | Y | - |

```json
{
  "loginId": "worker01",
  "password": "password123"
}
```

**Response (200)**

```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "passwordChangeRequired": true
  }
}
```

**Error**

| code | 상황 |
|---|---|
| `AUTH_NOT_FOUND` | loginId 없음 |
| `INVALID_PASSWORD` | 비밀번호 틀림 |

---

### 내 정보 조회

현재 로그인한 사용자의 프로필을 반환한다.

```
GET /api/auth/me
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "현재 사용자 조회 성공",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "ACTIVE",
    "shiftId": 1,
    "shiftName": "1조",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": false
  }
}
```

> `shiftId`, `shiftName`, `lineId`, `lineCode`, `lineName`, `email`, `phone`은 미설정 시 `null`.

---

### 비밀번호 변경

현재 비밀번호 확인 후 새 비밀번호로 변경한다.

```
PATCH /api/auth/password
Authorization: Bearer {accessToken}
```

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `currentPassword` | string | Y | - |
| `newPassword` | string | Y | 최소 8자 |
| `confirmPassword` | string | Y | newPassword와 동일해야 함 |

```json
{
  "currentPassword": "password123",
  "newPassword": "newPassword123!",
  "confirmPassword": "newPassword123!"
}
```

**Response (200)**

```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
```

**Error**

| code | 상황 |
|---|---|
| `INVALID_PASSWORD` | 현재 비밀번호 틀림 |
| `PASSWORD_CONFIRM_MISMATCH` | newPassword와 confirmPassword 불일치 |

---

## Admin Account API

> 모든 Admin API는 `ADMIN` 권한 필요. `WORKER` 토큰으로 호출 시 `403 FORBIDDEN`.

### 계정 생성

ADMIN이 직접 계정을 생성한다. 생성된 계정은 `passwordChangeRequired: true` 상태이며, 최초 로그인 후 비밀번호 변경이 필요하다.

```
POST /api/admin/accounts
Authorization: Bearer {accessToken}
```

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `userName` | string | Y | 최대 100자 |
| `loginId` | string | Y | 최대 50자, 중복 불가 |
| `password` | string | Y | 최소 8자 |
| `confirmPassword` | string | Y | password와 동일해야 함 |
| `role` | string | Y | `ADMIN` 또는 `WORKER` |
| `shiftId` | number | WORKER 필수 | ADMIN은 nullable |
| `lineId` | number | WORKER 필수 | ADMIN은 nullable |
| `email` | string | N | 이메일 형식, 최대 100자 |
| `phone` | string | N | 최대 20자 |

```json
{
  "userName": "홍길동",
  "loginId": "worker01",
  "password": "password123!",
  "confirmPassword": "password123!",
  "role": "WORKER",
  "shiftId": 1,
  "lineId": 1,
  "email": "worker01@example.com",
  "phone": "010-1234-5678"
}
```

**Response (201)**

```json
{
  "success": true,
  "message": "계정이 생성되었습니다.",
  "data": {
    "userId": 5,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "ACTIVE",
    "shiftId": 1,
    "shiftName": "1조",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": true
  }
}
```

**Error**

| code | 상황 |
|---|---|
| `DUPLICATE_LOGIN_ID` | loginId 중복 |
| `DUPLICATE_EMAIL` | 이메일 중복 |
| `PASSWORD_CONFIRM_MISMATCH` | 비밀번호 확인 불일치 |
| `SHIFT_REQUIRED_FOR_WORKER` | WORKER인데 shiftId 없음 |
| `LINE_REQUIRED_FOR_WORKER` | WORKER인데 lineId 없음 |
| `SHIFT_NOT_FOUND` | 존재하지 않는 shiftId |
| `LINE_NOT_FOUND` | 존재하지 않는 lineId |

---

### 로그인 ID 중복 확인

계정 생성 폼에서 loginId 입력 시 실시간 중복 확인에 사용한다.

```
GET /api/admin/accounts/login-id/availability?loginId=worker01
Authorization: Bearer {accessToken}
```

**Query Parameter**

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `loginId` | Y | 중복 확인할 로그인 ID |

**Response (200)**

```json
{
  "success": true,
  "message": "로그인 ID 사용 가능 여부 조회 성공",
  "data": {
    "loginId": "worker01",
    "available": false
  }
}
```

> `available: true` → 사용 가능, `available: false` → 중복

---

### 계정 요약 통계

계정 관리 화면 상단의 요약 카드에 사용한다.

```
GET /api/admin/accounts/summary
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "계정 요약 조회 성공",
  "data": {
    "totalCount": 12,
    "activeCount": 10,
    "inactiveCount": 1,
    "pendingCount": 1
  }
}
```

---

### 계정 목록 조회

```
GET /api/admin/accounts?keyword=홍길동&status=ACTIVE&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `keyword` | N | - | 이름 또는 loginId 검색 |
| `status` | N | - | `ACTIVE`, `INACTIVE`, `PENDING` |
| `page` | N | `0` | 페이지 번호 (0부터 시작) |
| `size` | N | `10` | 페이지당 항목 수 |

**Response (200)**

```json
{
  "success": true,
  "message": "계정 목록 조회 성공",
  "data": {
    "items": [
      {
        "userId": 1,
        "userName": "홍길동",
        "loginId": "worker01",
        "role": "WORKER",
        "status": "ACTIVE",
        "shiftId": 1,
        "shiftName": "1조",
        "lineId": 1,
        "lineCode": "A",
        "lineName": "A라인"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

---

### 계정 상세 조회

```
GET /api/admin/accounts/{userId}
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "계정 상세 조회 성공",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "ACTIVE",
    "shiftId": 1,
    "shiftName": "1조",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": false
  }
}
```

**Error**

| code | 상황 |
|---|---|
| `USER_NOT_FOUND` | 존재하지 않는 userId |

---

### 계정 수정

```
PUT /api/admin/accounts/{userId}
Authorization: Bearer {accessToken}
```

**Request Body**

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| `userName` | string | Y | 최대 100자 |
| `loginId` | string | Y | 최대 50자 |
| `role` | string | Y | `ADMIN` 또는 `WORKER` |
| `status` | string | Y | `ACTIVE`, `INACTIVE`, `PENDING` |
| `shiftId` | number | WORKER 필수 | ADMIN은 nullable |
| `lineId` | number | WORKER 필수 | ADMIN은 nullable |
| `email` | string | N | 이메일 형식, 최대 100자 |
| `phone` | string | N | 최대 20자 |

```json
{
  "userName": "홍길동",
  "loginId": "worker01",
  "role": "WORKER",
  "status": "ACTIVE",
  "shiftId": 2,
  "lineId": 2,
  "email": "worker01@example.com",
  "phone": "010-9999-8888"
}
```

**Response (200)**

```json
{
  "success": true,
  "message": "계정 정보가 수정되었습니다.",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "ACTIVE",
    "shiftId": 2,
    "shiftName": "2조",
    "lineId": 2,
    "lineCode": "B",
    "lineName": "B라인",
    "email": "worker01@example.com",
    "phone": "010-9999-8888",
    "passwordChangeRequired": false
  }
}
```

**Error**

| code | 상황 |
|---|---|
| `USER_NOT_FOUND` | 존재하지 않는 userId |
| `DUPLICATE_LOGIN_ID` | 변경하려는 loginId가 다른 계정에서 사용 중 |
| `DUPLICATE_EMAIL` | 변경하려는 이메일이 다른 계정에서 사용 중 |
| `SHIFT_REQUIRED_FOR_WORKER` | WORKER인데 shiftId 없음 |
| `LINE_REQUIRED_FOR_WORKER` | WORKER인데 lineId 없음 |

---

### 계정 상태 변경

계정의 상태만 단독으로 변경할 때 사용한다.

```
PATCH /api/admin/accounts/{userId}/status
Authorization: Bearer {accessToken}
```

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `status` | string | Y | `ACTIVE`, `INACTIVE`, `PENDING` |

```json
{
  "status": "INACTIVE"
}
```

**Response (200)**

```json
{
  "success": true,
  "message": "계정 상태가 변경되었습니다.",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "INACTIVE",
    "shiftId": 1,
    "shiftName": "1조",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": false
  }
}
```

---

## Line API

> 로그인된 사용자(ADMIN, WORKER) 모두 접근 가능.

### 라인 목록 조회

계정 생성/수정 폼의 라인 선택 드롭다운에 사용한다. 앱 시작 시 A, B, C 3개가 자동 삽입된다.

```
GET /api/lines
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "라인 목록 조회 성공",
  "data": [
    { "lineId": 1, "lineCode": "A", "lineName": "A라인", "isActive": true },
    { "lineId": 2, "lineCode": "B", "lineName": "B라인", "isActive": true },
    { "lineId": 3, "lineCode": "C", "lineName": "C라인", "isActive": true }
  ]
}
```

---

### 라인 상세 조회

```
GET /api/lines/{lineId}
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "라인 조회 성공",
  "data": {
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "isActive": true
  }
}
```

---

## Shift API

> 로그인된 사용자(ADMIN, WORKER) 모두 접근 가능.

### 교대조 목록 조회

계정 생성/수정 폼의 교대조 선택 드롭다운에 사용한다.

```
GET /api/shifts
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "교대조 목록 조회 성공",
  "data": [
    {
      "shiftId": 1,
      "shiftType": "DAY",
      "shiftName": "1조",
      "startTime": "09:00:00",
      "endTime": "18:00:00",
      "shiftOrder": 1,
      "isActive": true
    },
    {
      "shiftId": 2,
      "shiftType": "EVENING",
      "shiftName": "2조",
      "startTime": "18:00:00",
      "endTime": "03:00:00",
      "shiftOrder": 2,
      "isActive": true
    },
    {
      "shiftId": 3,
      "shiftType": "NIGHT",
      "shiftName": "3조",
      "startTime": "00:00:00",
      "endTime": "09:00:00",
      "shiftOrder": 3,
      "isActive": true
    }
  ]
}
```

---

### 교대조 상세 조회

```
GET /api/shifts/{shiftId}
Authorization: Bearer {accessToken}
```


### WORKER / ADMIN 역할별 필수 필드

| 필드 | WORKER | ADMIN |
|---|---|---|
| `shiftId` | 필수 | 선택 |
| `lineId` | 필수 | 선택 |
| `email` | 선택 | 선택 |
| `phone` | 선택 | 선택 |

### 계정 상태 값 표시

| status | 표시 |
|---|---|
| `ACTIVE` | 활성 |
| `INACTIVE` | 비활성 |
| `PENDING` | 승인 대기 |