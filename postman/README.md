# Postman 사용 가이드

아래 두 파일을 Postman에 import 하면 됩니다.

- `postman/capstone-api.postman_collection.json`
- `postman/capstone-local.postman_environment.json`

기본 `baseUrl`은 `http://localhost:8080` 입니다.

API가 추가/변경되어 collection 파일을 갱신한 경우에는 Postman에서
`postman/capstone-api.postman_collection.json`을 다시 import 한 뒤 기존
`Capstone API` collection을 replace/update 하면 됩니다.

## 추천 확인 순서

1. Spring 서버 실행
2. `Capstone Local` environment 선택
3. `Auth > Login` 실행
4. `Auth > Get Me` 실행
5. 이후 필요한 도메인 API 실행

`Auth > Login` 요청이 성공하면 `accessToken`이 컬렉션 변수에 자동 저장되어 이후 요청의 `Authorization: Bearer {{accessToken}}`에 재사용됩니다.

`Internal` 폴더의 요청은 JWT 대신 `X-Service-Key: {{serviceKey}}` 헤더를 사용합니다.
이미지 업로드 요청은 `imageFilePath` 변수에 로컬 이미지 경로를 넣거나 Postman form-data에서 파일을 직접 선택하면 됩니다.

## 현재 코드 기준 주의사항

- 공개 회원가입 API는 없습니다.
- `/api/admin/**`는 모두 `ADMIN` 권한이 필요합니다.
- 즉, 최초 관리자 계정이 없으면 `POST /api/admin/accounts`도 바로 호출할 수 없습니다.
- 앱 시작 시 라인 데이터 `A`, `B`, `C`는 자동 생성됩니다.
- 기본 포트는 별도 설정이 없어 `8080`입니다.

## 최초 관리자 계정이 필요한 이유

현재 보안 설정은 아래 정책입니다.

- `POST /api/auth/login`만 비인증 허용
- `/api/admin/**`는 `ADMIN` 권한 필수
- 나머지 API는 인증 필수

그래서 Postman으로 전체 API를 보려면 먼저 로그인 가능한 관리자 계정이 DB에 하나 있어야 합니다.
