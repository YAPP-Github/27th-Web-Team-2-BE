# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 소개
Kotlin을 사용하는 Spring Boot 3.2 멀티모듈 프로젝트입니다. (JDK 17, Kotlin 1.9)

## 모듈 구조 및 의존성 방향
```
app:api → core → port ← adapter:rdb
              ↓
           domain
```

| 모듈 | 역할 |
|------|------|
| `app:api` | Controller, Request/Response, Swagger |
| `core` | Service, 예외 정의 |
| `domain` | 순수 도메인 객체 |
| `port` | Repository 인터페이스 |
| `adapter:rdb` | JPA Entity, QueryDSL |
| `support:*` | 범용 유틸리티 |

## 테스트 프레임워크
- Kotest (kotest-runner-junit5, kotest-assertions-core)
- MockK

## Git 컨벤션
**커밋 메시지**: `type: subject`
- feat, fix, refactor, docs, test, chore, rename, style

**브랜치 전략**:
- `main`: 운영 서버
- `develop`: 운영 배포 스탠바이 (default)
- `feat/*`: 기능 개발 (Github Issue 번호)
- `base/*`: 큰 기능 작업 시 완료된 PR을 모아두는 브랜치
