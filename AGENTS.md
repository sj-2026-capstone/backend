# 스마트 공정/현장 관리 플랫폼 — Backend

공장 현장의 불량 검사 이력 관리, 교대조 기반 작업 추적, 실시간 알림을 제공하는 백엔드 서버.
주요 사용자: 현장근로자(WORKER), 관리자(ADMIN).

---

## Tech Stack

| 항목 | 버전 / 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT |
| Database | MySQL (로컬: localhost:3306/capstone) |
| Validation | Spring Boot Starter Validation |
| Build | Gradle |
| API 문서 | Swagger (OpenAPI) — 예정 |
| 알림 | SSE (Server-Sent Events) — 예정 |
| Cache | Redis — 예정 |
| CI/CD | GitHub Actions / Jenkins — 예정 |

---

## 패키지 구조

```
src/main/java/com/sjcapstone/
├── CapstoneApplication.java
├── domain/
│   ├── auth/                  # 로그인, JWT 발급/재발급 (예정)
│   ├── shift/                 # 교대조 마스터, 날짜별 배정
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   │   ├── Shift.java
│   │   │   ├── ShiftType.java (enum: DAY/EVENING/NIGHT)
│   │   │   └── ShiftAssignment.java
│   │   ├── dto/
│   │   └── exception/
│   ├── user/                  # 사용자 프로필 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java  (enum: ADMIN/WORKER)
│   │   │   └── UserStatus.java (enum: ACTIVE/INACTIVE)
│   │   ├── dto/
│   │   └── exception/
│   ├── inspection/            # 검사 이력 (예정)
│   └── notification/          # SSE 알림 (예정)
└── global/
    ├── config/
    │   ├── JpaAuditingConfig.java
    │   └── SecurityConfig.java
    ├── entity/
    │   └── BaseEntity.java    # createdAt, updatedAt (JPA Auditing)
    ├── exception/
    │   ├── CustomException.java
    │   ├── ErrorCode.java     # 모든 에러 코드 중앙 관리
    │   └── GlobalExceptionHandler.java
    ├── response/
    │   ├── CommonResponse.java
    │   └── ErrorResponse.java
    └── security/
        ├── CustomUserDetails.java
        └── jwt/
            └── JwtProvider.java
```

---

## 도메인 개요

### User (사용자)
- 역할: `ADMIN` (관리자), `WORKER` (현장근로자)
- 상태: `ACTIVE`, `INACTIVE`
- soft delete 지원 (`deletedAt` 필드)
- 현재 소속 교대조를 `shift_id` FK로 보유
- 사번(`employeeId`)은 UUID 타입, 서버에서 자동 생성

### Shift (교대조)
- 3교대: `DAY` / `EVENING` / `NIGHT` (ShiftType enum)
- 마스터 데이터: 1조(주간), 2조(오후), 3조(야간) — 초기 seed 데이터로 삽입
- `isActive`로 비활성화 관리 (물리 삭제 X)
- `BaseEntity` 상속 (createdAt, updatedAt 자동 관리)

### ShiftAssignment (날짜별 교대 배정)
- 사용자가 특정 날짜에 실제로 근무한 교대조 기록
- `User.shift`(기본 소속)와 별개로, 대타/교대 변경 등 실제 기록 추적
- `UNIQUE(user_id, work_date)` 제약으로 중복 배정 방지

### Auth (인증) — 예정
- `user`와 완전 분리: 로그인, JWT 발급, 재발급, 로그아웃
- `domain/auth/` 패키지에 `AuthController`, `AuthService`, 관련 DTO 위치

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

    public static ShiftResponse from(Shift shift) { ... }
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
```

### JPA Auditing
- `@EnableJpaAuditing`은 `JpaAuditingConfig.java`에 분리 선언 (CapstoneApplication에 두지 않음)
- `BaseEntity`의 `createdAt`, `updatedAt`이 자동 관리됨

---

## 현재 구현 현황

| 도메인 | 상태 |
|---|---|
| global (예외, 응답, 보안 기반) | 완료 |
| user (CRUD, 예외 연결) | 완료 |
| shift — 1단계: entity (Shift, ShiftType, ShiftAssignment) | 완료 |
| shift — 2단계: 예외 클래스, ErrorCode | 완료 |
| shift — 3단계: Repository | 완료 |
| shift — 4단계: DTO | 완료 |
| shift — 5단계: Service | 완료 |
| shift — 6단계: Controller | 완료 |
| auth | 예정 |
| inspection | 예정 |
| notification | 예정 |

---

## 아직 결정되지 않은 것들

- JWT secret key 및 만료 시간 설정값
- Redis 도입 시기 (refresh token 저장 용도)
- inspection 도메인과 shift의 연결 방식 (shift_id FK 여부)
- notification 알림 발송 대상 범위 (교대조 전체 / 관리자만 / 전체)
- Swagger 설정 추가 시점
- 초기 seed 데이터 삽입 방식 (data.sql vs ApplicationRunner)