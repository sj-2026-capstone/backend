# 스마트 공정/현장 관리 플랫폼 — Backend

공장 현장의 사용자 관리, 교대조 운영, 인증, 관리자 알림, 생산 라인 조회, 검사 생성/조회/분석 시작을 제공하는 백엔드 서버.
현재 구현 기준 주요 사용자 역할은 `WORKER`, `ADMIN` 이며, JWT 기반 인증을 사용한다.

---

## Tech Stack

| 항목 | 버전 / 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT |
| Database | MySQL (`localhost:3306/capstone`) |
| Validation | Spring Boot Starter Validation |
| Build | Gradle |
| 실시간 알림 | SSE (Server-Sent Events) |
| Cache | Redis — 미도입 |
| API 문서 | Swagger/OpenAPI — 미구현 |
| CI/CD | GitHub Actions / Jenkins — 미구현 |

---

## 현재 패키지 구조

```text
src/main/java/com/sjcapstone/
├── CapstoneApplication.java
├── domain/
│   ├── admin/
│   │   ├── controller/
│   │   ├── dto/
│   │   └── service/
│   ├── auth/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── inspection/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── line/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── notification/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── shift/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── user/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       └── service/
├── global/
│   ├── config/
│   ├── entity/
│   ├── exception/
│   ├── response/
│   └── security/
│       └── jwt/
└── internal/
    ├── callback/
    └── frame/
```

---

## 도메인 개요

### User
- 역할: `ADMIN`, `WORKER`
- 상태: `PENDING`, `ACTIVE`, `INACTIVE`
- `shift_id` FK로 기본 소속 교대조를 가진다.
- `employeeId` 는 `UUID` 로 생성된다.
- soft delete 를 사용하며 `deletedAt` 으로 관리한다.
- 현재 `User` 는 `BaseEntity` 를 상속하지 않고, `createdAt`, `updatedAt` 을 엔티티 내부 `@PrePersist`, `@PreUpdate` 로 관리한다.

### Shift
- 교대 타입: `DAY`, `EVENING`, `NIGHT`
- `isActive` 로 비활성화 처리한다.
- `BaseEntity` 를 상속한다.
- 날짜별 실제 근무 기록은 `ShiftAssignment` 로 분리되어 있다.

### ShiftAssignment
- 사용자별 날짜 단위 교대 배정을 기록한다.
- 대타/교대 변경 등 실제 근무 이력 추적 용도다.
- `UNIQUE(user_id, work_date)` 제약으로 날짜 중복 배정을 막는다.

### Auth
- `Auth` 엔티티로 로그인 정보를 `User` 와 분리 관리한다.
- 현재 구현 범위:
  - 로그인: `POST /api/auth/login`
  - 현재 사용자 조회: `GET /api/auth/me`
  - 비밀번호 변경: `PATCH /api/auth/password`
- 공개 회원가입 API는 없다.
- 로그인 성공 시 JWT access token 을 발급한다.
- refresh token, 재발급, 로그아웃은 아직 구현되지 않았다.

### Admin Account
- 관리자 전용 계정 관리 기능이 구현되어 있다.
- 계정 생성 시 `User` 와 `Auth` 를 함께 저장한다.
- `WORKER` 계정은 `shiftId`, `lineId` 가 필수이고, `ADMIN` 계정은 nullable 이다.
- 신규 생성 계정은 `passwordChangeRequired=true` 상태로 저장된다.

### Line
- 생산 라인 기준정보 조회 기능이 구현되어 있다.
- 현재 라인 코드는 `A`, `B`, `C` 를 사용한다.
- 앱 시작 시 `A라인`, `B라인`, `C라인` 이 자동 seed 된다.

### Notification
- 관리자용 알림 저장 및 SSE 구독 기능이 구현되어 있다.
- 현재 구현 범위:
  - SSE 구독: `GET /api/notifications/subscribe`
  - 알림 목록 조회: `GET /api/notifications`
  - 미확인 알림 개수 조회: `GET /api/notifications/unread-count`
  - 읽음 처리: `PATCH /api/notifications/{notificationId}/read`
  - 전체 읽음 처리: `PATCH /api/notifications/read-all`
- 알림 타입은 현재 `DEFECT_DETECTED`, `SYSTEM` 을 사용한다.
- SSE emitter 저장소는 인메모리 `SseEmitterRepository` 기반이다.

### Inspection
- 검사 생성/조회/상태 조회/분석 시작 기능이 구현되어 있다.
- 검사 상태는 `PENDING`, `PROCESSING`, `DONE`, `FAILED` 를 사용한다.
- `ADMIN` 은 검사 생성과 분석 시작이 가능하다.
- 목록/상세/상태 조회는 `ADMIN` 전체 조회, `WORKER` 자신 라인 기준 조회로 동작한다.

### Internal
- 내부 수집/분석 연동용 비공개 API가 존재한다.
- `/internal/**` 경로는 Spring Security 에서 인증 없이 허용된다.

---

## 구현된 API 범위

### Admin Account API
- `POST /api/admin/accounts`
- `GET /api/admin/accounts/login-id/availability?loginId=...`
- `GET /api/admin/accounts/summary`
- `GET /api/admin/accounts`
- `GET /api/admin/accounts/{userId}`
- `PUT /api/admin/accounts/{userId}`
- `PATCH /api/admin/accounts/{userId}/status`

### User API
- `GET /api/users/{userId}`
- `GET /api/users`
- `PUT /api/users/{userId}`
- `DELETE /api/users/{userId}`

### Shift API
- `POST /api/shifts`
- `GET /api/shifts`
- `GET /api/shifts/{shiftId}`
- `PUT /api/shifts/{shiftId}`
- `DELETE /api/shifts/{shiftId}`
- `POST /api/shifts/assignments`
- `GET /api/shifts/assignments?date=yyyy-MM-dd`
- `GET /api/shifts/assignments/users/{userId}`

### Auth API
- `POST /api/auth/login`
- `GET /api/auth/me`
- `PATCH /api/auth/password`

### Line API
- `GET /api/lines`
- `GET /api/lines/{lineId}`

### Notification API
- `GET /api/notifications/subscribe`
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `PATCH /api/notifications/{notificationId}/read`
- `PATCH /api/notifications/read-all`

### Inspection API
- `POST /api/inspections`
- `GET /api/inspections`
- `GET /api/inspections/{inspectionId}`
- `GET /api/inspections/{inspectionId}/status`
- `POST /api/inspections/{inspectionId}/analyze`

### Internal API
- `POST /internal/frames`
- `POST /internal/callbacks/{inspectionId}`

---

## 보안 / 인증

### SecurityConfig
- `POST /api/auth/login` 은 인증 없이 접근 가능하다.
- `/internal/**` 는 인증 없이 접근 가능하다.
- `/api/admin/**` 는 `ADMIN` 권한이 필요하다.
- 그 외 모든 요청은 인증이 필요하다.
- 세션은 `STATELESS` 로 설정되어 있다.
- `JwtAuthenticationFilter` 가 `UsernamePasswordAuthenticationFilter` 앞에서 동작한다.

### JWT
- `JwtProvider` 가 토큰 생성 및 검증을 담당한다.
- 현재 토큰 payload:
  - `sub`: userId
  - `loginId`
  - `role`
- `Authorization: Bearer {token}` 헤더를 사용한다.

### 현재 보안 상태
- `/api/admin/**` 에 대한 역할 분리는 `SecurityConfig` 에 적용되어 있다.
- `inspection` 도메인은 컨트롤러/서비스 레벨에서 추가 역할 검증을 수행한다.
- `/api/users/**`, `/api/shifts/**`, `/api/lines/**`, `/api/notifications/**`, `/api/inspections/**` 는 기본적으로 JWT 인증이 필요하다.

---

## 코딩 컨벤션

### DTO
- `record` 사용 금지, class 기반 유지
- 요청 DTO: `@Getter`, `@NoArgsConstructor`, validation 어노테이션 사용
- 응답 DTO: `@Getter`, `@Builder`, `@AllArgsConstructor`
- 응답 DTO는 `from(...)` 또는 `of(...)` 정적 팩토리 메서드를 사용한다.

### Entity
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- `@Builder` 는 생성자에 선언
- setter 대신 도메인 메서드로 상태를 변경
- soft delete 가 필요한 경우 `deletedAt` 사용

### Service
- 인터페이스와 구현체를 분리
- 구현체 클래스 레벨에 `@Transactional`
- 조회 메서드는 `@Transactional(readOnly = true)`

### 예외 처리
- 공통 예외는 `CustomException` 기반으로 관리
- 에러 코드는 `global/exception/ErrorCode.java` 에서 중앙 관리
- `GlobalExceptionHandler` 가 공통 예외 응답을 처리

### API 응답 형식
- 성공: `CommonResponse<T>`
- 실패: `ErrorResponse { code, message }`
- 상태 코드는 `ResponseEntity` 로 명시

---

## 주요 설정

### application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/capstone?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=root1234!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

jwt.secret=<base64 secret>
jwt.expiration=86400000
```

### Auditing
- `JpaAuditingConfig` 에서 `@EnableJpaAuditing` 을 활성화한다.
- `BaseEntity` 를 상속한 엔티티는 auditing 을 사용한다.
- 단, `User` 는 현재 `BaseEntity` 를 상속하지 않는다.

### Seed 데이터
- `LineDataInitializer` 가 `A라인`, `B라인`, `C라인` 을 자동 생성한다.
- `AdminDataInitializer` 가 `admin01 / password123` 테스트 관리자 계정을 자동 생성한다.

---

## 현재 구현 현황

| 도메인 | 상태 |
|---|---|
| global (예외, 응답, 보안 기반) | 완료 |
| admin (계정 생성/목록/상세/수정/상태 변경/요약) | 완료 |
| user (CRUD, soft delete) | 완료 |
| line (기준정보 조회, 초기 seed) | 완료 |
| shift (교대조 CRUD, 날짜별 배정) | 완료 |
| auth (로그인, 내 정보 조회, 비밀번호 변경, JWT 발급) | 완료 |
| notification (저장, 조회, 읽음 처리, unread count, SSE 구독) | 완료 |
| inspection (생성, 목록/상세/상태 조회, 분석 시작) | 완료 |
| internal (프레임 수집, 분석 콜백) | 완료 |
| refresh token / logout | 미구현 |
| Redis | 미도입 |
| Swagger/OpenAPI | 미구현 |

---

## 아직 열려 있는 항목

- JWT secret 실제 운영값 분리
- refresh token 저장 및 재발급 정책
- 로그아웃 처리 방식
- notification 발송 대상 정책
- SSE 재연결/다중 연결 정책
- 초기 seed 데이터 운영/개발 환경 분리
- 역할 기반 인가(`ADMIN`/`WORKER`) 추가 세분화
