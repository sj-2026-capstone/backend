# Analysis API

AI 공정 개선 분석 요청 및 결과 조회 API 문서.

> 모든 분석 API는 `ADMIN` 권한 필요.

---

## 공통

```
Base URL: http://localhost:8080
Authorization: Bearer {accessToken}
```

### Enum 값

| 타입 | 값 | 설명 |
|---|---|---|
| `status` | `PENDING` | 분석 대기 중 |
| `status` | `PROCESSING` | AI 서버 분석 중 |
| `status` | `DONE` | 분석 완료 |
| `status` | `FAILED` | 분석 실패 |
| `severity` | `HIGH` | 높음 |
| `severity` | `MEDIUM` | 중간 |
| `severity` | `LOW` | 관찰 |

---

## 비동기 처리 흐름

```
1. POST /api/analysis          → 분석 시작 (status: PROCESSING)
2. AI 서버가 분석 완료 후 백엔드로 콜백 전송
3. GET /api/analysis/latest    → 최신 결과 조회 (status: DONE 확인)
4. GET /api/analysis/{id}      → 상세 결과 조회
```

> 분석 소요 시간은 AI 서버 처리 시간에 따라 다르다.  
> `status`가 `DONE`이 될 때까지 `/api/analysis/latest` 폴링 또는 일정 시간 후 재조회한다.

---

## 공정 분석 시작

누적 검사 데이터를 기반으로 AI가 공정 개선 리포트를 생성한다.

```
POST /api/analysis
Authorization: Bearer {accessToken}
```

**Request Body**

없음.

**Response (200)**

```json
{
  "success": true,
  "message": "공정 분석이 시작되었습니다.",
  "data": {
    "analysisId": 3,
    "status": "PROCESSING"
  }
}
```

---

## 분석 목록 조회

```
GET /api/analysis?page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `page` | N | `0` | 페이지 번호 (0부터 시작) |
| `size` | N | `10` | 페이지당 항목 수 |

> 최신 순 정렬.

**Response (200)**

```json
{
  "success": true,
  "message": "분석 목록 조회 성공",
  "data": {
    "content": [
      {
        "analysisId": 3,
        "status": "DONE",
        "requestedAt": "2026-05-17T20:00:00",
        "analyzedAt": "2026-05-17T20:00:45"
      },
      {
        "analysisId": 2,
        "status": "DONE",
        "requestedAt": "2026-05-16T15:30:00",
        "analyzedAt": "2026-05-16T15:30:52"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

## 최신 분석 결과 조회

가장 최근에 완료된 분석 결과를 반환한다.

```
GET /api/analysis/latest
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "최신 분석 결과 조회 성공",
  "data": {
    "analysisId": 3,
    "status": "DONE",
    "patterns": [
      {
        "title": "A라인 스크래치 집중 발생",
        "description": "최근 7일간 A라인에서 스크래치 불량이 전체 불량의 62%를 차지합니다.",
        "severity": "HIGH"
      },
      {
        "title": "야간 교대 시 불량률 증가",
        "description": "야간 교대(00:00~09:00) 구간에서 불량률이 주간 대비 1.8배 높습니다.",
        "severity": "MEDIUM"
      }
    ],
    "recommendations": [
      {
        "title": "A라인 표면 처리 공정 점검",
        "description": "스크래치 발생 원인 파악을 위해 지그 및 이송 설비 점검을 권장합니다."
      },
      {
        "title": "야간 교대 작업 매뉴얼 보강",
        "description": "야간 교대 시 품질 체크 항목을 추가하고 작업 표준서를 업데이트하세요."
      }
    ],
    "requestedAt": "2026-05-17T20:00:00",
    "analyzedAt": "2026-05-17T20:00:45",
    "errorMessage": null
  }
}
```

**Error**

| code | 상황 |
|---|---|
| `ANALYSIS_NOT_FOUND` | 분석 결과가 하나도 없을 때 |

---

## 분석 상세 조회

```
GET /api/analysis/{analysisId}
Authorization: Bearer {accessToken}
```

**Response (200)**

최신 분석 결과 조회와 동일한 구조.

**Error**

| code | 상황 |
|---|---|
| `ANALYSIS_NOT_FOUND` | 존재하지 않는 analysisId |

---

## 필드 설명

### AnalysisResponse

| 필드 | 타입 | 설명 |
|---|---|---|
| `analysisId` | number | 분석 ID |
| `status` | string | 분석 상태 |
| `patterns` | array \| null | 발견된 불량 패턴 목록 (DONE일 때만 값 있음) |
| `recommendations` | array \| null | AI 개선 추천 목록 (DONE일 때만 값 있음) |
| `requestedAt` | datetime | 분석 요청 시각 |
| `analyzedAt` | datetime \| null | 분석 완료 시각 (DONE일 때만 값 있음) |
| `errorMessage` | string \| null | 실패 사유 (FAILED일 때만 값 있음) |

### PatternDto

| 필드 | 타입 | 설명 |
|---|---|---|
| `title` | string | 패턴 제목 |
| `description` | string | 패턴 설명 |
| `severity` | string | 심각도 (`HIGH`, `MEDIUM`, `LOW`) |

### RecommendationDto

| 필드 | 타입 | 설명 |
|---|---|---|
| `title` | string | 추천 조치 제목 |
| `description` | string | 추천 조치 설명 |