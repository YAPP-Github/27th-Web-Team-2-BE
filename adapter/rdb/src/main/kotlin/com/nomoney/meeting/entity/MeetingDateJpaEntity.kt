package com.nomoney.meeting.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "meeting_dates")
@IdClass(MeetingDateId::class)
class MeetingDateJpaEntity : BaseJpaEntity() {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id", nullable = false)
    lateinit var meeting: MeetingJpaEntity

    @Id
    @Column(name = "available_date", nullable = false)
    lateinit var availableDate: LocalDate
}

data class MeetingDateId(
    val meeting: String = "",
    val availableDate: LocalDate = LocalDate.now(),
) : Serializable
