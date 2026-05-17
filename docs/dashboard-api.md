# Dashboard API

대시보드 통계 집계 API 문서.

> `ADMIN` 권한 필요.

---

## 공통

```
Base URL: http://localhost:8080
Authorization: Bearer {accessToken}
```

---

## 대시보드 통합 조회

단일 엔드포인트로 대시보드에 필요한 모든 데이터를 반환한다.

```
GET /api/dashboard
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "대시보드 조회 성공",
  "data": {
    "summary": {
      "totalInspectionCount": 320,
      "totalInspectionChangeRate": 12.5,
      "defectRate": 8.4,
      "defectRateChange": -2.1,
      "todayInspectionCount": 45,
      "todayDefectCount": 4
    },
    "defectRateTrend": [
      { "date": "2026-05-11", "defectRate": 7.2, "inspectionCount": 40, "defectCount": 3 },
      { "date": "2026-05-12", "defectRate": 9.1, "inspectionCount": 44, "defectCount": 4 },
      { "date": "2026-05-13", "defectRate": 6.8, "inspectionCount": 44, "defectCount": 3 },
      { "date": "2026-05-14", "defectRate": 11.4, "inspectionCount": 44, "defectCount": 5 },
      { "date": "2026-05-15", "defectRate": 8.9, "inspectionCount": 45, "defectCount": 4 },
      { "date": "2026-05-16", "defectRate": 7.7, "inspectionCount": 39, "defectCount": 3 },
      { "date": "2026-05-17", "defectRate": 8.9, "inspectionCount": 45, "defectCount": 4 }
    ],
    "actionSummary": {
      "total": 0,
      "pendingCount": 0,
      "inProgressCount": 0,
      "completedCount": 0,
      "completionRate": 0.0
    },
    "lineDefectRates": [
      { "lineId": 1, "lineCode": "A", "lineName": "A라인", "defectRate": 7.2, "inspectionCount": 110, "defectCount": 8 },
      { "lineId": 2, "lineCode": "B", "lineName": "B라인", "defectRate": 9.8, "inspectionCount": 105, "defectCount": 10 },
      { "lineId": 3, "lineCode": "C", "lineName": "C라인", "defectRate": 7.9, "inspectionCount": 101, "defectCount": 8 }
    ],
    "lastUpdatedAt": "2026-05-17T21:42:50"
  }
}
```

---

## 필드 설명

### summary

| 필드 | 타입 | 설명 |
|---|---|---|
| `totalInspectionCount` | number | 최근 7일 전체 검사 수 |
| `totalInspectionChangeRate` | number | 최근 7일 vs 이전 7일 검사 수 변화율 (%) |
| `defectRate` | number | 최근 7일 전체 불량률 (%) |
| `defectRateChange` | number | 최근 7일 vs 이전 7일 불량률 변화 (%)포인트, 음수=개선 |
| `todayInspectionCount` | number | 오늘 검사 수 |
| `todayDefectCount` | number | 오늘 불량 수 |

### defectRateTrend

최근 7일 일별 불량률 추이. 배열 순서는 오래된 날짜 → 최신 날짜.

| 필드 | 타입 | 설명 |
|---|---|---|
| `date` | string | 날짜 (`yyyy-MM-dd`) |
| `defectRate` | number | 해당 날짜 불량률 (%) |
| `inspectionCount` | number | 해당 날짜 검사 수 |
| `defectCount` | number | 해당 날짜 불량 수 |

### actionSummary

> 현재 전부 `0` 반환. 조치 도메인 구현 후 실제 집계로 교체 예정.

| 필드 | 타입 | 설명 |
|---|---|---|
| `total` | number | 전체 조치 수 |
| `pendingCount` | number | 대기 중 조치 수 |
| `inProgressCount` | number | 진행 중 조치 수 |
| `completedCount` | number | 완료 조치 수 |
| `completionRate` | number | 조치 완료율 (%) |

### lineDefectRates

라인별 불량률 (전체 기간 누적 기준).

| 필드 | 타입 | 설명 |
|---|---|---|
| `lineId` | number | 라인 ID |
| `lineCode` | string | 라인 코드 (`A`, `B`, `C`) |
| `lineName` | string | 라인 이름 |
| `defectRate` | number | 불량률 (%) |
| `inspectionCount` | number | 전체 검사 수 |
| `defectCount` | number | 전체 불량 수 |