# Account Management API

현재 계정 관리와 기준정보 API 계약을 정리한 문서다. 프론트 계정 관리 UI는 이 문서를 기준으로 `auth`, `admin`, `user`, `line`, `shift` API를 연동한다.

---

## 구현 상태

`auth`, `admin`, `user`, `line`, `shift`의 1차 API는 구현 완료 상태다.

다만 아래 항목은 이후 도메인 확장에 따라 바뀔 수 있다.

- `inspection` 도메인 추가 시 사용자/라인/교대조 연결 정보가 더 필요할 수 있다.
- `notification` 발송 대상 정책이 정해지면 관리자 계정 관리 응답에 알림 설정이 추가될 수 있다.
- 운영 전에는 DB migration 전략과 초기 `shift` seed 방식 정리가 필요하다.

---

## 책임 분리

### Auth

인증 자체만 담당한다.

- 로그인
- 현재 사용자 조회
- 비밀번호 변경
- JWT 발급/검증

공개 회원가입 또는 관리자 계정 생성 책임은 갖지 않는다.

### Admin

관리자 운영용 계정 관리 기능을 담당한다.

- 계정 생성
- 계정 목록/상세 조회
- 계정 수정
- 계정 상태 변경
- 로그인 ID 중복 확인
- 계정 요약 조회

### User

사용자 프로필 조회/수정/삭제를 담당한다. 계정 생성은 담당하지 않는다.

### Line

생산라인 기준정보를 담당한다.

- 라인은 `A`, `B`, `C` 3개 고정
- 앱 시작 시 `A라인`, `B라인`, `C라인` 자동 삽입
- 실제 테이블명은 MySQL 예약어 충돌을 피하기 위해 `production_lines`

### Shift

근무조와 날짜별 교대 배정을 담당한다. `line`과는 별개다.

- `shift`: 근무조
- `line`: 생산라인

---

## 공통 응답 형식

성공 응답은 `CommonResponse<T>`로 감싼다.

```json
{
  "success": true,
  "message": "처리 메시지",
  "data": {}
}
```

실패 응답은 `ErrorResponse`를 사용한다.

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

인증이 필요한 API는 `Authorization` 헤더를 사용한다.

```http
Authorization: Bearer {accessToken}
```

---

## 정책

- `WORKER` 계정은 `shiftId`, `lineId`가 필수다.
- `ADMIN` 계정은 `shiftId`, `lineId`가 nullable이다.
- `email`, `phone`은 optional이다.
- `loginId`는 인증 식별자이며 중복될 수 없다.
- 신규 생성 계정은 `passwordChangeRequired: true` 상태로 생성된다.
- 비밀번호 변경이 완료되면 `passwordChangeRequired: false`로 바뀐다.

---

## Auth API

### 로그인

`POST /api/auth/login`

Request:

```json
{
  "loginId": "worker01",
  "password": "password123"
}
```

Response:

```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "passwordChangeRequired": true
  }
}
```

### 현재 사용자 조회

`GET /api/auth/me`

Response:

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
    "shiftName": "주간",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": false
  }
}
```

### 비밀번호 변경

`PATCH /api/auth/password`

Request:

```json
{
  "currentPassword": "password123",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

Response:

```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
```

---

## Admin Account API

### 계정 생성

`POST /api/admin/accounts`

Request:

```json
{
  "userName": "홍길동",
  "loginId": "worker01",
  "password": "password123",
  "confirmPassword": "password123",
  "role": "WORKER",
  "shiftId": 1,
  "lineId": 1,
  "email": "worker01@example.com",
  "phone": "010-1234-5678"
}
```

Response:

```json
{
  "success": true,
  "message": "계정이 생성되었습니다.",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "ACTIVE",
    "shiftId": 1,
    "shiftName": "주간",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": true
  }
}
```

### 로그인 ID 중복 확인

`GET /api/admin/accounts/login-id/availability?loginId=worker01`

Response:

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

### 계정 요약 조회

`GET /api/admin/accounts/summary`

Response:

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

### 계정 목록 조회

`GET /api/admin/accounts?keyword=홍길동&status=ACTIVE&page=0&size=10`

Query parameters:

- `keyword`: optional, 사용자 이름 또는 로그인 ID 검색
- `status`: optional, `PENDING`, `ACTIVE`, `INACTIVE`
- `page`: optional, default `0`
- `size`: optional, default `10`

Response:

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
        "shiftName": "주간",
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

### 계정 상세 조회

`GET /api/admin/accounts/{userId}`

Response:

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
    "shiftName": "주간",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "passwordChangeRequired": true
  }
}
```

### 계정 수정

`PUT /api/admin/accounts/{userId}`

Request:

```json
{
  "userName": "홍길동",
  "loginId": "worker01",
  "role": "WORKER",
  "shiftId": 2,
  "lineId": 2,
  "status": "ACTIVE",
  "email": "worker01@example.com",
  "phone": "010-9999-8888"
}
```

Response:

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
    "shiftName": "오후",
    "lineId": 2,
    "lineCode": "B",
    "lineName": "B라인",
    "email": "worker01@example.com",
    "phone": "010-9999-8888",
    "passwordChangeRequired": true
  }
}
```

### 계정 상태 변경

`PATCH /api/admin/accounts/{userId}/status`

Request:

```json
{
  "status": "INACTIVE"
}
```

Response:

```json
{
  "success": true,
  "message": "계정 상태가 변경되었습니다.",
  "data": {
    "userId": 1,
    "userName": "홍길동",
    "loginId": "worker01",
    "role": "WORKER",
    "status": "INACTIVE",
    "shiftId": 2,
    "shiftName": "오후",
    "lineId": 2,
    "lineCode": "B",
    "lineName": "B라인",
    "email": "worker01@example.com",
    "phone": "010-9999-8888",
    "passwordChangeRequired": true
  }
}
```

---

## User API

### 사용자 목록 조회

`GET /api/users`

Response:

```json
{
  "success": true,
  "message": "사용자 목록 조회 성공",
  "data": [
    {
      "userId": 1,
      "employeeId": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "홍길동",
      "email": "worker01@example.com",
      "role": "WORKER",
      "shiftId": 1,
      "shiftName": "주간",
      "lineId": 1,
      "lineCode": "A",
      "lineName": "A라인",
      "status": "ACTIVE"
    }
  ]
}
```

### 사용자 상세 조회

`GET /api/users/{userId}`

Response:

```json
{
  "success": true,
  "message": "사용자 조회 성공",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "email": "worker01@example.com",
    "phone": "010-1234-5678",
    "role": "WORKER",
    "shiftId": 1,
    "shiftName": "주간",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "status": "ACTIVE",
    "createdAt": "2026-04-11T04:00:00",
    "updatedAt": "2026-04-11T04:00:00"
  }
}
```

### 사용자 수정

`PUT /api/users/{userId}`

Request:

```json
{
  "userName": "홍길동",
  "phone": "010-9999-8888",
  "role": "WORKER",
  "shiftId": 1,
  "lineId": 1,
  "status": "ACTIVE"
}
```

Response:

```json
{
  "success": true,
  "message": "사용자 정보가 수정되었습니다.",
  "data": {
    "userId": 1,
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "홍길동",
    "email": "worker01@example.com",
    "phone": "010-9999-8888",
    "role": "WORKER",
    "shiftId": 1,
    "shiftName": "주간",
    "lineId": 1,
    "lineCode": "A",
    "lineName": "A라인",
    "status": "ACTIVE",
    "createdAt": "2026-04-11T04:00:00",
    "updatedAt": "2026-04-11T04:05:00"
  }
}
```

### 사용자 삭제

`DELETE /api/users/{userId}`

Response:

```json
{
  "success": true,
  "message": "사용자가 삭제되었습니다.",
  "data": null
}
```

---

## Line API

### 라인 목록 조회

`GET /api/lines`

Response:

```json
{
  "success": true,
  "message": "라인 목록 조회 성공",
  "data": [
    {
      "lineId": 1,
      "lineCode": "A",
      "lineName": "A라인",
      "isActive": true
    },
    {
      "lineId": 2,
      "lineCode": "B",
      "lineName": "B라인",
      "isActive": true
    },
    {
      "lineId": 3,
      "lineCode": "C",
      "lineName": "C라인",
      "isActive": true
    }
  ]
}
```

### 라인 상세 조회

`GET /api/lines/{lineId}`

Response:

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

### 교대조 생성

`POST /api/shifts`

Request:

```json
{
  "shiftType": "DAY",
  "shiftName": "주간",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "shiftOrder": 1
}
```

### 교대조 목록 조회

`GET /api/shifts`

Response:

```json
{
  "success": true,
  "message": "교대조 목록 조회 성공",
  "data": [
    {
      "shiftId": 1,
      "shiftType": "DAY",
      "shiftName": "주간",
      "startTime": "09:00:00",
      "endTime": "18:00:00",
      "shiftOrder": 1,
      "isActive": true
    }
  ]
}
```

### 교대조 상세 조회

`GET /api/shifts/{shiftId}`

### 교대조 수정

`PUT /api/shifts/{shiftId}`

Request:

```json
{
  "shiftName": "주간",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "shiftOrder": 1
}
```

### 교대조 비활성화

`DELETE /api/shifts/{shiftId}`

### 날짜별 교대 배정

`POST /api/shifts/assignments`

Request:

```json
{
  "userId": 1,
  "shiftId": 1,
  "workDate": "2026-04-11"
}
```

Response:

```json
{
  "success": true,
  "message": "교대 배정이 완료되었습니다.",
  "data": {
    "assignmentId": 1,
    "userId": 1,
    "userName": "홍길동",
    "shiftId": 1,
    "shiftName": "주간",
    "shiftType": "DAY",
    "workDate": "2026-04-11"
  }
}
```

### 날짜별 교대표 조회

`GET /api/shifts/assignments?date=2026-04-11`

### 사용자별 배정 이력 조회

`GET /api/shifts/assignments/users/{userId}`

---

## 프론트 연동 체크리스트

계정 생성/수정 화면 진입 시 먼저 기준정보를 조회한다.

- `GET /api/lines`
- `GET /api/shifts`

계정 생성 폼 필드:

- `userName`
- `loginId`
- `password`
- `confirmPassword`
- `role`
- `shiftId`
- `lineId`
- `email`
- `phone`

계정 수정 폼 필드:

- `userName`
- `loginId`
- `role`
- `shiftId`
- `lineId`
- `status`
- `email`
- `phone`

계정 목록 테이블 권장 컬럼:

- 이름
- 로그인 ID
- 역할
- 상태
- 근무조
- 생산라인

---

## 테스트 시나리오

- `WORKER` 계정 생성 시 `shiftId`가 없으면 실패한다.
- `WORKER` 계정 생성 시 `lineId`가 없으면 실패한다.
- `ADMIN` 계정 생성 시 `shiftId`, `lineId`가 없어도 성공한다.
- 중복 `loginId`로 계정 생성 시 실패한다.
- 비밀번호와 비밀번호 확인이 다르면 실패한다.
- `GET /api/lines`는 `A`, `B`, `C` 라인을 내려준다.
- `GET /api/auth/me`는 `lineId`, `lineCode`, `lineName`을 내려준다.
- `GET /api/admin/accounts`는 계정 목록 item에 `lineId`, `lineCode`, `lineName`을 포함한다.
- `GET /api/users/{userId}`는 사용자 상세에 `shift`, `line` 정보를 모두 포함한다.

---

## 현재 열린 항목

- `shift` 초기 데이터 seed 방식 확정
- 운영 DB migration 전략 확정
- 역할별 API 접근 권한 세분화 검토
- `inspection` 도메인 추가 후 `line`, `shift`, `user` 연결 방식 검토
