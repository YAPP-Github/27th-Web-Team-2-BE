# Time Voting Feature Design

**Date:** 2026-04-17  
**Status:** Approved

## Overview

기존 날짜 전용 투표에 시간 슬롯 투표 기능을 추가한다. 시간 투표는 optional이며, 미팅 생성 시 시간 범위를 지정하면 활성화된다. 시간은 30분 단위 슬롯으로 투표한다.

---

## 1. DB 스키마 변경

### 기존 테이블 수정

**`meetings`** — 시간 범위 및 확정 시간 컬럼 추가
```sql
ALTER TABLE meetings
  ADD COLUMN time_range_start     TIME NULL,
  ADD COLUMN time_range_end       TIME NULL,
  ADD COLUMN finalized_start_time TIME NULL,
  ADD COLUMN finalized_end_time   TIME NULL;
```
- `time_range_start`, `time_range_end` 둘 다 NULL → 날짜 전용 모드
- 둘 다 NOT NULL → 시간 투표 모드
- 모든 날짜에 동일한 시간 범위이므로 `meetings` 테이블에서 관리 (`meeting_dates` 아님)

### 신규 테이블

**`participant_vote_time_slots`**
```sql
CREATE TABLE participant_vote_time_slots (
  participant_id BIGINT      NOT NULL REFERENCES participants(participant_id),
  vote_date      DATE        NOT NULL,
  time_slots     VARCHAR(48) NOT NULL,
  PRIMARY KEY (participant_id, vote_date)
);
```

- `time_slots`: 48자리 `'0'`/`'1'` 문자열
  - index 0 = 00:00, index 1 = 00:30, ..., index 47 = 23:30
  - 슬롯 인덱스 계산: `hour * 2 + minute / 30`
- 00:00~23:30 전체 범위를 저장하여 미팅 시간 범위와 무관하게 확장 가능
- VARCHAR 길이 확장으로 슬롯 추가 대응 가능

---

## 2. 도메인 모델 변경

### Meeting

```kotlin
data class Meeting(
    // 기존 필드 유지...
    val timeRange: MeetingTimeRange? = null,       // null이면 날짜 전용 모드
    val finalizedStartTime: LocalTime? = null,
    val finalizedEndTime: LocalTime? = null,
)

data class MeetingTimeRange(
    val startTime: LocalTime,
    val endTime: LocalTime,
) {
    fun generateSlots(): List<LocalTime> // startTime~endTime 사이 30분 단위 슬롯 목록
    fun slotCount(): Int                 // 슬롯 수
    fun startIndex(): Int                // 전체 48슬롯에서 시작 인덱스 (startTime.hour*2 + startTime.minute/30)
}
```

### Participant

```kotlin
data class Participant(
    // 기존 필드 유지...
    val voteTimeSlots: Map<LocalDate, String> = emptyMap(),
    // key = 날짜, value = 48자리 비트마스크 문자열
)
```

---

## 3. API 변경

### 미팅 생성 `POST /api/v1/meeting`

```kotlin
data class CreateMeetingRequest(
    val title: String,
    val hostName: String,
    val maxParticipantCount: Int?,
    val dates: List<LocalDate>,
    val timeRange: TimeRangeRequest? = null,  // null이면 날짜 전용
)

data class TimeRangeRequest(
    val startTime: LocalTime,  // "09:00"
    val endTime: LocalTime,    // "18:00"
)
```

### 투표 `POST /api/v1/meeting/vote` / `PUT /api/v1/meeting/vote`

```kotlin
data class VoteRequest(
    val meetingId: MeetingId,
    val name: String,
    val voteDates: List<LocalDate>,              // 날짜 전용 모드 (시간 모드에서는 무시)
    val voteTimeSlots: List<List<Boolean>>? = null,
    // 시간 모드: [날짜 수][범위 내 슬롯 수]
    // voteTimeSlots[i][j] = dates[i]의 범위 내 j번째 슬롯 투표 여부
)
```

**시간 모드 처리:**
1. `voteTimeSlots[i]` 길이 = 미팅 시간 범위 슬롯 수 검증
2. 범위 시작 인덱스(offset) 계산: `startTime.hour * 2 + startTime.minute / 30`
3. `List<Boolean>` → 48자리 비트마스크 변환 (offset 위치에 삽입, 나머지 `'0'`)
4. 날짜 투표(`voteDates`) = `voteTimeSlots[i]`에 `true`가 1개 이상인 날짜들로 자동 도출

### 미팅 정보 응답 `GET /api/v1/meeting`

```kotlin
data class MeetingInfoResponse(
    // 기존 필드 유지...
    val timeRange: TimeRangeResponse? = null,
    val participants: List<ParticipantResponse>,
)

data class TimeRangeResponse(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val slotCount: Int,  // 범위 내 슬롯 수 (프론트 편의)
)

data class ParticipantResponse(
    val id: ParticipantId,
    val name: String,
    val voteDates: List<LocalDate>,
    val voteTimeSlots: List<List<Boolean>>? = null,
    // 시간 모드: [날짜 수][범위 내 슬롯 수]
    // 48자리 마스크에서 범위 슬롯만 슬라이싱하여 반환
    val hasVoted: Boolean,
)
```

### 확정 `POST /api/v1/host/meeting/finalize`

```kotlin
data class FinalizeMeetingRequest(
    val meetingId: MeetingId,
    val finalizedDate: LocalDate,
    val finalizedStartTime: LocalTime? = null,  // 시간 모드만
    val finalizedEndTime: LocalTime? = null,
)
```

---

## 4. 레이어별 변환 책임

| 레이어 | 책임 |
|--------|------|
| Controller | `List<List<Boolean>>` ↔ Request/Response DTO |
| Service | `List<List<Boolean>>` ↔ `Map<LocalDate, String>` 변환, 슬롯 검증 |
| Adapter(RDB) | `Map<LocalDate, String>` ↔ `ParticipantVoteTimeSlotJpaEntity` |

---

## 5. 비즈니스 규칙

1. **모드 결정**: 미팅 생성 시 `timeRange` 존재 여부로 결정, 이후 변경 불가
2. **투표 검증**: 시간 모드에서 `voteTimeSlots[i]` 길이 = 미팅 범위 슬롯 수 일치 필수
3. **날짜 도출**: 시간 모드에서 `voteDates`는 `voteTimeSlots`에서 자동 계산 (`true` 1개 이상인 날짜)
4. **확정 검증**: 시간 모드에서 확정 시 `finalizedStartTime`, `finalizedEndTime` 필수
5. **기존 호환성**: 날짜 전용 모드는 기존 로직과 동일하게 동작

---

## 6. 신규 파일 목록

- `adapter/rdb/src/main/resources/sql.ddl/V12_ALTER_MEETINGS_ADD_TIME_RANGE_AND_FINALIZED_TIME.sql`
- `adapter/rdb/src/main/resources/sql.ddl/V13_CREATE_PARTICIPANT_VOTE_TIME_SLOTS.sql`
- `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantVoteTimeSlotJpaEntity.kt`
- `domain/src/main/kotlin/com/nomoney/meeting/domain/MeetingTimeRange.kt`
