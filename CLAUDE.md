# 스마트 제조 품질관리 플랫폼 — Backend

제조 현장의 생산라인에서 카메라와 엣지 디바이스가 수집한 검사 데이터를 실시간으로 처리하고, 불량 발생 시 관리자에게 즉시 알림을 제공하는 스마트 품질관리 시스템.  
누적 검사 데이터를 기반으로 AI가 공정 개선 리포트를 자동 생성하며, 교대조별·생산라인별 불량 통계를 대시보드로 시각화해 관리자의 의사결정을 지원한다.  
현장 근로자, 관리자, 그리고 카메라·AI 분석 서버 같은 내부 시스템까지 서로 다른 인증 채널로 통합한 백엔드 플랫폼.

---

## Tech Stack

| 항목 | 버전 / 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Database | MySQL (로컬: localhost:3306/capstone) |
| Validation | Spring Boot Starter Validation |
| Build | Gradle |
| API 문서 | Swagger (OpenAPI) — 예정 |
| 알림 | SSE (Server-Sent Events) — 예정 |
| Cache | Redis — 예정 |
| CI/CD | GitHub Actions / Jenkins — 예정 |

---

## 주요 사용자

### Human Actor

| 역할 | 설명 |
|---|---|
| `WORKER` (현장 근로자) | 자신의 교대조 내 검사 이력 조회, 검사 상태 확인, 본인 프로필 조회 |
| `ADMIN` (관리자) | 사용자 가입 승인/거절, 전체 검사 이력 조회, 대시보드 통계, 공정 개선 분석 요청, 실시간 알림 수신 |

### System Actor (내부 시스템 — 별도 인증 채널)

| 시스템 | 역할 |
|---|---|
| 카메라 / 엣지 디바이스 | 생산라인에서 검사 프레임 데이터를 백엔드로 전송 |
| AI 분석 서버 | 프레임 분석 완료 후 결과 콜백 전달, 공정 개선 LLM 리포트 반환 |

> 내부 시스템은 사용자 JWT가 아닌 **서비스 키 / API Key** 기반 인증을 사용한다.

---

## 기능 영역

### 1. 사용자 / 인증

| 기능 | 주체 |
|---|---|
| 회원가입 요청 | WORKER, ADMIN 지망 |
| 로그인 / 로그아웃 | 모든 사용자 |
| 내 정보 조회 | 모든 사용자 |
| 가입 승인 / 거절 | ADMIN |
| 승인 대기 사용자 목록 | ADMIN |

> `UserStatus`에 `PENDING` 상태 추가 필요 — 승인 전 API 접근 제한

### 2. 검사 (Inspection)

| 기능 | 주체 |
|---|---|
| 검사 생성 | 내부 시스템 or ADMIN |
| 검사 프레임 업로드 | 카메라/엣지 디바이스 (내부 API) |
| 검사 분석 시작 | 시스템 자동 or ADMIN |
| 검사 이력 목록 조회 | WORKER, ADMIN (권한별 범위 다름) |
| 검사 상세 조회 | WORKER, ADMIN |
| 검사 상태 조회 | WORKER, ADMIN |
| 분석 완료 콜백 수신 | AI 분석 서버 (내부 API) |

> 검사 도메인은 **상태 머신** 성격: `PENDING → PROCESSING → DONE / FAILED`

### 3. 알림 (Notification)

| 기능 | 주체 |
|---|---|
| 관리자 알림 목록 조회 | ADMIN |
| 알림 읽음 처리 | ADMIN |
| SSE 실시간 알림 구독 | ADMIN |

> 알림의 트리거 주체는 **검사 도메인** (불량 확정 시 발생). 알림은 수신/관리만 담당.

### 4. 대시보드 / 통계 (Dashboard)

| 기능 | 주체 |
|---|---|
| 대시보드 요약 조회 | ADMIN |
| 불량률 추이 조회 | ADMIN |
| 불량 유형별 통계 조회 | ADMIN |
| 교대조별 불량 통계 조회 | ADMIN |
| 생산라인별 불량 통계 조회 | ADMIN |

> 별도 엔티티 없이 **검사 데이터 집계 쿼리** 기반. `line` 도메인과 연결 필요.

### 5. 공정 개선 분석 (Analysis)

| 기능 | 주체 |
|---|---|
| AI 자율 공정 분석 요청 | ADMIN |
| 공정 개선 분석 목록 조회 | ADMIN |
| 공정 개선 분석 상세 조회 | ADMIN |

> **비동기 처리 흐름**: 요청 → AI 서버 처리 → 결과 저장 → 조회  
> `inspection`과 연결되지만, 목적과 생명주기가 달라 **독립 도메인**으로 분리.

### 6. 내부 시스템 연동 API (Internal)

| 기능 | 주체 |
|---|---|
| 프레임 수집 API | 카메라/엣지 디바이스 → 백엔드 |
| 분석 완료 콜백 API | AI 서버 → 백엔드 |

> 사용자 JWT 아님. `/internal/**` prefix로 분리, 별도 Security Filter Chain 적용.

---

## 패키지 구조

```
src/main/java/com/sjcapstone/
├── CapstoneApplication.java
├── domain/
│   ├── auth/                  # 인증 (로그인, JWT 발급)
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   └── AuthServiceImpl.java
│   │   ├── repository/
│   │   │   └── AuthRepository.java
│   │   ├── entity/
│   │   │   └── Auth.java      # auth 테이블 (user_id FK, email, password)
│   │   ├── dto/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── LoginResponse.java
│   │   └── exception/
│   │       ├── InvalidPasswordException.java
│   │       └── AuthAlreadyExistsException.java
│   ├── user/                  # 사용자 프로필, 역할, 승인 상태
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java   (enum: ADMIN/WORKER)
│   │   │   └── UserStatus.java (enum: ACTIVE/INACTIVE/PENDING)
│   │   ├── dto/
│   │   └── exception/
│   ├── shift/                 # 교대조 마스터, 날짜별 배정
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   │   ├── Shift.java
│   │   │   ├── ShiftType.java  (enum: DAY/EVENING/NIGHT)
│   │   │   └── ShiftAssignment.java
│   │   ├── dto/
│   │   └── exception/
│   ├── line/                  # 생산라인 마스터 (예정)
│   ├── inspection/            # 검사 생성, 상태 머신, 결과 저장 (예정)
│   ├── notification/          # 알림 생성, SSE 구독, 읽음 처리 (예정)
│   ├── dashboard/             # 통계 집계 API — 읽기 전용 (예정)
│   └── analysis/              # AI 공정 개선 분석 요청/결과 관리 (예정)
├── internal/                  # 내부 시스템 전용 API (별도 보안 채널)
│   ├── frame/                 # 프레임 수집 (카메라/엣지 디바이스) (예정)
│   └── callback/              # 분석 완료 콜백 (AI 서버) (예정)
└── global/
    ├── config/
    │   ├── JpaAuditingConfig.java
    │   └── SecurityConfig.java   # 사용자 JWT + 내부 시스템 키 — 2개 Filter Chain
    ├── entity/
    │   └── BaseEntity.java       # createdAt, updatedAt (JPA Auditing)
    ├── exception/
    │   ├── CustomException.java
    │   ├── ErrorCode.java        # 모든 에러 코드 중앙 관리
    │   └── GlobalExceptionHandler.java
    ├── response/
    │   ├── CommonResponse.java
    │   └── ErrorResponse.java
    └── security/
        ├── CustomUserDetails.java
        ├── CustomUserDetailsService.java
        ├── jwt/
        │   ├── JwtProvider.java
        │   └── JwtAuthenticationFilter.java
        └── internal/              # 내부 시스템 키 검증 (예정)
```

---

## URL 구조

```
/api/auth/**           → 인증 (공개)
/api/users/**          → 사용자 (JWT 필요)
/api/shifts/**         → 교대조 (JWT 필요)
/api/inspections/**    → 검사 (JWT 필요)
/api/notifications/**  → 알림 (JWT 필요)
/api/dashboard/**      → 대시보드 통계 (ADMIN 전용)
/api/analysis/**       → 공정 개선 분석 (ADMIN 전용)

/internal/frames/**    → 프레임 수집 (내부 서비스 키)
/internal/callbacks/** → AI 분석 콜백 (내부 서비스 키)
```

---

## 도메인 개요

### User (사용자)
- 역할: `ADMIN` (관리자), `WORKER` (현장근로자)
- 상태: `PENDING` (가입 승인 대기) / `ACTIVE` / `INACTIVE`
- soft delete 지원 (`deletedAt` 필드)
- `shift_id` FK는 nullable — 가입 직후 PENDING 상태에서는 교대조 미배정, 관리자가 승인 시 지정
- 사번(`employeeId`)은 UUID 타입, 서버에서 자동 생성
- **비밀번호 없음** — 인증 정보는 Auth 도메인에서 완전 분리 관리

### Auth (인증)
- `user`와 완전 분리: `auth` 테이블에 `user_id`(FK), `email`, `password` 보관
- 인증 흐름:
  1. `POST /api/auth/register` — `userName`, `email`, `password`, `role`, `phone(선택)` 전송 → User(PENDING) + Auth 동시 생성
  2. `POST /api/users/{id}` (ADMIN) — 관리자가 `status: ACTIVE`, `shiftId` 지정하여 승인
  3. `POST /api/auth/login` — email + password → JWT 발급
- JWT는 stateless (세션 미사용), access token만 발급 (refresh token은 Redis 도입 후 추가 예정)
- `Authorization: Bearer <token>` 헤더로 인증

### Shift (교대조)
- 3교대: `DAY` / `EVENING` / `NIGHT` (ShiftType enum)
- 마스터 데이터: 1조(주간), 2조(오후), 3조(야간) — 초기 seed 데이터로 삽입
- `isActive`로 비활성화 관리 (물리 삭제 X)
- `BaseEntity` 상속 (createdAt, updatedAt 자동 관리)

### ShiftAssignment (날짜별 교대 배정)
- 사용자가 특정 날짜에 실제로 근무한 교대조 기록
- `User.shift`(기본 소속)와 별개로, 대타/교대 변경 등 실제 기록 추적
- `UNIQUE(user_id, work_date)` 제약으로 중복 배정 방지

### Line (생산라인) — 예정
- 생산라인 마스터 데이터
- `inspection`, `dashboard` 도메인과 연결

### Inspection (검사) — 예정
- 검사 생성, 상태 전환, 프레임 결과 저장
- 상태 머신: `PENDING → PROCESSING → DONE / FAILED`
- 불량 확정 시 `notification` 도메인으로 알림 트리거

### Notification (알림) — 예정
- 불량 발생 시 ADMIN 대상 알림 생성
- SSE 기반 실시간 push
- 읽음 처리

### Dashboard (대시보드/통계) — 예정
- 별도 엔티티 없이 검사 데이터 집계 쿼리 기반
- 불량률 추이, 불량 유형별/교대조별/라인별 통계

### Analysis (공정 개선 분석) — 예정
- AI 분석 서버에 누적 검사 데이터 기반 LLM 리포트 요청
- 비동기 처리: 요청 → AI 서버 처리 → 콜백 → 결과 저장 → 조회

---

## 보안 채널 구분

| 항목 | 외부 사용자 API | 내부 시스템 API |
|---|---|---|
| 인증 방식 | JWT (Bearer Token) | 서비스 키 / API Key |
| 호출 주체 | 사람 (브라우저/앱) | 서버 to 서버 |
| URL prefix | `/api/` | `/internal/` |
| Security Filter Chain | 사용자 JWT Filter | 내부 키 검증 Filter |

---

## 코딩 컨벤션

### DTO
- **record 사용 금지, class 기반으로 작성**
- 요청 DTO: `@Getter`, `@NoArgsConstructor`, `@Valid` 어노테이션 사용
- 응답 DTO: `@Getter`, `@Builder`, `@AllArgsConstructor` + `from(Entity)` 정적 팩토리 메서드

```java
// 요청 DTO 예시
@Getter
@NoArgsConstructor
public class ShiftCreateRequest {
    @NotNull
    private ShiftType shiftType;
}
// 응답 DTO 예시
@Getter
@Builder
@AllArgsConstructor
public class ShiftResponse {
    private Long shiftId;

    public static ShiftResponse from(Shift shift) {

    }
}
```

### Entity
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- `@Builder`는 클래스 레벨이 아닌 생성자에 붙임
- 상태 변경은 엔티티 내 메서드로 처리 (setter 사용 금지)
- 공통 시간 필드는 `BaseEntity` 상속으로 처리 (JPA Auditing)
- 물리 삭제 대신 soft delete (`deletedAt`) 권장

### 예외 처리
- 모든 도메인 예외는 `CustomException`을 상속
- 에러 코드는 `global/exception/ErrorCode.java`에서 중앙 관리
- `GlobalExceptionHandler`가 `CustomException`, `MethodArgumentNotValidException`, `Exception` 처리

```java
// 도메인 예외 예시
public class ShiftNotFoundException extends CustomException {
    public ShiftNotFoundException() {
        super(ErrorCode.SHIFT_NOT_FOUND);
    }
}
```

### ErrorCode HTTP 상태 기준
- `404 NOT_FOUND` — 존재하지 않는 리소스
- `409 CONFLICT` — 중복 (이메일, 사번, 배정 등)
- `400 BAD_REQUEST` — 잘못된 입력값
- `401 UNAUTHORIZED` — 인증 필요
- `403 FORBIDDEN` — 권한 없음

### Service
- 인터페이스(`ShiftService`) + 구현체(`ShiftServiceImpl`) 분리
- `@Transactional`은 구현체 클래스 레벨에 선언, 조회 메서드는 `@Transactional(readOnly = true)`

### API 응답 형식
- 성공: `CommonResponse<T>` 래핑
- 실패: `ErrorResponse { code, message }`
- HTTP 상태 코드는 `ResponseEntity`로 명시적으로 반환

---

## 중요 설정

### application.properties
```properties
# DB
spring.datasource.url=jdbc:mysql://localhost:3306/capstone?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=create        # 개발 중: create / 운영: validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
jwt.secret=<Base64 인코딩된 시크릿 키 — 운영 환경에서는 반드시 교체>
jwt.expiration=86400000   # 24시간 (ms)
```

### JPA Auditing
- `@EnableJpaAuditing`은 `JpaAuditingConfig.java`에 분리 선언 (CapstoneApplication에 두지 않음)
- `BaseEntity`의 `createdAt`, `updatedAt`이 자동 관리됨

### Security
- `/api/auth/**` — 인증 없이 접근 허용
- `/internal/**` — 내부 서비스 키 검증 (별도 Filter Chain)
- 나머지 모든 엔드포인트 — JWT 필요
- `JwtAuthenticationFilter`가 `UsernamePasswordAuthenticationFilter` 앞에서 실행

---

## 현재 구현 현황

| 도메인 | 상태 |
|---|---|
| global (예외, 응답, 보안 기반) | 완료 |
| user (CRUD, 예외 연결) | 완료 |
| shift — entity, 예외, Repository, DTO, Service, Controller | 완료 |
| auth — register(User+Auth 동시생성), login, JWT, Security | 완료 |
| notification — entity, SSE 구독, 알림 목록 조회, 읽음 처리 | 완료 |
| line | 예정 |
| inspection | 예정 |
| dashboard | 예정 |
| analysis | 예정 |
| internal (frame 수집, AI 콜백) | 예정 |

---

## 미결 사항

| 항목 | 내용 |
|---|---|
| PENDING 유저 API 접근 제한 | 승인 전 `/api/users/**`, `/api/shifts/**` 등 접근 차단 여부 결정 필요 |
| 가입 승인 전용 엔드포인트 | 현재 `PUT /api/users/{id}`로 우회 처리 중 — `POST /api/users/{id}/approve` 별도 추가 검토 |
| `line` 도메인 도입 | 생산라인별 통계 요구사항과 직결 |
| 내부 시스템 인증 방식 | API Key 정적 관리 vs 서비스 토큰 발급 방식 결정 필요 |
| 검사 상태 머신 정의 | `PENDING → PROCESSING → DONE/FAILED` 전환 규칙 명확화 |
| AI 분석 서버 연동 방식 | 동기 HTTP 호출 vs 비동기 메시지 큐 (향후 확장성) |
| dashboard 데이터 정합성 | 실시간 집계 쿼리 vs 별도 집계 테이블 캐싱 여부 |
| inspection과 frame의 관계 | 프레임을 inspection 하위로 볼지, 독립 엔티티로 볼지 |
| SSE 알림 대상 범위 | 전체 ADMIN / 특정 라인 담당자만 / 전체 |
| Redis 도입 시기 | refresh token 저장 용도 |
| seed 데이터 삽입 방식 | data.sql vs ApplicationRunner |
| Swagger 설정 추가 시점 | |