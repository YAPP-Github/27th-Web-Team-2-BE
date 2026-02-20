# 기능 명세 작성용 Context (현재 코드 분석 + PRD 반영)

작성일: 2026-02-15
대상 브랜치: feat/62

## 1) 프로젝트/아키텍처 컨텍스트

멀티모듈 Kotlin/Spring Boot 구조이며 의존 방향은 아래와 같다.

`app:api -> core -> port <- adapter:rdb`
`domain`은 core/port의 도메인 모델로 사용된다.

| 모듈 | 현재 역할 |
|---|---|
| `app:api` | Controller, HTTP Request/Response, Swagger |
| `core` | 비즈니스 로직(Service), 예외 처리 |
| `domain` | 순수 도메인 모델 (`Meeting`, `Participant`) |
| `port` | `MeetingRepository` 인터페이스 |
| `adapter:rdb` | JPA Entity/Repository/Adapter 구현 |

## 2) 현재 구현 상태 (As-Is)

### 2-1. API 기능 현황

현재 공개된 Meeting API는 아래 6개다.

- `GET /api/v1/meeting` 모임 단건 조회
- `GET /api/v1/meeting/list` 모임 목록 조회
- `POST /api/v1/meeting` 모임 생성
- `GET /api/v1/meeting/participant/exist` 참여자 이름 중복 확인
- `POST /api/v1/meeting/vote` 투표 생성
- `PUT /api/v1/meeting/vote` 투표 수정

### 2-2. 도메인/서비스 현황

- `Meeting` 도메인 필드
  - `id`, `title`, `hostName`, `dates`, `maxParticipantCount`, `participants`
- `Meeting`의 상태값(`status`) / 확정일(`finalizedDate`) / 마감시간(`deadline`)은 아직 없음
- `MeetingService.createMeeting()`은 `maxParticipantCount`를 받을 수 있지만,
  - Controller에서 `null`로 고정 전달
  - RDB Adapter에서 `maxParticipantCount`를 항상 `null`로 매핑
- 투표 가능 날짜 검증만 존재 (`voteDates`가 후보 날짜에 포함되는지)
- 인원 제한 검증 로직은 없음

### 2-3. DB 모델 현황

현재 Meeting 관련 테이블:

- `meetings`
- `meeting_dates`
- `participants`
- `participant_vote_dates`

`meetings`에는 `status/finalized_date/deadline/max_participant_count/memo/tag` 컬럼이 없다.

### 2-4. 인증/인가 현황

- `SecurityConfig`는 `anyRequest().permitAll()`
- 주최자 전용 작업(마감/확정/수정) 권한 모델이 아직 없다

### 2-5. 테스트 현황

Meeting/Vote 기능 대상 테스트는 현재 없음.

- core 테스트: auth 영역만 존재
- app/api 테스트: auth 영역만 존재
- adapter/rdb 테스트: auth 영역만 존재

## 3) 초안 대비 코드 검증 결과

초안의 핵심 진술은 대부분 현재 코드와 일치한다.

일치 항목:

- 모임 생성/조회/목록/투표 생성/투표 수정 기능 존재
- 참여자 이름 중복 확인 기능 존재
- `maxParticipantCount` 요청 필드 주석 처리됨
- 대시보드 전용 API/모델 부재
- 상태 전환 개념 부재

추가로 확인된 리스크:

- Meeting ID 생성 길이(12자)와 `MeetingJpaEntity`의 `@Column(length = 10)` 설정 불일치
  - DDL은 `VARCHAR(16)`이므로 JPA 설정과 스키마 설정 간 불일치 상태

## 4) 이번 기능 개발 대상 (To-Be)

### A. 주최자 대시보드 전용 조회

필요 구현:

- 주최자 기준 모임 목록 조회 API
- 상태별 분리(`투표중`, `확정`) + 요약 카운트
- 카드 데이터
  - D-day
  - 유력 날짜(최다 득표 + 동률 정책)
  - 투표 진행률(완료 인원/전체 또는 목표 인원)

예상 변경 모듈:

- `domain`: 상태/확정일 등 모델 추가
- `core`: 대시보드 조회 유즈케이스
- `port`: 조회용 Repository 메서드 확장
- `adapter:rdb`: 조회 쿼리/매핑 구현
- `app:api`: DTO + Endpoint

### B. 모임 마감/확정 플로우

필요 구현:

- 상태 전환 규칙 정의 및 적용
- 확정일 저장
- 공동 1위 시 날짜 선택 확정 API

핵심 예외 정책 필요:

- 이미 마감/확정된 모임 재요청 처리(멱등/에러)
- 후보 날짜 외 입력 에러
- 공동 1위인데 날짜 미지정 에러

### C. 모임 수정 플로우

필요 구현:

- 모임명 수정
- 최대 인원 수정
- 후보 날짜 수정
- 참여자 삭제

핵심 정책 필요:

- 마감 이후 수정 허용 범위
- 투표 완료 참여자 삭제 허용 여부
- 후보 날짜 삭제 시 기존 투표 데이터 처리 방식

### D. 생성 시 인원 제한 입력 복구

필요 구현:

- `CreateMeetingRequest.maxParticipantCount` 복구
- 도메인/서비스/RDB 컬럼 및 매핑 반영
- 생성/수정/투표 전 단계 일관 검증

검증 기준 예시:

- 1 이상
- 현재 참여자 수보다 작은 값으로 축소 불가
- 최대 인원 초과 시 신규 투표 차단

## 5) 명세 확정이 필요한 정책 항목

1. 상태 머신
- `VOTING -> CLOSED -> CONFIRMED` 3단계 여부
- `CLOSED` 생략 가능 여부

2. 공동 1위 처리
- 자동 확정 vs 수동 선택
- 수동 선택 시 API 스펙(필수 파라미터, 타이밍)

3. D-day 계산 기준
- 유력 날짜 기준 여부
- 과거 표기 정책(`D+N`, `마감지남` 등)

4. 수정 권한/시점
- 투표중만 수정 허용 여부
- 마감 후 부분 수정 허용 여부

5. 인원 제한 정책
- 최대 인원 도달 시 에러코드/메시지
- 주최자/기존 참여자 예외 여부

## 6) 수용 기준(AC) 초안

- 대시보드 API 1회 호출로 카드 렌더링 필수 데이터가 모두 반환된다.
- 마감 API 재호출 시 정책대로 일관 동작한다(멱등 또는 명시적 에러코드).
- 공동 1위 모임은 최종 날짜 선택 전 `CONFIRMED` 상태로 전환되지 않는다.
- 모임 수정 수행 후 투표 데이터 정합성이 유지된다.
- `maxParticipantCount` 규칙이 생성/수정/투표 단계에서 동일하게 적용된다.
- 테스트에서 상태 전환/공동 1위/인원 제한/수정 예외 케이스가 커버된다.

## 7) 모듈별 예상 변경 포인트 (dependency 순서)

1. `domain`
- `Meeting`에 status/finalizedDate/deadline/maxParticipantCount(실사용) 등 반영
- 필요 시 상태 enum/value object 추가

2. `port`
- 상태 전환/대시보드 조회/수정 지원 메서드 추가

3. `adapter:rdb`
- `meetings` 컬럼 추가 마이그레이션
- Entity/Adapter 매핑 확장
- 대시보드용 조회 쿼리 추가

4. `core`
- Dashboard 조회 서비스
- Close/Finalize 서비스
- Update meeting 서비스
- Capacity validation 정책 구현

5. `app:api`
- Dashboard 조회 endpoint + DTO
- 마감/확정 endpoint + DTO
- 모임 수정 endpoint + DTO
- CreateMeetingRequest `maxParticipantCount` 복구

## 8) 분석에 사용한 주요 코드 위치

- `app/api/src/main/kotlin/com/nomoney/api/meetvote/MeetingVoteController.kt`
- `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/CreateMeetingHttp.kt`
- `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingInfoResponse.kt`
- `core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt`
- `domain/src/main/kotlin/com/nomoney/meeting/domain/Meeting.kt`
- `port/src/main/kotlin/com/nomoney/meeting/port/MeetingRepository.kt`
- `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt`
- `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/MeetingJpaEntity.kt`
- `adapter/rdb/src/main/resources/sql.ddl/V2_CREATE_MEETING_PARTICIPANTS.sql`
- `app/api/src/main/kotlin/com/nomoney/api/security/SecurityConfig.kt`

## 9) 기능 개발 순서 (1개 기능씩 순차 진행)

순차 개발 원칙: 한 기능 완료(구현 + 테스트 + API 계약 확인) 후 다음 기능으로 이동한다.

1. B. 모임 마감하기/확정하기 플로우
- 선행 이유: 상태 머신(`VOTING/CLOSED/CONFIRMED`)과 `finalizedDate`가 이후 기능의 기준 데이터가 된다.

2. A. 주최자 대시보드 전용 모델/조회 API
- 선행 이유: B에서 확정된 상태/확정일을 조회 중심으로 활용하므로 구현 리스크가 낮다.

3. D. 모임 생성 시 “투표할 인원 입력” 지원
- 선행 이유: `maxParticipantCount`를 생성 단계부터 일관 적용하면 이후 수정 기능에서 동일 검증 규칙을 재사용할 수 있다.

4. C. 모임 수정 플로우
- 후순위 이유: 정책 의존성이 가장 크다(마감 후 수정 허용 범위, 참여자 삭제, 후보 날짜 변경 시 투표 정합성).
