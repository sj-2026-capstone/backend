# Inspection API

검사 생성, 조회, 분석 흐름을 다루는 API 문서.

---

## 공통

```
Base URL: http://localhost:8080
Authorization: Bearer {accessToken}
```

### Enum 값

| 타입 | 값 |
|---|---|
| `status` | `PENDING`, `PROCESSING`, `DONE`, `FAILED` |
| `defectType` | `SCRATCH`, `DENT`, `CRACK`, `CONTAMINATION`, `MISSING_PART`, `DIMENSION_ERROR` |

| defectType | 표시명 |
|---|---|
| `SCRATCH` | 스크래치 |
| `DENT` | 찌그러짐 |
| `CRACK` | 균열 |
| `CONTAMINATION` | 오염 |
| `MISSING_PART` | 부품 누락 |
| `DIMENSION_ERROR` | 치수 불량 |

---

## 실제 운영 흐름 (프론트 연동 기준)

```
1. 프론트에서 이미지 파일 선택
2. POST /internal/frames  → 검사 생성 + AI 분석 자동 시작 (status: PROCESSING)
3. GET /api/inspections/{inspectionId}/status 로 폴링 or SSE 알림 수신
4. status: DONE 이면 GET /api/inspections/{inspectionId} 로 결과 조회
```

---

## 프레임 수집 (이미지 업로드 + 분석 시작)

> 내부 시스템 API. JWT 대신 `X-Service-Key` 헤더 사용.

```
POST /internal/frames
Content-Type: multipart/form-data
X-Service-Key: {서비스 키}
```

**Request (form-data)**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `file` | File | Y | 검사 이미지 파일 |
| `lineId` | number | Y | 생산라인 ID |
| `workerId` | number | N | 담당 작업자 ID |
| `shiftId` | number | N | 교대조 ID |

**Response (201)**

```json
{
  "success": true,
  "message": "프레임이 접수되어 분석이 시작되었습니다.",
  "data": {
    "inspectionId": 51,
    "lineCode": "A",
    "lineName": "A라인",
    "shiftName": "1조",
    "workerName": "홍길동",
    "status": "PROCESSING",
    "defectType": null,
    "defectDisplayName": null,
    "hasDefect": false,
    "imageUrl": "http://localhost:8080/images/7c0d280d-ced9-4bfd-85e1-30e2b2f6ccee.jpg",
    "gradCamImageUrl": null,
    "resultNote": null,
    "inspectedAt": null,
    "createdAt": "2026-05-17T21:42:46.675"
  }
}
```

> 응답의 `inspectionId`를 저장해두고 이후 상태 폴링 또는 결과 조회에 사용한다.

---

## 검사 이력 목록 조회

```
GET /api/inspections?lineId=1&status=DONE&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `lineId` | N | - | 라인 ID 필터 (ADMIN만 유효) |
| `status` | N | - | `PENDING`, `PROCESSING`, `DONE`, `FAILED` |
| `page` | N | `0` | 페이지 번호 (0부터 시작) |
| `size` | N | `10` | 페이지당 항목 수 |

> `WORKER`는 자신이 배정된 라인의 검사만 조회된다. `lineId` 필터를 넘겨도 자신의 라인 외에는 조회되지 않는다.

**Response (200)**

```json
{
  "success": true,
  "message": "검사 목록 조회 성공",
  "data": {
    "inspections": [
      {
        "inspectionId": 51,
        "lineCode": "A",
        "lineName": "A라인",
        "shiftName": "1조",
        "workerName": "홍길동",
        "status": "DONE",
        "defectType": "SCRATCH",
        "defectDisplayName": "스크래치",
        "hasDefect": true,
        "inspectedAt": "2026-05-17T21:42:50.000",
        "createdAt": "2026-05-17T21:42:46.675"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 검사 상세 조회

```
GET /api/inspections/{inspectionId}
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "검사 상세 조회 성공",
  "data": {
    "inspectionId": 51,
    "lineCode": "A",
    "lineName": "A라인",
    "shiftName": "1조",
    "workerName": "홍길동",
    "status": "DONE",
    "defectType": "SCRATCH",
    "defectDisplayName": "스크래치",
    "hasDefect": true,
    "imageUrl": "http://localhost:8080/images/7c0d280d-ced9-4bfd-85e1-30e2b2f6ccee.jpg",
    "gradCamImageUrl": "http://localhost:8000/grad-cam-images/df40fc7a-3d65-4d88-ad6c-4cfc577bef6f.jpg",
    "resultNote": "prediction=SCRATCH, confidence=0.9823, defect_prob=0.9823",
    "inspectedAt": "2026-05-17T21:42:50.000",
    "createdAt": "2026-05-17T21:42:46.675"
  }
}
```

> `WORKER`는 자신의 라인이 아닌 검사 조회 시 `404 INSPECTION_NOT_FOUND`.

**Error**

| code | 상황 |
|---|---|
| `INSPECTION_NOT_FOUND` | 존재하지 않는 inspectionId, 또는 WORKER가 다른 라인 접근 |

---

## 검사 상태 조회

분석 완료 여부를 폴링할 때 사용한다. 상세 조회보다 응답이 가볍다.

```
GET /api/inspections/{inspectionId}/status
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "검사 상태 조회 성공",
  "data": {
    "inspectionId": 51,
    "status": "DONE"
  }
}
```

---

## 필드 설명

| 필드 | 타입 | 설명 |
|---|---|---|
| `inspectionId` | number | 검사 ID |
| `lineCode` | string | 라인 코드 (`A`, `B`, `C`) |
| `lineName` | string | 라인 이름 (`A라인`, `B라인`, `C라인`) |
| `shiftName` | string \| null | 교대조 이름 |
| `workerName` | string \| null | 담당 작업자 이름 |
| `status` | string | 검사 상태 |
| `defectType` | string \| null | 불량 유형 (DONE이고 불량일 때만 값 있음) |
| `defectDisplayName` | string \| null | 불량 유형 한글명 |
| `hasDefect` | boolean | 불량 여부 (DONE 이전에는 `false`) |
| `imageUrl` | string \| null | 원본 이미지 URL |
| `gradCamImageUrl` | string \| null | AI Grad-CAM 결과 이미지 URL |
| `resultNote` | string \| null | AI 분석 상세 메모 |
| `inspectedAt` | datetime \| null | 검사 완료 시각 (DONE일 때만 값 있음) |
| `createdAt` | datetime | 검사 생성 시각 |

---

## 폴링 예시

```javascript
const poll = async (inspectionId) => {
  const res = await fetch(`/api/inspections/${inspectionId}/status`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const { data } = await res.json();

  if (data.status === 'DONE' || data.status === 'FAILED') {
    // 분석 완료 → 상세 조회
    return fetchInspectionDetail(inspectionId);
  }

  // 아직 분석 중 → 2초 후 재시도
  setTimeout(() => poll(inspectionId), 2000);
};
```