# 스마트 제조 품질관리 플랫폼 — Backend

제조 현장의 생산라인에서 카메라와 엣지 디바이스가 수집한 검사 데이터를 실시간으로 처리하고, 불량 발생 시 전체 직원에게 즉시 알림을 제공하는 스마트 품질관리 시스템.  
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
| 알림 | SSE (Server-Sent Events) — 완료 |
| HTTP Client | RestTemplate (AI 서버 연동) — 완료 |
| Cache | Redis — 예정 |
| CI/CD | GitHub Actions / Jenkins — 예정 |

---

## 주요 사용자

### Human Actor

| 역할 | 설명 |
|---|---|
| `WORKER` (현장 근로자) | 자신의 교대조 내 검사 이력 조회, 검사 상태 확인, 본인 프로필 조회, 불량 감지 알림 수신 |
| `ADMIN` (관리자) | 계정 생성/수정/상태변경, 전체 검사 이력 조회, 공정 개선 분석 요청, 실시간 알림 수신 |

### System Actor (내부 시스템 — 별도 인증 채널)

| 시스템 | 역할 |
|---|---|
| 카메라 / 엣지 디바이스 | 생산라인에서 검사 프레임 데이터를 백엔드로 전송 |
| AI 분석 서버 | 프레임 분석 완료 후 결과 콜백 전달, 공정 개선 LLM 리포트 반환 |

> 내부 시스템은 사용자 JWT가 아닌 **서비스 키 / API Key** 기반 인증을 사용한다.

---

## 기능 영역

### 1. 사용자 / 인증

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| 로그인 | 모든 사용자 | `POST /api/auth/login` |
| 내 정보 조회 | 로그인된 사용자 | `GET /api/auth/me` |
| 비밀번호 변경 | 로그인된 사용자 | `PATCH /api/auth/password` |
| 계정 생성 | ADMIN | `POST /api/admin/accounts` |
| 계정 목록 조회 (페이징, 검색) | ADMIN | `GET /api/admin/accounts` |
| 계정 상세 조회 | ADMIN | `GET /api/admin/accounts/{userId}` |
| 계정 수정 | ADMIN | `PUT /api/admin/accounts/{userId}` |
| 계정 상태 변경 | ADMIN | `PATCH /api/admin/accounts/{userId}/status` |
| 계정 요약 통계 | ADMIN | `GET /api/admin/accounts/summary` |
| 로그인 ID 중복 확인 | ADMIN | `GET /api/admin/accounts/login-id/availability` |

> - 회원가입은 없음. **관리자가 직접 계정을 생성**하는 방식.
> - 계정 생성 시 `passwordChangeRequired = true`로 설정 — 최초 로그인 후 비밀번호 변경 유도.
> - `UserStatus`: `PENDING` / `ACTIVE` / `INACTIVE`

### 2. 생산라인 (Line)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| 라인 목록 조회 | 로그인된 사용자 | `GET /api/lines` |
| 라인 상세 조회 | 로그인된 사용자 | `GET /api/lines/{lineId}` |

> - `LineCode` enum: `A`, `B`, `C`
> - 초기 seed 데이터는 `LineDataInitializer`(ApplicationRunner)로 삽입
> - `isActive`로 비활성화 관리; 비활성 라인에는 WORKER 배정 불가

### 3. 검사 (Inspection)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| 이미지 업로드 + 검사 생성 (테스트용) | ADMIN | `POST /api/inspections/upload` |
| 검사 생성 (테스트용) | ADMIN | `POST /api/inspections` |
| 최근 불량 5개 조회 (실시간 모니터링) | ADMIN | `GET /api/inspections/latest` |
| 검사 이력 목록 조회 (필터/페이징) | WORKER, ADMIN | `GET /api/inspections?lineId=&status=&page=&size=` |
| 검사 상세 조회 | WORKER, ADMIN | `GET /api/inspections/{inspectionId}` |
| 검사 상태 조회 | WORKER, ADMIN | `GET /api/inspections/{inspectionId}/status` |
| 조치 완료 처리 | WORKER, ADMIN | `PATCH /api/inspections/{inspectionId}/action` |
| 검사 분석 시작 (테스트용) | ADMIN | `POST /api/inspections/{inspectionId}/analyze` |
| 프레임 수집 (검사 생성 + 분석 즉시 시작) | 카메라/엣지 디바이스 | `POST /internal/frames` |
| 분석 완료 콜백 수신 | AI 분석 서버 | `POST /internal/callbacks/{inspectionId}` |

> - 검사 도메인은 **상태 머신** 성격: `PENDING → PROCESSING → DONE / FAILED`
> - `WORKER`는 자신이 배정된 라인의 검사만 조회 가능 (다른 라인 접근 시 404)
> - `ADMIN`은 전체 조회 가능, `lineId` / `status` 필터 지원
> - 콜백 수신 시 `hasDefect=true`이면 `NotificationService.sendDefectDetected()` 자동 호출 → ADMIN 전체 알림 발송, `actionStatus = UNRESOLVED` 자동 세팅
> - `DefectType`: `SCRATCH` / `DENT` / `CRACK` / `CONTAMINATION` / `MISSING_PART` / `DIMENSION_ERROR`
> - `ActionStatus`: `UNRESOLVED` (불량 확정 시 자동) / `RESOLVED` (조치 완료 처리 후)
> - **실제 운영 흐름** (`POST /internal/frames`): 카메라가 이미지 + lineId만 전송 → 교대조/근로자는 현재 시간·라인 배정 기준으로 백엔드가 자동 결정 → 검사 생성 + AI 분석 즉시 시작
> - **개발/테스트 흐름** (`POST /api/inspections/upload` → `POST /api/inspections/{id}/analyze`): 브라우저에서 이미지 업로드 후 분석 시작. 카메라 연동 완료 후 제거 검토 필요

### 4. 알림 (Notification)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| SSE 실시간 알림 구독 | WORKER, ADMIN | `GET /api/notifications/subscribe` |
| 알림 목록 조회 (필터/페이징) | WORKER, ADMIN | `GET /api/notifications?read=&page=&size=` |
| 미확인 알림 개수 조회 | WORKER, ADMIN | `GET /api/notifications/unread-count` |
| 단건 읽음 처리 | WORKER, ADMIN | `PATCH /api/notifications/{notificationId}/read` |
| 전체 읽음 처리 | WORKER, ADMIN | `PATCH /api/notifications/read-all` |

> - 알림의 트리거 주체는 **검사 도메인** (불량 확정 시 발생). 알림은 수신/관리만 담당.
> - `read` 쿼리 파라미터: 없으면 전체, `false`면 미확인, `true`면 확인완료
> - SSE 이벤트 이름: 최초 연결 시 `connected`, 새 알림 수신 시 `notification`
> - inspection 도메인에서 `NotificationService.sendDefectDetected(lineName, defectType, handlerName)` 호출로 **ACTIVE 상태 전체 사용자(ADMIN + WORKER) 발송**
> - `NotificationType`: `DEFECT_DETECTED` / `SYSTEM`

### 5. 대시보드 / 통계 (Dashboard)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| 대시보드 통합 조회 | ADMIN | `GET /api/dashboard` |

> - 별도 엔티티 없이 **검사 데이터 집계 쿼리** 기반
> - 응답 구조: `summary` (요약 지표) / `defectRateTrend` (최근 7일 일별 불량률) / `actionSummary` (조치 현황) / `lineDefectRates` (라인별 불량률) / `latestAnalysis` (최신 RAG 분석 요약) / `lastUpdatedAt`
> - 불량 판정 기준: `Inspection.hasDefect = true`
> - 변화율(`totalInspectionChangeRate`, `defectRateChange`): 최근 7일 vs 이전 7일 비교
> - `actionSummary`: `Inspection.actionStatus` 기반 실제 집계 — `total`(전체 불량 수) / `unresolvedCount`(미처리) / `resolvedCount`(처리 완료) / `completionRate`(처리율 %)
> - `latestAnalysis`: `AnalysisRepository.findTopByRagUsedTrueOrderByCreatedAtDesc()`로 조회 — `analysisId`, `status`, `fromDate`, `toDate`, `totalInspectionCount`, `totalDefectCount`, `patternCount`, `highestSeverity`, `analyzedAt` 반환. 이력 없으면 `{ analysisId: null, status: null }` 반환

### 6. 공정 개선 분석 (Analysis)

#### 6-1. 레거시 분석 (RAG 미적용)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| AI 자율 공정 분석 시작 | ADMIN | `POST /api/analysis` |
| 공정 개선 분석 목록 조회 (페이징) | ADMIN | `GET /api/analysis?page=&size=` |
| 최신 분석 결과 조회 | ADMIN | `GET /api/analysis/latest` |
| 공정 개선 분석 상세 조회 | ADMIN | `GET /api/analysis/{analysisId}` |
| 공정 분석 완료 콜백 수신 (예비용) | AI 분석 서버 | `POST /internal/analysis-callbacks/{analysisId}` |

> - **처리 흐름 (OpenAI 직접 연동)**: `POST /api/analysis` → 최근 30일 검사 데이터 수집 → `OpenAiClient`로 OpenAI API(`gpt-4o-mini`) 직접 호출 → 결과 즉시 DB 저장 → `GET /api/analysis/latest`로 조회
> - `inspection`과 연결되지만 목적·생명주기가 달라 **독립 도메인**으로 분리
> - 상태 머신: `PENDING → PROCESSING → DONE / FAILED`
> - 결과는 `patterns`(발견된 패턴 목록)와 `recommendations`(추천 조치 목록)로 구성 — DB에 JSON TEXT로 저장
> - `SeverityLevel`: `HIGH`(높음) / `MEDIUM`(중간) / `LOW`(관찰)
> - AI 서버 콜백 엔드포인트(`/internal/analysis-callbacks/{id}`)는 코드에 유지되어 있으나 현재 흐름에서는 미사용

#### 6-2. RAG 기반 공정 분석 (신규)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| RAG 공정 분석 시작 | ADMIN | `POST /api/analysis/process/start` |
| RAG 최신 분석 결과 조회 | ADMIN | `GET /api/analysis/process/latest` |
| RAG 분석 상세 조회 | ADMIN | `GET /api/analysis/process/{analysisId}` |
| RAG 분석 이력 목록 조회 (페이징) | ADMIN | `GET /api/analysis/process/history?page=&size=` |

> - **처리 흐름 (RAG + OpenAI)**:
>   1. `POST /api/analysis/process/start` → `ProcessAnalysis` 저장(`ragUsed=true`, PENDING→PROCESSING)
>   2. 기간/라인 조건으로 `InspectionRepository` 집계 쿼리 5종 실행
>   3. `SimpleRagChunkRetriever`가 집계 결과를 텍스트 청크로 변환 + 관련성 점수 계산 → 상위 5개 선별
>   4. 선별된 청크를 프롬프트에 삽입 → `OpenAiClient.analyzeProcess(prompt)` 호출 (temperature=0.5)
>   5. 응답 JSON 파싱 → `complete()` 호출(DONE) → 결과 저장
> - **DB 커넥션 분리**: `startAnalysis()`는 `NOT_SUPPORTED` + `TransactionTemplate` 패턴 — 각 DB 작업을 개별 트랜잭션으로 분리, OpenAI 호출 중 커넥션 미점유
> - `ragUsed=true` 플래그로 레거시 분석과 구분 — 기존 `/api/analysis/**` 엔드포인트에 영향 없음
> - **요청 파라미터** (모두 선택): `fromDate`, `toDate` (미입력 시 최근 30일), `lineId` (미입력 시 전체 라인)
> - **패턴 응답**: `patternId`, `title`, `description`, `severity`, `relatedLine`, `relatedTimeRange`, `metric`, `evidenceSummary`
> - **추천 응답**: `priority`, `title`, `description`, `targetLine`, `expectedEffect`, `relatedPatternIds`
> - **메타데이터**: `analysisBaseTime`, `fromDate`, `toDate`, `totalInspectionCount`, `totalDefectCount`, `modelName`, `ragUsed`
> - **RAG 청크 5종**: `LINE_STATS` / `SHIFT_STATS` / `HOUR_STATS` / `DEFECT_TYPE_STATS` / `WEEKLY_TREND`
> - `RagChunkRetriever` 인터페이스 뒤에 `SimpleRagChunkRetriever` 구현체 — 향후 pgvector/Elasticsearch/Qdrant로 교체 가능

### 7. 내부 시스템 연동 API (Internal)

| 기능 | 주체 | 엔드포인트 |
|---|---|---|
| 프레임 수집 API | 카메라/엣지 디바이스 → 백엔드 | `POST /internal/frames` |
| 검사 분석 완료 콜백 API | AI 서버 → 백엔드 | `POST /internal/callbacks/{inspectionId}` |
| 공정 분석 완료 콜백 API | AI 서버 → 백엔드 | `POST /internal/analysis-callbacks/{analysisId}` |

> 사용자 JWT 아님. `/internal/**` prefix로 분리, 별도 Security Filter Chain 적용.

---

## 패키지 구조

```
src/main/java/com/sjcapstone/
├── CapstoneApplication.java
├── domain/
│   ├── admin/                 # 관리자 전용 계정 관리
│   │   ├── controller/
│   │   │   └── AdminAccountController.java
│   │   ├── service/
│   │   │   ├── AdminAccountService.java
│   │   │   └── AdminAccountServiceImpl.java
│   │   ├── dto/
│   │   │   ├── AdminAccountCreateRequest.java
│   │   │   ├── AdminAccountUpdateRequest.java
│   │   │   ├── AdminAccountStatusUpdateRequest.java
│   │   │   ├── AdminAccountResponse.java
│   │   │   ├── AdminAccountListItemResponse.java
│   │   │   ├── AdminAccountPageResponse.java
│   │   │   ├── AdminAccountSummaryResponse.java
│   │   │   └── LoginIdAvailabilityResponse.java
│   │   └── exception/
│   ├── auth/                  # 인증 (로그인, 내 정보, 비밀번호 변경)
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   └── AuthServiceImpl.java
│   │   ├── repository/
│   │   │   └── AuthRepository.java
│   │   ├── entity/
│   │   │   └── Auth.java      # auth 테이블 (user_id FK, login_id, password, passwordChangeRequired)
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── MeResponse.java
│   │   │   └── ChangePasswordRequest.java
│   │   └── exception/
│   │       ├── AuthNotFoundException.java
│   │       ├── InvalidPasswordException.java
│   │       ├── DuplicateLoginIdException.java
│   │       └── PasswordConfirmMismatchException.java
│   ├── user/                  # 사용자 프로필, 역할, 승인 상태
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   └── UserServiceImpl.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java   (enum: ADMIN/WORKER)
│   │   │   └── UserStatus.java (enum: ACTIVE/INACTIVE/PENDING)
│   │   ├── dto/
│   │   │   ├── UserResponse.java
│   │   │   ├── UserListResponse.java
│   │   │   └── UserUpdateRequest.java
│   │   └── exception/
│   │       ├── UserNotFoundException.java
│   │       ├── ShiftRequiredForWorkerException.java
│   │       ├── LineRequiredForWorkerException.java
│   │       ├── DuplicateEmailException.java
│   │       ├── DuplicateEmployeeIdException.java
│   │       └── DuplicateEmployeeNumberException.java
│   ├── shift/                 # 교대조 마스터, 날짜별 배정
│   │   ├── controller/
│   │   │   └── ShiftController.java
│   │   ├── service/
│   │   │   ├── ShiftService.java
│   │   │   └── ShiftServiceImpl.java
│   │   ├── repository/
│   │   │   ├── ShiftRepository.java
│   │   │   └── ShiftAssignmentRepository.java
│   │   ├── entity/
│   │   │   ├── Shift.java
│   │   │   ├── ShiftType.java  (enum: DAY/EVENING/NIGHT)
│   │   │   └── ShiftAssignment.java
│   │   ├── dto/
│   │   │   ├── ShiftCreateRequest.java
│   │   │   ├── ShiftUpdateRequest.java
│   │   │   ├── ShiftResponse.java
│   │   │   ├── ShiftAssignmentRequest.java
│   │   │   └── ShiftAssignmentResponse.java
│   │   └── exception/
│   │       ├── ShiftNotFoundException.java
│   │       ├── ShiftInactiveException.java
│   │       ├── ShiftAlreadyAssignedException.java
│   │       └── InvalidShiftTimeException.java
│   ├── line/                  # 생산라인 마스터
│   │   ├── controller/
│   │   │   └── LineController.java
│   │   ├── service/
│   │   │   ├── LineService.java
│   │   │   └── LineServiceImpl.java
│   │   ├── repository/
│   │   │   └── LineRepository.java
│   │   ├── entity/
│   │   │   ├── Line.java       # production_lines 테이블
│   │   │   └── LineCode.java   (enum: A/B/C)
│   │   ├── dto/
│   │   │   └── LineResponse.java
│   │   └── exception/
│   │       └── LineNotFoundException.java
│   ├── inspection/            # 검사 생성, 상태 머신, AI 연동, 결과 저장 — 완료
│   │   ├── controller/
│   │   │   └── InspectionController.java
│   │   ├── service/
│   │   │   ├── InspectionService.java
│   │   │   └── InspectionServiceImpl.java
│   │   ├── repository/
│   │   │   └── InspectionRepository.java
│   │   ├── entity/
│   │   │   ├── Inspection.java
│   │   │   ├── InspectionStatus.java  (enum: PENDING/PROCESSING/DONE/FAILED)
│   │   │   ├── ActionStatus.java      (enum: UNRESOLVED/RESOLVED)
│   │   │   └── DefectType.java        (enum: SCRATCH/DENT/CRACK/CONTAMINATION/MISSING_PART/DIMENSION_ERROR)
│   │   ├── dto/
│   │   │   ├── InspectionCreateRequest.java
│   │   │   ├── InspectionResponse.java
│   │   │   ├── InspectionListItemResponse.java
│   │   │   ├── InspectionPageResponse.java
│   │   │   ├── InspectionStatusResponse.java
│   │   │   └── RecentDefectResponse.java
│   │   └── exception/
│   │       ├── InspectionNotFoundException.java
│   │       └── InvalidInspectionStatusException.java
│   ├── notification/          # 알림 생성, SSE 구독, 필터/페이징 조회, 읽음 처리 — 완료
│   │   ├── controller/
│   │   │   └── NotificationController.java
│   │   ├── service/
│   │   │   ├── NotificationService.java
│   │   │   └── NotificationServiceImpl.java
│   │   ├── repository/
│   │   │   ├── NotificationRepository.java
│   │   │   └── SseEmitterRepository.java
│   │   ├── entity/
│   │   │   ├── Notification.java
│   │   │   └── NotificationType.java  (enum: DEFECT_DETECTED/SYSTEM)
│   │   ├── dto/
│   │   │   ├── NotificationResponse.java
│   │   │   ├── NotificationPageResponse.java
│   │   │   └── UnreadCountResponse.java
│   │   └── exception/
│   │       └── NotificationNotFoundException.java
│   ├── dashboard/             # 통계 집계 API — 읽기 전용 — 완료
│   │   ├── controller/
│   │   │   └── DashboardController.java
│   │   ├── service/
│   │   │   ├── DashboardService.java
│   │   │   └── DashboardServiceImpl.java
│   │   └── dto/
│   │       ├── DashboardResponse.java
│   │       ├── DashboardSummaryResponse.java
│   │       ├── DefectRateTrendItemResponse.java
│   │       ├── ActionSummaryResponse.java
│   │       ├── LineDefectRateResponse.java
│   │       └── projection/
│   │           ├── DailyDefectStatsProjection.java
│   │           └── LineDefectStatsProjection.java
│   └── analysis/              # AI 공정 개선 분석 요청/결과 관리 — 완료
│       ├── controller/
│       │   ├── AnalysisController.java          # 레거시 /api/analysis/**
│       │   └── ProcessAnalysisController.java   # RAG 기반 /api/analysis/process/**
│       ├── service/
│       │   ├── AnalysisService.java
│       │   ├── AnalysisServiceImpl.java
│       │   ├── ProcessAnalysisService.java      # RAG 분석 서비스 인터페이스
│       │   └── ProcessAnalysisServiceImpl.java  # RAG 분석 구현체
│       ├── repository/
│       │   └── AnalysisRepository.java          # ragUsed 구분 쿼리 포함
│       ├── entity/
│       │   ├── ProcessAnalysis.java             # from_date, to_date, filter_line_id, total_inspection_count, total_defect_count, model_name, rag_used 컬럼 추가
│       │   ├── AnalysisStatus.java  (enum: PENDING/PROCESSING/DONE/FAILED)
│       │   └── SeverityLevel.java   (enum: HIGH/MEDIUM/LOW)
│       ├── dto/
│       │   ├── PatternDto.java                       # 레거시용
│       │   ├── RecommendationDto.java                # 레거시용
│       │   ├── ProcessAnalysisCallbackRequest.java
│       │   ├── AnalysisStartResponse.java
│       │   ├── AnalysisResponse.java
│       │   ├── AnalysisListItemResponse.java
│       │   ├── AnalysisPageResponse.java
│       │   ├── ProcessPatternDto.java                # RAG 패턴: patternId, title, description, severity, relatedLine, relatedTimeRange, metric, evidenceSummary
│       │   ├── ProcessRecommendationDto.java         # RAG 추천: priority, title, description, targetLine, expectedEffect, relatedPatternIds
│       │   ├── AnalysisMetadataDto.java              # analysisBaseTime, fromDate, toDate, totalInspectionCount, totalDefectCount, modelName, ragUsed
│       │   ├── ProcessAnalysisStartRequest.java      # fromDate?, toDate?, lineId? (모두 선택)
│       │   ├── ProcessAnalysisResponse.java          # RAG 분석 상세 응답
│       │   ├── ProcessAnalysisListItemResponse.java  # RAG 분석 목록 항목
│       │   ├── ProcessAnalysisPageResponse.java      # RAG 분석 페이징 응답
│       │   └── projection/
│       │       ├── LineDefectRangeProjection.java    # lineId, lineName, inspectionCount, defectCount
│       │       ├── ShiftDefectRangeProjection.java   # shiftName, startTime, endTime, inspectionCount, defectCount
│       │       ├── HourlyDefectProjection.java       # hour, inspectionCount, defectCount
│       │       ├── DefectTypeRangeProjection.java    # defectType, defectCount
│       │       └── WeeklyDefectProjection.java       # week, inspectionCount, defectCount
│       └── exception/
│           └── AnalysisNotFoundException.java
├── internal/                  # 내부 시스템 전용 API (별도 보안 채널)
│   ├── frame/                 # 프레임 수집 (카메라/엣지 디바이스) — 완료
│   │   └── InternalFrameController.java
│   ├── callback/              # 검사 분석 완료 콜백 (AI 서버) — 완료
│   │   ├── InternalCallbackController.java
│   │   └── dto/
│   │       └── AnalysisCallbackRequest.java
│   └── analysis/              # 공정 분석 완료 콜백 (AI 서버) — 완료
│       └── InternalAnalysisCallbackController.java
└── global/
    ├── config/
    │   ├── JpaAuditingConfig.java
    │   ├── SecurityConfig.java      # 사용자 JWT + 내부 시스템 키 — 2개 Filter Chain
    │   ├── RestTemplateConfig.java  # RestTemplate 빈 등록 (AI 서버 HTTP 호출용)
    │   ├── CorsConfig.java
    │   ├── AdminDataInitializer.java
    │   └── LineDataInitializer.java # ApplicationRunner — A/B/C 라인 seed 데이터
    ├── client/
    │   ├── AiAnalysisClient.java    # AI 서버 HTTP 호출 (POST /analyze — 검사 분석용)
    │   ├── AiAnalysisRequest.java   # 검사 분석 요청 DTO { inspectionId, imageUrl, callbackUrl }
    │   ├── AiProcessAnalysisRequest.java  # 공정 분석 요청 DTO { analysisId, callbackUrl } (현재 미사용)
    │   └── OpenAiClient.java        # OpenAI API 직접 호출 — analyze()(레거시), analyzeProcess()(RAG용, temperature=0.5)
    ├── rag/
    │   ├── RagQuery.java            # { from, to, lineId, topN }
    │   ├── RagChunk.java            # { chunkId, type(ChunkType), content, relevanceScore }
    │   ├── RagChunkRetriever.java   # 인터페이스 — 향후 Vector DB 교체 포인트
    │   └── SimpleRagChunkRetriever.java  # SQL 집계 기반 구현체, 5종 청크 + 관련성 점수 계산
    ├── entity/
    │   └── BaseEntity.java          # createdAt, updatedAt (JPA Auditing)
    ├── exception/
    │   ├── CustomException.java
    │   ├── ErrorCode.java           # 모든 에러 코드 중앙 관리
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
        └── internal/              # 내부 시스템 키 검증 — 완료
            ├── InternalApiKeyFilter.java
            └── InternalApiKeyProperties.java
```

---

## URL 구조

```
/api/auth/login            → 로그인 (공개)
/api/auth/me               → 내 정보 조회 (JWT 필요)
/api/auth/password         → 비밀번호 변경 (JWT 필요)

/api/admin/**              → 관리자 전용 (ADMIN 권한 필요)

/api/users/**              → 사용자 (JWT 필요)
/api/shifts/**             → 교대조 (JWT 필요)
/api/lines/**              → 생산라인 (JWT 필요)
/api/inspections/**        → 검사 (JWT 필요)
/api/notifications/subscribe          → SSE 구독 (WORKER, ADMIN, JWT 필요)
/api/notifications                    → 알림 목록 조회 (WORKER, ADMIN, ?read=&page=&size=)
/api/notifications/unread-count       → 미확인 개수 조회 (WORKER, ADMIN)
/api/notifications/{id}/read          → 단건 읽음 처리 (WORKER, ADMIN)
/api/notifications/read-all           → 전체 읽음 처리 (WORKER, ADMIN)
/api/dashboard/**          → 대시보드 통계 (ADMIN 전용)
/api/analysis/**           → 공정 개선 분석 레거시 (ADMIN 전용)
/api/analysis/process/**   → RAG 기반 공정 분석 (ADMIN 전용)

/internal/frames/**                  → 프레임 수집 (내부 서비스 키)
/internal/callbacks/**               → 검사 AI 분석 콜백 (내부 서비스 키)
/internal/analysis-callbacks/**      → 공정 분석 콜백 (내부 서비스 키)
```

---

## 도메인 개요

### User (사용자)
- 역할: `ADMIN` (관리자), `WORKER` (현장근로자)
- 상태: `PENDING` (승인 대기) / `ACTIVE` / `INACTIVE`
- soft delete 지원 (`deletedAt` 필드)
- `shift_id` FK, `line_id` FK 모두 nullable (ADMIN 계정은 shift/line 미배정 가능)
- `WORKER` 계정 생성·수정 시 `shiftId`, `lineId` 필수
- 사번(`employeeId`)은 UUID 타입, 서버에서 자동 생성
- `email`은 nullable — 선택 입력 (loginId가 주 식별자)
- **비밀번호 없음** — 인증 정보는 Auth 도메인에서 완전 분리 관리
- `UserRepository.findAllByStatusAndDeletedAtIsNull(UserStatus)` — notification 도메인에서 ACTIVE 전체 발송 시 사용

### Auth (인증)
- `user`와 완전 분리: `auth` 테이블에 `user_id`(FK), `login_id`, `password`, `passwordChangeRequired` 보관
- **이메일 기반 로그인 아님** — `loginId`(최대 50자) 기반 로그인
- JWT 클레임: `sub`(userId), `loginId`, `role`
- 인증 흐름:
  1. `POST /api/admin/accounts` (ADMIN) — User(ACTIVE) + Auth(`passwordChangeRequired=true`) 동시 생성
  2. `POST /api/auth/login` — loginId + password → JWT 발급, `passwordChangeRequired` 응답 포함
  3. `PATCH /api/auth/password` — 현재 비밀번호 확인 후 신규 비밀번호로 변경
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

### Line (생산라인)
- `LineCode` enum: `A`, `B`, `C`
- `production_lines` 테이블, `isActive`로 비활성화 관리
- `LineDataInitializer`(ApplicationRunner)가 앱 시작 시 A/B/C 라인 seed 삽입 (중복 삽입 방지)
- 비활성 라인에는 WORKER 배정 불가 (`findByIdAndIsActiveTrue` 사용)
- `User`의 `line_id` FK로 연결

### Inspection (검사)
- 검사 생성, 상태 전환, 프레임 결과 저장
- 상태 머신: `PENDING → PROCESSING → DONE / FAILED`
- 불량 확정 시 `notification` 도메인으로 알림 트리거 + `actionStatus = UNRESOLVED` 자동 세팅
- `ActionStatus` enum: `UNRESOLVED` (조치 미처리) / `RESOLVED` (조치 완료) — 정상 검사는 `null`
- **실시간 검사 흐름** (카메라/엣지 디바이스): `POST /internal/frames` (이미지 + lineId) → 교대조·근로자 자동 결정 → `createInspectionAndStartAnalysis()` → DB 커밋 후 AI 호출 (커넥션 분리)
- **테스트 흐름** (ADMIN): `POST /api/inspections/upload` (이미지 업로드 + 검사 생성) → `POST /api/inspections/{id}/analyze` → AI 서버 연동
- `POST /internal/frames` 파라미터: `file` (이미지) + `lineId`만 필요. 교대조는 `ShiftRepository.findCurrentShift(LocalTime)` (야간 교대 자정 넘김 포함), 근로자는 `UserRepository.findFirstByLine_IdAndRoleAndStatusAndDeletedAtIsNull()`로 자동 결정
- AI 서버 요청 페이로드: `{ inspectionId, imageUrl, callbackUrl }` (`callbackUrl` = `app.base-url + /internal/callbacks/{id}`)
- **DB 커넥션 분리**: `createInspectionAndStartAnalysis()`, `startAnalysis()` 메서드는 `@Transactional(propagation = NOT_SUPPORTED)` + `TransactionTemplate`으로 DB 저장을 먼저 커밋한 뒤 AI HTTP 호출 수행 → 커넥션 풀 고갈 방지
- Grad-CAM 이미지: AI 서버가 콜백으로 `gradCamImageUrl` 전달 → `inspections.grad_cam_image_url`에 저장 → 백엔드가 `/grad-cam-images/**` 경로로 정적 서빙 (`file.grad-cam-dir` 설정 필요)

### Notification (알림)
- 불량 발생 시 **ACTIVE 상태 전체 사용자(ADMIN + WORKER)** 대상 알림 생성 (soft delete된 사용자 제외)
- SSE 기반 실시간 push (`connected` / `notification` 이벤트)
- 알림 목록 조회: `isRead` 필터(전체/미확인/확인완료) + 페이징 (`NotificationPageResponse`)
- 미확인 알림 개수 조회 (`UnreadCountResponse`)
- 단건 읽음 처리 / 전체 읽음 처리 (bulk update)
- **SSE 커넥션 풀 고갈 방지**: `subscribe()` 메서드는 `@Transactional(propagation = NOT_SUPPORTED)` 적용 — SSE 연결이 30분간 유지되는 동안 DB 커넥션을 점유하지 않음
- **inspection 도메인 연동 진입점**:
  - `sendToAdmins(type, title, message)` — 모든 ADMIN에게만 직접 발송 (시스템 알림 등에 사용)
  - `sendDefectDetected(lineName, defectType, handlerName)` — 불량 감지 시 **ACTIVE 전체 사용자** 발송 (`handlerName` nullable)
  - inspection 엔티티에 직접 의존하지 않음; 파라미터는 primitive/String 기반
- `NotificationType`: `DEFECT_DETECTED` / `SYSTEM`
- 알림 응답 예시:
  ```json
  {
    "notificationId": 10,
    "notificationType": "DEFECT_DETECTED",
    "title": "불량 부품 감지",
    "message": "A라인 - 불량 감지 | 담당자: 김철수",
    "isRead": false,
    "createdAt": "2026-04-11T14:32:05"
  }
  ```

### Dashboard (대시보드/통계)
- 별도 엔티티 없이 검사 데이터 집계 쿼리 기반
- `GET /api/dashboard` — ADMIN 전용, 단일 엔드포인트로 전체 대시보드 데이터 반환
- **집계 항목**: 전체/오늘 검사 수, 불량률, 최근 7일 불량률 추이, 라인별 불량률, 최신 RAG 분석 요약
- **변화율 산정**: 최근 7일 vs 이전 7일 구간 비교
- `InspectionRepository`에 native query 추가 (`findDailyDefectStatsSince`, `findLineDefectStats`)
- `actionSummary`: `InspectionRepository.countByActionStatus(ActionStatus)`로 실제 집계 — `total` / `unresolvedCount` / `resolvedCount` / `completionRate` 반환
- `latestAnalysis`: `AnalysisRepository.findTopByRagUsedTrueOrderByCreatedAtDesc()`로 최신 RAG 분석 요약 반환 (`LatestAnalysisSummaryResponse`) — 이력 없으면 `none()` 반환
- `DashboardServiceImpl` 의존성: `InspectionRepository`, `LineRepository`, `AnalysisRepository`, `ObjectMapper`

### Analysis (공정 개선 분석)

#### 레거시 분석 (`/api/analysis/**`)
- `process_analyses` 테이블, `BaseEntity` 상속 (createdAt, updatedAt)
- 상태 머신: `PENDING → PROCESSING → DONE / FAILED`
- `patterns`(JSON TEXT), `recommendations`(JSON TEXT) — Jackson ObjectMapper로 직렬화/역직렬화
- **처리 흐름 (OpenAI 직접 연동)**:
  1. `POST /api/analysis` → `ProcessAnalysis` 저장(PENDING→PROCESSING)
  2. 최근 30일 `DONE` 상태 검사 데이터 수집
  3. 라인별·불량 유형별·교대조별 집계 → 텍스트 프롬프트 구성
  4. `OpenAiClient.analyze(prompt)` → OpenAI `gpt-4o-mini` 호출 (DB 커넥션 미점유)
  5. 응답 JSON 파싱 → `complete()` 호출(DONE) → 조회
- AI 호출 실패 시 `fail(errorMessage)` 호출, 상태 FAILED (예외 전파 없음)
- **DB 커넥션 분리**: `startAnalysis()`는 `NOT_SUPPORTED` + `TransactionTemplate` 패턴으로 각 DB 작업을 개별 트랜잭션으로 분리
- `AnalysisRepository.findTopByOrderByCreatedAtDesc()` — `GET /api/analysis/latest`용
- `processCallback()` 메서드는 코드에 유지 (외부 AI 서버 콜백 수신 예비용) — 현재 흐름에서는 미호출

#### RAG 기반 분석 (`/api/analysis/process/**`)
- 동일한 `process_analyses` 테이블 공유, `ragUsed=true`로 구분
- **ProcessAnalysis 엔티티 추가 컬럼**: `from_date`, `to_date`, `filter_line_id`, `total_inspection_count`, `total_defect_count`, `model_name`, `rag_used`
- **AnalysisRepository 추가 쿼리**:
  - `findByIdAndRagUsedTrue(Long id)` — RAG 분석 상세 조회 (레거시 id 차단)
  - `findTopByRagUsedTrueOrderByCreatedAtDesc()` — 최신 RAG 분석 (대시보드용)
  - `findByRagUsedTrueOrderByCreatedAtDesc(Pageable)` — RAG 분석 이력 페이징
- **InspectionRepository 추가 쿼리** (5종 집계 + 2종 카운트):
  - `findLineDefectStatsByRange(from, to, lineId)` → `LineDefectRangeProjection`
  - `findShiftDefectStatsByRange(from, to, lineId)` → `ShiftDefectRangeProjection`
  - `findHourlyDefectStatsByRange(from, to, lineId)` → `HourlyDefectProjection`
  - `findDefectTypeStatsByRange(from, to, lineId)` → `DefectTypeRangeProjection`
  - `findWeeklyDefectStatsByRange(from, to, lineId)` → `WeeklyDefectProjection`
  - `countByStatusAndCreatedAtBetween(status, from, to)`
  - `countDefectsByStatusAndRange(status, from, to)`
- **RAG 청크 관련성 점수 기준**: lineId 일치 +8~10, 불량률 >15% +5, 불량 건수 많음 +4, 주간 추이 증가 +5
- **OpenAiClient 메서드 분리**: `analyze(prompt)` (레거시, temperature=0.7) / `analyzeProcess(prompt)` (RAG용, temperature=0.5, 더 엄격한 JSON 스키마 요구)
- `ProcessAnalysisServiceImpl.startAnalysis()`: `@Transactional(propagation = NOT_SUPPORTED)` + `TransactionTemplate` 동일 패턴

---

## 보안 채널 구분

| 항목 | 외부 사용자 API | 내부 시스템 API |
|---|---|---|
| 인증 방식 | JWT (Bearer Token) | 서비스 키 / API Key |
| 호출 주체 | 사람 (브라우저/앱) | 서버 to 서버 |
| URL prefix | `/api/` | `/internal/` |
| Security Filter Chain | 사용자 JWT Filter | 내부 키 검증 Filter |

### SecurityConfig 공개 엔드포인트
- `POST /api/auth/login` — 인증 없이 접근 허용 (로그인만 공개, `/api/auth/**` 전체 공개 아님)
- `/images/**` — 인증 없이 접근 허용 (업로드 이미지 정적 서빙)
- `/api/admin/**` — `ADMIN` 권한 필요 (`hasRole("ADMIN")`)
- `/api/dashboard/**` — `ADMIN` 권한 필요 (`hasRole("ADMIN")`)
- `/api/analysis/**` — `ADMIN` 권한 필요 (`hasRole("ADMIN")`) — 레거시 및 RAG 분석 모두 포함 (`/api/analysis/process/**` 포함)
- 나머지 모든 엔드포인트 (`/api/notifications/**`, `/api/inspections/**` 등) — JWT 필요 (WORKER, ADMIN 모두 접근 가능)

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
- `409 CONFLICT` — 중복 (loginId, 이메일, 사번, 배정 등)
- `400 BAD_REQUEST` — 잘못된 입력값 (비밀번호 불일치, 비활성 라인/교대조 등)
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

# Internal API Key
internal.service-key=YOUR_INTERNAL_SERVICE_KEY

# AI Server (ngrok URL - update on every ngrok restart) — 검사 분석용
ai.server.url=https://your-ai-ngrok-url.ngrok.io

# Backend public URL (used as callback base URL for AI server)
app.base-url=https://your-backend-ngrok-url.ngrok.io

# OpenAI API Key — 공정 개선 분석용 (gpt-4o-mini)
openai.api-key=YOUR_OPENAI_API_KEY

# File upload
file.upload-dir=./uploads/images
file.grad-cam-dir=/Users/kimsohee/PycharmProjects/AI/ai/grad_cam_images  # AI 서버가 저장하는 절대경로
```

> `ai.server.url`과 `app.base-url`은 ngrok 재실행 시마다 새 URL로 교체 필요.
> `file.grad-cam-dir`은 AI 서버의 grad-cam 이미지 저장 경로 — 백엔드가 `/grad-cam-images/**`로 정적 서빙.
> `openai.api-key`는 공정 개선 분석(`POST /api/analysis`)에서 OpenAI API 직접 호출 시 필요.

### JPA Auditing
- `@EnableJpaAuditing`은 `JpaAuditingConfig.java`에 분리 선언 (CapstoneApplication에 두지 않음)
- `BaseEntity`의 `createdAt`, `updatedAt`이 자동 관리됨

### Seed 데이터
- `LineDataInitializer` — ApplicationRunner 방식, A/B/C 라인 초기 삽입 (중복 시 skip)
- Shift 마스터 데이터도 동일 방식 권장

---

## 현재 구현 현황

| 도메인 | 상태 |
|---|---|
| global (예외, 응답, 보안 기반) | 완료 |
| auth — loginId 기반 로그인, 내 정보 조회, 비밀번호 변경, JWT | 완료 |
| admin — 계정 생성/수정/상태변경/목록/상세/요약/loginId 중복확인 | 완료 |
| user — CRUD, 예외 연결 | 완료 |
| shift — entity, 예외, Repository, DTO, Service, Controller | 완료 |
| line — entity, Repository, DTO, Service, Controller, seed 초기화 | 완료 |
| notification — entity, SSE 구독, 필터/페이징 목록 조회, 미확인 개수, 단건/전체 읽음 처리, 전체 사용자(ADMIN+WORKER) 발송, SSE 커넥션 풀 고갈 버그 수정 | 완료 |
| inspection — entity, 상태 머신, CRUD, 분석 시작, AI 서버 HTTP 호출, 콜백 수신, 조치 완료(`save` 명시적 호출), 최근 불량 조회, 이미지 업로드 | 완료 |
| global/client — AiAnalysisClient (검사 분석, RestTemplate), OpenAiClient (공정 분석, gpt-4o-mini 직접 호출) | 완료 |
| dashboard — GET /api/dashboard, 집계 쿼리 (요약/추이/라인별), projection, latestAnalysis(RAG 분석 요약) 추가 | 완료 |
| analysis (레거시) — entity, 상태 머신, CRUD, OpenAI 직접 연동, JSON 직렬화, DB 커넥션 분리 | 완료 |
| analysis/process (RAG) — ProcessAnalysisController/Service/ServiceImpl, 5종 RAG 청크, SimpleRagChunkRetriever, ProcessAnalysis 엔티티 확장, 신규 DTO 7종 + Projection 5종 | 완료 |
| global/rag — RagQuery, RagChunk, RagChunkRetriever(인터페이스), SimpleRagChunkRetriever(구현체) | 완료 |
| internal (frame 수집, 검사 AI 콜백, 공정 분석 콜백) | 완료 |

---

## 미결 사항

| 항목 | 내용 |
|---|---|
| PENDING 유저 API 접근 제한 | 승인 전 `/api/users/**`, `/api/shifts/**` 등 접근 차단 여부 결정 필요 |
| 내부 시스템 인증 방식 | API Key 정적 관리 vs 서비스 토큰 발급 방식 결정 필요 |
| 검사 상태 머신 정의 | `PENDING → PROCESSING → DONE/FAILED` 전환 규칙 명확화 |
| AI 분석 서버 연동 방식 | `TransactionTemplate`으로 DB 트랜잭션을 AI HTTP 호출 전 커밋 분리 완료 — 커넥션 풀 고갈 해결. 추가 트래픽 증가 시 비동기 메시지 큐 전환 검토 가능 |
| dashboard actionSummary | ~~조치 도메인 미구현~~ — `Inspection.actionStatus` 기반 실제 집계로 교체 완료 (2026-05-27) |
| dashboard 데이터 정합성 | 현재 실시간 집계 쿼리 — 데이터 증가 시 Redis 캐싱 여부 검토 필요 |
| inspection과 frame의 관계 | 프레임을 inspection 하위로 볼지, 독립 엔티티로 볼지 |
| Redis 도입 시기 | refresh token 저장 용도 |
| Swagger 설정 추가 시점 | |
| `user` 도메인 직접 접근 API 정리 | `PUT /api/users/{id}` 가 admin 기능과 중복될 수 있으므로 역할 명확화 필요 |
| PENDING 유저 API 접근 제한 | 승인 전 `/api/users/**`, `/api/shifts/**` 등 접근 차단 여부 결정 필요 |