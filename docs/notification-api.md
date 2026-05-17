# Notification API

실시간 알림 구독(SSE), 알림 목록 조회, 읽음 처리 API 문서.

> 모든 알림 API는 `ADMIN` 권한 필요.

---

## 공통

```
Base URL: http://localhost:8080
Authorization: Bearer {accessToken}
```

### Enum 값

| 타입 | 값 | 설명 |
|---|---|---|
| `notificationType` | `DEFECT_DETECTED` | 불량 감지 알림 |
| `notificationType` | `SYSTEM` | 시스템 알림 |

---

## SSE 실시간 알림 구독

관리자 화면 진입 시 연결한다. 서버에서 불량이 감지되면 `notification` 이벤트를 push한다.

```
GET /api/notifications/subscribe
Authorization: Bearer {accessToken}
Accept: text/event-stream
```

**SSE 이벤트 종류**

| 이벤트명 | 발생 시점 | data |
|---|---|---|
| `connected` | 최초 연결 시 | `"연결되었습니다."` |
| `notification` | 불량 감지 시 | NotificationResponse JSON |

**최초 연결 이벤트**

```
event: connected
data: "연결되었습니다."
```

**불량 감지 이벤트**

```
event: notification
data: {
  "notificationId": 10,
  "notificationType": "DEFECT_DETECTED",
  "title": "불량 부품 감지",
  "message": "A라인 - 스크래치 | 담당자: 홍길동",
  "isRead": false,
  "createdAt": "2026-05-17T14:32:05"
}
```

**연결 예시 (JavaScript)**

```javascript
const token = localStorage.getItem('accessToken');
const es = new EventSource(
  `http://localhost:8080/api/notifications/subscribe`,
  {
    headers: { Authorization: `Bearer ${token}` }
    // 주의: 브라우저 기본 EventSource는 커스텀 헤더 미지원
    // → fetch-event-source 라이브러리 사용 권장
  }
);

es.addEventListener('connected', (e) => {
  console.log(e.data); // "연결되었습니다."
});

es.addEventListener('notification', (e) => {
  const notification = JSON.parse(e.data);
  // 알림 뱃지 업데이트, 토스트 표시 등
});
```

> 브라우저 기본 `EventSource`는 커스텀 헤더를 지원하지 않는다.  
> `@microsoft/fetch-event-source` 또는 유사 라이브러리 사용을 권장한다.

---

## 알림 목록 조회

```
GET /api/notifications?read=false&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `read` | N | 없음(전체) | `true`=읽은 알림, `false`=안 읽은 알림, 생략=전체 |
| `page` | N | `0` | 페이지 번호 (0부터 시작) |
| `size` | N | `10` | 페이지당 항목 수 |

**Response (200)**

```json
{
  "success": true,
  "message": "알림 목록 조회 성공",
  "data": {
    "items": [
      {
        "notificationId": 10,
        "notificationType": "DEFECT_DETECTED",
        "title": "불량 부품 감지",
        "message": "A라인 - 스크래치 | 담당자: 홍길동",
        "isRead": false,
        "createdAt": "2026-05-17T14:32:05"
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

## 미확인 알림 개수 조회

알림 뱃지 숫자에 사용한다.

```
GET /api/notifications/unread-count
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "미확인 알림 개수 조회 성공",
  "data": {
    "count": 5
  }
}
```

---

## 단건 읽음 처리

알림 항목 클릭 시 호출한다.

```
PATCH /api/notifications/{notificationId}/read
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "알림을 읽음 처리했습니다.",
  "data": {
    "notificationId": 10,
    "notificationType": "DEFECT_DETECTED",
    "title": "불량 부품 감지",
    "message": "A라인 - 스크래치 | 담당자: 홍길동",
    "isRead": true,
    "createdAt": "2026-05-17T14:32:05"
  }
}
```

**Error**

| code | 상황 |
|---|---|
| `NOTIFICATION_NOT_FOUND` | 존재하지 않는 notificationId |

---

## 전체 읽음 처리

"모두 읽음" 버튼에 사용한다.

```
PATCH /api/notifications/read-all
Authorization: Bearer {accessToken}
```

**Response (200)**

```json
{
  "success": true,
  "message": "모든 알림을 읽음 처리했습니다.",
  "data": null
}
```

---

## 필드 설명

| 필드 | 타입 | 설명 |
|---|---|---|
| `notificationId` | number | 알림 ID |
| `notificationType` | string | `DEFECT_DETECTED` 또는 `SYSTEM` |
| `title` | string | 알림 제목 |
| `message` | string | 알림 내용 (`{라인명} - {불량유형} \| 담당자: {이름}`) |
| `isRead` | boolean | 읽음 여부 |
| `createdAt` | datetime | 알림 생성 시각 |

---

## 프론트 연동 가이드

```
1. 관리자 화면 진입 → GET /api/notifications/unread-count 로 초기 뱃지 설정
2. GET /api/notifications/subscribe 로 SSE 연결
3. notification 이벤트 수신 → 뱃지 +1, 토스트 표시
4. 알림 목록 화면 → GET /api/notifications (read 파라미터로 필터)
5. 알림 클릭 → PATCH /api/notifications/{id}/read
6. 모두 읽음 클릭 → PATCH /api/notifications/read-all
```