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
