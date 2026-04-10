# Backend

산업 현장의 불량 검사 이력 관리 및 알림 기능을 제공하는 플랫폼의 백엔드 서버입니다.  
사용자 인증, 검사 이력 관리, 알림 기능, 권한별 데이터 조회 API 등을 제공합니다.

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL / PostgreSQL
- Redis
- Docker
- GitHub Actions / Jenkins
- Swagger(OpenAPI)

## Features

- 회원가입 / 로그인 / JWT 인증
- 권한별 사용자 관리
- 검사 이력 등록 / 조회 / 수정
- 실시간 알림 기능(SSE)
- 대시보드 데이터 조회
- Swagger 기반 API 문서 제공

## Project Structure

- `config` : Spring Security, Swagger, Web 설정
- `auth` : 로그인, JWT, 인증/인가 처리
- `user` : 사용자 관련 기능
- `inspection` : 검사 이력 관련 기능
- `notification` : SSE 알림 기능
- `global` : 공통 응답, 예외 처리, 유틸