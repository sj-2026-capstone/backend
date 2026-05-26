# API 명세서

> **Base URL**: `http://localhost:8080`  
> **인증**: `Authorization: Bearer {JWT}` 헤더 (공개 엔드포인트 제외)  
> **내부 시스템 인증**: `X-Service-Key: {서비스키}` 헤더  
> **응답 공통 형식**: `{ "success": true, "message": "...", "data": { ... } }`

---

## 1. 인증 (Auth)

### 로그인
```
POST /api/auth/login
```
- 인증: 불필요 (공개)
- Request Body:
```json
{
  "loginId": "admin01",
  "password": "password123"
}
```
- Response:
```json
{
  "token": "eyJhbGci...",
  "role": "ADMIN",
  "passwordChangeRequired": false
}
```

---

### 내 정보 조회
```
GET /api/auth/me
```
- 인증: JWT (ADMIN, WORKER)
- Response:
```json
{
  "userId": 1,
  "loginId": "admin01",
  "userName": "김관리",
  "role": "ADMIN",
  "status": "ACTIVE"
}
```

---

### 비밀번호 변경
```
PATCH /api/auth/password
```
- 인증: JWT (ADMIN, WORKER)
- Request Body:
```json
{
  "currentPassword": "oldPass",
  "newPassword": "newPass",
  "newPasswordConfirm": "newPass"
}
```

---

## 2. 관리자 계정 관리 (Admin Accounts)

> 모든 엔드포인트: 인증 JWT + ADMIN 권한 필요

### 계정 생성
```
POST /api/admin/accounts
```
- Request Body:
```json
{
  "loginId": "worker01",
  "password": "pass1234",
  "userName": "홍길동",
  "role": "WORKER",
  "shiftId": 1,
  "lineId": 1,
  "email": "hong@example.com"
}
```
- Response: `201 Created` — 생성된 계정 정보

---

### 계정 목록 조회
```
GET /api/admin/accounts?keyword=&status=&page=0&size=10
```
| 파라미터 | 필수 | 설명 |
|---|---|---|
| keyword | N | 이름/loginId 검색 |
| status | N | `PENDING` / `ACTIVE` / `INACTIVE` |
| page | N | 페이지 번호 (기본값 0) |
| size | N | 페이지 크기 (기본값 10) |

---

### 계정 상세 조회
```
GET /api/admin/accounts/{userId}
```

---

### 계정 수정
```
PUT /api/admin/accounts/{userId}
```
- Request Body: 수정할 계정 정보 (이름, 이메일, 라인, 교대조 등)

---

### 계정 상태 변경
```
PATCH /api/admin/accounts/{userId}/status
```
- Request Body:
```json
{
  "status": "ACTIVE"
}
```
- `status`: `PENDING` / `ACTIVE` / `INACTIVE`

---

### 계정 요약 통계
```
GET /api/admin/accounts/summary
```
- Response: 전체/활성/비활성/대기 계정 수

---

### 로그인 ID 중복 확인
```
GET /api/admin/accounts/login-id/availability?loginId=worker01
```
- Response:
```json
{
  "available": true
}
```

---

## 3. 사용자 (Users)

> 인증: JWT 필요

### 사용자 조회
```
GET /api/users/{userId}
```

### 사용자 목록 조회
```
GET /api/users
```

### 사용자 수정
```
PUT /api/users/{userId}
```

### 사용자 삭제 (soft delete)
```
DELETE /api/users/{userId}
```

---

## 4. 교대조 (Shifts)

> 인증: JWT 필요

### 교대조 생성 (ADMIN)
```
POST /api/shifts
```
- Request Body:
```json
{
  "shiftType": "DAY",
  "shiftName": "1조(주간)",
  "startTime": "06:00",
  "endTime": "14:00",
  "shiftOrder": 1
}
```

---

### 교대조 목록 조회
```
GET /api/shifts
```
- Response: 활성 교대조 리스트

---

### 교대조 단건 조회
```
GET /api/shifts/{shiftId}
```

---

### 교대조 수정 (ADMIN)
```
PUT /api/shifts/{shiftId}
```

---

### 교대조 비활성화 (ADMIN)
```
DELETE /api/shifts/{shiftId}
```

---

### 날짜별 교대 배정 (ADMIN)
```
POST /api/shifts/assignments
```
- Request Body:
```json
{
  "userId": 3,
  "shiftId": 2,
  "workDate": "2026-05-20"
}
```

---

### 날짜별 교대표 조회
```
GET /api/shifts/assignments?date=2026-05-20
```

---

### 특정 사용자 배정 이력 조회 (ADMIN)
```
GET /api/shifts/assignments/users/{userId}
```

---

## 5. 생산라인 (Lines)

> 인증: JWT 필요

### 라인 목록 조회
```
GET /api/lines
```
- Response:
```json
[
  { "lineId": 1, "lineCode": "A", "lineName": "A라인", "isActive": true },
  { "lineId": 2, "lineCode": "B", "lineName": "B라인", "isActive": true },
  { "lineId": 3, "lineCode": "C", "lineName": "C라인", "isActive": true }
]
```

---

### 라인 단건 조회
```
GET /api/lines/{lineId}
```

---

## 6. 검사 (Inspection)

> 인증: JWT 필요

### 이미지 업로드 + 검사 생성 (ADMIN, 카메라 미연결 시 테스트용)
```
POST /api/inspections/upload
Content-Type: multipart/form-data
```
| 파라미터 | 필수 | 설명 |
|---|---|---|
| file | Y | 이미지 파일 |
| lineId | Y | 라인 ID |
| workerId | N | 근로자 ID |
| shiftId | N | 교대조 ID |

- Response: `201 Created` — 검사 정보 (status: `PENDING`)
- 이후 분석 시작은 `POST /api/inspections/{id}/analyze` 별도 호출

---

### 검사 생성 (ADMIN, 테스트용)
```
POST /api/inspections
```
- Request Body:
```json
{
  "lineId": 1,
  "workerId": 3,
  "shiftId": 1,
  "imageUrl": "/images/abc.jpg"
}
```
- Response: `201 Created`

---

### 최근 불량 5개 조회 (ADMIN, 실시간 모니터링)
```
GET /api/inspections/latest
```
- 인증: JWT + ADMIN
- `inspectedAt` 기준 최신순, `hasDefect = true` & `status = DONE` 조건
- Response:
```json
[
  {
    "inspectionId": 51,
    "lineName": "C라인",
    "imageUrl": "/images/abc.jpg",
    "gradCamImageUrl": "/grad-cam-images/xyz.jpg",
    "inspectedAt": "2026-05-20T10:30:50"
  }
]
```

---

### 검사 목록 조회
```
GET /api/inspections?lineId=&status=&page=0&size=10
```
| 파라미터 | 필수 | 설명 |
|---|---|---|
| lineId | N | 라인 필터 (ADMIN만 유효) |
| status | N | `PENDING` / `PROCESSING` / `DONE` / `FAILED` |
| page | N | 기본값 0 |
| size | N | 기본값 10 |

- WORKER: 자신의 라인 검사만 조회 (다른 라인 접근 시 404)
- ADMIN: 전체 조회, lineId/status 필터 가능
- Response에 `actionStatus` 포함 (`UNRESOLVED` / `RESOLVED` / `null`)

---

### 검사 상세 조회
```
GET /api/inspections/{inspectionId}
```
- Response:
```json
{
  "inspectionId": 51,
  "lineCode": "A",
  "lineName": "A라인",
  "shiftName": "1조(주간)",
  "workerName": "홍길동",
  "status": "DONE",
  "defectType": "SCRATCH",
  "defectDisplayName": "스크래치",
  "hasDefect": true,
  "actionStatus": "UNRESOLVED",
  "imageUrl": "/images/abc.jpg",
  "gradCamImageUrl": "/grad-cam-images/xyz.jpg",
  "inspectedAt": "2026-05-20T10:30:00",
  "createdAt": "2026-05-20T10:29:50"
}
```

---

### 검사 상태 조회
```
GET /api/inspections/{inspectionId}/status
```
- Response:
```json
{
  "inspectionId": 51,
  "status": "DONE"
}
```

---

### 조치 완료 처리 (WORKER, ADMIN)
```
PATCH /api/inspections/{inspectionId}/action
```
- 불량 검사의 `actionStatus`를 `UNRESOLVED` → `RESOLVED`로 변경
- WORKER는 자신의 라인 검사만 처리 가능

---

### 분석 시작 (ADMIN, 테스트용)
```
POST /api/inspections/{inspectionId}/analyze
```
- `PENDING` 상태의 검사를 `PROCESSING`으로 전환 후 AI 서버 호출

---

## 7. 알림 (Notification)

> 인증: JWT 필요 (WORKER, ADMIN 모두 접근 가능)  
> 불량 감지 시 **ACTIVE 상태 전체 사용자**에게 알림 생성 및 SSE 전송

### SSE 실시간 알림 구독
```
GET /api/notifications/subscribe
Accept: text/event-stream
```
- 이벤트 종류:
  - `connected`: 최초 연결 확인 (`"SSE 연결 완료"`)
  - `notification`: 새 알림 수신 (불량 감지 시 자동 발송)
- SSE 연결 유지 시간: 최대 30분 (재연결 필요 시 다시 구독)

---

### 알림 목록 조회
```
GET /api/notifications?read=&page=0&size=10
```
| 파라미터 | 설명 |
|---|---|
| read 없음 | 전체 |
| read=false | 미확인만 |
| read=true | 확인완료만 |

- Response:
```json
{
  "content": [
    {
      "notificationId": 26,
      "notificationType": "DEFECT_DETECTED",
      "title": "불량 부품 감지",
      "message": "A라인 - 불량 감지",
      "isRead": false,
      "createdAt": "2026-05-21T01:32:31"
    }
  ],
  "totalElements": 26,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 10
}
```

---

### 미확인 알림 개수 조회
```
GET /api/notifications/unread-count
```
- Response:
```json
{ "count": 5 }
```

---

### 단건 읽음 처리
```
PATCH /api/notifications/{notificationId}/read
```

---

### 전체 읽음 처리
```
PATCH /api/notifications/read-all
```

---

## 8. 대시보드 (Dashboard)

> 인증: JWT + ADMIN 권한 필요

### 대시보드 통합 조회
```
GET /api/dashboard
```
- Response:
```json
{
  "summary": {
    "totalInspectionCount": 120,
    "todayInspectionCount": 8,
    "todayDefectCount": 2,
    "defectRate": 15.0,
    "totalInspectionChangeRate": 5.0,
    "defectRateChange": -2.0
  },
  "defectRateTrend": [
    { "date": "2026-05-14", "inspectionCount": 18, "defectCount": 3, "defectRate": 16.7 }
  ],
  "actionSummary": {
    "total": 15,
    "unresolvedCount": 8,
    "resolvedCount": 7,
    "completionRate": 46.7
  },
  "lineDefectRates": [
    { "lineId": 1, "lineName": "A라인", "defectRate": 0.12 }
  ],
  "lastUpdatedAt": "2026-05-20T10:30:00"
}
```

---

## 9. 공정 개선 분석 (Analysis)

> 인증: JWT + ADMIN 권한 필요

### AI 공정 분석 시작
```
POST /api/analysis
```
- **동기 처리**: 최근 30일 검사 데이터를 수집해 OpenAI(`gpt-4o-mini`)를 직접 호출하고 결과를 즉시 저장 후 반환
- 응답까지 수 초~수십 초 소요될 수 있음
- Response (`DONE` 성공 / `FAILED` 실패):
```json
{
  "analysisId": 5,
  "status": "DONE"
}
```

---

### 분석 목록 조회
```
GET /api/analysis?page=0&size=10
```

---

### 최신 분석 결과 조회
```
GET /api/analysis/latest
```

---

### 분석 상세 조회
```
GET /api/analysis/{analysisId}
```
- Response:
```json
{
  "analysisId": 5,
  "status": "DONE",
  "patterns": [
    { "title": "스크래치 집중 발생", "description": "A라인에서 스크래치 빈도 증가", "severity": "HIGH" }
  ],
  "recommendations": [
    { "title": "도구 점검 필요", "description": "절삭 공구 마모 확인 권장" }
  ],
  "createdAt": "2026-05-20T09:00:00"
}
```

---

## 10. 내부 시스템 API (Internal)

> 인증: `X-Service-Key: {서비스키}` 헤더  
> 호출 주체: 카메라/엣지 디바이스, AI 분석 서버

### 프레임 수집 (카메라 → 백엔드)
```
POST /internal/frames
Content-Type: multipart/form-data
```
| 파라미터 | 필수 | 설명 |
|---|---|---|
| file | Y | 촬영 이미지 |
| lineId | Y | 카메라가 설치된 라인 ID |

- 교대조, 근로자는 백엔드가 현재 시간과 라인 배정 기준으로 자동 결정
- 이미지 저장 + 검사 생성 + AI 분석 요청 즉시 수행

---

### 검사 분석 완료 콜백 (AI 서버 → 백엔드)
```
POST /internal/callbacks/{inspectionId}
```
- Request Body:
```json
{
  "prediction": "DEFECT",
  "gradCamImageUrl": "grad-cam-images/uuid.jpg"
}
```
- `prediction`: `"DEFECT"` 또는 `"NORMAL"`
- 불량 확정 시 **ACTIVE 상태 전체 사용자(ADMIN + WORKER)**에게 SSE 알림 자동 발송, `actionStatus = UNRESOLVED` 설정

---

### 공정 분석 완료 콜백 (AI 서버 → 백엔드, 예비용)
```
POST /internal/analysis-callbacks/{analysisId}
```
> 현재 공정 분석은 OpenAI 직접 호출 방식으로 변경되어 이 엔드포인트는 사용되지 않음.  
> 외부 AI 서버 콜백 방식으로 전환 시 활성화 가능.

- Request Body:
```json
{
  "patterns": [
    { "title": "패턴명", "description": "설명", "severity": "HIGH" }
  ],
  "recommendations": [
    { "title": "조치명", "description": "설명" }
  ]
}
```
- `severity`: `HIGH` / `MEDIUM` / `LOW`

---

## 공통 에러 응답

```json
{
  "success": false,
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

| HTTP 상태 | 의미 |
|---|---|
| 400 | 잘못된 요청 (유효성 검사 실패, 비밀번호 불일치 등) |
| 401 | 인증 필요 (토큰 없음 또는 만료) |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 중복 (loginId, 이메일, 사번 등) |
| 500 | 서버 내부 오류 |