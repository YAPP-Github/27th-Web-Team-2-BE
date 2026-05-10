# Time Voting Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 날짜 전용 투표 모임에 시간 슬롯(30분 단위) 선택 기능을 optional로 추가한다.

**Architecture:** 기존 날짜 전용 모드는 손대지 않고, 미팅 생성 시 `timeRange` 설정 여부로 모드를 결정한다. DB에 시간 범위 컬럼을 meetings 테이블에 추가하고, 참가자별 시간 슬롯을 `participant_vote_time_slots` 테이블(48자리 VARCHAR 비트마스크)로 저장한다. API는 `List<List<Boolean>>`으로 통신하며 Service에서 비트마스크로 변환한다.

**Tech Stack:** Kotlin, Spring Boot, JPA/Hibernate, PostgreSQL, Flyway, Kotest, Mockk

---

## File Map

| 구분 | 파일 | 변경 내용 |
|------|------|-----------|
| Create | `adapter/rdb/src/main/resources/sql.ddl/V12_ALTER_MEETINGS_ADD_TIME_RANGE_AND_FINALIZED_TIME.sql` | meetings 시간 컬럼 추가 |
| Create | `adapter/rdb/src/main/resources/sql.ddl/V13_CREATE_PARTICIPANT_VOTE_TIME_SLOTS.sql` | 신규 테이블 |
| Create | `domain/src/main/kotlin/com/nomoney/meeting/domain/MeetingTimeRange.kt` | 새 도메인 모델 |
| Modify | `domain/src/main/kotlin/com/nomoney/meeting/domain/Meeting.kt` | timeRange, finalizedStartTime, finalizedEndTime 추가 |
| Modify | `domain/src/main/kotlin/com/nomoney/meeting/domain/Participant.kt` | voteTimeSlots 추가 |
| Create | `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantVoteTimeSlotJpaEntity.kt` | 신규 JPA 엔티티 |
| Modify | `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/MeetingJpaEntity.kt` | 4개 시간 컬럼 추가 |
| Modify | `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantJpaEntity.kt` | voteTimeSlots 연관관계 추가 |
| Modify | `adapter/rdb/src/main/kotlin/com/nomoney/meeting/repository/MeetingJpaRepository.kt` | 시간슬롯 조회 메서드 추가 |
| Modify | `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt` | 시간 매핑 로직 추가 |
| Modify | `core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt` | 서비스 메서드 시간 파라미터 추가 |
| Modify | `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/CreateMeetingHttp.kt` | timeRange 필드 추가 |
| Modify | `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/VoteHttp.kt` | voteTimeSlots 필드 추가 |
| Modify | `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingInfoResponse.kt` | timeRange, voteTimeSlots 응답 추가 |
| Modify | `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingStatusHttp.kt` | 확정 시간 필드 추가 |
| Modify | `app/api/src/main/kotlin/com/nomoney/api/meetvote/MeetingVoteController.kt` | 서비스 호출 업데이트 |
| Modify | `core/src/test/kotlin/com/nomoney/meeting/service/MeetingServiceTest.kt` | 시간 투표 테스트 추가 |

---

## Task 1: DB Migrations

**Files:**
- Create: `adapter/rdb/src/main/resources/sql.ddl/V12_ALTER_MEETINGS_ADD_TIME_RANGE_AND_FINALIZED_TIME.sql`
- Create: `adapter/rdb/src/main/resources/sql.ddl/V13_CREATE_PARTICIPANT_VOTE_TIME_SLOTS.sql`

- [ ] **Step 1: V12 마이그레이션 파일 작성**

```sql
-- adapter/rdb/src/main/resources/sql.ddl/V12_ALTER_MEETINGS_ADD_TIME_RANGE_AND_FINALIZED_TIME.sql
ALTER TABLE meetings
    ADD COLUMN time_range_start     TIME NULL,
    ADD COLUMN time_range_end       TIME NULL,
    ADD COLUMN finalized_start_time TIME NULL,
    ADD COLUMN finalized_end_time   TIME NULL;
```

- [ ] **Step 2: V13 마이그레이션 파일 작성**

```sql
-- adapter/rdb/src/main/resources/sql.ddl/V13_CREATE_PARTICIPANT_VOTE_TIME_SLOTS.sql
CREATE TABLE IF NOT EXISTS participant_vote_time_slots (
    participant_id BIGINT      NOT NULL,
    vote_date      DATE        NOT NULL,
    time_slots     VARCHAR(48) NOT NULL,
    FOREIGN KEY (participant_id) REFERENCES participants(participant_id) ON DELETE CASCADE,
    PRIMARY KEY (participant_id, vote_date)
);
```

- [ ] **Step 3: 커밋**

```bash
git add adapter/rdb/src/main/resources/sql.ddl/V12_ALTER_MEETINGS_ADD_TIME_RANGE_AND_FINALIZED_TIME.sql \
        adapter/rdb/src/main/resources/sql.ddl/V13_CREATE_PARTICIPANT_VOTE_TIME_SLOTS.sql
git commit -m "feat: DB migration - 시간 투표 스키마 추가 (V12, V13)"
```

---

## Task 2: Domain — MeetingTimeRange + Meeting + Participant

**Files:**
- Create: `domain/src/main/kotlin/com/nomoney/meeting/domain/MeetingTimeRange.kt`
- Modify: `domain/src/main/kotlin/com/nomoney/meeting/domain/Meeting.kt`
- Modify: `domain/src/main/kotlin/com/nomoney/meeting/domain/Participant.kt`

- [ ] **Step 1: MeetingTimeRange 실패 테스트 작성**

`domain/src/test/kotlin/com/nomoney/meeting/domain/MeetingTimeRangeTest.kt` 파일을 생성한다 (없으면 디렉토리 생성 포함).

```kotlin
package com.nomoney.meeting.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalTime

class MeetingTimeRangeTest : DescribeSpec({
    describe("MeetingTimeRange") {
        describe("slotCount") {
            it("09:00~18:00은 18개 슬롯이다") {
                val range = MeetingTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0))
                range.slotCount shouldBe 18
            }
            it("00:00~00:30은 1개 슬롯이다") {
                val range = MeetingTimeRange(LocalTime.of(0, 0), LocalTime.of(0, 30))
                range.slotCount shouldBe 1
            }
        }
        describe("startIndex") {
            it("09:00의 startIndex는 18이다") {
                val range = MeetingTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0))
                range.startIndex shouldBe 18
            }
            it("00:00의 startIndex는 0이다") {
                val range = MeetingTimeRange(LocalTime.of(0, 0), LocalTime.of(1, 0))
                range.startIndex shouldBe 0
            }
            it("09:30의 startIndex는 19이다") {
                val range = MeetingTimeRange(LocalTime.of(9, 30), LocalTime.of(10, 0))
                range.startIndex shouldBe 19
            }
        }
    }
})
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
cd /home/hoyeon/Project/nomoney/27th-Web-Team-2-BE
./gradlew :domain:test --tests "com.nomoney.meeting.domain.MeetingTimeRangeTest" 2>&1 | tail -20
```

Expected: 컴파일 에러 또는 ClassNotFoundException (MeetingTimeRange 없음)

- [ ] **Step 3: MeetingTimeRange 구현**

```kotlin
// domain/src/main/kotlin/com/nomoney/meeting/domain/MeetingTimeRange.kt
package com.nomoney.meeting.domain

import java.time.LocalTime

data class MeetingTimeRange(
    val startTime: LocalTime,
    val endTime: LocalTime,
) {
    val slotCount: Int
        get() = ((endTime.hour * 60 + endTime.minute) - (startTime.hour * 60 + startTime.minute)) / 30

    val startIndex: Int
        get() = startTime.hour * 2 + startTime.minute / 30
}
```

- [ ] **Step 4: Meeting 도메인 업데이트**

```kotlin
// domain/src/main/kotlin/com/nomoney/meeting/domain/Meeting.kt
package com.nomoney.meeting.domain

import com.nomoney.auth.domain.UserId
import java.time.LocalDate
import java.time.LocalTime

@JvmInline
value class MeetingId(val value: String)

enum class MeetingStatus {
    VOTING,
    CLOSED,
    CONFIRMED,
}

data class Meeting(
    val id: MeetingId,
    val title: String,
    val hostName: String?,
    val hostUserId: UserId? = null,
    val dates: Set<LocalDate>,
    val maxParticipantCount: Int?,
    val participants: List<Participant>,
    val memo: String? = null,
    val status: MeetingStatus = MeetingStatus.VOTING,
    val finalizedDate: LocalDate? = null,
    val timeRange: MeetingTimeRange? = null,
    val finalizedStartTime: LocalTime? = null,
    val finalizedEndTime: LocalTime? = null,
) {
    fun isVoteDatesAllowed(voteDates: Set<LocalDate>): Boolean {
        return (voteDates - dates).isEmpty()
    }
}
```

- [ ] **Step 5: Participant 도메인 업데이트**

```kotlin
// domain/src/main/kotlin/com/nomoney/meeting/domain/Participant.kt
package com.nomoney.meeting.domain

import java.time.LocalDate
import java.time.LocalDateTime

@JvmInline
value class ParticipantId(val value: Long)

data class Participant(
    val id: ParticipantId,
    val name: String,
    val voteDates: Set<LocalDate>,
    val hasVoted: Boolean,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val voteTimeSlots: Map<LocalDate, String> = emptyMap(),
)
```

- [ ] **Step 6: 테스트 실행 - 통과 확인**

```bash
./gradlew :domain:test --tests "com.nomoney.meeting.domain.MeetingTimeRangeTest" 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, 5 tests passed

- [ ] **Step 7: 커밋**

```bash
git add domain/src/main/kotlin/com/nomoney/meeting/domain/ \
        domain/src/test/kotlin/com/nomoney/meeting/domain/
git commit -m "feat: MeetingTimeRange 도메인 모델 추가, Meeting/Participant 시간 필드 추가"
```

---

## Task 3: JPA Entities + Repository

**Files:**
- Modify: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/MeetingJpaEntity.kt`
- Create: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantVoteTimeSlotJpaEntity.kt`
- Modify: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantJpaEntity.kt`
- Modify: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/repository/MeetingJpaRepository.kt`

- [ ] **Step 1: MeetingJpaEntity 시간 컬럼 추가**

`adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/MeetingJpaEntity.kt`의 `finalizedDate` 컬럼 선언 아래에 4개 컬럼을 추가하고, `of()` 컴패니언에도 파라미터를 추가한다.

```kotlin
// MeetingJpaEntity.kt 전체 파일 (기존 파일을 이 내용으로 교체)
package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import com.nomoney.meeting.domain.MeetingStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "meetings")
class MeetingJpaEntity : BaseJpaEntity() {
    @Id
    @Column(name = "meet_id", length = 16, nullable = false)
    lateinit var meetId: String

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "host_name", nullable = true)
    var hostName: String? = null

    @Column(name = "host_user_id", nullable = true)
    var hostUserId: Long? = null

    @Column(name = "max_participant_count", nullable = true)
    var maxParticipantCount: Int? = null

    @Column(name = "memo", nullable = true, length = 200)
    var memo: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: MeetingStatus = MeetingStatus.VOTING

    @Column(name = "finalized_date", nullable = true)
    var finalizedDate: LocalDate? = null

    @Column(name = "time_range_start", nullable = true)
    var timeRangeStart: LocalTime? = null

    @Column(name = "time_range_end", nullable = true)
    var timeRangeEnd: LocalTime? = null

    @Column(name = "finalized_start_time", nullable = true)
    var finalizedStartTime: LocalTime? = null

    @Column(name = "finalized_end_time", nullable = true)
    var finalizedEndTime: LocalTime? = null

    @OneToMany(mappedBy = "meeting", cascade = [CascadeType.ALL], orphanRemoval = true)
    var dates: MutableSet<MeetingDateJpaEntity> = mutableSetOf()

    @OneToMany(mappedBy = "meeting", cascade = [CascadeType.ALL], orphanRemoval = true)
    var participants: MutableSet<ParticipantJpaEntity> = mutableSetOf()

    companion object {
        fun of(
            meetId: String,
            title: String,
            hostName: String?,
            hostUserId: Long? = null,
            maxParticipantCount: Int? = null,
            memo: String? = null,
            status: MeetingStatus = MeetingStatus.VOTING,
            finalizedDate: LocalDate? = null,
            timeRangeStart: LocalTime? = null,
            timeRangeEnd: LocalTime? = null,
            finalizedStartTime: LocalTime? = null,
            finalizedEndTime: LocalTime? = null,
            dates: MutableSet<MeetingDateJpaEntity> = mutableSetOf(),
            participants: MutableSet<ParticipantJpaEntity> = mutableSetOf(),
        ): MeetingJpaEntity {
            return MeetingJpaEntity().apply {
                this.meetId = meetId
                this.title = title
                this.hostName = hostName
                this.hostUserId = hostUserId
                this.maxParticipantCount = maxParticipantCount
                this.memo = memo
                this.status = status
                this.finalizedDate = finalizedDate
                this.timeRangeStart = timeRangeStart
                this.timeRangeEnd = timeRangeEnd
                this.finalizedStartTime = finalizedStartTime
                this.finalizedEndTime = finalizedEndTime
                this.dates = dates
                this.participants = participants
            }
        }
    }
}
```

- [ ] **Step 2: ParticipantVoteTimeSlotJpaEntity 신규 생성**

```kotlin
// adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantVoteTimeSlotJpaEntity.kt
package com.nomoney.meeting.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "participant_vote_time_slots")
class ParticipantVoteTimeSlotJpaEntity {
    @EmbeddedId
    lateinit var id: ParticipantVoteTimeSlotId

    @MapsId("participantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    lateinit var participant: ParticipantJpaEntity

    @Column(name = "time_slots", length = 48, nullable = false)
    lateinit var timeSlots: String

    @get:Transient
    val voteDate: LocalDate
        get() = id.voteDate

    companion object {
        fun of(
            participant: ParticipantJpaEntity,
            voteDate: LocalDate,
            timeSlots: String,
        ): ParticipantVoteTimeSlotJpaEntity {
            return ParticipantVoteTimeSlotJpaEntity().apply {
                this.id = ParticipantVoteTimeSlotId(participantId = 0L, voteDate = voteDate)
                this.participant = participant
                this.timeSlots = timeSlots
            }
        }
    }
}

@Embeddable
data class ParticipantVoteTimeSlotId(
    @Column(name = "participant_id")
    var participantId: Long = 0L,

    @Column(name = "vote_date")
    var voteDate: LocalDate = LocalDate.now(),
) : Serializable
```

- [ ] **Step 3: ParticipantJpaEntity voteTimeSlots 연관관계 추가**

`adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ParticipantJpaEntity.kt`의 `voteDates` 필드 아래에 추가:

```kotlin
// ParticipantJpaEntity.kt 전체 파일
package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "participants")
class ParticipantJpaEntity : BaseJpaEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id", nullable = false)
    var participantId: Long = 0L

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id", nullable = false)
    lateinit var meeting: MeetingJpaEntity

    @Column(name = "name", length = 100, nullable = false)
    lateinit var name: String

    @Column(name = "has_voted", nullable = false)
    var hasVoted: Boolean = false

    @OneToMany(mappedBy = "participant", cascade = [CascadeType.ALL], orphanRemoval = true)
    var voteDates: MutableSet<ParticipantVoteDateJpaEntity> = mutableSetOf()

    @OneToMany(mappedBy = "participant", cascade = [CascadeType.ALL], orphanRemoval = true)
    var voteTimeSlots: MutableSet<ParticipantVoteTimeSlotJpaEntity> = mutableSetOf()

    companion object {
        fun of(
            participantId: Long = 0L,
            meeting: MeetingJpaEntity,
            name: String,
            hasVoted: Boolean,
            voteDates: MutableSet<ParticipantVoteDateJpaEntity> = mutableSetOf(),
        ): ParticipantJpaEntity {
            return ParticipantJpaEntity().apply {
                this.participantId = participantId
                this.meeting = meeting
                this.name = name
                this.hasVoted = hasVoted
                this.voteDates = voteDates
            }
        }
    }
}
```

- [ ] **Step 4: MeetingJpaRepository 쿼리 추가**

`adapter/rdb/src/main/kotlin/com/nomoney/meeting/repository/MeetingJpaRepository.kt` 수정:

```kotlin
package com.nomoney.meeting.repository

import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.entity.MeetingDateJpaEntity
import com.nomoney.meeting.entity.MeetingJpaEntity
import com.nomoney.meeting.entity.ParticipantJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteDateJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteTimeSlotJpaEntity
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MeetingJpaRepository : JpaRepository<MeetingJpaEntity, String> {
    @Query(
        """
        SELECT DISTINCT m FROM MeetingJpaEntity m
        LEFT JOIN FETCH m.dates
        LEFT JOIN FETCH m.participants p
        LEFT JOIN FETCH p.voteDates
        LEFT JOIN FETCH p.voteTimeSlots
        WHERE m.meetId = :meetId
        """,
    )
    fun findByMeetIdWithParticipants(@Param("meetId") meetId: String): MeetingJpaEntity?

    fun findAllByHostUserId(hostUserId: Long): List<MeetingJpaEntity>

    @Query("SELECT d FROM MeetingDateJpaEntity d WHERE d.meeting.meetId IN :meetIds")
    fun findAllMeetingDatesByMeetIds(@Param("meetIds") meetIds: Collection<String>): List<MeetingDateJpaEntity>

    @Query("SELECT p FROM ParticipantJpaEntity p WHERE p.meeting.meetId IN :meetIds")
    fun findAllParticipantsByMeetIds(@Param("meetIds") meetIds: Collection<String>): List<ParticipantJpaEntity>

    @Query("SELECT v FROM ParticipantVoteDateJpaEntity v WHERE v.participant.participantId IN :participantIds")
    fun findAllVoteDatesByParticipantIds(@Param("participantIds") participantIds: Collection<Long>): List<ParticipantVoteDateJpaEntity>

    @Query("SELECT t FROM ParticipantVoteTimeSlotJpaEntity t WHERE t.participant.participantId IN :participantIds")
    fun findAllVoteTimeSlotsByParticipantIds(@Param("participantIds") participantIds: Collection<Long>): List<ParticipantVoteTimeSlotJpaEntity>

    @Query(
        """
        SELECT
            m.meetId as meetId,
            m.title as title,
            m.hostName as hostName,
            m.status as status,
            m.finalizedDate as finalizedDate
        FROM MeetingJpaEntity m
        """,
    )
    fun findAllMeetingSummaries(): List<MeetingSummaryProjection>

    fun existsByHostUserIdAndStatusAndMeetIdNotAndFinalizedDate(
        hostUserId: Long,
        status: MeetingStatus,
        meetId: String,
        finalizedDate: LocalDate,
    ): Boolean

    @Modifying
    @Query("UPDATE MeetingJpaEntity m SET m.hostUserId = :toUserId WHERE m.hostUserId = :fromUserId")
    fun updateHostUserId(
        @Param("fromUserId") fromUserId: Long,
        @Param("toUserId") toUserId: Long,
    )
}
```

- [ ] **Step 5: 컴파일 확인**

```bash
./gradlew :adapter:rdb:compileKotlin 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/ \
        adapter/rdb/src/main/kotlin/com/nomoney/meeting/repository/
git commit -m "feat: JPA 엔티티 및 리포지토리 시간 슬롯 지원 추가"
```

---

## Task 4: MeetingAdapter — 시간 슬롯 매핑

**Files:**
- Modify: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt`

- [ ] **Step 1: MeetingAdapter 전체 업데이트**

아래 내용으로 `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt`를 교체한다:

```kotlin
package com.nomoney.meeting.adapter

import com.nomoney.auth.domain.UserId
import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.domain.MeetingSummary
import com.nomoney.meeting.domain.MeetingTimeRange
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import com.nomoney.meeting.entity.MeetingDateJpaEntity
import com.nomoney.meeting.entity.MeetingJpaEntity
import com.nomoney.meeting.entity.ParticipantJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteDateJpaEntity
import com.nomoney.meeting.entity.ParticipantVoteTimeSlotJpaEntity
import com.nomoney.meeting.port.MeetingRepository
import com.nomoney.meeting.repository.MeetingJpaRepository
import com.nomoney.meeting.repository.MeetingSummaryProjection
import java.time.LocalDate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MeetingAdapter(
    private val meetingJpaRepository: MeetingJpaRepository,
) : MeetingRepository {

    @Transactional(readOnly = true)
    override fun findByMeetingId(meetingId: MeetingId): Meeting? {
        val meeting = meetingJpaRepository.findById(meetingId.value).orElse(null)
            ?: return null
        return mapMeetingsToDomain(listOf(meeting)).firstOrNull()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Meeting> {
        val meetings = meetingJpaRepository.findAll()
        return mapMeetingsToDomain(meetings)
    }

    @Transactional(readOnly = true)
    override fun findAllByHostUserId(hostUserId: UserId): List<Meeting> {
        val meetings = meetingJpaRepository.findAllByHostUserId(hostUserId.value)
        return mapMeetingsToDomain(meetings)
    }

    @Transactional(readOnly = true)
    override fun findAllMeetingSummaries(): List<MeetingSummary> {
        return meetingJpaRepository.findAllMeetingSummaries()
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun existsConfirmedMeetingByHostUserIdAndFinalizedDate(
        hostUserId: UserId,
        meetingIdToExclude: MeetingId,
        finalizedDate: LocalDate,
    ): Boolean {
        return meetingJpaRepository.existsByHostUserIdAndStatusAndMeetIdNotAndFinalizedDate(
            hostUserId = hostUserId.value,
            status = MeetingStatus.CONFIRMED,
            meetId = meetingIdToExclude.value,
            finalizedDate = finalizedDate,
        )
    }

    @Transactional
    override fun reassignHostUserId(fromUserId: UserId, toUserId: UserId) {
        meetingJpaRepository.updateHostUserId(fromUserId.value, toUserId.value)
    }

    @Transactional
    override fun save(meeting: Meeting): Meeting {
        val existing = meetingJpaRepository.findByMeetIdWithParticipants(meeting.id.value)

        val entity = if (existing != null) {
            existing.apply { this.updateFrom(meeting) }
        } else {
            meeting.toEntity()
        }

        val savedEntity = meetingJpaRepository.save(entity)
        return mapMeetingsToDomain(listOf(savedEntity)).first()
    }

    private fun MeetingJpaEntity.toDomain(
        dates: Set<LocalDate>,
        participants: List<Participant>,
    ): Meeting {
        val timeRange = if (timeRangeStart != null && timeRangeEnd != null) {
            MeetingTimeRange(startTime = timeRangeStart!!, endTime = timeRangeEnd!!)
        } else {
            null
        }
        return Meeting(
            id = MeetingId(this.meetId),
            title = this.title,
            hostName = this.hostName,
            hostUserId = this.hostUserId?.let(::UserId),
            dates = dates,
            maxParticipantCount = this.maxParticipantCount,
            participants = participants,
            memo = this.memo,
            status = this.status,
            finalizedDate = this.finalizedDate,
            timeRange = timeRange,
            finalizedStartTime = this.finalizedStartTime,
            finalizedEndTime = this.finalizedEndTime,
        )
    }

    private fun ParticipantJpaEntity.toDomain(
        voteDates: Set<LocalDate>,
        voteTimeSlots: Map<LocalDate, String> = emptyMap(),
    ): Participant {
        return Participant(
            id = ParticipantId(this.participantId),
            name = this.name,
            voteDates = voteDates,
            hasVoted = this.hasVoted,
            updatedAt = this.updatedAt,
            voteTimeSlots = voteTimeSlots,
        )
    }

    private fun findVoteDatesByParticipantIds(
        participants: List<ParticipantJpaEntity>,
    ): Map<Long, Set<LocalDate>> {
        val participantIds = participants.map { it.participantId }
        if (participantIds.isEmpty()) return emptyMap()

        return meetingJpaRepository.findAllVoteDatesByParticipantIds(participantIds)
            .groupBy { it.participant.participantId }
            .mapValues { (_, entities) -> entities.map { it.voteDate }.toSet() }
    }

    private fun findVoteTimeSlotsByParticipantIds(
        participants: List<ParticipantJpaEntity>,
    ): Map<Long, Map<LocalDate, String>> {
        val participantIds = participants.map { it.participantId }
        if (participantIds.isEmpty()) return emptyMap()

        return meetingJpaRepository.findAllVoteTimeSlotsByParticipantIds(participantIds)
            .groupBy { it.participant.participantId }
            .mapValues { (_, entities) -> entities.associate { it.voteDate to it.timeSlots } }
    }

    private fun mapMeetingsToDomain(meetings: List<MeetingJpaEntity>): List<Meeting> {
        if (meetings.isEmpty()) return emptyList()

        val meetIds = meetings.map { it.meetId }
        val datesByMeetId = meetingJpaRepository.findAllMeetingDatesByMeetIds(meetIds)
            .groupBy { it.meeting.meetId }
            .mapValues { (_, entities) -> entities.map { it.availableDate }.toSet() }

        val participants = meetingJpaRepository.findAllParticipantsByMeetIds(meetIds)
        val voteDatesByParticipantId = findVoteDatesByParticipantIds(participants)
        val voteTimeSlotsByParticipantId = findVoteTimeSlotsByParticipantIds(participants)

        val participantsByMeetId = participants
            .groupBy { it.meeting.meetId }
            .mapValues { (_, entities) ->
                entities.map { participant ->
                    participant.toDomain(
                        voteDates = voteDatesByParticipantId[participant.participantId].orEmpty(),
                        voteTimeSlots = voteTimeSlotsByParticipantId[participant.participantId].orEmpty(),
                    )
                }
            }

        return meetings.map { meeting ->
            meeting.toDomain(
                dates = datesByMeetId[meeting.meetId].orEmpty(),
                participants = participantsByMeetId[meeting.meetId].orEmpty(),
            )
        }
    }

    private fun MeetingSummaryProjection.toDomain(): MeetingSummary {
        return MeetingSummary(
            id = MeetingId(this.meetId),
            title = this.title,
            hostName = this.hostName,
            status = this.status,
            finalizedDate = this.finalizedDate,
        )
    }

    private fun Meeting.toEntity(): MeetingJpaEntity {
        val meetingEntity = MeetingJpaEntity.of(
            meetId = this.id.value,
            title = this.title,
            hostName = this.hostName,
            hostUserId = this.hostUserId?.value,
            maxParticipantCount = this.maxParticipantCount,
            memo = this.memo,
            status = this.status,
            finalizedDate = this.finalizedDate,
            timeRangeStart = this.timeRange?.startTime,
            timeRangeEnd = this.timeRange?.endTime,
            finalizedStartTime = this.finalizedStartTime,
            finalizedEndTime = this.finalizedEndTime,
        )

        meetingEntity.addMeetingDates(this.dates)
        meetingEntity.addParticipants(this.participants)

        return meetingEntity
    }

    private fun MeetingJpaEntity.addMeetingDates(incomingDates: Set<LocalDate>) {
        incomingDates.forEach { date ->
            this.dates.add(MeetingDateJpaEntity.of(meeting = this, availableDate = date))
        }
    }

    private fun MeetingJpaEntity.addParticipants(incomingParticipants: List<Participant>) {
        incomingParticipants.forEach { participant ->
            this.participants.add(participant.toEntity(this))
        }
    }

    private fun Participant.toEntity(meeting: MeetingJpaEntity): ParticipantJpaEntity {
        val participantEntity = ParticipantJpaEntity.of(
            participantId = this.id.value,
            meeting = meeting,
            name = this.name,
            hasVoted = this.hasVoted,
        )
        this.voteDates.forEach { voteDate ->
            participantEntity.voteDates.add(
                ParticipantVoteDateJpaEntity.of(participant = participantEntity, voteDate = voteDate),
            )
        }
        this.voteTimeSlots.forEach { (voteDate, timeSlots) ->
            participantEntity.voteTimeSlots.add(
                ParticipantVoteTimeSlotJpaEntity.of(
                    participant = participantEntity,
                    voteDate = voteDate,
                    timeSlots = timeSlots,
                ),
            )
        }
        return participantEntity
    }

    private fun MeetingJpaEntity.updateFrom(meeting: Meeting) {
        this.title = meeting.title
        this.hostUserId = meeting.hostUserId?.value
        this.maxParticipantCount = meeting.maxParticipantCount
        this.memo = meeting.memo
        this.status = meeting.status
        this.finalizedDate = meeting.finalizedDate
        this.finalizedStartTime = meeting.finalizedStartTime
        this.finalizedEndTime = meeting.finalizedEndTime
        this.updateMeetingDates(meeting.dates)
        this.updateParticipants(meeting.participants)
    }

    private fun MeetingJpaEntity.updateMeetingDates(dates: Set<LocalDate>) {
        this.dates.removeIf { it.availableDate !in dates }
        val existingDates = this.dates.map { it.availableDate }.toSet()
        val datesToAdd = dates - existingDates
        datesToAdd.forEach { date ->
            this.dates.add(MeetingDateJpaEntity.of(meeting = this, availableDate = date))
        }
    }

    private fun MeetingJpaEntity.updateParticipants(participants: List<Participant>) {
        val existingById = indexExistingParticipants()
        val incomingIds = participants
            .filterNot { it.isNew() }
            .map { it.id.value }
            .toSet()

        removeParticipantsNotIn(incomingIds)

        val remainingIds = this.participants.map { it.participantId }.toSet()

        participants.forEach { participant ->
            val participantEntity = resolveParticipantEntity(participant, existingById)

            participantEntity.name = participant.name
            participantEntity.hasVoted = participant.hasVoted
            updateVoteDates(participantEntity, participant.voteDates)
            updateVoteTimeSlots(participantEntity, participant.voteTimeSlots)

            if (participant.isNew() || participant.id.value !in remainingIds) {
                this.participants.add(participantEntity)
            }
        }
    }

    private fun MeetingJpaEntity.indexExistingParticipants(): Map<Long, ParticipantJpaEntity> {
        return this.participants
            .filter { it.participantId != 0L }
            .associateBy { it.participantId }
    }

    private fun MeetingJpaEntity.removeParticipantsNotIn(incomingIds: Set<Long>) {
        this.participants.removeIf { entity ->
            entity.participantId != 0L && entity.participantId !in incomingIds
        }
    }

    private fun MeetingJpaEntity.resolveParticipantEntity(
        participant: Participant,
        existingById: Map<Long, ParticipantJpaEntity>,
    ): ParticipantJpaEntity {
        return if (participant.isNew()) {
            ParticipantJpaEntity.of(
                participantId = 0L,
                meeting = this,
                name = participant.name,
                hasVoted = participant.hasVoted,
            )
        } else {
            existingById[participant.id.value]
                ?: ParticipantJpaEntity.of(
                    participantId = participant.id.value,
                    meeting = this,
                    name = participant.name,
                    hasVoted = participant.hasVoted,
                )
        }
    }

    private fun updateVoteDates(
        participantEntity: ParticipantJpaEntity,
        voteDates: Set<LocalDate>,
    ) {
        participantEntity.voteDates.removeIf { it.voteDate !in voteDates }
        val existingDates = participantEntity.voteDates.map { it.voteDate }.toSet()
        val datesToAdd = voteDates - existingDates
        datesToAdd.forEach { voteDate ->
            participantEntity.voteDates.add(
                ParticipantVoteDateJpaEntity.of(participant = participantEntity, voteDate = voteDate),
            )
        }
    }

    private fun updateVoteTimeSlots(
        participantEntity: ParticipantJpaEntity,
        voteTimeSlots: Map<LocalDate, String>,
    ) {
        participantEntity.voteTimeSlots.removeIf { it.voteDate !in voteTimeSlots }
        voteTimeSlots.forEach { (voteDate, timeSlots) ->
            val existing = participantEntity.voteTimeSlots.find { it.voteDate == voteDate }
            if (existing == null) {
                participantEntity.voteTimeSlots.add(
                    ParticipantVoteTimeSlotJpaEntity.of(participantEntity, voteDate, timeSlots),
                )
            } else {
                existing.timeSlots = timeSlots
            }
        }
    }

    private fun Participant.isNew(): Boolean = this.id.value == 0L
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew :adapter:rdb:compileKotlin 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt
git commit -m "feat: MeetingAdapter 시간 슬롯 매핑 추가"
```

---

## Task 5: MeetingService — 시간 투표 지원

**Files:**
- Modify: `core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt`
- Modify: `core/src/test/kotlin/com/nomoney/meeting/service/MeetingServiceTest.kt`

- [ ] **Step 1: 실패 테스트 작성**

`MeetingServiceTest.kt` 파일 맨 아래 `fixtureMeeting` 함수 위에 아래 describe 블록을 추가한다.

```kotlin
describe("시간 투표 모드") {
    describe("createMeeting - 시간 범위 설정") {
        it("timeRange를 설정하면 생성된 미팅에 timeRange가 포함된다") {
            val timeRange = MeetingTimeRange(
                startTime = java.time.LocalTime.of(9, 0),
                endTime = java.time.LocalTime.of(18, 0),
            )
            every { meetingRepository.save(any()) } answers { firstArg() }

            val result = meetingService.createMeeting(
                title = "시간 투표 모임",
                hostName = "주최자",
                hostUserId = UserId(1L),
                dates = setOf(LocalDate.of(2026, 2, 20)),
                maxParticipantCount = null,
                timeRange = timeRange,
            )

            result.timeRange shouldBe timeRange
        }
    }

    describe("submitVote - 시간 슬롯 투표") {
        it("시간 투표 모드에서 슬롯 수가 맞지 않으면 예외가 발생한다") {
            val timeRange = MeetingTimeRange(
                startTime = java.time.LocalTime.of(9, 0),
                endTime = java.time.LocalTime.of(18, 0),
            )
            val meeting = fixtureMeeting(
                dates = setOf(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)),
                timeRange = timeRange,
            )
            every { meetingRepository.findByMeetingId(meeting.id) } returns meeting

            shouldThrow<InvalidRequestException> {
                meetingService.submitVote(
                    meetingId = meeting.id,
                    name = "참여자",
                    voteDates = emptyList(),
                    // dates가 2개인데 voteTimeSlots도 2개여야 하지만 1개만 전달
                    voteTimeSlots = listOf(List(18) { false }),
                )
            }
        }

        it("시간 투표 모드에서 슬롯 내 true가 있는 날짜는 voteDates에 포함된다") {
            val date1 = LocalDate.of(2026, 2, 20)
            val date2 = LocalDate.of(2026, 2, 21)
            val timeRange = MeetingTimeRange(
                startTime = java.time.LocalTime.of(9, 0),
                endTime = java.time.LocalTime.of(18, 0),
            )
            val meeting = fixtureMeeting(
                dates = setOf(date1, date2),
                timeRange = timeRange,
            )
            every { meetingRepository.findByMeetingId(meeting.id) } returns meeting
            every { meetingRepository.save(any()) } answers { firstArg() }

            val slots1 = List(18) { i -> i == 0 }  // date1: 09:00만 선택
            val slots2 = List(18) { false }          // date2: 없음

            val result = meetingService.submitVote(
                meetingId = meeting.id,
                name = "참여자",
                voteDates = emptyList(),
                voteTimeSlots = listOf(slots1, slots2),
            )

            val savedParticipant = result.participants.first { it.name == "참여자" }
            savedParticipant.voteDates shouldBe setOf(date1)
            savedParticipant.voteTimeSlots[date1] shouldBe "000000000000000000" + "1" + "0".repeat(29)
        }
    }
}
```

또한 `fixtureMeeting` 함수에 `timeRange` 파라미터를 추가한다:

```kotlin
private fun fixtureMeeting(
    id: MeetingId = MeetingId("meeting-1"),
    title: String = "테스트 모임",
    hostName: String = "주최자",
    hostUserId: UserId = UserId(1L),
    status: MeetingStatus = MeetingStatus.VOTING,
    finalizedDate: LocalDate? = null,
    dates: Set<LocalDate> = setOf(LocalDate.of(2026, 2, 20)),
    participants: List<Participant> = emptyList(),
    timeRange: com.nomoney.meeting.domain.MeetingTimeRange? = null,
): Meeting {
    return Meeting(
        id = id,
        title = title,
        hostName = hostName,
        hostUserId = hostUserId,
        dates = dates,
        maxParticipantCount = null,
        participants = participants,
        status = status,
        finalizedDate = finalizedDate,
        timeRange = timeRange,
    )
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew :core:test --tests "com.nomoney.meeting.service.MeetingServiceTest" 2>&1 | tail -30
```

Expected: 컴파일 에러 (submitVote 시그니처 없음)

- [ ] **Step 3: MeetingService 업데이트**

`core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt`에서 import 추가 및 메서드 시그니처/구현 변경.

import에 추가:
```kotlin
import com.nomoney.meeting.domain.MeetingTimeRange
import java.time.LocalTime
```

`createMeeting` 메서드 교체:
```kotlin
fun createMeeting(
    title: String,
    hostName: String?,
    hostUserId: UserId?,
    dates: Set<LocalDate>,
    maxParticipantCount: Int? = null,
    timeRange: MeetingTimeRange? = null,
): Meeting {
    assertValidMaxParticipantCount(maxParticipantCount)

    val meetingId = generateMeetId()
    val meeting = Meeting(
        id = meetingId,
        title = title,
        hostName = hostName,
        hostUserId = hostUserId,
        dates = dates,
        maxParticipantCount = maxParticipantCount,
        participants = emptyList(),
        status = MeetingStatus.VOTING,
        finalizedDate = null,
        timeRange = timeRange,
    )
    return meetingRepository.save(meeting)
}
```

`addParticipant` 메서드 교체:
```kotlin
fun addParticipant(
    meetingId: MeetingId,
    name: String,
    voteDates: Set<LocalDate>,
    hasVoted: Boolean,
    voteTimeSlots: Map<LocalDate, String> = emptyMap(),
): Meeting {
    val meeting = getMeetingInfo(meetingId)
        ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

    assertAvailableParticipantCapacity(meeting)

    val newParticipant = Participant(
        id = ParticipantId(0L),
        name = name,
        voteDates = voteDates,
        hasVoted = hasVoted,
        voteTimeSlots = voteTimeSlots,
    )

    val updatedMeeting = meeting.copy(
        participants = meeting.participants + newParticipant,
    )

    return meetingRepository.save(updatedMeeting)
}
```

`updateParticipant` 메서드 교체:
```kotlin
fun updateParticipant(
    meetingId: MeetingId,
    name: String,
    voteDates: Set<LocalDate>,
    voteTimeSlots: Map<LocalDate, String> = emptyMap(),
): Meeting {
    val meeting = getMeetingInfo(meetingId)
        ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

    assertAllowedVoteDates(meeting, voteDates)

    if (meeting.participants.none { it.name == name }) {
        throw NotFoundException("참여자를 찾을 수 없습니다.", "name: $name")
    }

    val updatedParticipants = meeting.participants.map { participant ->
        if (participant.name == name) {
            participant.copy(
                voteDates = voteDates,
                hasVoted = true,
                voteTimeSlots = voteTimeSlots,
            )
        } else {
            participant
        }
    }

    val updatedMeeting = meeting.copy(participants = updatedParticipants)
    return meetingRepository.save(updatedMeeting)
}
```

`submitVote` 메서드 교체:
```kotlin
fun submitVote(
    meetingId: MeetingId,
    name: String,
    voteDates: List<LocalDate>,
    voteTimeSlots: List<List<Boolean>>? = null,
): Meeting {
    val meeting = getMeetingInfo(meetingId)
        ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

    val (resolvedVoteDates, resolvedVoteTimeSlots) = resolveVoteData(meeting, voteDates, voteTimeSlots)
    assertAllowedVoteDates(meeting, resolvedVoteDates)

    val participant = meeting.participants.firstOrNull { it.name == name }
    return when {
        participant == null -> addParticipant(
            meetingId = meetingId,
            name = name,
            voteDates = resolvedVoteDates,
            hasVoted = true,
            voteTimeSlots = resolvedVoteTimeSlots,
        )
        participant.hasVoted -> throw DuplicateContentException("이미 투표를 완료한 참여자입니다.", "name: $name")
        else -> {
            require(meeting.hostName == name) { "주최자 Participant는 반드시 meeting.hostName과 동일한 name을 가져야 한다.: $name" }
            updateParticipant(
                meetingId = meetingId,
                name = name,
                voteDates = resolvedVoteDates,
                voteTimeSlots = resolvedVoteTimeSlots,
            )
        }
    }
}
```

`finalizeMeeting` 메서드 교체:
```kotlin
fun finalizeMeeting(
    meetingId: MeetingId,
    selectedDate: LocalDate?,
    requesterUserId: UserId,
    finalizedStartTime: LocalTime? = null,
    finalizedEndTime: LocalTime? = null,
): Meeting {
    val meeting = getMeetingInfo(meetingId)
        ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
    assertMeetingHostOwnership(meeting, requesterUserId)

    if (meeting.timeRange != null && (finalizedStartTime == null || finalizedEndTime == null)) {
        throw InvalidRequestException(
            "시간 투표 모임은 확정 시 시작/종료 시간이 필요합니다.",
            "meetingId=${meetingId.value}",
        )
    }

    if (meeting.status == MeetingStatus.CONFIRMED) {
        return if (selectedDate == null || selectedDate == meeting.finalizedDate) {
            meeting
        } else {
            throw InvalidRequestException(
                "이미 다른 날짜로 확정된 모임입니다.",
                "meetingId=${meetingId.value}, finalizedDate=${meeting.finalizedDate}",
            )
        }
    }

    if (meeting.dates.isEmpty()) {
        throw InvalidRequestException(
            "후보 날짜가 없는 모임은 확정할 수 없습니다.",
            "meetingId=${meetingId.value}",
        )
    }

    if (selectedDate != null && selectedDate !in meeting.dates) {
        throw InvalidRequestException(
            "모임 후보 날짜에 없는 값은 확정일로 선택할 수 없습니다.",
            "meetingId=${meetingId.value}, selectedDate=$selectedDate",
        )
    }

    val resolvedFinalizedDate = resolveFinalizedDate(meeting, selectedDate)
    return meetingRepository.save(
        meeting.copy(
            status = MeetingStatus.CONFIRMED,
            finalizedDate = resolvedFinalizedDate,
            finalizedStartTime = finalizedStartTime,
            finalizedEndTime = finalizedEndTime,
        ),
    )
}
```

`checkFinalizedDateConflictAndFinalizeMeeting` 메서드도 시간 파라미터를 받도록 교체:
```kotlin
fun checkFinalizedDateConflictAndFinalizeMeeting(
    meetingId: MeetingId,
    finalizedDate: LocalDate,
    requesterUserId: UserId,
    finalizedStartTime: LocalTime? = null,
    finalizedEndTime: LocalTime? = null,
): Boolean {
    if (
        hasDateConflictWithConfirmedMeetings(
            requesterUserId = requesterUserId,
            meetingId = meetingId,
            finalizedDate = finalizedDate,
        )
    ) {
        return true
    }

    finalizeMeeting(
        meetingId = meetingId,
        selectedDate = finalizedDate,
        requesterUserId = requesterUserId,
        finalizedStartTime = finalizedStartTime,
        finalizedEndTime = finalizedEndTime,
    )
    return false
}
```

파일 맨 아래 private 헬퍼 메서드 추가:
```kotlin
private fun resolveVoteData(
    meeting: Meeting,
    voteDates: List<LocalDate>,
    voteTimeSlots: List<List<Boolean>>?,
): Pair<Set<LocalDate>, Map<LocalDate, String>> {
    if (voteTimeSlots == null) {
        return voteDates.toSet() to emptyMap()
    }

    val timeRange = meeting.timeRange
        ?: throw InvalidRequestException(
            "날짜 전용 모임에는 시간 슬롯 투표를 할 수 없습니다.",
            "meetingId=${meeting.id.value}",
        )

    val sortedDates = meeting.dates.sorted()
    if (voteTimeSlots.size != sortedDates.size) {
        throw InvalidRequestException(
            "날짜 수와 시간 슬롯 배열 수가 일치하지 않습니다.",
            "expected=${sortedDates.size}, actual=${voteTimeSlots.size}",
        )
    }

    val offset = timeRange.startIndex
    val slotCount = timeRange.slotCount

    val resultMap = sortedDates.mapIndexed { i, date ->
        val bools = voteTimeSlots[i]
        if (bools.size != slotCount) {
            throw InvalidRequestException(
                "슬롯 수가 일치하지 않습니다.",
                "expected=$slotCount, actual=${bools.size}",
            )
        }
        val mask = buildString(48) {
            repeat(offset) { append('0') }
            bools.forEach { append(if (it) '1' else '0') }
            repeat(48 - offset - slotCount) { append('0') }
        }
        date to mask
    }.toMap()

    val derivedVoteDates = resultMap
        .filterValues { mask -> mask.contains('1') }
        .keys

    return derivedVoteDates to resultMap
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
./gradlew :core:test --tests "com.nomoney.meeting.service.MeetingServiceTest" 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL, all tests passed

- [ ] **Step 5: 커밋**

```bash
git add core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt \
        core/src/test/kotlin/com/nomoney/meeting/service/MeetingServiceTest.kt
git commit -m "feat: MeetingService 시간 투표 지원 (createMeeting, submitVote, finalizeMeeting)"
```

---

## Task 6: API Layer — 요청/응답 모델 + 컨트롤러

**Files:**
- Modify: `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/CreateMeetingHttp.kt`
- Modify: `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/VoteHttp.kt`
- Modify: `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingInfoResponse.kt`
- Modify: `app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingStatusHttp.kt`
- Modify: `app/api/src/main/kotlin/com/nomoney/api/meetvote/MeetingVoteController.kt`

- [ ] **Step 1: CreateMeetingHttp 업데이트**

```kotlin
// app/api/src/main/kotlin/com/nomoney/api/meetvote/model/CreateMeetingHttp.kt
package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "모임 생성 요청")
data class CreateMeetingRequest(
    @Schema(description = "모임 제목", example = "팀 회식", required = true)
    val title: String,

    @Schema(description = "주최자 이름", example = "이파이", required = true)
    val hostName: String,

    @Schema(description = "최대 참여 인원 (제한 없으면 null)", example = "10")
    val maxParticipantCount: Int?,

    @Schema(description = "모임 가능한 날짜 목록", example = "[\"2025-01-15\", \"2025-01-16\"]", required = true)
    val dates: List<LocalDate>,

    @Schema(description = "시간 투표 범위 (null이면 날짜 전용 모드)")
    val timeRange: TimeRangeRequest? = null,
)

@Schema(description = "시간 범위 요청")
data class TimeRangeRequest(
    @Schema(description = "시작 시간", example = "09:00")
    val startTime: LocalTime,

    @Schema(description = "종료 시간", example = "18:00")
    val endTime: LocalTime,
)

@Schema(description = "모임 생성 응답")
data class CreateMeetingResponse(
    @Schema(description = "생성된 모임 ID", example = "aBcDeFgHiJ")
    val id: MeetingId,
)
```

- [ ] **Step 2: VoteHttp 업데이트**

```kotlin
// app/api/src/main/kotlin/com/nomoney/api/meetvote/model/VoteHttp.kt
package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "투표 생성 요청")
data class VoteRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(description = "투표자 이름", example = "홍길동", required = true)
    val name: String,

    @Schema(description = "투표한 날짜 목록 (날짜 전용 모드)", example = "[\"2025-01-15\", \"2025-01-16\"]")
    val voteDates: List<LocalDate> = emptyList(),

    @Schema(description = "시간 슬롯 투표 (시간 모드): [날짜 수][범위 내 슬롯 수]. voteTimeSlots[i][j] = dates[i]의 j번째 슬롯 가능 여부")
    val voteTimeSlots: List<List<Boolean>>? = null,
)

@Schema(description = "투표 응답")
data class VoteResponse(
    @Schema(description = "성공 여부", example = "true")
    val success: Boolean,
)
```

- [ ] **Step 3: MeetingInfoResponse 업데이트**

```kotlin
// app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingInfoResponse.kt
package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.Meeting
import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.domain.Participant
import com.nomoney.meeting.domain.ParticipantId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "모임 정보 응답")
data class MeetingInfoResponse(
    @Schema(description = "모임 ID", example = "asdfqwer")
    val id: MeetingId,

    @Schema(description = "모임 제목", example = "팀 회식")
    val title: String,

    @Schema(description = "모임 가능한 날짜 목록, 정렬 되어 있음")
    val dates: List<LocalDate>,

    @Schema(description = "모임 상태", example = "VOTING")
    val status: MeetingStatus,

    @Schema(description = "최종 확정 날짜 (확정 전 null)")
    val finalizedDate: LocalDate?,

    @Schema(description = "최대 모임 참여자 수, 정해지지 않았으면 null")
    val maxParticipantCount: Int?,

    @Schema(description = "투표에 참여한 참여자 정보")
    val participants: List<ParticipantResponse>,

    @Schema(description = "모임을 만든 주최자 이름")
    val hostName: String?,

    @Schema(description = "시간 투표 범위 (날짜 전용 모드이면 null)")
    val timeRange: TimeRangeResponse? = null,
)

@Schema(description = "시간 범위 응답")
data class TimeRangeResponse(
    val startTime: LocalTime,
    val endTime: LocalTime,
    @Schema(description = "범위 내 슬롯 수")
    val slotCount: Int,
)

data class ParticipantResponse(
    val id: ParticipantId,
    val name: String,
    val voteDates: List<LocalDate>,
    @Schema(description = "시간 슬롯 투표 (시간 모드만): [날짜 수][범위 내 슬롯 수]")
    val voteTimeSlots: List<List<Boolean>>? = null,
    val hasVoted: Boolean,
)

fun Meeting.toResponse(): MeetingInfoResponse {
    val sortedDates = this.dates.toList().sorted()
    val timeRange = this.timeRange
    return MeetingInfoResponse(
        id = this.id,
        title = this.title,
        dates = sortedDates,
        status = this.status,
        finalizedDate = this.finalizedDate,
        maxParticipantCount = this.maxParticipantCount,
        participants = this.participants.map { it.toResponse(sortedDates, timeRange) },
        hostName = this.hostName,
        timeRange = timeRange?.let {
            TimeRangeResponse(
                startTime = it.startTime,
                endTime = it.endTime,
                slotCount = it.slotCount,
            )
        },
    )
}

fun Participant.toResponse(
    sortedDates: List<LocalDate>,
    timeRange: com.nomoney.meeting.domain.MeetingTimeRange?,
): ParticipantResponse {
    val voteTimeSlotsResponse = if (timeRange != null) {
        val offset = timeRange.startIndex
        val slotCount = timeRange.slotCount
        sortedDates.map { date ->
            val mask = this.voteTimeSlots[date] ?: "0".repeat(48)
            (offset until offset + slotCount).map { i -> mask[i] == '1' }
        }
    } else {
        null
    }
    return ParticipantResponse(
        id = this.id,
        name = this.name,
        voteDates = this.voteDates.toList().sorted(),
        voteTimeSlots = voteTimeSlotsResponse,
        hasVoted = this.hasVoted,
    )
}
```

- [ ] **Step 4: MeetingStatusHttp 업데이트 (확정 시간 필드 추가)**

```kotlin
// app/api/src/main/kotlin/com/nomoney/api/meetvote/model/MeetingStatusHttp.kt
package com.nomoney.api.meetvote.model

import com.nomoney.meeting.domain.MeetingId
import com.nomoney.meeting.domain.MeetingStatus
import com.nomoney.meeting.service.MeetingDateVoteDetail
import com.nomoney.meeting.service.MeetingFinalizePreview
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "모임 확정 후보 조회 응답")
data class FinalizeMeetingPreviewResponse(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ")
    val meetingId: MeetingId,

    @Schema(description = "모임 제목", example = "팀 점심")
    val meetingTitle: String,

    @Schema(description = "최다 득표 날짜 상세 정보")
    val topDateVoteDetails: List<FinalizeMeetingDateVoteDetailResponse>,

    @Schema(description = "확정일 선택 필요 여부(동률 1위가 2개 이상인 경우 true)", example = "false")
    val requiresDateSelection: Boolean,
)

data class FinalizeMeetingDateVoteDetailResponse(
    @Schema(description = "날짜", example = "2026-02-20")
    val date: LocalDate,

    @Schema(description = "해당 날짜 투표 인원 수", example = "3")
    val voteCount: Int,

    @Schema(description = "해당 날짜 투표자 이름 목록", example = "[\"홍길동\", \"김철수\"]")
    val voterNames: List<String>,
)

@Schema(description = "모임 확정 요청")
data class FinalizeMeetingRequest(
    @Schema(description = "모임 ID", example = "aBcDeFgHiJ", required = true)
    val meetingId: MeetingId,

    @Schema(description = "최종 확정 날짜 (공동 1위인 경우 필수)", example = "2026-02-20")
    val finalizedDate: LocalDate? = null,

    @Schema(description = "확정 시작 시간 (시간 투표 모임 필수)", example = "10:00")
    val finalizedStartTime: LocalTime? = null,

    @Schema(description = "확정 종료 시간 (시간 투표 모임 필수)", example = "12:00")
    val finalizedEndTime: LocalTime? = null,
)

@Schema(description = "모임 확정 응답")
data class FinalizeMeetingResponse(
    @Schema(description = "변경된 모임 상태", example = "CONFIRMED")
    val status: MeetingStatus,

    @Schema(description = "최종 확정 날짜", example = "2026-02-20")
    val finalizedDate: LocalDate,

    @Schema(description = "확정 시작 시간 (시간 투표 모임만)", example = "10:00")
    val finalizedStartTime: LocalTime? = null,

    @Schema(description = "확정 종료 시간 (시간 투표 모임만)", example = "12:00")
    val finalizedEndTime: LocalTime? = null,
)

fun MeetingFinalizePreview.toFinalizePreviewResponse(): FinalizeMeetingPreviewResponse = FinalizeMeetingPreviewResponse(
    meetingId = this.meetingId,
    meetingTitle = this.meetingTitle,
    topDateVoteDetails = this.topDateVoteDetails.map { it.toResponse() },
    requiresDateSelection = this.requiresDateSelection,
)

private fun MeetingDateVoteDetail.toResponse(): FinalizeMeetingDateVoteDetailResponse = FinalizeMeetingDateVoteDetailResponse(
    date = this.date,
    voteCount = this.voteCount,
    voterNames = this.voterNames,
)
```

- [ ] **Step 5: HostMeetingDetailHttp 업데이트**

`app/api/src/main/kotlin/com/nomoney/api/meetvote/model/HostMeetingDetailHttp.kt`의 `toHostDetailResponse()` 함수를 교체한다. `Participant.toResponse()` 시그니처가 변경되었으므로 인수를 전달해야 한다.

```kotlin
fun MeetingHostDetail.toHostDetailResponse(): HostMeetingDetailResponse {
    val sortedDates = this.meeting.dates.toList().sorted()
    val timeRange = this.meeting.timeRange
    return HostMeetingDetailResponse(
        id = this.meeting.id,
        title = this.meeting.title,
        dates = sortedDates,
        status = this.meeting.status,
        finalizedDate = this.meeting.finalizedDate,
        maxParticipantCount = this.meeting.maxParticipantCount,
        participants = this.meeting.participants.map { it.toResponse(sortedDates, timeRange) },
        hostName = this.meeting.hostName,
        memo = this.meeting.memo,
        notVotedParticipantCount = this.notVotedParticipantCount,
    )
}
```

- [ ] **Step 6: MeetingVoteController 업데이트**

`MeetingVoteController.kt`에서 아래 3개 메서드를 교체한다.

`createMeeting` 교체:
```kotlin
@PostMapping("/api/v1/meeting")
fun createMeeting(
    @RequestBody request: CreateMeetingRequest,
): CreateMeetingResponse {
    val hostUserId = getSecurityUserId()
    val timeRange = request.timeRange?.let {
        com.nomoney.meeting.domain.MeetingTimeRange(
            startTime = it.startTime,
            endTime = it.endTime,
        )
    }
    val meeting = meetingService.createMeeting(
        title = request.title,
        hostName = request.hostName,
        hostUserId = hostUserId,
        dates = request.dates.toSet(),
        maxParticipantCount = request.maxParticipantCount,
        timeRange = timeRange,
    )

    if (hostUserId == null) {
        meetingService.addParticipant(
            meetingId = meeting.id,
            name = request.hostName,
            voteDates = emptySet(),
            hasVoted = false,
        )
    }

    return CreateMeetingResponse(id = meeting.id)
}
```

`createVote` 교체:
```kotlin
@PostMapping("/api/v1/meeting/vote")
fun createVote(
    @RequestBody request: VoteRequest,
): VoteResponse {
    meetingService.submitVote(
        meetingId = request.meetingId,
        name = request.name,
        voteDates = request.voteDates,
        voteTimeSlots = request.voteTimeSlots,
    )
    return VoteResponse(success = true)
}
```

`updateVote` 교체 — `updateParticipant`도 동일하게 boolean 배열을 받아야 하므로, 컨트롤러에서 변환 없이 서비스에 위임한다. `updateParticipant`에도 `voteTimeSlots: List<List<Boolean>>?` 파라미터를 추가하는 방식으로 리팩터링한다.

`MeetingService.updateParticipant` 시그니처를 변경한다 (Step 3에서 이미 `Map<LocalDate, String>`을 받도록 했으므로, 컨트롤러 레이어에서 변환이 필요). 대신 서비스에 boolean 배열을 처리하는 퍼블릭 메서드를 추가한다:

`MeetingService.kt`에 아래 메서드를 추가한다 (기존 `updateParticipant` 유지, 새 오버로드 추가):
```kotlin
fun updateParticipantWithTimeSlots(
    meetingId: MeetingId,
    name: String,
    voteDates: List<LocalDate>,
    voteTimeSlots: List<List<Boolean>>? = null,
): Meeting {
    val meeting = getMeetingInfo(meetingId)
        ?: throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")

    val (resolvedVoteDates, resolvedVoteTimeSlots) = resolveVoteData(meeting, voteDates, voteTimeSlots)
    return updateParticipant(
        meetingId = meetingId,
        name = name,
        voteDates = resolvedVoteDates,
        voteTimeSlots = resolvedVoteTimeSlots,
    )
}
```

`updateVote` 컨트롤러 교체:
```kotlin
@PutMapping("/api/v1/meeting/vote")
fun updateVote(
    @RequestBody request: VoteRequest,
): VoteResponse {
    meetingService.updateParticipantWithTimeSlots(
        meetingId = request.meetingId,
        name = request.name,
        voteDates = request.voteDates,
        voteTimeSlots = request.voteTimeSlots,
    )
    return VoteResponse(success = true)
}
```

`finalizeMeeting` 교체:
```kotlin
@PostMapping("/api/v1/host/meeting/finalize")
fun finalizeMeeting(
    @RequestBody request: FinalizeMeetingRequest,
): FinalizeMeetingResponse {
    val requesterUserId = getSecurityUserIdOrThrow()
    val meeting = meetingService.finalizeMeeting(
        meetingId = request.meetingId,
        selectedDate = request.finalizedDate,
        requesterUserId = requesterUserId,
        finalizedStartTime = request.finalizedStartTime,
        finalizedEndTime = request.finalizedEndTime,
    )
    return FinalizeMeetingResponse(
        status = meeting.status,
        finalizedDate = requireNotNull(meeting.finalizedDate) {
            "CONFIRMED 상태의 모임에는 finalizedDate가 반드시 존재해야 합니다. meetingId=${meeting.id.value}"
        },
        finalizedStartTime = meeting.finalizedStartTime,
        finalizedEndTime = meeting.finalizedEndTime,
    )
}
```

- [ ] **Step 7: 빌드 확인**

```bash
./gradlew :app:api:compileKotlin 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 전체 테스트 실행**

```bash
./gradlew test 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL, all tests passed

- [ ] **Step 9: 커밋**

```bash
git add app/api/src/main/kotlin/com/nomoney/api/meetvote/
git commit -m "feat: API 레이어 시간 투표 지원 (CreateMeeting, Vote, MeetingInfo, Finalize)"
```
